package com.app.faizanzw.ui.postLogin.fragments.search.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ModelPaymentSearch(

    @field:SerializedName("data")
    val data: ArrayList<NewSearchPayment>,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,


    @field:SerializedName("status")
    val status: Int
)

@Parcelize
data class NewSearchPayment(

    @field:SerializedName("TranID")
    val tranID: String,

    @field:SerializedName("TranNo")
    val tranNo: String,

    @field:SerializedName("Amount")
    val amount: String,

    @field:SerializedName("PaymentTo")
    val paymentTo: Int,

    @field:SerializedName("BranchID")
    val branchID: Int,

    @field:SerializedName("SentByName")
    val sentByName: String,

    @field:SerializedName("ReciveByName")
    val reciveByName: String,

    @field:SerializedName("CurrencyKey")
    val currencyKey: Int,

    @field:SerializedName("CurrencyName")
    val currencyName: String,

    @field:SerializedName("LoginUserID")
    val loginUserID: Int,

    @field:SerializedName("IsAllowToChange")
    val isAllowToChange: Boolean,

    @field:SerializedName("Trandate")
    val trandate: String,

    @field:SerializedName("Remarks")
    val remarks: String,

    @field:SerializedName("TranStatus")
    var tranStatus: String,

    @field:SerializedName("CompanyCode")
    val companyCode: String,

    @field:SerializedName("CurrencyType")
    val currencyType: String,

    @field:SerializedName("CreatedBy")
    val CreatedBy: Int,

) : Parcelable
