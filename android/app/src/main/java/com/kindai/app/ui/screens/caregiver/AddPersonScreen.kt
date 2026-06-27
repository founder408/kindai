package com.kindai.app.ui.screens.caregiver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.elderly.ElderlyCreateRequest
import com.kindai.app.data.model.elderly.ElderlyCreateWithAccountRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

private data class RelationshipOption(val key: String, val label: String)

private val RELATIONSHIPS = listOf(
    RelationshipOption("O'zim", "O'zim"),
    RelationshipOption("Otam", "Otam"),
    RelationshipOption("Onam", "Onam"),
    RelationshipOption("Bobom", "Bobom"),
    RelationshipOption("Buvim", "Buvim"),
    RelationshipOption("Farzandim", "Farzandim"),
    RelationshipOption("Turmush o'rtog'im", "Turmush o'rtog'im"),
    RelationshipOption("Qaramog'imdagi", "Qaramog'imdagi"),
    RelationshipOption("Boshqa", "Boshqa"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    apiService: ApiService,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var selectedRelationship by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var chronicDiseases by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var createAccount by remember { mutableStateOf(false) }
    var loginPhone by remember { mutableStateOf("") }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var consentChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isSelf = selectedRelationship == "O'zim"
    val isChild = selectedRelationship == "Farzandim"
    val needsConsent = !isSelf && !isChild && selectedRelationship.isNotBlank()

    Scaffold(
        topBar = {
            KindTopAppBar(title = "Profil yaratish", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Relationship
            KindSectionHeader(title = "Kimni kuzatmoqchisiz?")
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RELATIONSHIPS) { option ->
                    val isSelected = selectedRelationship == option.key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRelationship = option.key },
                        label = {
                            Text(
                                text = option.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        shape = KindShapes.chip,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.PrimaryLight,
                            selectedLabelColor = KindColors.Primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Basic info
            KindSectionHeader(title = "Asosiy ma'lumotlar")
            Spacer(modifier = Modifier.height(8.dp))

            KindTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "To'liq ism *",
                leadingIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KindTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = "Yosh",
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Cake
                )
                KindTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = "Jins",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            KindTextField(
                value = chronicDiseases,
                onValueChange = { chronicDiseases = it },
                label = "Surunkali kasalliklar",
                leadingIcon = Icons.Default.MedicalServices,
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Contact
            KindSectionHeader(title = "Bog'lanish")
            Spacer(modifier = Modifier.height(8.dp))

            KindTextField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = "Favqulodda telefon",
                leadingIcon = Icons.Default.Phone
            )
            Spacer(modifier = Modifier.height(12.dp))

            KindTextField(
                value = address,
                onValueChange = { address = it },
                label = "Manzil",
                leadingIcon = Icons.Default.LocationOn,
                singleLine = false,
                maxLines = 2
            )

            // Section 4: Account creation (only for elderly relatives)
            AnimatedVisibility(visible = !isSelf && !isChild && selectedRelationship.isNotBlank()) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    KindSectionHeader(title = "Hisob yaratish")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kattangiz o'zi telefonda kirishi uchun",
                        fontSize = 13.sp,
                        color = KindColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Login hisob yaratish",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Switch(
                            checked = createAccount,
                            onCheckedChange = { createAccount = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = KindColors.Primary)
                        )
                    }

                    AnimatedVisibility(visible = createAccount) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            KindTextField(
                                value = loginPhone,
                                onValueChange = { loginPhone = it },
                                label = "Telefon raqami",
                                leadingIcon = Icons.Default.Phone
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            KindTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = "Email",
                                leadingIcon = Icons.Default.Email
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            KindPasswordField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = "Parol *",
                                leadingIcon = Icons.Default.Lock
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Birinchi kirishda parol o'zgartiriladi",
                                fontSize = 12.sp,
                                color = KindColors.Primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Consent
            if (needsConsent) {
                Card(
                    shape = KindShapes.card,
                    colors = CardDefaults.cardColors(containerColor = KindColors.InfoBg)
                ) {
                    Row(
                        modifier = Modifier.padding(KindSpacing.cardPadding),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = consentChecked,
                            onCheckedChange = { consentChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = KindColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Men ushbu insonning sog'liq ma'lumotlarini kiritish va kuzatish uchun roziligiga yoki qonuniy asosga egaman.",
                            fontSize = 13.sp,
                            color = KindColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else if (isChild) {
                Card(
                    shape = KindShapes.card,
                    colors = CardDefaults.cardColors(containerColor = KindColors.InfoBg)
                ) {
                    Row(modifier = Modifier.padding(KindSpacing.cardPadding)) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = KindColors.Info,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ota-ona sifatida farzandingiz sog'liq holatini kuzatish huquqiga egasiz.",
                            fontSize = 13.sp,
                            color = KindColors.TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
                consentChecked = true
            } else if (isSelf) {
                consentChecked = true
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg),
                    shape = KindShapes.medium
                ) {
                    Text(
                        text = errorMessage!!,
                        color = KindColors.Error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val canSubmit = fullName.isNotBlank() && selectedRelationship.isNotBlank() && consentChecked &&
                (!createAccount || (loginPassword.isNotBlank() && (loginPhone.isNotBlank() || loginEmail.isNotBlank())))

            KindPrimaryButton(
                text = if (createAccount) "Profil + hisob yaratish" else "Profil yaratish",
                isLoading = isLoading,
                enabled = canSubmit,
                icon = Icons.Default.PersonAdd,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        if (createAccount) {
                            val result = safeApiCall {
                                apiService.createElderlyWithAccount(
                                    ElderlyCreateWithAccountRequest(
                                        fullName = fullName.trim(),
                                        age = age.toIntOrNull(),
                                        gender = gender.trim().ifBlank { null },
                                        chronicDiseases = chronicDiseases.trim().ifBlank { null },
                                        emergencyPhone = emergencyPhone.trim().ifBlank { null },
                                        address = address.trim().ifBlank { null },
                                        relationshipToElderly = selectedRelationship,
                                        loginPhone = loginPhone.trim().ifBlank { null },
                                        loginEmail = loginEmail.trim().ifBlank { null },
                                        loginPassword = loginPassword
                                    )
                                )
                            }
                            when (result) {
                                is UiState.Success -> onSuccess()
                                is UiState.Error -> errorMessage = result.message
                                else -> {}
                            }
                        } else {
                            val result = safeApiCall {
                                apiService.createElderly(
                                    ElderlyCreateRequest(
                                        fullName = fullName.trim(),
                                        age = age.toIntOrNull(),
                                        gender = gender.trim().ifBlank { null },
                                        chronicDiseases = chronicDiseases.trim().ifBlank { null },
                                        emergencyPhone = emergencyPhone.trim().ifBlank { null },
                                        address = address.trim().ifBlank { null },
                                        relationshipToElderly = selectedRelationship
                                    )
                                )
                            }
                            when (result) {
                                is UiState.Success -> onSuccess()
                                is UiState.Error -> errorMessage = result.message
                                else -> {}
                            }
                        }
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
