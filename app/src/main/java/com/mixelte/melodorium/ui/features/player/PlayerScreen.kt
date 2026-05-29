package com.mixelte.melodorium.ui.features.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mixelte.melodorium.R
import com.mixelte.melodorium.domain.models.MusicLang
import com.mixelte.melodorium.domain.models.MusicLike
import com.mixelte.melodorium.domain.models.MusicMood
import com.mixelte.melodorium.ui.common.UiTrack
import com.mixelte.melodorium.ui.common.TrackArtwork
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerRoute(viewModel: PlayerViewModel, onBackClick: () -> Unit) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    var showPlaylist by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        onBackClick()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlayerScreen(
            state,
            onBackClick,
            onPlaylistButtonClick = { showPlaylist = true },
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onPreviousClick = { viewModel.prevTrack() },
            onNextClick = { viewModel.nextTrack() },
            onSeekTo = { viewModel.seekTo(it) },
        )

        if (showPlaylist) {
            ModalBottomSheet(
                onDismissRequest = { showPlaylist = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                PlaylistScreen(
                    tracks,
                    onBackClick = { showPlaylist = false },
                    onClearQueueClick = { viewModel.clearPlaylist() },
                    onTrackClick = { viewModel.playTrack(it) },
                )
            }
        }
    }
}

@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onBackClick: () -> Unit,
    onPlaylistButtonClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Float) -> Unit,
) {
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 100.dp.toPx() }

    var totalDragX = 0f
    var totalDragY = 0f
    var isGestureConsumed = false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                        isGestureConsumed = false
                    },
                    onDrag = { change, dragAmount ->
                        if (isGestureConsumed) return@detectDragGestures

                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (abs(totalDragX) > swipeThresholdPx || abs(totalDragY) > swipeThresholdPx) {
                            isGestureConsumed = true

                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > 0) {
                                    onPreviousClick()
                                } else {
                                    onNextClick()
                                }
                            } else {
                                if (totalDragY > 0) {
                                    onBackClick()
                                }
                            }
                            change.consume()
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Назад",
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(onClick = {}, label = { Text(state.track?.mood?.toName() ?: "Затишье") })
                SuggestionChip(onClick = {}, label = { Text(state.track?.like?.toName() ?: "Скрыто") })
                SuggestionChip(onClick = {}, label = { Text(state.track?.lang?.toName() ?: "\uD83C\uDFB5") })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.track?.title ?: "Не выбрано",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.track?.artist ?: "",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .size(260.dp)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                TrackArtwork(state.track?.artwork, size = 260.dp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Slider(
                value = state.progress,
                onValueChange = onSeekTo,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${state.currentTime ?: "0:00"}/${state.fullTime ?: "0:00"}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(painterResource(id = R.drawable.ic_shuffle), contentDescription = "Перемешать")
                }
                IconButton(onClick = onPreviousClick) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Предыдущий")
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            imageVector = if (state.track?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Старт/Пауза",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                IconButton(onClick = onNextClick) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Следующий")
                }
                IconButton(onClick = {}) {
                    Icon(painterResource(id = R.drawable.ic_repeat), contentDescription = "Повтор")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

//            Text(
//                text = "А снится нам трава, трава у дома",
//                style = MaterialTheme.typography.titleLarge.copy(
//                    fontWeight = FontWeight.Medium,
//                    color = MaterialTheme.colorScheme.onBackground
//                ),
//                textAlign = TextAlign.Center
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = "Зелёная, зелёная трава",
//                style = MaterialTheme.typography.titleLarge.copy(
//                    fontWeight = FontWeight.Medium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                ),
//                textAlign = TextAlign.Center
//            )
        }

        // 3. Имитация BottomBar (Нижняя панель)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Поделиться",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onPlaylistButtonClick) {
                Icon(
                    painterResource(id = R.drawable.ic_queue_music),
                    contentDescription = "Плейлист",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF8F8)
@Composable
fun PlayerScreenPreview() {
    PlayerScreen(
        PlayerUiState(
            track = UiTrack(
                id = 0,
                title = "Трава у дома",
                artist = "Земляне",
                mood = MusicMood.Energistic,
                like = MusicLike.Like,
                lang = MusicLang.Ru,
            ),
            progress = 0.45f,
            currentTime = "1:24",
            fullTime = "2:03"
        ),
        {}, {}, {}, {}, {},
    ) {}
}
