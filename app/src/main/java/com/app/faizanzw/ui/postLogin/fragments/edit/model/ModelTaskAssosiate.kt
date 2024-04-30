package com.app.faizanzw.ui.postLogin.fragments.edit.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import kotlinx.parcelize.Parcelize

data class ModelTaskAssosiate(

	@field:SerializedName("data")
	val data: AssosiateData,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)

@Parcelize
data class AssosiateData(

	@field:SerializedName("Progress")
	var progress: Int = 0,

	@field:SerializedName("Description")
	var description: String = "null",

	@field:SerializedName("DocByte")
	var docByte: String ?= "",

	@field:SerializedName("T2AKey")
	var t2AKey: Int = 0,

	@field:SerializedName("TaskStatus")
	var taskStatus: String = "",

	@field:SerializedName("TaskKey")
	var taskKey: Int = 0,

	@field:SerializedName("CompanyCode")
	var companyCode: String = "",
): Parcelable
