package app.polaroam.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import app.polaroam.ui.components.PixelLoadingDots
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PolaroidPhoto
import app.polaroam.ui.preview.PreviewData
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GalleryContent(state)
}

@Composable
fun GalleryContent(state: GalleryUiState, modifier: Modifier = Modifier) {
    val colors = PolaroamTheme.colors
    val spacing = PolaroamTheme.spacing
    val typography = PolaroamTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.cork),
    ) {
        when (state) {
            GalleryUiState.Loading -> PixelLoadingDots(Modifier.align(Alignment.Center), color = colors.photoPaper)

            GalleryUiState.Error -> PixelPanel(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.l),
            ) {
                Text(stringResource(R.string.gallery_error), style = typography.body)
            }

            is GalleryUiState.Loaded -> if (state.items.isEmpty()) {
                PixelPanel(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(spacing.l),
                ) {
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
                        PolaroidPhoto(
                            photoPath = item.photoPath,
                            caption = item.title,
                            contentDescription = stringResource(R.string.gallery_photo_cd, item.title),
                            tiltDegrees = item.tiltDegrees,
                        )
                    }
                }
            }
        }
    }
}

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
