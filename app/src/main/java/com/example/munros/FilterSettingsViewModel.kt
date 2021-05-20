package com.example.munros

import androidx.lifecycle.*
import com.example.munrolibrary.constants.MunroCategories
import com.example.munrolibrary.constants.MunroSortOptions
import com.example.munros.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FilterSettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val categoryNoneCheckedLiveData = MutableLiveData(true)
    val categoryMunCheckedLiveData = MutableLiveData(false)
    val categoryTopCheckedLiveData = MutableLiveData(false)

    val minHeightLiveData = MutableLiveData("")
    val maxHeightLiveData = MutableLiveData("")
    val itemLimitLiveData = MutableLiveData("")

    val heightSortAscCheckedLiveData = MutableLiveData(true)
    val heightSortDescCheckedLiveData = MutableLiveData(false)

    val nameSortAscCheckedLiveData = MutableLiveData(true)
    val nameSortDescCheckedLiveData = MutableLiveData(false)

    fun applyChanges(onCompletion: () -> (Unit)) {
        viewModelScope.launch {

            val isNone = categoryNoneCheckedLiveData.value ?: false
            val isMun = categoryMunCheckedLiveData.value ?: false
            val isTop = categoryTopCheckedLiveData.value ?: false

            val category = if (!isNone && !isMun && !isTop) {
                MunroCategories.NONE
            } else if (isNone) {
                MunroCategories.NONE
            } else if (isMun) {
                MunroCategories.MUNRO
            } else if (isTop) {
                MunroCategories.MUNRO_TOP
            } else {
                MunroCategories.NONE
            }

            val heightSortOption = if (heightSortDescCheckedLiveData.value == true) {
                MunroSortOptions.DESC
            } else {
                MunroSortOptions.ASC
            }
            val nameSortOption = if (nameSortDescCheckedLiveData.value == true) {
                MunroSortOptions.DESC
            } else {
                MunroSortOptions.ASC
            }

            preferencesRepository.applyAll(
                category,
                minHeightLiveData.value,
                maxHeightLiveData.value,
                itemLimitLiveData.value,
                heightSortOption,
                nameSortOption
            )
            withContext(Dispatchers.Main) {
                onCompletion.invoke()
            }
        }
    }

    fun getCachedCategoryLiveData(): LiveData<String> =
        preferencesRepository.categoryFilterFlow.asLiveData()

    fun getCachedMinHeightLiveData(): LiveData<String> =
        preferencesRepository.minHeightFlow.asLiveData()

    fun getCachedMaxHeightLiveData(): LiveData<String> =
        preferencesRepository.maxHeightFlow.asLiveData()

    fun getCachedItemLimitLiveData(): LiveData<String> =
        preferencesRepository.itemLimitFlow.asLiveData()

    fun getCachedHeightSortOptionLiveData(): LiveData<String> =
        preferencesRepository.heightSortFlow.asLiveData()

    fun getCachedNameSortOptionLiveData(): LiveData<String> =
        preferencesRepository.nameSortFlow.asLiveData()

    fun setCategory(@MunroCategories category: String) {
        when (category) {
            MunroCategories.MUNRO -> categoryMunCheckedLiveData.postValue(true)
            MunroCategories.MUNRO_TOP -> categoryTopCheckedLiveData.postValue(true)
            else -> categoryNoneCheckedLiveData.postValue(true)
        }
    }

    fun setMinHeight(minHeight: String?) {
        minHeightLiveData.postValue(minHeight ?: "")
    }

    fun setMaxHeight(maxHeight: String?) {
        maxHeightLiveData.postValue(maxHeight ?: "")
    }

    fun setItemLimit(itemLimit: String?) {
        itemLimitLiveData.postValue(itemLimit ?: "")
    }

    fun setHeightSortOption(@MunroSortOptions option: String) {
        when (option) {
            MunroSortOptions.DESC -> heightSortDescCheckedLiveData.postValue(true)
            else -> heightSortAscCheckedLiveData.postValue(true)
        }
    }

    fun setNameSortOption(@MunroSortOptions option: String) {
        when (option) {
            MunroSortOptions.DESC -> nameSortDescCheckedLiveData.postValue(true)
            else -> nameSortAscCheckedLiveData.postValue(true)
        }
    }
}