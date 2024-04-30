package com.app.faizanzw.ui.postLogin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.*
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.CommonFunctions
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(val apiRepo: BaseApiRepo) : ViewModel() {

    private val _dashboardData = MutableLiveData<AppState<JsonElement>>()
    val dashboardData: MutableLiveData<AppState<JsonElement>> get() = _dashboardData

    fun getDashboardData(companyCode: String, loginUserID: Int) = viewModelScope.launch {
        _dashboardData.value = AppState.Loading()
        apiRepo.getDashboardData(companyCode, loginUserID).collect {
            _dashboardData.value = it
        }
    }

    val allSyncData = MutableLiveData<AppState<ModelSyncData>>()
    fun getAllSyncData(companyCode: String, userID: Int) =
        viewModelScope.launch {
            try {
                allSyncData.value = AppState.Loading()
                val data1 = async { apiRepo.getComboBranch(companyCode, "Branch") }
                val data2 = async { apiRepo.getExpenseType(companyCode) }
                val data3 = async { apiRepo.getTaskType(companyCode, "AppTaskType") }
                val data4 = async { apiRepo.getAllEmployee(companyCode) }
                val data5 = async { apiRepo.getDeliveryTaskApi2(companyCode, userID) }

                val (res1,
                    res2,
                    res3,
                    res4,
                    res5) = awaitAll(
                    data1,
                    data2,
                    data3,
                    data4,
                    data5
                )
                val branchData = extractBranchData(res1 as AppState<DBModelBranch>)
                val expenseType = extractExpenseData(res2 as AppState<DBModelExpenseType>)
                val taskType =
                    CommonFunctions.extractTaskTypeData(res3 as AppState<DBModelTaskType>)
                val allEmployee =
                    CommonFunctions.extractEmployeeData(res4 as AppState<DBModelEmployee>)
                val deliveryData =
                    CommonFunctions.extractDeliveryData(res5 as AppState<ModelDeliveryTask>)

                allSyncData.value =
                    AppState.Success(
                        ModelSyncData(
                            branchData,
                            expenseType,
                            taskType,
                            allEmployee,
                            deliveryData
                        )
                    );
            } catch (e: Exception) {
                e.printStackTrace()
                allSyncData.value = AppState.Error(e.message + "")
            }
        }

    private fun extractBranchData(data: AppState<DBModelBranch>): ArrayList<DBBranch>? {
        var branchData: ArrayList<DBBranch>? = null
        when (data) {
            is AppState.Success -> {
                if (data.model.status == 200)
                    branchData = data.model.data
                else
                    branchData = null
            }
            is AppState.Error -> {
                branchData = null
            }
            is AppState.Loading -> {
                branchData = null
            }
        }
        return branchData
    }

    private fun extractExpenseData(data: AppState<DBModelExpenseType>): ArrayList<DBExpenseType>? {
        var expenseData: ArrayList<DBExpenseType>? = null
        when (data) {
            is AppState.Success -> {
                if (data.model.status == 200)
                    expenseData = data.model.data
                else
                    expenseData = null
            }
            is AppState.Error -> {
                expenseData = null
            }
            is AppState.Loading -> {
                expenseData = null
            }
        }
        return expenseData
    }

    data class ModelSyncData(
        val branch: ArrayList<DBBranch>?,
        var expenseType: ArrayList<DBExpenseType>?,
        val taskType: ArrayList<DBTaskType>?,
        val employeeType: ArrayList<DBEmployee>?,
        val deliveryData: ArrayList<DBDeliveryTasK>?
    )

}