package com.app.faizanzw.ui.postLogin.fragments.model

import com.google.gson.annotations.SerializedName

data class ModelCombo(

    @field:SerializedName("data")
    val data: ArrayList<DataItem>,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)

data class DataItem(

    @field:SerializedName("ComboID")
    val comboID: String,

    @field:SerializedName("ComboValue")
    val comboValue: String,

    @field:SerializedName("AllowTOAddImage")
    val AllowTOAddImage: Boolean = false

) {
    override fun toString(): String {
        return this.comboValue
    }
}
