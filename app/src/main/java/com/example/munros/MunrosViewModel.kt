package com.example.munros

import androidx.lifecycle.*
import com.example.munrolibrary.*
import com.example.munrolibrary.constants.MunroCategories
import com.example.munrolibrary.constants.MunroSortOptions
import com.example.munrolibrary.data.Munro
import com.example.munros.custom.SingleLiveEvent
import com.example.munros.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MunrosViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val munroRepository = MunroRepository()
    private val munrosLiveData = MediatorLiveData<List<Munro>>()
    private val errorLiveData = SingleLiveEvent<String>()

    val isLoadingLiveData = MutableLiveData(false)
    val actionEnabledLiveData = Transformations.map(isLoadingLiveData) { isLoading -> !isLoading }
    val dataNotLoadedVisibilityLiveData = MutableLiveData(true)
    val filterOptionVisibilityLiveData =
        Transformations.map(dataNotLoadedVisibilityLiveData) { isDataNotLaded -> !isDataNotLaded }
    val emptyStateVisibilityLiveData = MediatorLiveData<Boolean>().apply { this.value = false }

    private val cachedCategoryLiveData = preferencesRepository.categoryFilterFlow.asLiveData()
    private val cachedMinHeightLiveData = preferencesRepository.minHeightFlow.asLiveData()
    private val cachedMaxHeightLiveData = preferencesRepository.maxHeightFlow.asLiveData()
    private val cachedHeightSortOptionLiveData = preferencesRepository.heightSortFlow.asLiveData()
    private val cachedNameSortOptionLiveData = preferencesRepository.nameSortFlow.asLiveData()
    private val cachedItemLimitLiveData = preferencesRepository.itemLimitFlow.asLiveData()

    init {
        emptyStateVisibilityLiveData.addSource(dataNotLoadedVisibilityLiveData) { updateEmptyStateVisibility() }
        emptyStateVisibilityLiveData.addSource(munrosLiveData) { updateEmptyStateVisibility() }

        munrosLiveData.addSource(cachedCategoryLiveData) { getMunroList() }
        munrosLiveData.addSource(cachedMinHeightLiveData) { getMunroList() }
        munrosLiveData.addSource(cachedMaxHeightLiveData) { getMunroList() }
        munrosLiveData.addSource(cachedHeightSortOptionLiveData) { getMunroList() }
        munrosLiveData.addSource(cachedNameSortOptionLiveData) { getMunroList() }
        munrosLiveData.addSource(cachedItemLimitLiveData) { getMunroList() }
    }

    fun getMunrosLiveData(): LiveData<List<Munro>> = munrosLiveData
    fun getErrorLiveData(): LiveData<String> = errorLiveData

    private fun setLoadingState(isLoading: Boolean) {
        isLoadingLiveData.postValue(isLoading)
    }

    private fun setMunros(munros: List<Munro>) {
        munrosLiveData.postValue(munros)
    }

    private fun setError(errorMessage: String) {
        errorLiveData.postValue(errorMessage)
    }

    private fun setDataNotLoadedVisibility(isDataLoaded: Boolean) {
        dataNotLoadedVisibilityLiveData.postValue(!isDataLoaded)
    }

    private fun updateEmptyStateVisibility() {
        val isDataNotLoaded = dataNotLoadedVisibilityLiveData.value ?: true
        val isMunroListNotEmpty = munrosLiveData.value.isNullOrEmpty()

        emptyStateVisibilityLiveData.postValue(!isDataNotLoaded  && isMunroListNotEmpty)
    }

    fun loadCsvFromInputStream(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoadingState(true)
            when (val result = munroRepository.readCsvInputStream(inputStream)) {
                is CsvReadResult.Success -> {
                    getMunroList()
                }
                is CsvReadResult.Error -> {
                    setError(result.errorMessage)
                }
            }
            setDataNotLoadedVisibility(munroRepository.isDataLoaded())
            setLoadingState(false)
        }
    }

    private fun getMunroList(){
        if (munroRepository.isDataLoaded()) {
            val categoryFilter = cachedCategoryLiveData.value ?: MunroCategories.NONE
            val minHeight = cachedMinHeightLiveData.value?.toDoubleOrNull()
            val maxHeight = cachedMaxHeightLiveData.value?.toDoubleOrNull()
            val heightSortOption = cachedHeightSortOptionLiveData.value ?: MunroSortOptions.ASC
            val nameSortOption = cachedNameSortOptionLiveData.value ?: MunroSortOptions.ASC
            val itemLimit = cachedItemLimitLiveData.value?.toIntOrNull()

            setMunros(
                munroRepository.getMunros(
                    categoryFilter = categoryFilter,
                    minHeight = minHeight,
                    maxHeight = maxHeight,
                    heightSortOption = heightSortOption,
                    nameSortOptions = nameSortOption,
                    limitItemNumber = itemLimit
                )
            )
        }
    }
}