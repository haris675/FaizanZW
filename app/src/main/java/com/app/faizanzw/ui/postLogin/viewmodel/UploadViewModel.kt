package com.app.faizanzw.ui.postLogin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.database.DBDeliveryTasK
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(val apiRepo: BaseApiRepo, val pref: PreferenceModule) :
    ViewModel() {

    var count = 0
    var deliveryData: DBDeliveryTasK? = null

    private fun uploadDataToServer() {
        if (pref.get(PrefEnum.ISLOGIN, 0) == 1) {
            val dataList = DataBaseUtils.getOfflineDeliveries()
            dataList?.let {
                deliveryData = it[0]
                addTask(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    pref.get(PrefEnum.USERID, 0).toString(),
                    deliveryData!!.branchID.toString(),
                    deliveryData!!.tranType,
                    deliveryData!!.taskPriority,
                    deliveryData!!.taskDescription,
                    deliveryData!!.trandate.convertTodate(),
                    deliveryData!!.assignTo.toString(),
                    "",
                    deliveryData!!.tranID.toString(),
                    deliveryData!!.subject
                )
            }
        }
    }

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
            when (it) {
                is AppState.Error -> {
                    Log.e("Worker : ", "Worker is Error")
                }
                is AppState.Success -> {
                    Log.e("Worker : ", "Worker is Success")
                    deliveryData?.let {
                        DataBaseUtils.updateDeliveriesToOnline(deliveryData!!)
                    }
                    uploadDataToServer()
                }
                is AppState.Loading -> {
                    Log.e("Worker : ", "Worker is Loading")
                }
            }
        }
    }

}