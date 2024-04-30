package com.app.faizanzw.database

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

data class DBModelBranch(
    @field:SerializedName("data")
    val data: ArrayList<DBBranch>,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)

open class DBBranch(
    @PrimaryKey
    @field:SerializedName("ComboID")
    var comboID: Int = 0,

    @field:SerializedName("ComboValue")
    var comboValue: String? = null,
) : RealmObject() {
    override fun toString(): String {
        if (comboValue != null)
            return this.comboValue!!
        else
            return ""
    }
}