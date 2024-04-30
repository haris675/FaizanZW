package com.app.faizanzw.ui.postLogin.fragments.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.model.ModelRegularTask
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegularTaskViewModel @Inject constructor(
    val apiRepo: BaseApiRepo,
    val pref: PreferenceModule
) : ViewModel() {

    val taskList = MutableLiveData<AppState<ModelRegularTask>>()

    fun getTaskData(companyCode: String,branchId:Int, userId: Int) = viewModelScope.launch {
        taskList.value = AppState.Loading()
        apiRepo.getRegularTaskApi(companyCode, branchId, userId).collect {
            taskList.value = it
        }
    }

    val taskSave = MutableLiveData<AppState<JsonElement>>()

    fun getTaskSave(companyCode: String,branchId:Int, userId: Int) = viewModelScope.launch {
        taskSave.value = AppState.Loading()
        apiRepo.getRegularTaskSaveApi(companyCode, branchId, userId).collect {
            taskSave.value = it
        }
    }

}