package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.db.PestReportEntity
import com.example.ui.components.formatDateTime
import com.example.ui.viewmodel.PestOpState

private val CROP_SYMPTOMS = listOf(
    "Yellowing Leaves",
    "Brown Spots / Blight",
    "Rolled / Curled Leaves",
    "Chewed Edges / Holes",
    "Powdery Mildew",
    "Aphids / Whiteflies",
    "Wilting Stems"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PestDetectionScreen(
    pestReports: List<PestReportEntity>,
    pestState: PestOpState,
    onAnalyzePest: (imageUri: Uri?, symptoms: List<String>, notes: String) -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var notes by rememberSaveable { mutableStateOf("") }

    // Zero-permission Android Photo Picker (Google Play policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.size(4.dp))
            Column(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pest & Crop Health Scanner (CR-07)",
                    style = MaterialTheme.typography.headlineSmall
                )

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedImageUri != null) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected crop photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clip(MaterialTheme.shapes.large),
                                        contentScale = ContentScale.Crop
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Photo attached for multimodal AI scan",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(onClick = { selectedImageUri = null }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Remove photo")
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("pick_pest_image_button")
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Upload Crop Photo (Optional)")
                            }
                        }

                        Text(
                            text = "Observed Crop Symptoms",
                            style = MaterialTheme.typography.titleSmall
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CROP_SYMPTOMS.forEach { symptom ->
                                val selected = symptom in selectedSymptoms
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (selected) selectedSymptoms.remove(symptom)
                                        else selectedSymptoms.add(symptom)
                                    },
                                    label = { Text(symptom) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Crop variety & field observations (optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("pest_notes_input"),
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                onAnalyzePest(selectedImageUri, selectedSymptoms.toList(), notes)
                            },
                            enabled = pestState !is PestOpState.Loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("run_pest_diagnosis_button")
                        ) {
                            if (pestState is PestOpState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Diagnosing via Gemini Vision...")
                            } else {
                                Icon(Icons.Filled.Biotech, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Diagnose & Save to PestReports")
                            }
                        }

                        if (pestState is PestOpState.Error) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pestState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (pestState is PestOpState.Success) {
                            val d = pestState.diagnosis
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = d.detectedIssue,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = "${d.severity} • ${d.confidencePercent}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text("Immediate: ${d.immediateAction}", style = MaterialTheme.typography.bodySmall)
                                    Text("Treatment: ${d.organicTreatment}", style = MaterialTheme.typography.bodySmall)
                                    Text("Prevention: ${d.preventiveMeasure}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "PestReports History (${pestReports.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        items(pestReports, key = { it.reportId }) { report ->
            Card(
                modifier = Modifier.widthIn(max = 880.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = report.detectedIssue,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = formatDateTime(report.reportedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = report.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "image_ref: ${report.imageRef.take(48)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.size(16.dp)) }
    }
}
