package com.app.faizanzw.ui.postLogin.fragments.search.model

import com.google.gson.annotations.SerializedName

data class ModelEditPayment(
    @field:SerializedName("data")
    val data: NewSearchPayment,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)