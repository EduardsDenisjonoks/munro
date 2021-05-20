package com.example.munros

import androidx.lifecycle.*
import com.example.munrolibrary.*
import com.example.munrolibrary.constants.MunroCategories
import com.example.munrolibrary.constants.MunroSortOptions
import com.example.munrolibrary.data.Munro
import com.example.munros.custom.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class MunrosViewModel @Inject constructor() : ViewModel() {

    private val munroRepository = MunroRepository()//todo inject this
    private val munrosLiveData = MutableLiveData<List<Munro>>(emptyList())
    private val errorLiveData = SingleLiveEvent<String>()

    val isLoadingLiveData = MutableLiveData(false)
    val actionEnabledLiveData = Transformations.map(isLoadingLiveData) { isLoading -> !isLoading }
    val dataNotLoadedVisibilityLiveData = MutableLiveData(true)
    val filterOptionVisibilityLiveData =
        Transformations.map(dataNotLoadedVisibilityLiveData) { isDataNotLaded -> !isDataNotLaded }
    val emptyStateVisibilityLiveData = MediatorLiveData<Boolean>().apply { this.value = false }

    init {
        emptyStateVisibilityLiveData.addSource(dataNotLoadedVisibilityLiveData) { updateEmptyStateVisibility() }
        emptyStateVisibilityLiveData.addSource(munrosLiveData) { updateEmptyStateVisibility() }
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
                    setMunros(
                        munroRepository.getMunros(
                            categoryFilter = MunroCategories.NONE,
                            minHeight = null,
                            maxHeight = null,
                            heightSortOption = MunroSortOptions.ASC,
                            nameSortOptions = MunroSortOptions.ASC,
                            limitItemNumber = null
                        )
                    )
                }
                is CsvReadResult.Error -> {
                    setError(result.errorMessage)
                }
            }
            setDataNotLoadedVisibility(munroRepository.isDataLoaded())
            setLoadingState(false)
        }
    }
}