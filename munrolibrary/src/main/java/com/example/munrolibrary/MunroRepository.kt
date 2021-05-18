package com.example.munrolibrary

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class MunroRepository {

    //Raw data extracted from CSV
    private val dataList = ArrayList<Munro>()
    //Flag indicating if data has been successfully loaded
    private var isDataLoaded = false

    /**
     * Get raw data (no sorting or filtering is applied)
     * @return List of Munro objects
     */
    fun getFullMunroData(): List<Munro> = dataList

    /**
     * Clears all data and resets all flags
     */
    fun clearData() {
        dataList.clear()
        isDataLoaded = false
    }

    /**
     * Suspend functions runs on IO context
     * Gets lines from input stream and converts to data @see convertStringListToData
     *
     * @param inputStream - InputStream which will be read if null CsvReadResult.Error will be returned
     *
     * @return CsvReadResult.Success if lines where successfully converted to data or
     * CsvReadResult.Error with errorMessage indicating that went wrong
     */
    suspend fun readCsvInputStream(inputStream: InputStream?) : CsvReadResult<Boolean> {
        return withContext(Dispatchers.IO) {
            var bufferReader : BufferedReader? = null
            try {
                if (inputStream == null) {
                    return@withContext CsvReadResult.error("Null input stream was provided")
                }
                bufferReader = BufferedReader(inputStream.reader(Charset.defaultCharset()))
                convertStringListToData(bufferReader.readLines())
            } catch (ex: Exception) {
                CsvReadResult.error(ex.localizedMessage ?: ex.message ?: "")
            } finally {
                try {
                    bufferReader?.close()
                } catch (ex: IOException){
                    Log.e(TAG, "Unable to close buffer reader", ex)
                }
            }
        }
    }

    /**
     * Suspend functions runs on IO context
     * Gets lines from file and converts to data @see convertStringListToData
     *
     * @param file - File which will be read if null CsvReadResult.Error will be returned
     *
     * @return CsvReadResult.Success if lines where successfully converted to data or
     * CsvReadResult.Error with errorMessage indicating that went wrong
     */
    suspend fun readCsvFile(file: File?): CsvReadResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (file == null) {
                    return@withContext CsvReadResult.error("Null file was provided")
                }
                convertStringListToData(file.readLines())
            } catch (ex: Exception) {
                CsvReadResult.error(ex.localizedMessage ?: ex.message ?: "")
            }
        }
    }

    /**
     * Lines with string extracted from CSV file containing data
     * First line (with header data) should contain following headers @see MunroConstants
     * - Name
     * - Height (m)
     * - Post 1997  <- referred as category
     * - Grid ref
     *
     * @return CsvReadResult.Success if lines where successfully converted to data or
     * CsvReadResult.Error with errorMessage indicating that went wrong
     */
    private fun convertStringListToData(lines: List<String>) :  CsvReadResult<Boolean>{
        try {
            if (lines.isEmpty()) {
                return CsvReadResult.error("Empty input stream was provided")
            }

            var nameColumnIndex: Int = -1
            var heightInMetersColumnIndex: Int = -1
            var categoryColumnIndex: Int = -1
            var gridRefColumnIndex: Int = -1

            dataList.clear() //Clear old data

            lines.forEachIndexed { index, line ->
                if (index == 0) {
                    val columns = line.split(",")
                    nameColumnIndex = columns.indexOf(MUNRO_COLUMN_NAME)
                    if (nameColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    heightInMetersColumnIndex = columns.indexOf(MUNRO_COLUMN_HEIGHT_M)
                    if (heightInMetersColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    categoryColumnIndex = columns.indexOf(MUNRO_COLUMN_CATEGORY)
                    if (categoryColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    gridRefColumnIndex = columns.indexOf(MUNRO_COLUMN_GRID_REF)
                    if (gridRefColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                } else {
                    val columns = line.split(",")
                    dataList.add(
                        Munro(
                            name = columns[nameColumnIndex],
                            heightInMeters = columns[heightInMetersColumnIndex].toLong(),
                            category = columns[categoryColumnIndex],
                            gridRef = columns[gridRefColumnIndex]
                        )
                    )
                }
            }

            isDataLoaded = true
            return CsvReadResult.success(true)
        } catch (ex: Exception) {
            return CsvReadResult.error(ex.localizedMessage ?: ex.message ?: "")
        }
    }

    companion object {
        val TAG = MunroRepository::class.simpleName
    }
}