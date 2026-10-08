package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.GeneratedGrid
import com.example.model.GridStatistics
import com.example.model.PlacementOrder
import com.example.model.TextRule
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

object GridGenerator {

    sealed class GenerationResult {
        data class Success(val grid: GeneratedGrid) : GenerationResult()
        data class Error(val message: String) : GenerationResult()
    }

    fun generate(
        rows: Int,
        cols: Int,
        totalNumbers: Int,
        numOrder: PlacementOrder,
        rules: List<TextRule>
    ): GenerationResult {
        val safeRows = max(1, rows)
        val safeCols = max(1, cols)
        val totalCells = safeRows * safeCols

        // Filter valid rules
        val validRules = rules.filter { it.count > 0 && it.text.isNotBlank() }
        val totalCustomTextCount = validRules.sumOf { it.count }
        val projectedFilled = totalNumbers + totalCustomTextCount

        if (projectedFilled > totalCells) {
            val errorMsg = "ڈبوں کی تعداد کم ہے! کل ڈبے $totalCells ہیں اور آپ نے $projectedFilled چیزیں ڈالنے کا کہا ہے۔ برائے مہربانی مقدر کم کریں۔"
            return GenerationResult.Error(errorMsg)
        }

        val cells = Array<String?>(totalCells) { null }

        // 1. Place numbers first
        val numItems = (1..totalNumbers).map { it.toString() }
        placeItems(cells, numItems, numOrder)

        // 2. Place custom texts
        for (rule in validRules) {
            val textItems = List(rule.count) { rule.text }
            placeItems(cells, textItems, rule.order)
        }

        val stats = GridStatistics(
            totalCells = totalCells,
            filledCells = projectedFilled,
            emptyCells = totalCells - projectedFilled
        )

        return GenerationResult.Success(
            GeneratedGrid(
                rows = safeRows,
                cols = safeCols,
                cells = cells.toList(),
                stats = stats
            )
        )
    }

    private fun placeItems(cells: Array<String?>, items: List<String>, order: PlacementOrder) {
        val emptyIndices = mutableListOf<Int>()
        for (i in cells.indices) {
            if (cells[i] == null) {
                emptyIndices.add(i)
            }
        }

        if (order == PlacementOrder.SEQUENTIAL) {
            for (i in items.indices) {
                if (i < emptyIndices.size) {
                    cells[emptyIndices[i]] = items[i]
                }
            }
        } else {
            val available = emptyIndices.toMutableList()
            for (item in items) {
                if (available.isEmpty()) break
                val randIdx = Random.nextInt(available.size)
                val targetCellIdx = available.removeAt(randIdx)
                cells[targetCellIdx] = item
            }
        }
    }

