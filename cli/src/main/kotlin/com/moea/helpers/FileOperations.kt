package com.moea.helpers

import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

object FileOperations {
    fun saveToFile(fileName: String?, content: String) {
        fileName ?: "experimentResults"

        try {
            val file = File("$fileName.csv")
            file.writeText(content)
            println("CSV results saved as $fileName.csv")
        } catch (e: Exception) {
            println("Error: Unable to save the file. Reason: ${e.message}")
        }
    }

    fun saveResponseBodyToFile(fileName: String?, responseBody: ResponseBody) {
        fileName ?: "experimentResultsPlot"

        val file = File("$fileName.png")
        try {
            FileOutputStream(file).use { outputStream ->
                responseBody.byteStream().copyTo(outputStream)
            }
            println("Plot saved successfully as $fileName.png")
        } catch (e: Exception) {
            println("Error saving file. Reason: ${e.message}")
        }
    }
}