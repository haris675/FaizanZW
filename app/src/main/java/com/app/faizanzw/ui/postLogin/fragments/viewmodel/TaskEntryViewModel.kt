package com.app.faizanzw.ui.postLogin.fragments.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.DBModelBranch
import com.app.faizanzw.database.DBModelEmployee
import com.app.faizanzw.database.DBModelExpenseType
import com.app.faizanzw.database.DBModelTaskType
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.search.model.ModelEditTask
import com.app.faizanzw.ui.postLogin.fragments.search.model.ModelSearchTask
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.CommonFunctions
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

@HiltViewModel
class TaskEntryViewModel @Inject constructor(
    val apiRepo: BaseApiRepo, val pref: PreferenceModule
) : ViewModel() {

    val stateValidate = MutableLiveData("")

    val taskList = MutableLiveData<AppState<DBModelTaskType>>()

    fun getTaskData(
        companyCode: String,
        fromDate: String = "",
        toDate: String = "",
        status: String = "ALL",
        type: String = "-1",
        branchId: String?,
        userId: String?,
        isAllowToShow: Boolean
    ) = viewModelScope.launch {

        try {
            taskList.value = AppState.Loading()

            if (isAllowToShow) {
                //val data1 = async { apiRepo.getComboFillList2(companyCode, "AppTaskType") }
                val data1 = async { CommonFunctions.getTaskTypeData() }
                //val data2 = async { apiRepo.getComboFillList2(companyCode, "Branch") }
                val data2 = async { CommonFunctions.getBranchData() }
                val data4 = async {
                    apiRepo.doSearchTask2(
                        fromDate,
                        toDate,
                        status,
                        type,
                        branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
                        userId ?: pref.get(PrefEnum.USERID, 0).toString(),
                        pref.get(PrefEnum.USERID, 0)
                    )
                }

                val (res1, res2, res4) = awaitAll(
                    data1, data2, data4
                )

                taskList.value = res1 as AppState<DBModelTaskType>
                branchList.value = res2 as AppState<DBModelBranch>
                stateSearch.value = res4 as AppState<ModelSearchTask>
            } else {
                //val data1 = async { apiRepo.getComboFillList2(companyCode, "AppTaskType") }
                val data1 = async { CommonFunctions.getTaskTypeData() }
                val data2 = async {
                    apiRepo.doSearchTask2(
                        fromDate,
                        toDate,
                        status,
                        type,
                        branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
                        userId ?: pref.get(PrefEnum.USERID, 0).toString(),
                        pref.get(PrefEnum.USERID, 0)
                    )
                }

                val (res1, res2) = awaitAll(data1, data2)

                taskList.value = res1 as AppState<DBModelTaskType>
                stateSearch.value = res2 as AppState<ModelSearchTask>
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    val branchList = MutableLiveData<AppState<DBModelBranch>>()
    val employeeList = MutableLiveData<AppState<DBModelEmployee>>()

    fun getEmployeeData(branchId: String?) = viewModelScope.launch {
        employeeList.value = AppState.Loading()
        employeeList.value = CommonFunctions.getEmployeeDataByBranch(branchId!!.toInt())
    /*apiRepo.getEmployeeFillList(companyCode, branchId ?: "-1").collect {
            employeeList.value = it
        }*/
    }

    //val balanceData = MutableLiveData<AppState<JsonElement>>()
    fun getMergedData(companyCode: String) = viewModelScope.launch {

        taskList.value = AppState.Loading()

        val data1 = async { CommonFunctions.getBranchData() }
        val data2 = async { CommonFunctions.getTaskTypeData() }

        val (res1,res2) = awaitAll(data1,data2)


        branchList.value = res1 as AppState<DBModelBranch>
        taskList.value = res2 as AppState<DBModelTaskType>

        /*apiRepo.getComboFillList(companyCode, "AppTaskType")
            .zip(CommonFunctions.getBranchData()) { first, second ->

                return@zip BranchAndTypeData2(first, second)
            }.flowOn(Dispatchers.IO).catch {
                stateValidate.value = "${it.message}"
            }.collect {
                taskList.value = it.type
                branchList.value = it.branch
            }*/
    }

    val stateAddTask = MutableLiveData<AppState<JsonElement>>()
    fun addTask(
        companyCode: String,
        loginUserID: String,
        branchID: String,
        tranType: String,
        taskPriority: String,
        taskDescription: String,
        tranDate: String,
        assignTo: String,
        image: String,
        tranID: String,
        subject: String
    ) = viewModelScope.launch {

        var fDate = tranDate
        if (tranDate.isNotEmpty()) {
            val strs1 = tranDate.split("/").toTypedArray()
            fDate = strs1[1] + "/" + strs1[0] + "/" + strs1[2]
        }

        stateAddTask.value = AppState.Loading()
        apiRepo.addTaskApi(
            companyCode,
            loginUserID,
            branchID,
            tranType,
            taskPriority,
            taskDescription,
            fDate,
            assignTo,
            image,
            tranID,
            subject
        ).collect {
            stateAddTask.value = it
        }
    }

    val stateSearch = MutableLiveData<AppState<ModelSearchTask>>()
    fun searchTask(
        fromDate: String = "",
        toDate: String = "",
        status: String = "SELECT ALL",
        type: String = "SELECT ALL",
        branchId: String?,
        userId: String?,
        loginUserID: Int = pref.get(PrefEnum.USERID, 0)
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

        stateSearch.value = AppState.Loading()
        apiRepo.doSearchTask(
            fDate,
            tDate,
            status,
            type,
            branchId ?: pref.get(PrefEnum.BRANCHID, 0).toString(),
            userId ?: pref.get(PrefEnum.USERID, 0).toString(),
            loginUserID
        ).collect {
            stateSearch.value = it
        }
    }

    val taskData = MutableLiveData<AppState<ModelEditTask>>()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMergeData(companyCode: String, tranID: String, userId: Int, isAssigned: Boolean) =
        viewModelScope.launch {
            try {
                if (!isAssigned) {
                    taskList.value = AppState.Loading()
                    //val data1 = async { apiRepo.getComboFillList2(companyCode, "AppTaskType") }
                    val data1 = async { CommonFunctions.getTaskTypeData() }
                    //val data2 = async { apiRepo.getComboFillList2(companyCode, "Branch") }
                    val data2 = async { CommonFunctions.getBranchData() }
                    val data4 = async { apiRepo.doTastEntryGet(companyCode, tranID, userId) }

                    val (res1, res2,
                        res4) = awaitAll(
                        data1, data2,
                        data4
                    )

                    taskList.value = res1 as AppState<DBModelTaskType>
                    branchList.value = res2 as AppState<DBModelBranch>
                    taskData.value = res4 as AppState<ModelEditTask>
                } else {
                    taskData.value = AppState.Loading()
                    val data1 = async { apiRepo.doTastEntryGet(companyCode, tranID, userId) }
                    val res1 = data1.await()
                    taskData.value = res1
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    val statusData = MutableLiveData<Triple<Boolean, String, String>>()
    fun updateStatus(
        companyCode: String, tranID: String, tranStatus: String, loginUserID: String
    ) = viewModelScope.launch {
        statusData.value = Triple(false, "Loading", "")
        apiRepo.doUpdateTaskStatus(
            companyCode, tranID, tranStatus, loginUserID
        ).collect {
            when (it) {
                is AppState.Success -> {
                    if (it.model.getStatus()) statusData.value =
                        Triple(true, it.model.getMessage(), tranStatus)
                    else statusData.value = Triple(false, it.model.getMessage(), "")
                }
                is AppState.Error -> {
                    statusData.value = Triple(false, it.error, "")
                }
                else -> {}
            }
        }
    }

    val assosiateDate = MutableLiveData<AppState<JsonElement>>()
    fun saveAssosiate(
        companyCode: String,
        t2AKey: String,
        taskKey: String,
        description: String,
        progress: String,
        taskStatus: String,
        image: String
    ) = viewModelScope.launch {
        assosiateDate.value = AppState.Loading()
        apiRepo.doSaveAssosiateTask(
            companyCode,
            t2AKey,
            taskKey,
            description,
            progress,
            taskStatus,
            image
        ).collect {
            assosiateDate.value = it
        }
    }

    val acceptTask = MutableLiveData<AppState<JsonElement>>()
    fun callAcceptTask(
        companyCode: String, tranID: Int, tranStatus: String, loginUserID: Int
    ) = viewModelScope.launch {
        acceptTask.value = AppState.Loading()
        apiRepo.doAcceptTaskStatus(companyCode, tranID, tranStatus, loginUserID).collect {
            acceptTask.value = it
        }
    }

}


data class BranchAndTypeData2(val type: AppState<ModelCombo>, val branch: AppState<DBModelBranch>)

