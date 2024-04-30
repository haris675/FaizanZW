package com.app.faizanzw.ui.postLogin.fragments.search.model

import android.os.Parcelable
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize

data class ModelSearchTask(

    @field:SerializedName("data")
    val data: ArrayList<TaskItem>,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)

@Parcelize
data class TaskItem(

    @field:SerializedName("AssignToName")
    var assignToName: String="",

    @field:SerializedName("CreatedByName")
    var createdByName: String="",

    @field:SerializedName("TaskPriority")
    var taskPriority: String="",

    @field:SerializedName("TaskDescription")
    var taskDescription: String="",

    @field:SerializedName("Trandate")
    var trandate: String="",

    @field:SerializedName("TranTypeName")
    var tranTypeName: String="",

    @field:SerializedName("TranNo")
    var tranNo: String="",

    @field:SerializedName("TranStatus")
    var tranStatus: String="",

    @field:SerializedName("IsAllowToChange")
    var IsAllowToChange: Boolean=false,

    @field:SerializedName("DocByte")
    var DocByte: String? = "",

    @field:SerializedName("TranType")
    var TranType: String = "",

    @field:SerializedName("BranchID")
    var BranchID: String = "",

    @field:SerializedName("AssignTo")
    var AssignTo: String = "",

    @PrimaryKey
    @field:SerializedName("TranID")
    var TranID: Int = 0,

    @field:SerializedName("Subject")
    var subject: String = "",

    @field:SerializedName("CreatedBy")
    var createdBy: Int = 0,

    @field:SerializedName("TaskPercentage")
    var taskPercentage: Int = 0,

    @field:SerializedName("LstAssociate")
    var lstAssociate: ArrayList<AssosiateData>,

    @field:SerializedName("WebView")
    var WebView: String = "") : Parcelable
