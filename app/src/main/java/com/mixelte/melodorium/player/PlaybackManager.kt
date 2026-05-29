package com.mixelte.melodorium.player


import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.mixelte.melodorium.data.db.PlaybackStateEntity
import com.mixelte.melodorium.data.repository.MusicRepository
import com.mixelte.melodorium.data.repository.PlaylistEntry
import com.mixelte.melodorium.domain.models.MusicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackManager(
    private val musicRepository: MusicRepository,
) : Player.Listener {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var playlistSyncJob: Job? = null
    private var mediaController: MediaController? = null

    private val _playlist = MutableStateFlow<List<PlaylistEntry>?>(null)
    val playlist = _playlist.asStateFlow()

    private val _currentItem = MutableStateFlow<PlaylistEntry?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    fun setMediaController(controller: MediaController) {
        this.mediaController = controller
        controller.addListener(this)
        _isPlaying.value = controller.isPlaying
        _duration.value = controller.duration.coerceAtLeast(0L)
        _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)

        startProgressTimer()

        scope.launch {
            val savedState = musicRepository.getPlaybackState()
            observeDatabasePlaylist(savedState)
        }
    }

    private fun observeDatabasePlaylist(savedState: PlaybackStateEntity?) {
        playlistSyncJob?.cancel()
        var isFirstLoad = true
        playlistSyncJob = scope.launch {
            musicRepository.currentPlaylist.collectLatest { dbPlaylist ->
                _playlist.value = dbPlaylist
                _currentItem.value = _currentItem.value?.id?.let { id -> dbPlaylist.find { it.id == id } }

                mediaController?.let { controller ->
                    val mediaItems = dbPlaylist.map {
                        MediaItem.Builder().setMediaId(it.id.toString()).setUri(it.file.uri).build()
                    }

                    val controllerIds =
                        (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it).mediaId }
                    val dbIds = mediaItems.map { it.mediaId }
                    if (controllerIds == dbIds) return@let
                    controller.setMediaItems(mediaItems, false)

                    if (isFirstLoad && savedState != null) {
                        isFirstLoad = false
                        val savedIndex = dbPlaylist.indexOfFirst { it.file.rpath == savedState.currentTrackRpath }
                        if (savedIndex != -1) {
                            controller.seekTo(savedIndex, savedState.currentPositionMs)
                            _currentItem.value = dbPlaylist[savedIndex]
                            if (savedState.isPlaying) controller.play()
                        }
                    } else {
                        val currentIdx = controller.currentMediaItemIndex
                        val currentPos = controller.currentPosition
                        if (currentIdx in mediaItems.indices) {
                            controller.seekTo(currentIdx, currentPos)
                        }
                    }
                }
            }
        }
    }

    private fun savePlaylist(tracks: List<PlaylistEntry>) {
        scope.launch {
            musicRepository.saveCurrentPlaylist(tracks)
        }
    }

    fun indexOfEntry(id: Long): Int {
        return _playlist.value?.let { playlist -> playlist.indexOfFirst { it.id == id } } ?: -1
    }

    fun setPlaylist(tracks: List<MusicFile>) {
        savePlaylist(tracks.map { PlaylistEntry(id = 0, file = it) })
    }

    fun addTrack(track: MusicFile) {
        _playlist.value?.let { savePlaylist(it + PlaylistEntry(id = 0, file = track)) }
    }

    fun addTracks(tracks: List<MusicFile>) {
        _playlist.value?.let { savePlaylist(it + tracks.map { file -> PlaylistEntry(id = 0, file = file) }) }
    }

    fun removeTrack(id: Long) = _playlist.value?.let { playlist -> removeTrack(playlist.indexOfFirst { it.id == id }) }
    fun removeTrack(index: Int) {
        _playlist.value?.let { playlist ->
            if (index !in playlist.indices) return
            val updated = playlist.toMutableList().apply { removeAt(index) }
            savePlaylist(updated)
        }
    }

    fun removeTracks(ids: List<Long>) =
        _playlist.value?.let { playlist -> removeTracks(ids.map { id -> playlist.indexOfFirst { it.id == id } }) }

    fun removeTracks(indices: List<Int>) {
        _playlist.value?.let { playlist ->
            val validSortedIndices = indices
                .filter { it in playlist.indices }
                .distinct()
                .sortedDescending()

            if (validSortedIndices.isEmpty()) return

            val updated = playlist.toMutableList().apply {
                for (index in validSortedIndices) {
                    removeAt(index)
                }
            }

            savePlaylist(updated)
        }
    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        _playlist.value?.let { playlist ->
            val list = playlist.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                savePlaylist(list)
            }
        }
    }

    fun shuffle(idsToShuffle: List<Long>? = null) =
        _playlist.value?.let { playlist -> shuffle(idsToShuffle?.map { id -> playlist.indexOfFirst { it.id == id } }) }

    fun shuffle(indicesToShuffle: List<Int>? = null) {
        _playlist.value?.let { playlist ->
            val playlist = playlist.toMutableList()
            val curItem = _currentItem.value

            if (indicesToShuffle == null) {
                playlist.shuffle()
                val curIndex = playlist.indexOf(curItem)
                if (curIndex > 0) {
                    val item = playlist.removeAt(curIndex)
                    playlist.add(0, item)
                }
            } else {
                val indicesToShuffleFiltered = indicesToShuffle.filter { it in playlist.indices }
                val pairs = indicesToShuffleFiltered.map { it to playlist[it] }
                pairs.shuffled().forEachIndexed { i, it ->
                    playlist[it.first] = pairs[i].second
                }
            }
            savePlaylist(playlist)
        }
    }

    fun clear() = setPlaylist(emptyList())
    fun playPause() = mediaController?.let { if (it.isPlaying) it.pause() else it.play() }

    fun seekTo(index: Int) {
        playlist.value?.let { playlist ->
            if (index in playlist.indices) mediaController?.seekTo(index, 0)
        }
    }

    fun seekTo(positionMs: Long) = mediaController?.let { controller ->
        controller.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun next() = mediaController?.seekToNextMediaItem()
    fun prev() = mediaController?.seekToPreviousMediaItem()

    private fun startProgressTimer() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var counter = 0
            while (this.isActive) {
                mediaController?.let { controller ->
                    val pos = controller.currentPosition.coerceAtLeast(0L)
                    _currentPosition.value = pos
                    if (_duration.value != controller.duration) {
                        _duration.value = controller.duration.coerceAtLeast(0L)
                    }

                    counter++
                    if (counter >= 20 && controller.isPlaying && _currentItem.value != null) {
                        counter = 0
                        musicRepository.updatePlaybackState(
                            rpath = _currentItem.value?.file?.rpath,
                            positionMs = pos,
                            isPlaying = true
                        )
                    }
                }
                delay(500)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        scope.launch {
            musicRepository.updatePlaybackState(
                rpath = _currentItem.value?.file?.rpath,
                positionMs = _currentPosition.value,
                isPlaying = isPlaying
            )
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val items = _playlist.value
        val newCurrent = mediaItem?.let { currentMedia ->
            items?.find { it.id.toString() == currentMedia.mediaId }
        }
        _currentItem.value = newCurrent

        scope.launch {
            musicRepository.updatePlaybackState(
                rpath = newCurrent?.file?.rpath,
                positionMs = 0L,
                isPlaying = _isPlaying.value
            )

            newCurrent?.let {
                musicRepository.getArtworkFile(it.file)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        playlistSyncJob?.cancel()
        mediaController?.removeListener(this)
        mediaController = null
        scope.coroutineContext.cancelChildren()
    }
}
