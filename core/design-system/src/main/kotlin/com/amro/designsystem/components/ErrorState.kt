package com.amro.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amro.designsystem.R
import com.amro.designsystem.theme.PreviewSurface
import com.amro.designsystem.theme.spacers

@Composable
fun ErrorState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacers.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(size = 64.dp),
        )

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacers.medium),
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacers.small),
        )

        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = MaterialTheme.spacers.medium),
                content = { Text(text = stringResource(id = R.string.retry)) }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStateWithRetryPreview() = PreviewSurface {
    ErrorState(
        icon = Icons.Outlined.CloudOff,
        title = "No Connection",
        message = "There is no internet connection. Check your network and try again.",
        onRetry = {},
    )
}

@PreviewLightDark
@Composable
private fun ErrorStateWithoutRetryPreview() = PreviewSurface {
    ErrorState(
        icon = Icons.Outlined.ErrorOutline,
        title = "Something went wrong",
        message = "We're sorry, but something went wrong. Please try again later."
    )
}
