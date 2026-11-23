package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SOIL_TYPES
import com.example.ui.components.VersionBadgeChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onSignUp: (
        name: String,
        email: String,
        password: String,
        phone: String,
        farmName: String,
        location: String,
        totalLandHa: String,
        soilType: String
    ) -> Unit,
    onLoadDemoFarmer: () -> Unit,
    onClearError: () -> Unit
) {
    var modeIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = Login, 1 = Sign Up
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var farmName by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("Bogura, Bangladesh") }
    var totalLandHa by rememberSaveable { mutableStateOf("3.0") }
    var soilType by rememberSaveable { mutableStateOf(SOIL_TYPES.first()) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Banner Card
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_farm_hero),
                            contentDescription = "Cultiveq smart farm terrace fields",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color(0xFF0C2A17).copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            VersionBadgeChip()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Cultiveq V2",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Smarter Farming, Better Decisions",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFC8F7D4)
                                )
                                Text(
                                    text = "Evolved from v1.0 (Stateless) → v2.0 (3NF SQLite + AI)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                // Instant Demo Farmer Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Instant Evaluation Mode",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Sign in as Amina Rahman with pre-seeded 3NF farm, tasks, expenses, weather & AI history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = onLoadDemoFarmer,
                            enabled = !isBusy,
                            modifier = Modifier.testTag("load_demo_farmer_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Load Demo Farmer")
                        }
                    }
                }

                // Auth Form Card
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = modeIndex == 0,
                                onClick = {
                                    modeIndex = 0
                                    onClearError()
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                modifier = Modifier.testTag("auth_tab_login")
                            ) {
                                Text("Login")
                            }
                            SegmentedButton(
                                selected = modeIndex == 1,
                                onClick = {
                                    modeIndex = 1
                                    onClearError()
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                modifier = Modifier.testTag("auth_tab_signup")
                            ) {
                                Text("Create Account")
                            }
                        }

                        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (modeIndex == 1) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_phone_input")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        if (modeIndex == 1) {
                            HorizontalDivider()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Agriculture,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "1:1 Farm Information Setup",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            OutlinedTextField(
                                value = farmName,
                                onValueChange = { farmName = it },
                                label = { Text("Farm Name (e.g. Sunrise Agro Plot)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_farm_name_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("Location / City") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.4f).testTag("signup_location_input")
                                )
                                OutlinedTextField(
                                    value = totalLandHa,
                                    onValueChange = { totalLandHa = it },
                                    label = { Text("Land (ha)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f).testTag("signup_land_input")
                                )
                            }
                            Text(
                                text = "Soil Type",
                                style = MaterialTheme.typography.labelLarge
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SOIL_TYPES.forEach { option ->
                                    FilterChip(
                                        selected = soilType == option,
                                        onClick = { soilType = option },
                                        label = { Text(option) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (modeIndex == 0) {
                                    onLogin(email, password)
                                } else {
                                    onSignUp(name, email, password, phone, farmName, location, totalLandHa, soilType)
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (modeIndex == 0) "Unlock My Farm Dashboard" else "Create Account & Farm",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                modeIndex = if (modeIndex == 0) 1 else 0
                                onClearError()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (modeIndex == 0) "New to Cultiveq V2? Register a Farm" else "Already have an account? Login"
                            )
                        }
                    }
                }
            }
        }
    }
}
