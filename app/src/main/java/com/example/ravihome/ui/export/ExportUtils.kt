package com.example.ravihome.ui.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

object ExportUtils {

    private fun targetDir(context: Context): File =
        context.getExternalFilesDir(null) ?: context.filesDir

    fun exportCsv(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(targetDir(context), "$fileName.csv")
        file.printWriter().use { out ->
            rows.forEach { row ->
                out.println(row.joinToString(",") { escapeCsv(it) })
            }
        }
    }

    fun exportHtml(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(context.getExternalFilesDir(null), "$fileName.html")
        file.writeText(
            "<html><body><table border='1'>" +
                    rows.joinToString("") { r ->
                        "<tr>${r.joinToString("") { "<td>${escapeHtml(it)}</td>" }}</tr>"
                    } +
                    "</table></body></html>"
        )
    }

    fun exportPdf(context: Context, fileName: String, rows: List<List<String>>) {
        if (rows.isEmpty()) return
        val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val rowHeight = 24f
        val marginTop = 36f
        val marginBottom = 36f
        val marginLeft = 24f
        val marginRight = 24f
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }
        val headerBackground = Paint().apply { color = Color.parseColor("#E3F2FD") }
        val stripeBackground = Paint().apply { color = Color.parseColor("#F7F9FC") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D0D7DE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val columns = rows.maxOf { it.size }.coerceAtLeast(1)
        val tableWidth = pageWidth - marginLeft - marginRight
        val columnWidth = tableWidth / columns
        var y = marginTop
        var pageNumber = 1
        var page =
            doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas

        rows.forEachIndexed { index, row ->
            if (y + rowHeight > pageHeight - marginBottom) {
                doc.finishPage(page)
                pageNumber += 1
                page = doc.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = marginTop
            }
            val isHeader = index == 0
            val backgroundPaint = when {
                isHeader -> headerBackground
                index % 2 == 0 -> stripeBackground
                else -> null
            }
            backgroundPaint?.let {
                canvas.drawRect(
                    marginLeft,
                    y - rowHeight + 4f,
                    marginLeft + tableWidth,
                    y + 6f,
                    it
                )
            }
            row.forEachIndexed { colIndex, value ->
                val x = marginLeft + colIndex * columnWidth + 8f
                val maxWidth = columnWidth - 12f
                val paint = if (isHeader) headerPaint else textPaint
                canvas.drawText(fitText(value, maxWidth, paint), x, y, paint)
            }
            canvas.drawRect(
                marginLeft,
                y - rowHeight + 4f,
                marginLeft + tableWidth,
                y + 6f,
                borderPaint
            )
            y += rowHeight
        }

        doc.finishPage(page)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    fun exportExcel(context: Context, fileName: String, rows: List<List<String>>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Data")
        val maxColumns = rows.maxOfOrNull { it.size } ?: 0
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
        }
        val stripeStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        val normalStyle = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        rows.forEachIndexed { r, row ->
            val excelRow = sheet.createRow(r)
            row.forEachIndexed { c, value ->
                val cell = excelRow.createCell(c)
                cell.setCellValue(value)
                cell.cellStyle = when {
                    r == 0 -> headerStyle
                    r % 2 == 0 -> stripeStyle
                    else -> normalStyle
                }
            }
        }
        for (c in 0 until maxColumns) {
            sheet.autoSizeColumn(c)
        }

        val file = File(context.getExternalFilesDir(null), "$fileName.xlsx")
        file.outputStream().use { workbook.write(it) }
        workbook.close()
    }

    private fun fitText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var trimmed = text
        while (trimmed.isNotEmpty() && paint.measureText(trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.dropLast(1)
        }
        return if (trimmed.isBlank()) ellipsis else trimmed + ellipsis
    }

    private fun escapeCsv(value: String): String {
        val needsEscaping =
            value.contains(',') || value.contains('"') || value.contains('\n') || value.contains(
                '\r'
            )
        if (!needsEscaping) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun escapeHtml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
