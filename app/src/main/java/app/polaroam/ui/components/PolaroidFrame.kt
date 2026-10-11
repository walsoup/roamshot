package app.polaroam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.polaroam.R
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.theme.PolaroamTheme
import java.io.File

@Composable
fun PolaroidFrame(
    caption: String,
    modifier: Modifier = Modifier,
    tiltDegrees: Float = 0f,
    photo: @Composable BoxScope.() -> Unit,
) {
    val skin = PolaroamTheme.skin.polaroid
    Column(
        modifier = modifier
            .graphicsLayer { rotationZ = tiltDegrees }
            .pixelFrame(skin.frame),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(skin.photoBackground)
                .clipToBounds(),
            content = photo,
        )
        Text(
            text = caption,
            style = PolaroamTheme.typography.caption,
            color = skin.captionColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = skin.captionPadding),
        )
    }
}

@Composable
fun PolaroidPhoto(
    photoPath: String?,
    caption: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tiltDegrees: Float = 0f,
) {
    PolaroidFrame(caption = caption, modifier = modifier, tiltDegrees = tiltDegrees) {
        if (photoPath != null) {
            PixelAsyncImage(
                model = File(photoPath),
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            PixelImage(
                id = R.drawable.photo_placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