    /**
     * Renders the grid into an Ultra-High Definition (UHD / 300 DPI) Bitmap with crisp typography,
     * high-contrast borders, statistics pill, and subpixel anti-aliasing.
     */
    fun createGridBitmap(grid: GeneratedGrid): Bitmap {
        val padding = 80
        val headerHeight = 260
        val footerHeight = 70

        // Adaptive high-resolution cell sizing ensuring crystal clarity
        // Target canvas width around 2400-2800px for sharp HD viewing and printing
        val targetTableWidth = 2400
        val rawCellWidth = (targetTableWidth / grid.cols).coerceIn(180, 520)
        val rawCellHeight = (rawCellWidth * 0.62f).toInt().coerceIn(120, 360)

        // Ensure we respect Android's max safe bitmap allocation dimensions (4096 x 4096)
        val maxDimension = 4096
        val cellWidth = minOf(rawCellWidth, (maxDimension - (padding * 2)) / grid.cols)
        val cellHeight = minOf(rawCellHeight, (maxDimension - headerHeight - footerHeight - (padding * 2)) / grid.rows)

        val tableWidth = grid.cols * cellWidth
        val tableHeight = grid.rows * cellHeight
        val totalWidth = tableWidth + (padding * 2)
        val totalHeight = tableHeight + headerHeight + footerHeight + (padding * 2)

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Crisp white background
        canvas.drawColor(Color.WHITE)

        // Header Paints with Anti-aliasing and Subpixel rendering
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.parseColor("#111827")
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isDither = true
        }

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.parseColor("#374151")
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isDither = true
        }

        val centerX = totalWidth / 2f
        canvas.drawText("Advanced Lottery Grid Maker", centerX, padding + 60f, titlePaint)
        canvas.drawText("گرڈ سیٹنگز اور قرعہ اندازی شیٹ", centerX, padding + 115f, subTitlePaint)

        // Stats card/pill background
        val statsCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
            isDither = true
        }
        val statsBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isDither = true
        }
        val statsPillWidth = minOf(totalWidth - (padding * 2f), 1200f)
        val statsRect = RectF(
            centerX - (statsPillWidth / 2f),
            padding + 145f,
            centerX + (statsPillWidth / 2f),
            padding + 225f
        )
        canvas.drawRoundRect(statsRect, 20f, 20f, statsCardPaint)
        canvas.drawRoundRect(statsRect, 20f, 20f, statsBorderPaint)

        val statsPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.parseColor("#1F2937")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isDither = true
        }
        val statsText = "کل ڈبے: ${grid.stats.totalCells}    |    بھرے ہوئے: ${grid.stats.filledCells}    |    خالی: ${grid.stats.emptyCells}"
        val statsBaseline = statsRect.centerY() - ((statsPaint.descent() + statsPaint.ascent()) / 2f)
        canvas.drawText(statsText, centerX, statsBaseline, statsPaint)

        // Outer table border paint (extra crisp, bold)
        val outerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 5f
            style = Paint.Style.STROKE
            isDither = true
        }

        // Cell border paint
        val cellBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isDither = true
        }

        // Cell Text Paint
        val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isDither = true
        }

        val startX = padding.toFloat()
        val startY = (padding + headerHeight).toFloat()

        // Draw grid cells and content
        var cellIndex = 0

        for (r in 0 until grid.rows) {
            val cellTop = startY + (r * cellHeight)
            val cellBottom = cellTop + cellHeight

            for (c in 0 until grid.cols) {
                val cellLeft = startX + (c * cellWidth)
                val cellRight = cellLeft + cellWidth

                // Draw cell border
                canvas.drawRect(cellLeft, cellTop, cellRight, cellBottom, cellBorderPaint)

                // Draw cell content with adaptive scaling
                val cellValue = if (cellIndex < grid.cells.size) grid.cells[cellIndex] else null
                if (!cellValue.isNullOrEmpty()) {
                    var optimalTextSize = (cellHeight * 0.46f).coerceAtMost(cellWidth * 0.38f)
                    cellTextPaint.textSize = optimalTextSize

                    // Ensure text doesn't touch borders
                    val maxTextWidth = cellWidth - 28f
                    val currentTextWidth = cellTextPaint.measureText(cellValue)
                    if (currentTextWidth > maxTextWidth && currentTextWidth > 0f) {
                        optimalTextSize *= (maxTextWidth / currentTextWidth)
                        cellTextPaint.textSize = optimalTextSize.coerceAtLeast(20f)
                    }

                    val textX = cellLeft + (cellWidth / 2f)
                    val textY = cellTop + (cellHeight / 2f) - ((cellTextPaint.descent() + cellTextPaint.ascent()) / 2f)
                    canvas.drawText(cellValue, textX, textY, cellTextPaint)
                }

                cellIndex++
            }
        }

        // Draw bold outer border
        canvas.drawRect(
            startX,
            startY,
            startX + tableWidth,
            startY + tableHeight,
            outerBorderPaint
        )

        // Footer note
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.parseColor("#6B7280")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isDither = true
        }
        val footerY = startY + tableHeight + 46f
        canvas.drawText("Generated with Advanced Lottery Grid Maker • Ultra HD Quality", centerX, footerY, footerPaint)

        return bitmap
    }

    /**
     * Saves the grid bitmap to cache and returns its FileProvider Uri for sharing.
     */
    fun saveToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesFolder, "lottery-grid.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Saves the image to device's MediaStore Pictures directory.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val filename = "lottery-grid-${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LotteryGrid")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    true
                } else false
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "LotteryGrid").apply { mkdirs() }
                val file = File(appDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Launches Android Share sheet with the generated lottery grid.
     */
    fun shareGridImage(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Advanced Lottery Grid HD")
            putExtra(Intent.EXTRA_TEXT, "Lottery Grid Maker - اعلیٰ کوالٹی تصویر قرعہ اندازی گرڈ")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Lottery Grid (تصویر بھیجیں)"))
    }

    /**
     * Creates an authentic, official vector PDF document of the lottery grid and returns its FileProvider Uri.
     * Features:
     * - Formal A4 document styling with certificate border and header
     * - Official metadata block (Sheet ID, Date & Time, Dimensions, Stats)
     * - Authentic lottery grid matrix with Column headers (1..N) and Row headers (1..N)
     * - Multi-page pagination when grid has many rows
     * - Official Verification & Signatures section
     * - 100% Vector PDF rendering (selectable text, infinite sharpness at any zoom)
     */
    fun createGridPdf(context: Context, grid: GeneratedGrid): Uri? {
        var pdfDocument: PdfDocument? = null
        return try {
            val doc = PdfDocument()
            pdfDocument = doc

            // Select orientation: Landscape if wide table, Portrait if tall table
            val isLandscape = grid.cols > (grid.rows * 1.15f)
            val pageWidth = if (isLandscape) 842 else 595
            val pageHeight = if (isLandscape) 595 else 842

            val maxRowsPerPage = if (isLandscape) 14 else 22
            val totalPages = max(1, (grid.rows + maxRowsPerPage - 1) / maxRowsPerPage)

            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            val currentDateStr = dateFormat.format(Date())
            val sheetId = "#LG-" + (System.currentTimeMillis() % 100000).toString().padStart(5, '0')

            // Common Paints
            val frameOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                strokeWidth = 1.4f
                style = Paint.Style.STROKE
            }
            val frameInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
            }

            val docTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val docSubPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#475569")
                textSize = 9.5f
                textAlign = Paint.Align.CENTER
            }

            val metaBoxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            val metaBoxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 0.75f
                style = Paint.Style.STROKE
            }
            val metaLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val metaValuePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val thBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.FILL
            }
            val rowThBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F1F5F9")
                style = Paint.Style.FILL
            }
            val thTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val cellTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.BLACK
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val tableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                strokeWidth = 0.75f
                style = Paint.Style.STROKE
            }
            val outerTableBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }
            val sigLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                strokeWidth = 0.85f
                style = Paint.Style.STROKE
            }
            val sigLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#334155")
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 7.5f
                textAlign = Paint.Align.LEFT
            }

            // Loop through pages
            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawColor(Color.WHITE)

                // 1. Double border frame
                canvas.drawRect(14f, 14f, pageWidth - 14f, pageHeight - 14f, frameOuterPaint)
                canvas.drawRect(17f, 17f, pageWidth - 17f, pageHeight - 17f, frameInnerPaint)

                val centerX = pageWidth / 2f

                // 2. Official Document Header
                canvas.drawText("OFFICIAL LOTTERY DRAW SHEET - قرعہ اندازی گرڈ شیٹ", centerX, 36f, docTitlePaint)
                canvas.drawText("Advanced Lottery Grid Maker • تصدیق شدہ پرچی و دفتری ریکارڈ", centerX, 49f, docSubPaint)

                // 3. Metadata summary block
                val metaTop = 56f
                val metaBottom = 96f
                val metaLeft = 24f
                val metaRight = pageWidth - 24f
                val metaRect = RectF(metaLeft, metaTop, metaRight, metaBottom)
                canvas.drawRoundRect(metaRect, 4f, 4f, metaBoxBg)
                canvas.drawRoundRect(metaRect, 4f, 4f, metaBoxBorder)

                // 4 columns in metadata box
                val metaColWidth = (metaRight - metaLeft) / 4f
                for (i in 1..3) {
                    val lineX = metaLeft + (i * metaColWidth)
                    canvas.drawLine(lineX, metaTop, lineX, metaBottom, metaBoxBorder)
                }

                // Col 1: Sheet ID
                val c1X = metaLeft + (metaColWidth / 2f)
                canvas.drawText("شیٹ نمبر (Sheet ID)", c1X, metaTop + 14f, metaLabelPaint)
                canvas.drawText(sheetId, c1X, metaTop + 30f, metaValuePaint)

                // Col 2: Date
                val c2X = metaLeft + metaColWidth + (metaColWidth / 2f)
                canvas.drawText("تاریخ (Date & Time)", c2X, metaTop + 14f, metaLabelPaint)
                canvas.drawText(currentDateStr, c2X, metaTop + 30f, metaValuePaint)

                // Col 3: Dimensions & Total
                val c3X = metaLeft + (metaColWidth * 2f) + (metaColWidth / 2f)
                canvas.drawText("گرڈ سائز (Dimensions)", c3X, metaTop + 14f, metaLabelPaint)
                val dimText = "${grid.cols} کالم × ${grid.rows} قطاریں (${grid.stats.totalCells} ڈبے)"
                canvas.drawText(dimText, c3X, metaTop + 30f, metaValuePaint)

                // Col 4: Statistics
                val c4X = metaLeft + (metaColWidth * 3f) + (metaColWidth / 2f)
                canvas.drawText("شماریات (Statistics)", c4X, metaTop + 14f, metaLabelPaint)
                val statsSummary = "بھرے: ${grid.stats.filledCells} | خالی: ${grid.stats.emptyCells}"
                canvas.drawText(statsSummary, c4X, metaTop + 30f, metaValuePaint)

                // 4. Authentic Lottery Grid Matrix
                val rowStart = pageIndex * maxRowsPerPage
                val rowEnd = minOf(grid.rows, rowStart + maxRowsPerPage)
                val rowsOnThisPage = rowEnd - rowStart

                val isLastPage = (pageIndex == totalPages - 1)
                val bottomReserved = if (isLastPage) 85f else 40f

                val tableTopMax = 106f
                val tableAvailHeight = (pageHeight - bottomReserved) - tableTopMax
                val tableAvailWidth = pageWidth - 48f

                // Row header column width (e.g. 30pt)
                val rowHeaderWidth = minOf(34f, tableAvailWidth / (grid.cols + 1f))
                val dataColWidth = (tableAvailWidth - rowHeaderWidth) / grid.cols

                val colHeaderHeight = 20f
                val maxDataRowHeight = (tableAvailHeight - colHeaderHeight) / rowsOnThisPage
                val dataRowHeight = minOf(maxDataRowHeight, dataColWidth * 0.72f).coerceAtLeast(14f)

                val actualTableWidth = rowHeaderWidth + (grid.cols * dataColWidth)
                val actualTableHeight = colHeaderHeight + (rowsOnThisPage * dataRowHeight)

                val tableLeft = 24f + ((tableAvailWidth - actualTableWidth) / 2f)
                val tableTop = tableTopMax + ((tableAvailHeight - actualTableHeight) / 3f).coerceAtLeast(0f)

                // Draw Table Header Corner: Row \ Col (قطار \ کالم)
                val cornerRect = RectF(tableLeft, tableTop, tableLeft + rowHeaderWidth, tableTop + colHeaderHeight)
                canvas.drawRect(cornerRect, thBgPaint)
                canvas.drawRect(cornerRect, tableBorderPaint)
                val cornerTextY = cornerRect.centerY() - ((thTextPaint.descent() + thTextPaint.ascent()) / 2f)
                canvas.drawText("R \\ C", cornerRect.centerX(), cornerTextY, thTextPaint)

                // Draw Column Headers: 1, 2, 3..
                for (c in 0 until grid.cols) {
                    val cLeft = tableLeft + rowHeaderWidth + (c * dataColWidth)
                    val cRight = cLeft + dataColWidth
                    val colRect = RectF(cLeft, tableTop, cRight, tableTop + colHeaderHeight)
                    canvas.drawRect(colRect, thBgPaint)
                    canvas.drawRect(colRect, tableBorderPaint)
                    val colTextY = colRect.centerY() - ((thTextPaint.descent() + thTextPaint.ascent()) / 2f)
                    canvas.drawText((c + 1).toString(), colRect.centerX(), colTextY, thTextPaint)
                }

                // Draw Rows for this page
                for (rIdx in 0 until rowsOnThisPage) {
                    val globalRow = rowStart + rIdx
                    val rTop = tableTop + colHeaderHeight + (rIdx * dataRowHeight)
                    val rBottom = rTop + dataRowHeight

                    // Row Header Cell (Row Number)
                    val rowHeaderRect = RectF(tableLeft, rTop, tableLeft + rowHeaderWidth, rBottom)
                    canvas.drawRect(rowHeaderRect, rowThBgPaint)
                    canvas.drawRect(rowHeaderRect, tableBorderPaint)
                    val rowTextY = rowHeaderRect.centerY() - ((thTextPaint.descent() + thTextPaint.ascent()) / 2f)
                    canvas.drawText((globalRow + 1).toString(), rowHeaderRect.centerX(), rowTextY, thTextPaint)

                    // Data Cells
                    for (c in 0 until grid.cols) {
                        val cellLeft = tableLeft + rowHeaderWidth + (c * dataColWidth)
                        val cellRight = cellLeft + dataColWidth
                        val cellRect = RectF(cellLeft, rTop, cellRight, rBottom)

                        // Draw cell border
                        canvas.drawRect(cellRect, tableBorderPaint)

                        // Draw cell content
                        val globalCellIndex = (globalRow * grid.cols) + c
                        val cellValue = if (globalCellIndex < grid.cells.size) grid.cells[globalCellIndex] else null
                        if (!cellValue.isNullOrEmpty()) {
                            var optimalSize = (dataRowHeight * 0.48f).coerceAtMost(dataColWidth * 0.38f)
                            cellTextPaint.textSize = optimalSize

                            val maxTextWidth = dataColWidth - 5f
                            val currentTextWidth = cellTextPaint.measureText(cellValue)
                            if (currentTextWidth > maxTextWidth && currentTextWidth > 0f) {
                                optimalSize *= (maxTextWidth / currentTextWidth)
                                cellTextPaint.textSize = optimalSize.coerceAtLeast(6.5f)
                            }

                            val textX = cellLeft + (dataColWidth / 2f)
                            val textY = cellRect.centerY() - ((cellTextPaint.descent() + cellTextPaint.ascent()) / 2f)
                            canvas.drawText(cellValue, textX, textY, cellTextPaint)
                        }
                    }
                }

                // Outer border for table
                canvas.drawRect(
                    tableLeft,
                    tableTop,
                    tableLeft + actualTableWidth,
                    tableTop + actualTableHeight,
                    outerTableBorderPaint
                )

                // 5. Official Verification / Signatures block on the last page
                if (isLastPage) {
                    val sigY = pageHeight - 64f
                    val sigLineWidth = 150f

                    // Left signature: Organizer
                    val sigLeftCenterX = 24f + (tableAvailWidth * 0.28f)
                    val sig1Start = sigLeftCenterX - (sigLineWidth / 2f)
                    val sig1End = sigLeftCenterX + (sigLineWidth / 2f)
                    canvas.drawLine(sig1Start, sigY, sig1End, sigY, sigLinePaint)
                    canvas.drawText("نگران کے دستخط (Supervisor Signature)", sigLeftCenterX, sigY + 13f, sigLabelPaint)

                    // Right signature: Official Stamp / Witness
                    val sigRightCenterX = 24f + (tableAvailWidth * 0.72f)
                    val sig2Start = sigRightCenterX - (sigLineWidth / 2f)
                    val sig2End = sigRightCenterX + (sigLineWidth / 2f)
                    canvas.drawLine(sig2Start, sigY, sig2End, sigY, sigLinePaint)
                    canvas.drawText("قرعہ اندازی مہر و تاریخ (Official Stamp / Date)", sigRightCenterX, sigY + 13f, sigLabelPaint)
                }

                // 6. Page Footer
                val footerY = pageHeight - 20f
                footerPaint.textAlign = Paint.Align.LEFT
                canvas.drawText("Official Record Copy • دفتری ریکارڈ", 24f, footerY, footerPaint)

                footerPaint.textAlign = Paint.Align.CENTER
                val pageStr = "صفحہ ${pageIndex + 1} از $totalPages (Page ${pageIndex + 1} of $totalPages)"
                canvas.drawText(pageStr, centerX, footerY, footerPaint)

                footerPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Advanced Lottery Grid Maker", pageWidth - 24f, footerY, footerPaint)

                doc.finishPage(page)
            }

            val docsFolder = File(context.cacheDir, "documents").apply { mkdirs() }
            val filename = "lottery-grid-${System.currentTimeMillis()}.pdf"
            val file = File(docsFolder, filename)

            FileOutputStream(file).use { out ->
                doc.writeTo(out)
            }
            doc.close()
            pdfDocument = null

            savePdfToPublic(context, file, filename)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            try { pdfDocument?.close() } catch (_: Throwable) {}
            null
        }
    }

    private fun savePdfToPublic(context: Context, sourceFile: File, filename: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/LotteryGrid")
                }
                val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            } else {
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val appDir = File(docsDir, "LotteryGrid").apply { mkdirs() }
                val target = File(appDir, filename)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(target).use { out ->
                        input.copyTo(out)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens the generated PDF in the system PDF viewer or shares it.
     */
    fun openOrSharePdf(context: Context, uri: Uri) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Official Lottery Draw Sheet PDF")
            putExtra(Intent.EXTRA_TEXT, "Official Lottery Draw Sheet (قرعہ اندازی شیٹ)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(viewIntent, "Open PDF with / پی ڈی ایف کھولیں").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(shareIntent))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Lottery Grid PDF (پی ڈی ایف بھیجیں)"))
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    /**
     * Launches Android Share sheet for the generated PDF.
     */
    fun shareGridPdf(context: Context, uri: Uri) {
        openOrSharePdf(context, uri)
    }
}
