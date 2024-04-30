package com.app.faizanzw.ui.postLogin.fragments.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.DBModelBranch
import com.app.faizanzw.database.DBModelEmployee
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.search.model.ModelEditPayment
import com.app.faizanzw.ui.postLogin.fragments.search.model.ModelPaymentSearch
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.CommonFunctions
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@HiltViewModel
class PaymentEntryViewModel @Inject constructor(
    val apiRepo: BaseApiRepo,
    val pref: PreferenceModule
) : ViewModel() {

    val stateValidate = MutableLiveData("")

    fun validate(
        branch: String, employee: String, currency: String, amount: String, remarks: String
    ) = viewModelScope.launch {
        if (branch.isEmpty()) {
            stateValidate.value = "Please select Branch"
            return@launch
        }

        if (employee.isEmpty()) {
            stateValidate.value = "Please select Employee"
            return@launch
        }
        if (currency.isEmpty()) {
            stateValidate.value = "Please Select Currency"
            return@launch
        }

        if (amount.isEmpty()) {
            stateValidate.value = "Please enter Amount"
            return@launch
        }
        if (amount.toDouble() <= 0) {
            stateValidate.value = "Please enter valid Amount"
            return@launch
        }
        if (remarks.isEmpty()) {
            stateValidate.value = "Please enter Remarks"
            return@launch
        }
        stateValidate.value = "success"
    }

    val branchList = MutableLiveData<AppState<DBModelBranch>>()
    //val currencyList = MutableLiveData<AppState<ModelCombo>>()
    val employeeList = MutableLiveData<AppState<DBModelEmployee>>()

    fun getEmployeeData(branchId: String) = viewModelScope.launch {
        employeeList.value = AppState.Loading()
        employeeList.value = CommonFunctions.getEmployeeDataByBranch(branchId.toInt())
        /*apiRepo.getEmployeeFillList(companyCode, branchId).collect {
            employeeList.value = it
        }*/
    }

    val balanceData = MutableLiveData<AppState<JsonElement>>()
    fun getMergedData(companyCode: String, branchId: Int, userID: Int) = viewModelScope.launch {
        branchList.value = AppState.Loading()

        /*apiRepo.getComboFillList(companyCode,"Branch").collect{
            branchList.value = it
        }*/

        val branchCall = async {
                CommonFunctions.getBranchData()
        }
        val balanceCall = async { apiRepo.getBalanceApi(companyCode, branchId, userID) }

        val (resp1, resp2) = awaitAll(branchCall, balanceCall)

        branchList.value = resp1 as AppState<DBModelBranch>
        balanceData.value = resp2 as AppState<JsonElement>
    }

    val paymentData = MutableLiveData<AppState<ModelEditPayment>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getEditDataData(
        companyCode: String,
        tranID: String,
        branchId: Int,
        userID: Int,
        isNetworkConnected: Boolean
    ) =
        viewModelScope.launch {
            try {
                branchList.value = AppState.Loading()
                val data1 = async { apiRepo.getBalanceApi(companyCode, branchId, userID) }
                val data2 = async {
                        CommonFunctions.getBranchData()
                }
                val data4 = async { apiRepo.doPaymentEntryGet(companyCode, tranID, userID) }

                val (res1,
                    res2,
                    res4) = awaitAll(
                    data1,
                    data2,
                    data4
                )

                balanceData.value = res1 as AppState<JsonElement>
                branchList.value = res2 as AppState<DBModelBranch>
                paymentData.value = res4 as AppState<ModelEditPayment>
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    val modelSearchPayment = MutableLiveData<AppState<ModelPaymentSearch>>()

    fun getSearchData(
        companyCode: String,
        fromDate: String,
        toDate: String,
        status: String,
        branchID: String,
        paymentTo: String,
    ) = viewModelScope.launch {

        var fDate = fromDate
        var tDate = toDate
        if (fromDate.isNotEmpty()) {
            val strs1 = fromDate.split("/").toTypedArray()
            fDate = strs1[1] + "/" + strs1[0] + "/" + strs1[2]
        }

        if (toDate.isNotEmpty()) {
            val strs2 = toDate.split("/").toTypedArray()
            tDate = strs2[1] + "/" + strs2[0] + "/" + strs2[2]
        }

        modelSearchPayment.value = AppState.Loading()
        apiRepo.doSearchPaymentFlow(
            fDate,
            tDate,
            status,
            branchID,
            paymentTo,
            //userID,
            pref.get(PrefEnum.USERID, 0)
        ).collect {
            modelSearchPayment.value = it
        }
    }

    fun getMergedData2(
        companyCode: String, fromDate: String,
        toDate: String,
        status: String,
        branchID: String,
        paymentTo: String,
    ) = viewModelScope.launch {
        modelSearchPayment.value = AppState.Loading()

        val data1 = async { apiRepo.doSearchPayment(
            toDate,
            fromDate,
            status,
            branchID,
            paymentTo,/* userID,*/
            pref.get(PrefEnum.USERID, 0)
        ) }

        val data2 = async { CommonFunctions.getBranchData() }

        val (res1,res2) = awaitAll(data1,data2)

        branchList.value = res2 as AppState<DBModelBranch>
        modelSearchPayment.value = res1 as AppState<ModelPaymentSearch>

        /*apiRepo.doSearchPayment(
            toDate,
            fromDate,
            status,
            branchID,
            paymentTo,*//* userID,*//*
            pref.get(PrefEnum.USERID, 0)
        )
            .zip(apiRepo.getBranchFlow(companyCode, "Branch")) { first, second ->

                return@zip BranchAndTSearch(first, second)
            }.flowOn(Dispatchers.IO).catch {
                stateValidate.value = "${it.message}"
            }.collect {
                branchList.value = it.branch
                modelSearchPayment.value = it.type
            }*/
    }

    val statePayment = MutableLiveData<AppState<JsonElement>>()
    fun addPayment(
        tranID: String,
        trandate: String,
        amount: String,
        remarks: String,
        //currencyKey: String,
        branchID: String,
        paymentTo: String,
        currencyType: Boolean
    ) = viewModelScope.launch {

        var fDate = trandate
        if (trandate.isNotEmpty()) {
            val strs1 = trandate.split("/").toTypedArray()
            fDate = strs1[1] + "/" + strs1[0] + "/" + strs1[2]
        }

        var cType = "USD"
        if (currencyType) {
            cType = "OTHER"
        }

        statePayment.value = AppState.Loading()
        apiRepo.addPaymentApi(
            tranID,
            fDate,
            amount,
            remarks,
            branchID,
            paymentTo,
            cType
        )
            .collect {
                statePayment.value = it
            }
    }

    val statusData = MutableLiveData<Triple<Boolean, String, String>>()
    fun updateStatus(
        companyCode: String,
        tranID: String,
        tranStatus: String,
        loginUserID: String
    ) = viewModelScope.launch {
        statusData.value = Triple(false, "Loading", "")
        apiRepo.doUpdatePaymentStatus(
            companyCode, tranID, tranStatus, loginUserID
        ).collect {
            when (it) {
                is AppState.Success -> {
                    if (it.model.getStatus())
                        statusData.value = Triple(true, it.model.getMessage(), tranStatus)
                    else
                        statusData.value = Triple(false, it.model.getMessage(), "")
                }
                is AppState.Error -> {
                    statusData.value = Triple(false, it.error, "")
                }
                else -> {}
            }
        }
    }

}

data class BranchAndTSearch(
    val type: AppState<ModelPaymentSearch>,
    val branch: AppState<DBModelBranch>
)