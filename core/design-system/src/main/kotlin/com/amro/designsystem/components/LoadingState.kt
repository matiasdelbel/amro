package com.amro.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.amro.designsystem.R
import com.amro.designsystem.theme.PreviewSurface
import com.amro.designsystem.theme.spacers

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
) {
    val contentDescription = title ?: stringResource(R.string.loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = MaterialTheme.spacers.medium)
            .semantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        CircularProgressIndicator(
            modifier = Modifier.size(size = 64.dp),
            color = MaterialTheme.colorScheme.primary,
        )

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = MaterialTheme.spacers.medium),
            )
        }

        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = MaterialTheme.spacers.small),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingStateDefaultPreview() = PreviewSurface {
    LoadingState()
}

@PreviewLightDark
@Composable
private fun LoadingStateWithTitleAndMessagePreview() = PreviewSurface {
    LoadingState(
        title = "Loading movies",
        message = "Fetching this week's top trending titles…",
    )
}
