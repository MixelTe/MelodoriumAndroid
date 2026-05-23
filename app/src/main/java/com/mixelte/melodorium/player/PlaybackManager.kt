package com.mixelte.melodorium.player


import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.mixelte.melodorium.models.MusicFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.media3.common.Player as Media3Player

class PlaybackManager : Media3Player.Listener {
    private var mediaController: MediaController? = null

    private val _playlist = MutableStateFlow<List<PlayerItem>>(emptyList())
    val playlist = _playlist.asStateFlow()

    private val _currentItem = MutableStateFlow<PlayerItem?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    var onTrackEndedListener: (() -> Unit)? = null

    fun setMediaController(controller: MediaController) {
        this.mediaController = controller
        controller.addListener(this)
        _isPlaying.value = controller.isPlaying
        restorePlaylistFromController(controller)
    }

    private fun restorePlaylistFromController(controller: MediaController) {
        val items = mutableListOf<PlayerItem>()
        for (i in 0 until controller.mediaItemCount) {
            val mediaItem = controller.getMediaItemAt(i)
            // Здесь предполагается наличие фабрики или кэша для воссоздания PlayerItem из MediaItem
        }
        _playlist.value = items
        _currentItem.value = items.getOrNull(controller.currentMediaItemIndex)
    }

    fun addTrack(track: MusicFile) = addTracks(listOf(track))
    fun addTracks(tracks: List<MusicFile>) {
        val newItems = tracks.map { PlayerItem(it) }
        val updatedList = _playlist.value + newItems
        _playlist.value = updatedList

        if (_currentItem.value == null) {
            _currentItem.value = updatedList.firstOrNull()
        }

        mediaController?.addMediaItems(newItems.map { it.mediaItem })
    }

    fun removeTrack(index: Int) {
        if (index < 0 || index >= _playlist.value.size) return
        val updatedList = _playlist.value.toMutableList().apply { removeAt(index) }
        _playlist.value = updatedList
        mediaController?.removeMediaItem(index)
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(index: Int) {
        mediaController?.seekTo(index, 0)
    }

    fun next() = mediaController?.seekToNextMediaItem()
    fun prev() = mediaController?.seekToPreviousMediaItem()

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        val list = _playlist.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _playlist.value = list
            mediaController?.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun shuffle(indicesToShuffle: List<Int>? = null) {
        val currentList = _playlist.value.toMutableList()
        val curItem = _currentItem.value

        if (indicesToShuffle == null) {
            currentList.shuffle()
            val curIndex = currentList.indexOf(curItem)
            if (curIndex > 0) {
                val item = currentList.removeAt(curIndex)
                currentList.add(0, item)
            }
        } else {
            val indicesToShuffleFiltered = indicesToShuffle.filter { it in currentList.indices }
            val pairs = indicesToShuffleFiltered.map { it to currentList[it] }
            pairs.shuffled().forEachIndexed { i, it ->
                currentList[it.first] = pairs[i].second
            }
        }

        _playlist.value = currentList
        mediaController?.let { controller ->
            val curPos = controller.currentPosition
            controller.setMediaItems(currentList.map { it.mediaItem })
            val newCurIndex = currentList.indexOf(curItem)
            if (newCurIndex >= 0) {
                controller.seekTo(newCurIndex, curPos)
            }
        }
    }

    fun clear() {
        _playlist.value = emptyList()
        _currentItem.value = null
        mediaController?.clearMediaItems()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val items = _playlist.value
        val newCurrent = mediaItem?.let { currentMedia -> items.find { it.mediaId == currentMedia.mediaId } }
        _currentItem.value = newCurrent

        if (items.lastOrNull() == newCurrent) {
            onTrackEndedListener?.invoke()
        }
    }
}