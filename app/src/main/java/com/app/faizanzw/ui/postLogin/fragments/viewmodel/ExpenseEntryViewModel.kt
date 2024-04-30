package com.app.faizanzw.ui.postLogin.fragments.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.DBModelBranch
import com.app.faizanzw.database.DBModelEmployee
import com.app.faizanzw.database.DBModelExpenseType
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.search.ModelSearchExpense
import com.app.faizanzw.ui.postLogin.fragments.search.model.ModelEditExpense
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.CommonFunctions
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    val apiRepo: BaseApiRepo,
    val pref: PreferenceModule
) : ViewModel() {

    val currencyList = MutableLiveData<AppState<ModelCombo>>()
    val typeList = MutableLiveData<AppState<DBModelExpenseType>>()

    val branchList = MutableLiveData<AppState<DBModelBranch>>()
    val employeeList = MutableLiveData<AppState<DBModelEmployee>>()

    fun getEmployeeData(branchId: String?) = viewModelScope.launch {
        employeeList.value = AppState.Loading()
        employeeList.value = CommonFunctions.getEmployeeDataByBranch(branchId!!.toInt())
        /*apiRepo.getEmployeeFillList(companyCode, branchId?:"-1").collect {
            employeeList.value = it
        }*/
    }

    fun getCurrencyData(companyCode: String) = viewModelScope.launch {
        currencyList.value = AppState.Loading()
        apiRepo.getComboFillList(companyCode, "Currency").collect {
            currencyList.value = it
        }
    }

    val balanceData = MutableLiveData<AppState<JsonElement>>()
    fun getTypeData(companyCode: String, branchId: Int, userId: Int) = viewModelScope.launch {
        //typeList.value = AppState.Loading()
        balanceData.value = AppState.Loading()

        //val data1 = async { apiRepo.getComboFillList2(companyCode, "ExpenseType") }
        val data1 = async { apiRepo.getBalanceApi(companyCode, branchId, userId) }
        val data2 = async { getExpenseData() }

        val (res1, res2) = awaitAll(data1, data2)

        typeList.value = res2 as AppState<DBModelExpenseType>
        balanceData.value = res1 as AppState<JsonElement>
    }

    private suspend fun getExpenseData(): AppState<DBModelExpenseType> {
        var result = DataBaseUtils.getAllExpenseType()
        var dataList = ArrayList<DBModelExpenseType>()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Expense Found")
            } else {
                return AppState.Success(DBModelExpenseType(result, true, "", 200))
            }
        }
        return AppState.Error("No Expense Found")

    }

    fun getFillSearchData(
        companyCode: String,
        fromDate: String = "", toDate: String = "", status: String = "ALL", type: String = "-1",
        branchId: String?,
        userId: String?,
        isAllowToShow: Boolean
    ) = viewModelScope.launch {
        try {
            typeList.value = AppState.Loading()

            if (isAllowToShow) {
                //val data1 = async { apiRepo.getComboFillList2(companyCode, "ExpenseType") }
                val data1 = async { getExpenseData() }
                //val data2 = async { apiRepo.getComboFillList2(companyCode, "Branch") }
                val data2 = async { getBranchData() }
                val data4 = async {
                    apiRepo.doSearchExpense(
                        fromDate,
                        toDate,
                        status,
                        type,
                        branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
                        userId ?: pref.get(PrefEnum.USERID, 0).toString()
                    )
                }

                val (res1,
                    res2,
                    res4) = awaitAll(
                    data1,
                    data2,
                    data4
                )

                typeList.value = res1 as AppState<DBModelExpenseType>
                branchList.value = res2 as AppState<DBModelBranch>
                stateSearch.value = res4 as AppState<ModelSearchExpense>
            } else {
                //val data1 = async { apiRepo.getComboFillList2(companyCode, "ExpenseType") }
                val data1 = async {  getExpenseData() }
                val data2 = async {
                    apiRepo.doSearchExpense(
                        fromDate,
                        toDate,
                        status,
                        type,
                        branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
                        userId ?: pref.get(PrefEnum.USERID, 0).toString()
                    )
                }

                val (res1, res2) = awaitAll(data1, data2)

                typeList.value = res1 as AppState<DBModelExpenseType>
                stateSearch.value = res2 as AppState<ModelSearchExpense>
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getBranchData(): AppState<DBModelBranch> {
        var result = DataBaseUtils.getAllBranch()
        var dataList = ArrayList<DBModelBranch>()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Branch Found")
            } else {
                return AppState.Success(DBModelBranch(result, true, "", 200))
            }
        }
        return AppState.Error("No Branch Found")

    }

    val stateAddEntry = MutableLiveData<AppState<JsonElement>>()
    fun addExpenseEntry(
        pref: PreferenceModule,
        tranID: String,
        trandate: String,
        isPaymetClear: String,
        accGroupKey: String,
        netAmount: String,
        oneDollarValue: String,
        paymentType: String,
        cardChecqueNo: String,
        tranremarks: String,
        image: String,
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

        stateAddEntry.value = AppState.Loading()
        apiRepo.addExpenseEntry(
            pref.get(PrefEnum.COMPANYCODE, ""),
            pref.get(PrefEnum.BRANCHID, 0).toString(),
            pref.get(PrefEnum.USERID, 0).toString(),
            tranID,
            //tranAccountKey,
            fDate,
            isPaymetClear,
            accGroupKey,
            netAmount,
            oneDollarValue,
            paymentType,
            cardChecqueNo,
            tranremarks,
            image,
            cType
        ).collect {
            stateAddEntry.value = it
        }
    }

    val stateSearch = MutableLiveData<AppState<ModelSearchExpense>>()
    fun searchExpense(
        fromDate: String = "", toDate: String = "", status: String = "ALL", type: String = "-1",
        branchId: String?,
        userId: String?
    ) = viewModelScope.launch {

        var fDate = fromDate
        var tDate = toDate
        if (fromDate.isNotEmpty()) {
            val strs1 = fromDate.split("/").toTypedArray()
            fDate = strs1[1] + "/" + strs1[0] + "/" + strs1[2]
        }

        if (tDate.isNotEmpty()) {
            val strs2 = toDate.split("/").toTypedArray()
            tDate = strs2[1] + "/" + strs2[0] + "/" + strs2[2]
        }

        stateSearch.value = AppState.Loading()
        val response =
            async {
                apiRepo.doSearchExpense(
                    fDate,
                    tDate,
                    status,
                    type,
                    branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
                    userId ?: pref.get(PrefEnum.USERID, 0).toString()
                )
            }
        val data = response.await()
        stateSearch.value = data

    }

    val dataExpense = MutableLiveData<AppState<ModelEditExpense>>()
    /*fun getExpenseEntry(tranID: String) = viewModelScope.launch {
        dataExpense.value = AppState.Loading()
        apiRepo.doExpenseEntryGet(tranID).collect {
            dataExpense.value = it
        }
    }*/

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMergeData(companyCode: String, tranID: String, branchId: Int, userId: Int) =
        viewModelScope.launch {
            try {
                typeList.value = AppState.Loading()
                val data1 = async { apiRepo.getBalanceApi(companyCode, branchId, userId) }
                //val data2 = async { apiRepo.getComboFillList2(companyCode, "ExpenseType") }
                val data2 = async { getExpenseData() }
                val data3 = async { apiRepo.doExpenseEntryGet(tranID, userId) }

                val (res1, res2, res3) = awaitAll(data1, data2, data3)

                balanceData.value = res1 as AppState<JsonElement>
                typeList.value = res2 as AppState<DBModelExpenseType>
                dataExpense.value = res3 as AppState<ModelEditExpense>
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


}