package com.app.faizanzw.database

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

data class DBModelEmployee(

	@field:SerializedName("data")
	val data: ArrayList<DBEmployee>,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: Int
)

open class DBEmployee(

	@PrimaryKey
	@field:SerializedName("UserMasterKey")
	var userMasterKey: Int=0,

	@field:SerializedName("BranchID")
	var branchID: Int=0,

	@field:SerializedName("UserFullName")
	var userFullName: String?=null
) : RealmObject(){
	override fun toString(): String {
		if (userFullName != null)
			return this.userFullName!!
		else
			return ""
	}
}
