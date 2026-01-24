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
            rows.forEach { out.println(it.joinToString(",")) }
        }
    }

    fun exportHtml(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(context.getExternalFilesDir(null), "$fileName.html")
        file.writeText(
            "<html><body><table border='1'>" +
                    rows.joinToString("") { r ->
                        "<tr>${r.joinToString("") { "<td>$it</td>" }}</tr>"
                    } +
                    "</table></body></html>"
        )
    }

    fun exportPdf(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(300, 600, 1).create())
        val canvas = page.canvas
        var y = 20f

        rows.forEach { row ->
            canvas.drawText(row.joinToString(" | "), 10f, y, Paint())
            y += 20
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
}
