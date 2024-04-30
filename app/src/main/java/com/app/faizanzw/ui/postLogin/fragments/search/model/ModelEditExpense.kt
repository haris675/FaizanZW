package com.app.faizanzw.ui.postLogin.fragments.search.model

import com.app.faizanzw.ui.postLogin.fragments.search.ExpenseSearch
import com.google.gson.annotations.SerializedName

data class ModelEditExpense(
    @field:SerializedName("data")
    val data: ExpenseSearch,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)
