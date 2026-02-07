package com.example.ravihome.ui.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

object ExportUtils {

    fun exportCsv(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(context.getExternalFilesDir(null), "$fileName.csv")
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
        val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val lineHeight = 18f
        val marginTop = 32f
        val marginBottom = 32f
        val paint = Paint()
        var pageNumber = 1
        var y = marginTop
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas

        rows.forEach { row ->
            if (y + lineHeight > pageHeight - marginBottom) {
                doc.finishPage(page)
                pageNumber += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = marginTop
            }
            canvas.drawText(row.joinToString(" | "), 16f, y, paint)
            y += lineHeight
        }

        doc.finishPage(page)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    fun exportExcel(context: Context, fileName: String, rows: List<List<String>>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Data")

        rows.forEachIndexed { r, row ->
            val excelRow = sheet.createRow(r)
            row.forEachIndexed { c, value ->
                excelRow.createCell(c).setCellValue(value)
            }
        }

        val file = File(context.getExternalFilesDir(null), "$fileName.xlsx")
        file.outputStream().use { workbook.write(it) }
        workbook.close()
    }
    private fun escapeCsv(value: String): String {
        val needsEscaping = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
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
