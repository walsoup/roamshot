package app.polaroam.ui.gallery

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.ui.components.PanelIllustration
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelWalker
import app.polaroam.ui.components.PolaroidPhoto
import app.polaroam.ui.preview.PreviewData
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.tiledBackground
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GalleryContent(state)
}

@Composable
fun GalleryContent(state: GalleryUiState, modifier: Modifier = Modifier) {
    val spacing = PolaroamTheme.spacing
    val typography = PolaroamTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .tiledBackground(R.drawable.bg_cork),
    ) {
        when (state) {
            GalleryUiState.Loading -> PixelWalker(Modifier.align(Alignment.Center))

            GalleryUiState.Error -> PixelPanel(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.l),
            ) {
                PanelIllustration(R.drawable.illo_error)
                Text(stringResource(R.string.gallery_error), style = typography.body)
            }

            is GalleryUiState.Loaded -> if (state.items.isEmpty()) {
                PixelPanel(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(spacing.l),
                ) {
                    PanelIllustration(R.drawable.illo_gallery_empty)
                    Text(stringResource(R.string.gallery_empty_title), style = typography.label)
                    Text(stringResource(R.string.gallery_empty_body), style = typography.body)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(spacing.l),
                    horizontalArrangement = Arrangement.spacedBy(spacing.l),
                    verticalArrangement = Arrangement.spacedBy(spacing.l),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(key = "title", span = { GridItemSpan(maxLineSpan) }) {
                        PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.gallery_title), style = typography.title)
                        }
                    }
                    items(state.items, key = { it.questId }) { item ->
                        val overlap = PolaroamTheme.dimens.pixel * FASTENER_OVERLAP_PX
                        Box(
                            modifier = Modifier.padding(top = overlap),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            PolaroidPhoto(
                                photoPath = item.photoPath,
                                caption = item.title,
                                contentDescription = stringResource(R.string.gallery_photo_cd, item.title),
                                tiltDegrees = item.tiltDegrees,
                            )
                            PixelArt(
                                id = fastenerFor(item.questId),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = -overlap),
                            )
                            stickerFor(item.questId)?.let { stickerRes ->
                                PixelArt(
                                    id = stickerRes,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Source pixels of the pin or tape that hang above the photo's top edge. */
private const val FASTENER_OVERLAP_PX = 6

private val fasteners = listOf(R.drawable.pin_gold, R.drawable.tape_cream, R.drawable.pin_moss)

/** Keyed on the quest so a photo keeps its pin or tape as the gallery grows. */
@DrawableRes
private fun fastenerFor(questId: String): Int = fasteners[Math.floorMod(questId.hashCode(), fasteners.size)]

private val stickers = listOf(R.drawable.sticker_star, R.drawable.sticker_heart, null)

@DrawableRes
private fun stickerFor(questId: String): Int? = stickers[Math.floorMod(questId.hashCode() / 7, stickers.size)]

@Preview(showBackground = true)
@Composable
private fun GalleryPreview() {
    PolaroamTheme {
        GalleryContent(GalleryUiState.Loaded(PreviewData.galleryItems))
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryEmptyPreview() {
    PolaroamTheme {
        GalleryContent(GalleryUiState.Loaded(emptyList()))
    }
}
