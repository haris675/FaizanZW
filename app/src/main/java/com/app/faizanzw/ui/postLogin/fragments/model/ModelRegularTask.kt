package com.app.faizanzw.ui.postLogin.fragments.model

import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.google.gson.annotations.SerializedName

data class ModelRegularTask(

	@field:SerializedName("data")
	val data: ArrayList<RegularData>,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)

data class RegularData(

	@field:SerializedName("TranType")
	val tranType: String,

	@field:SerializedName("CreatedByName")
	val createdByName: String,

	@field:SerializedName("DeliveryKey")
	val deliveryKey: Int,

	@field:SerializedName("CreatedBy")
	val createdBy: Int,

	@field:SerializedName("TaskPriority")
	val taskPriority: String,

	@field:SerializedName("AssignTo")
	val assignTo: Int,

	@field:SerializedName("DocByte")
	val docByte: String = "",

	@field:SerializedName("TranNo")
	val tranNo: String? = null,

	@field:SerializedName("SourceType")
	val sourceType: String,

	@field:SerializedName("BranchID")
	val branchID: Int,

	@field:SerializedName("TranID")
	val tranID: Int,

	@field:SerializedName("Subject")
	val subject: String,

	@field:SerializedName("AssignToName")
	val assignToName: String,

	@field:SerializedName("TaskPercentage")
	val taskPercentage: Int,

	@field:SerializedName("LoginUserID")
	val loginUserID: Int,

	@field:SerializedName("LstAssociate")
	val lstAssociate: ArrayList<AssosiateData>,

	@field:SerializedName("TaskDescription")
	val taskDescription: String,

	@field:SerializedName("IsAllowToChange")
	val isAllowToChange: Boolean,

	@field:SerializedName("Trandate")
	val trandate: String,

	@field:SerializedName("TranTypeName")
	val tranTypeName: String,

	@field:SerializedName("TranStatus")
	val tranStatus: String,

	@field:SerializedName("WebView")
	val webView: String = "",

	@field:SerializedName("CompanyCode")
	val companyCode: String,


)
