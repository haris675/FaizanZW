package com.app.faizanzw.ui.postLogin.fragments.search

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ModelSearchExpense(

    @field:SerializedName("data")
    val data: ArrayList<ExpenseSearch>,

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: Int
)

@Parcelize
data class ExpenseSearch(

    @field:SerializedName("Action")
    val action: Int,

    @field:SerializedName("TranNo")
    val tranNo: String,

    @field:SerializedName("PartyName")
    var partyName: String,

    @field:SerializedName("TranID")
    val tranID: Int,

    @field:SerializedName("RefrenceNo")
    val refrenceNo: String = "",

    @field:SerializedName("AccGroupName")
    val accGroupName: String,

    @field:SerializedName("Trandate")
    val trandate: String,

    @field:SerializedName("NetAmount")
    var netAmount: String,

    @field:SerializedName("OneDollarValue")
    val oneDollarValue: String,

    @field:SerializedName("Currencey")
    val currencey: String,

    @field:SerializedName("PaymentType")
    var paymentType: String,

    @field:SerializedName("AccountName")
    val accountName: String,

    @field:SerializedName("ExpenseStatus")
    var expenseStatus: String,

    @field:SerializedName("IsAllowToChange")
    val IsAllowToChange: Boolean,

    @field:SerializedName("DocByte")
    val DocByte: String? = "",

    @field:SerializedName("AccGroupKey")
    val AccGroupKey: String = "0",

    @field:SerializedName("Tranremarks")
    val tranremarks: String = "",

    @field:SerializedName("TranAccountKey")
    val TranAccountKey: String = "",

    @field:SerializedName("CurrencyType")
    val currencyType: String="",

    @field:SerializedName("TaskPercentage")
    val TaskPercentage: Int,

    @field:SerializedName("CreatedBy")
    val CreatedBy: Int = 0,

    @field:SerializedName("IsPaymetClear")
    val isPaymetClear: String = "",

    @field:SerializedName("TranPartyKey")
    val TranPartyKey: Int = 0,

    @field:SerializedName("CardChecqueNo")
    val CardChecqueNo: String = ""

) : Parcelable
