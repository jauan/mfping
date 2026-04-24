package com.masterflight.ping.ui.traceroute

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masterflight.ping.R
import com.masterflight.ping.data.network.TraceStep
import com.masterflight.ping.ui.traceroute.viewmodel.TraceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceScreen(
    viewModel: TraceViewModel,
    onBack: () -> Unit
) {
    val steps by viewModel.steps.collectAsState()
    val isTracing by viewModel.isTracing.collectAsState()
    var host by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.traceroute_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(id = R.string.host_input_label)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (isTracing) viewModel.stopTrace() else viewModel.startTrace(host)
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        if (isTracing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isTracing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(steps) { step ->
                    TraceStepItem(step)
                }
            }
        }
    }
}

@Composable
fun TraceStepItem(step: TraceStep) {
    ListItem(
        leadingContent = {
            Text(
                text = "${step.hop}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
        },
        headlineContent = {
            Text(
                text = if (step.isTimeout) "*" else (step.ip ?: "Unknown"),
                color = if (step.isTimeout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            if (!step.isTimeout && step.responseTimeMs != null) {
                Text(text = "${String.format("%.1f", step.responseTimeMs)} ms")
            }
        }
    )
    HorizontalDivider()
}
