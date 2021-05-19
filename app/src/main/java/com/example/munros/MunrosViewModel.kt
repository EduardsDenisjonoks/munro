package com.example.munros

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.munrolibrary.CsvReadResult
import com.example.munrolibrary.Munro
import com.example.munrolibrary.MunroRepository
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


    fun getMunrosLiveData() : LiveData<List<Munro>> = munrosLiveData
    fun getErrorLiveData() : LiveData<String> = errorLiveData

    private fun setLoadingState(isLoading: Boolean) {
        isLoadingLiveData.postValue(isLoading)
    }

    private fun setMunros(munros: List<Munro>) {
        munrosLiveData.postValue(munros)
    }

    private fun setError(errorMessage: String) {
        errorLiveData.postValue(errorMessage)
    }

    fun loadCsvFromInputStream(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoadingState(true)
            when(val result = munroRepository.readCsvInputStream(inputStream)){
                is CsvReadResult.Success -> {
                    setMunros(munroRepository.getFullMunroData())//todo apply filters
                }
                is CsvReadResult.Error -> {
                    setError(result.errorMessage)
                }
            }
            setLoadingState(false)
        }
    }
}