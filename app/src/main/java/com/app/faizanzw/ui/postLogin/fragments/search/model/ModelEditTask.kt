package com.app.faizanzw.ui.postLogin.fragments.search.model

import android.os.Parcelable
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelEditTask(
    @field:SerializedName("data")
    val data: TaskItem,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int,
) : Parcelable