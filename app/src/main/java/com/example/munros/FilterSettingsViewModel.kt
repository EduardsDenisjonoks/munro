package com.example.munros

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilterSettingsViewModel @Inject constructor() : ViewModel() {

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
}