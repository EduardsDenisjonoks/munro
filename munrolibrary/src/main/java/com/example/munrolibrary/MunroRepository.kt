package com.example.munrolibrary

import android.util.Log
import com.example.munrolibrary.constants.MunroCategories
import com.example.munrolibrary.constants.MunroSortOptions
import com.example.munrolibrary.data.Munro
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
    fun getRawMunroData(): List<Munro> = dataList

    /**
     * Get sorted and filtered munros list. All parameters are optional and are defaulted to return
     * whole list filtered by if contains category and sorted by height ASC and name ASC
     *
     * @param categoryFilter - optional string non null value. Defaulted to MunroCategories.NONE
     * @see MunroCategories - supported categories.
     * @see filterByCategory - category filtering function.
     * @param minHeight - optional nullable double value. Defaulted to null
     * @param maxHeight - optional nullable double value. Defaulted to null
     * @see filterByHeightRange - height filtering method.
     * @param heightSortOption - optional string non null value. Defaulted to MunroSortOptions.ASC
     * @param nameSortOptions - optional string non null value. Defaulted to MunroSortOptions.ASC
     * @see MunroSortOptions - supported sort options.
     * @see sortByHeightAndName - height and name sort method.
     * @param limitItemNumber - optional nullable int value. Defaulted to null
     * @see limitItems - function which limits max amount of items returned in the list
     */
    fun getMunros(
        @MunroCategories categoryFilter: String = MunroCategories.NONE,
        minHeight: Double? = null,
        maxHeight: Double? = null,
        @MunroSortOptions heightSortOption: String = MunroSortOptions.ASC,
        @MunroSortOptions nameSortOptions: String = MunroSortOptions.ASC,
        limitItemNumber: Int? = null
    ): List<Munro> {
        return dataList.filterByCategory(categoryFilter)
            .filterByHeightRange(minHeight, maxHeight)
            .sortByHeightAndName(heightSortOption, nameSortOptions)
            .limitItems(limitItemNumber)
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
     * First line (with header data) should contain following headers
     * @see MunroConstants
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
     * @param height - string -> height in meters of munro
     * @param category - string -> munro category post 1997
     * @param gridRef - string -> munro grid reference
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

    //region MUNRO LIST EXTENSIONS
    /**
     * Filters list by category. Parameter is optional and is defaulted to MunroCategories.NONE.
     * MunroCategories.NONE is filtering list by checking if category is not blank.
     * MunroCategories.MUNRO is filtering list by checking if category is MUN
     * MunroCategories.MUNRO_TOP is filtering list by checking if category is TOP
     * If invalid category is provided unmodified list is returned
     *
     * @param filter - optional string non null value. Defaulted to MunroCategories.NONE
     * @see MunroCategories - supported categories.
     *
     * @return filtered munro list
     */
    private fun List<Munro>.filterByCategory(@MunroCategories filter: String = MunroCategories.NONE): List<Munro> {
        return when (filter) {
            MunroCategories.MUNRO -> {
                this.filter { munro -> munro.category == filter }
            }
            MunroCategories.MUNRO_TOP -> {
                this.filter { munro -> munro.category == filter }
            }
            MunroCategories.NONE -> {
                this.filter { munro -> munro.category.isNotBlank() }
            }
            else -> {
                Log.e(TAG, "Invalid munro category filter $filter")
                this
            }
        }
    }

    /**
     * Filter list by min or/and max value. Parameters are optional and are defaulted to null.
     * If both min and max values are provided, they are validated and in case validation fails
     * unmodified list is returned
     *
     * @param minHeight - optional nullable double value. Defaulted to null
     * @param maxHeight - optional nullable double value. Defaulted to null
     *
     * @return filtered munro list
     */
    private fun List<Munro>.filterByHeightRange(
        minHeight: Double? = null,
        maxHeight: Double? = null
    ): List<Munro> {
        if (maxHeight != null && minHeight != null && minHeight > maxHeight) {
            Log.e(
                TAG,
                "Invalid height range, max value ($maxHeight) should be bigger than min value ($minHeight)"
            )
            return this
        }
        val filteredByMin = when (minHeight) {
            null -> {
                this
            }
            else -> {
                this.filter { munro -> munro.heightInMeters >= minHeight }
            }
        }
        return when (maxHeight) {
            null -> {
                filteredByMin
            }
            else -> {
                filteredByMin.filter { munro -> munro.heightInMeters <= maxHeight }
            }
        }
    }

    /**
     * Sort list by height and then by name. Parameters are optional and are defaulted to MunroSortOptions.ASC
     * If one of provided sort options is not supported default sort option will be applied.
     *
     * @param heightSortOption - optional string non null value. Defaulted to MunroSortOptions.ASC
     * @param nameSortOptions - optional string non null value. Defaulted to MunroSortOptions.ASC
     * @see MunroSortOptions - supported sort options.
     *
     * @return sorted list by height and then by name
     */
    private fun List<Munro>.sortByHeightAndName(
        @MunroSortOptions heightSortOption: String = MunroSortOptions.ASC,
        @MunroSortOptions nameSortOptions: String = MunroSortOptions.ASC
    ): List<Munro> {
        return when {
            heightSortOption == MunroSortOptions.ASC && nameSortOptions == MunroSortOptions.ASC -> {
                this.sortedWith(compareBy<Munro> { it.heightInMeters }.thenBy { it.name })
            }
            heightSortOption == MunroSortOptions.ASC && nameSortOptions == MunroSortOptions.DESC -> {
                this.sortedWith(compareBy<Munro> { it.heightInMeters }.thenByDescending { it.name })
            }
            heightSortOption == MunroSortOptions.DESC && nameSortOptions == MunroSortOptions.ASC -> {
                this.sortedWith(compareByDescending<Munro> { it.heightInMeters }.thenBy { it.name })
            }
            heightSortOption == MunroSortOptions.DESC && nameSortOptions == MunroSortOptions.DESC -> {
                this.sortedWith(compareByDescending<Munro> { it.heightInMeters }.thenByDescending { it.name })
            }
            else -> {
                Log.e(
                    TAG,
                    "Invalid sort options -> height: $heightSortOption, name: $nameSortOptions"
                )
                this.sortedWith(compareBy<Munro> { it.heightInMeters }.thenBy { it.name })
            }
        }
    }

    /**
     * If limit was provided and it is not negative, when it will return list with equal size or lower
     * in case if there was less of items than specified amount.
     * In case if null or negative parameter is provided, limit will be ignored and whole list will be returned
     *
     * @param limit - optional nullable int value. Defaulted to null
     *
     * @return list with limited max number of items if parameter was provided
     */
    private fun List<Munro>.limitItems(limit: Int? = null): List<Munro> {
        return when {
            limit == null -> {
                this
            }
            limit < 0 -> {
                this
            }
            limit >= this.size -> {
                this
            }
            else -> {
                this.subList(0, limit)
            }
        }
    }
    //endregion

    companion object {
        val TAG = MunroRepository::class.simpleName
    }
}