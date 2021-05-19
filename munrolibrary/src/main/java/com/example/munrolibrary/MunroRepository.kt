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

    fun getMunros(
        @MunroCategories categoryFilter: String = MunroCategories.NONE,
        @MunroSortOptions heightSortOption: String = MunroSortOptions.ASC,
        @MunroSortOptions nameSortOptions: String = MunroSortOptions.ASC
    ): List<Munro> {
        val list = when (categoryFilter) {
            MunroCategories.MUNRO -> {
                dataList.filter { munro -> munro.category == categoryFilter }
            }
            MunroCategories.MUNRO_TOP -> {
                dataList.filter { munro -> munro.category == categoryFilter }
            }
            MunroCategories.NONE -> {
                dataList.filter { munro -> munro.category.isNotBlank() }
            }
            else -> {
                Log.e(TAG, "Invalid category filter $categoryFilter")
                dataList
            }
        }

        return when {
            heightSortOption == MunroSortOptions.ASC && nameSortOptions == MunroSortOptions.ASC -> {
                list.sortedWith(compareBy<Munro> { it.heightInMeters }.thenBy { it.name })
            }
            heightSortOption == MunroSortOptions.ASC && nameSortOptions == MunroSortOptions.DESC -> {
                list.sortedWith(compareBy<Munro> { it.heightInMeters }.thenByDescending { it.name })
            }
            heightSortOption == MunroSortOptions.DESC && nameSortOptions == MunroSortOptions.ASC -> {
                list.sortedWith(compareByDescending<Munro> { it.heightInMeters }.thenBy { it.name })
            }
            heightSortOption == MunroSortOptions.DESC && nameSortOptions == MunroSortOptions.DESC -> {
                list.sortedWith(compareByDescending<Munro> { it.heightInMeters }.thenByDescending { it.name })
            }
            else -> {
                Log.e(TAG, "Invalid sort options -> height: $heightSortOption, name: $nameSortOptions")
                list.sortedWith(compareBy<Munro> { it.heightInMeters }.thenBy { it.name })
            }
        }
    }

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
    suspend fun readCsvInputStream(inputStream: InputStream?): CsvReadResult<Boolean> {
        return withContext(Dispatchers.IO) {
            var bufferReader: BufferedReader? = null
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
                } catch (ex: IOException) {
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
     * @param lines - list of strings with line data
     *
     * @return CsvReadResult.Success if lines where successfully converted to data or
     * CsvReadResult.Error with errorMessage indicating that went wrong
     */
    private fun convertStringListToData(lines: List<String>): CsvReadResult<Boolean> {
        try {
            if (lines.isEmpty()) {
                return CsvReadResult.error("Empty input stream was provided")
            }
            Log.i(TAG, "--- Start converting string list to data ---")
            dataList.clear() //Clear old data

            var nameColumnIndex: Int = -1
            var heightInMetersColumnIndex: Int = -1
            var categoryColumnIndex: Int = -1
            var gridRefColumnIndex: Int = -1

            lines.forEachIndexed { index, line ->
                if (index == 0) {
                    val columns = line.split(",")
                    //NOTE: `streetmap` field is split into 2 values as it contains ',' so add +1 to column index
                    nameColumnIndex =
                        columns.indexOf(MUNRO_COLUMN_NAME) + 1
                    if (nameColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    heightInMetersColumnIndex = columns.indexOf(MUNRO_COLUMN_HEIGHT_M) + 1
                    if (heightInMetersColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    categoryColumnIndex = columns.indexOf(MUNRO_COLUMN_CATEGORY) + 1
                    if (categoryColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                    gridRefColumnIndex = columns.indexOf(MUNRO_COLUMN_GRID_REF) + 1
                    if (gridRefColumnIndex == -1) {
                        return CsvReadResult.error("Column '$MUNRO_COLUMN_GRID_REF' is missing")
                    }
                } else {
                    val columns = line.split(",")
                    validateAndAddToDataList(
                        columns[nameColumnIndex],
                        columns[heightInMetersColumnIndex],
                        columns[categoryColumnIndex],
                        columns[gridRefColumnIndex]
                    )
                }
            }

            Log.i(TAG, "--- Ended converting, items added: ${dataList.size} ---")
            isDataLoaded = true
            return CsvReadResult.success(true)
        } catch (ex: Exception) {
            return CsvReadResult.error(ex.localizedMessage ?: ex.message ?: "")
        }
    }

    /**
     * Validate munro parameters before adding the to the data list
     *
     * @param name - string -> name of munro
     * @param height -> string -> height in meters of munro
     * @param category -> string -> munro category post 1997
     * @param gridRef -> string -> munro grid reference
     *
     * @return CsvReadResult.Success with boolean value, if entry was added
     * return true if failed validation or exception return false
     */
    private fun validateAndAddToDataList(
        name: String,
        height: String,
        category: String,
        gridRef: String
    ): CsvReadResult<Boolean> {
        try {
            if (name.isBlank() ||
                height.isBlank() ||
                category.isBlank() ||
                gridRef.isBlank()
            ) {
                Log.e(
                    TAG,
                    "Entry was not added -> name: $name, height: $height, category: $category, gridRef: $gridRef"
                )
                return CsvReadResult.success(false)
            }
            Log.i(
                TAG,
                "Entry added -> name: $name, height: $height, category: $category, gridRef: $gridRef"
            )
            dataList.add(
                Munro(
                    name,
                    height.toDouble(),
                    category,
                    gridRef
                )
            )
            return CsvReadResult.success(true)
        } catch (ex: Exception) {
            Log.e(
                TAG,
                "Unable to add -> name: $name, height: $height, category: $category, gridRef: $gridRef "
            )
            return CsvReadResult.success(false)
        }
    }

    companion object {
        val TAG = MunroRepository::class.simpleName
    }
}