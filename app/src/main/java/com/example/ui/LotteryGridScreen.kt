package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GeneratedGrid
import com.example.model.PlacementOrder
import com.example.model.TextRule

private val DarkActionBg = Color(0xFF1F2937)
private val GreenExportBg = Color(0xFF16A34A)
private val RedPdfBg = Color(0xFFDC2626)
private val BlueSettingsBg = Color(0xFFEFF6FF)
private val BlueSettingsBorder = Color(0xFFDBEAFE)
private val GreenSettingsBg = Color(0xFFF0FDF4)
private val GreenSettingsBorder = Color(0xFFDCFCE7)
private val StatsGrayBg = Color(0xFFF3F4F6)
private val ScreenBg = Color(0xFFF3F4F6)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LotteryGridApp(viewModel: LotteryViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissInfo()
        }
    }

    // Always enforce RTL layout matching dir="rtl" in the HTML template
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = ScreenBg
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Controls Panel (گرڈ سیٹنگز)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title: گرڈ سیٹنگز (Grid Settings)
                        Text(
                            text = "گرڈ سیٹنگز (Grid Settings)",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1F2937)
                            )
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 10.dp, bottom = 14.dp),
                            thickness = 1.dp,
                            color = Color(0xFFE5E7EB)
                        )

                        // Grid Size: Columns & Rows (In RTL: Columns on Right, Rows on Left)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // کالم (Columns)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "کالم (Columns):",
                                    style = TextStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = uiState.colsInput,
                                    onValueChange = viewModel::onColsChanged,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_cols"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF9FAFB),
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFD1D5DB),
                                        focusedBorderColor = Color(0xFF4B5563)
                                    )
                                )
                            }

                            // قطاریں (Rows)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "قطاریں (Rows):",
                                    style = TextStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = uiState.rowsInput,
                                    onValueChange = viewModel::onRowsChanged,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_rows"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color(0xFFF9FAFB),
                                        focusedContainerColor = Color.White,
                                        unfocusedBorderColor = Color(0xFFD1D5DB),
                                        focusedBorderColor = Color(0xFF4B5563)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Numbers Settings Box (bg-blue-50)
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = BlueSettingsBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BlueSettingsBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "نمبرز کی ترتیب (1, 2, 3...)",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E40AF)
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // کہاں تک؟ (Right in RTL)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "کہاں تک؟",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        OutlinedTextField(
                                            value = uiState.totalNumbersInput,
                                            onValueChange = viewModel::onTotalNumbersChanged,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                textAlign = TextAlign.Center,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_total_numbers"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.Black,
                                                unfocusedTextColor = Color.Black,
                                                unfocusedContainerColor = Color.White,
                                                focusedContainerColor = Color.White,
                                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                                focusedBorderColor = Color(0xFF3B82F6)
                                            )
                                        )
                                    }

                                    // طریقہ (Placement) (Left in RTL)
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Text(
                                            text = "طریقہ (Placement)",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        StyledPlacementDropdown(
                                            selectedOrder = uiState.numOrder,
                                            onOrderSelected = viewModel::onNumOrderChanged,
                                            modifier = Modifier.testTag("dropdown_num_order")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Text Settings Box (bg-green-50)
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenSettingsBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GreenSettingsBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "خاص الفاظ (Custom Texts)",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF166534)
                                        )
                                    )

                                    Button(
                                        onClick = { viewModel.addTextRule("", 5) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenExportBg),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("button_add_text_rule")
                                    ) {
                                        Text(
                                            text = "+ نیا لفظ شامل کریں",
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                if (uiState.textRules.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        uiState.textRules.forEachIndexed { index, rule ->
                                            CustomTextRuleCard(
                                                rule = rule,
                                                index = index,
                                                onUpdate = { newText, newCount, newOrder ->
                                                    viewModel.updateTextRule(rule.id, newText, newCount, newOrder)
                                                },
                                                onDelete = {
                                                    viewModel.removeTextRule(rule.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons: Generate Grid, Export as Image & Export as PDF
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.generateGrid() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkActionBg,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("button_generate_grid")
                            ) {
                                Text(
                                    text = "Generate Grid (گرڈ بنائیں)",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.exportAndShare(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GreenExportBg,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !uiState.isExporting && !uiState.isExportingPdf,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("button_export_grid")
                                ) {
                                    if (uiState.isExporting) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Export HD Image (تصویر)",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1
                                    )
                                }

                                Button(
                                    onClick = { viewModel.exportAndSharePdf(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RedPdfBg,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !uiState.isExporting && !uiState.isExportingPdf,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("button_export_pdf")
                                ) {
                                    if (uiState.isExportingPdf) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Export HD PDF (پی ڈی ایف)",
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Card 2: Grid Display Area
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        uiState.generatedGrid?.let { grid ->
                            // Statistics Bar matching Screenshot 2 exactly:
                            // Right: کل ڈبے: 90  |  Center: بھرے ہوئے: 45  |  Left: خالی: 45
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = StatsGrayBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right (RTL): کل ڈبے: 90
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "کل ڈبے: ",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.Black
                                            )
                                        )
                                        Text(
                                            text = grid.stats.totalCells.toString(),
                                            style = TextStyle(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = Color.Black
                                            )
                                        )
                                    }

                                    // Center: بھرے ہوئے: 45
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "بھرے ہوئے: ",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1D4ED8)
                                            )
                                        )
                                        Text(
                                            text = grid.stats.filledCells.toString(),
                                            style = TextStyle(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = Color(0xFF1D4ED8)
                                            )
                                        )
                                    }

                                    // Left: خالی: 45
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "خالی: ",
                                            style = TextStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFDC2626)
                                            )
                                        )
                                        Text(
                                            text = grid.stats.emptyCells.toString(),
                                            style = TextStyle(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = Color(0xFFDC2626)
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid Table
                            ResponsiveLotteryTable(grid = grid)
                        } ?: run {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "پہلے Generate Grid پر کلک کریں!",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Validation Alert Dialog matching JS alert in HTML
            if (uiState.validationError != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissError() },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.testTag("dismiss_error_button")
                        ) {
                            Text("ٹھیک ہے (OK)", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text(
                            text = "توجہ فرمائیں (Notice)",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = uiState.validationError ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun StyledPlacementDropdown(
    selectedOrder: PlacementOrder,
    onOrderSelected: (PlacementOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = if (selectedOrder == PlacementOrder.RANDOM) "Random (بکھرے ہوئے)" else "Sequential (ترتیب سے)",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                ),
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Random (بکھرے ہوئے)") },
                onClick = {
                    onOrderSelected(PlacementOrder.RANDOM)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sequential (ترتیب سے)") },
                onClick = {
                    onOrderSelected(PlacementOrder.SEQUENTIAL)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun CustomTextRuleCard(
    rule: TextRule,
    index: Int,
    onUpdate: (String, Int, PlacementOrder) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(6.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "کیا لکھنا ہے؟",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("button_delete_rule_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            OutlinedTextField(
                value = rule.text,
                onValueChange = { onUpdate(it, rule.count, rule.order) },
                placeholder = { Text("مثال: غبارے", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_rule_text_$index"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedBorderColor = Color(0xFF10B981)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "کتنے ڈبوں میں؟",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = rule.count.toString(),
                        onValueChange = {
                            val newCount = it.toIntOrNull() ?: 0
                            onUpdate(rule.text, newCount, rule.order)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp, color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_rule_count_$index"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedBorderColor = Color(0xFF10B981)
                        )
                    )
                }

                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "طریقہ",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    StyledPlacementDropdown(
                        selectedOrder = rule.order,
                        onOrderSelected = { onUpdate(rule.text, rule.count, it) },
                        modifier = Modifier.testTag("dropdown_rule_order_$index")
                    )
                }
            }
        }
    }
}

@Composable
fun ResponsiveLotteryTable(grid: GeneratedGrid) {
    val isScrollable = grid.cols > 8

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isScrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            .background(Color.White)
            .border(2.dp, Color.Black)
            .testTag("lottery_grid_table")
    ) {
        Column(modifier = if (isScrollable) Modifier.width((grid.cols * 64).dp) else Modifier.fillMaxWidth()) {
            var cellIndex = 0
            for (r in 0 until grid.rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until grid.cols) {
                        val cellValue = if (cellIndex < grid.cells.size) grid.cells[cellIndex] else null
                        cellIndex++

                        val cellModifier = if (isScrollable) {
                            Modifier
                                .width(64.dp)
                                .height(48.dp)
                        } else {
                            Modifier
                                .weight(1f)
                                .height(48.dp)
                        }

                        Box(
                            modifier = cellModifier
                                .border(1.dp, Color.Black)
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cellValue.isNullOrEmpty()) {
                                // For two-word Urdu phrases like "جعلی نوٹ", split with newline for vertical stacking
                                val displayText = if (cellValue.contains(" ") && cellValue.length > 5) {
                                    cellValue.replace(" ", "\n")
                                } else {
                                    cellValue
                                }

                                val isNumber = cellValue.toIntOrNull() != null

                                Text(
                                    text = displayText,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isNumber) 16.sp else 13.sp,
                                        lineHeight = if (isNumber) 16.sp else 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black
                                    ),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
