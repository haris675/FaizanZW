package com.app.faizanzw.database

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

data class ModelDeliveryTask(

    @field:SerializedName("data")
    val data: ArrayList<DBDeliveryTasK>? = null,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)

open class DBDeliveryTasK(

    @field:SerializedName("TranType")
    var tranType: String = "",

    @field:SerializedName("CreatedByName")
    var createdByName: String = "",

    @field:SerializedName("DeliveryKey")
    var deliveryKey: Int = 0,

    @field:SerializedName("CreatedBy")
    var createdBy: Int = 0,

    @field:SerializedName("TaskPriority")
    var taskPriority: String = "",

    @field:SerializedName("AssignTo")
    var assignTo: Int = 0,

    @field:SerializedName("DocByte")
    var docByte: String? = "",

    @field:SerializedName("TranNo")
    var tranNo: String = "",

    @field:SerializedName("SourceType")
    var sourceType: String = "",

    @field:SerializedName("BranchID")
    var branchID: Int = 0,

    @PrimaryKey
    @field:SerializedName("TranID")
    var tranID: Int = 0,

    @field:SerializedName("Subject")
    var subject: String = "null",

    @field:SerializedName("AssignToName")
    var assignToName: String = "",

    @field:SerializedName("TaskPercentage")
    var taskPercentage: Int = 0,

    @field:SerializedName("LoginUserID")
    var loginUserID: Int = 0,

    /*@field:SerializedName("LstAssociate")
    var lstAssociate: Any? = null,*/

    @field:SerializedName("TaskDescription")
    var taskDescription: String = "",

    @field:SerializedName("IsAllowToChange")
    var isAllowToChange: Boolean = false,

    @field:SerializedName("Trandate")
    var trandate: String = "",

    @field:SerializedName("TranTypeName")
    var tranTypeName: String = "",

    @field:SerializedName("TranStatus")
    var tranStatus: String = "",

    @field:SerializedName("WebView")
    var webView: String = "",

    @field:SerializedName("CompanyCode")
    var companyCode: String = "",

    var remarks: String = "",
    var isOnline: Int = 1
) : RealmObject()
