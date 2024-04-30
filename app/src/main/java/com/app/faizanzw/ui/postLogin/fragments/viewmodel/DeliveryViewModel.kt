package com.app.faizanzw.ui.postLogin.fragments.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.*
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.CommonFunctions
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryViewModel @Inject constructor(val apiRepo: BaseApiRepo) : ViewModel() {

    val deliveryData = MutableLiveData<AppState<ModelDeliveryTask>>()

    fun getDeliveryData(companyCode: String, userId: Int, isConnected: Boolean) =
        viewModelScope.launch {
            val data = DataBaseUtils.getLocalDeliveriesCount()
            deliveryData.value = AppState.Loading()
           /* when (isConnected) {
                true -> {
                    apiRepo.getDeliveryTaskApi(companyCode, userId).collect {
                        deliveryData.value = it
                    }
                }
                else -> {*/
                    if (data > 0)
                        deliveryData.value = CommonFunctions.getDeliveryData()
                    else
                        deliveryData.value = AppState.Error("No Data Found")
               // }
        }

    val taskList = MutableLiveData<AppState<DBModelTaskType>>()
    val branchList = MutableLiveData<AppState<DBModelBranch>>()
    val employeeList = MutableLiveData<AppState<DBModelEmployee>>()
    val taskData = MutableLiveData<AppState<DBDeliveryTasK>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMergeData(tranID: String) =
        viewModelScope.launch {
            try {
                taskList.value = AppState.Loading()
                val data1 = async { CommonFunctions.getTaskTypeData() }
                val data2 = async { CommonFunctions.getBranchData() }
                val data4 = async { CommonFunctions.getDeliveryTask(tranID) }

                val (res1, res2,
                    res4
                ) = awaitAll(
                    data1, data2,
                    data4
                )

                taskList.value = res1 as AppState<DBModelTaskType>
                branchList.value = res2 as AppState<DBModelBranch>
                taskData.value = res4 as AppState<DBDeliveryTasK>

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    val stateAddTask = MutableLiveData<AppState<JsonElement>>()
    fun addTask(
        companyCode: String,
        taskDescription: String,
        image: String,
        tranID: String,
    ) = viewModelScope.launch {
        stateAddTask.value = AppState.Loading()
        apiRepo.doSaveAssosiateTask(
            companyCode,"0",tranID,taskDescription,"100","COMPLETED",image
        ).collect{
            stateAddTask.value = it
        }
    }

    fun getEmployeeData(branchId: String?) = viewModelScope.launch {
        employeeList.value = AppState.Loading()
        employeeList.value = CommonFunctions.getEmployeeDataByBranch(branchId!!.toInt())
    }

}