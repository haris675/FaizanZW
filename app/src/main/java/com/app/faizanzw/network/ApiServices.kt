package com.app.faizanzw.network

import com.app.faizanzw.database.*
import com.app.faizanzw.ui.postLogin.fragments.edit.model.ModelTaskAssosiate
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.model.ModelRegularTask
import com.app.faizanzw.ui.postLogin.fragments.search.ModelSearchExpense
import com.app.faizanzw.ui.postLogin.fragments.search.model.*
import com.app.faizanzw.utils.Constants
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface ApiServices {


    //@POST(Constants.LOGIN)
    @POST()
    @FormUrlEncoded
    suspend fun doLogin(
        @Url url:String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("UserName") UserName: String,
        @Field("UserPassword") UserPassword: String
    ): Response<JsonElement>

    @POST(Constants.GET_URL)
    @FormUrlEncoded
    suspend fun doGetCode(
        @Field("CompanyCode") CompanyCode: String
    ): Response<JsonElement>

    //@POST(Constants.DASHBOARD)
    @POST()
    @FormUrlEncoded
    suspend fun doGetDashboardData(
        @Url url:String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<JsonElement>

    //@POST(Constants.COMBO_FILL)
    @POST()
    @FormUrlEncoded
    suspend fun doComboFill(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("ComboName") ComboName: String
    ): Response<ModelCombo>

    //@POST(Constants.COMBO_FILL)
    @POST()
    @FormUrlEncoded
    suspend fun doTaskType(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("ComboName") ComboName: String
    ): Response<DBModelTaskType>

    //@POST(Constants.COMBO_FILL)
    @POST()
    @FormUrlEncoded
    suspend fun doExpenseType(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("ComboName") ComboName: String
    ): Response<DBModelExpenseType>

    //@POST(Constants.COMBO_FILL)
    @POST()
    @FormUrlEncoded
    suspend fun doComboBranch(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("ComboName") ComboName: String
    ): Response<DBModelBranch>

    //@POST(Constants.ADD_EXPENSE)
    @POST()
    @Multipart
    suspend fun doaddExpenseEntry(
        @Url url: String,
        @Part("CompanyCode") CompanyCode: RequestBody,
        @Part("BranchID") BranchID: RequestBody,
        @Part("TranPartyKey") TranPartyKey: RequestBody,
        @Part("TranID") TranID: RequestBody,
        //@Part("TranAccountKey") TranAccountKey: RequestBody,
        @Part("Trandate") Trandate: RequestBody,
        @Part("IsPaymetClear") IsPaymetClear: RequestBody,
        @Part("AccGroupKey") AccGroupKey: RequestBody,
        @Part("NetAmount") NetAmount: RequestBody,
        @Part("OneDollarValue") OneDollarValue: RequestBody,
        @Part("PaymentType") PaymentType: RequestBody,
        @Part("CardChecqueNo") CardChecqueNo: RequestBody,
        @Part("Tranremarks") Tranremarks: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("CurrencyType") currencyType: RequestBody
    ): Response<JsonElement>

    /*@POST(Constants.EMPLOYEE_LIST)
    @FormUrlEncoded
    suspend fun doEmployeeFill(
        @Field("CompanyCode") CompanyCode: String,
        @Field("BranchID") BranchID: String
    ): Response<ModelCombo>*/

    //@POST(Constants.ADD_TASK)
    @POST()
    @Multipart
    suspend fun doaddTaskEntry(
        @Url url: String,
        @Part("CompanyCode") CompanyCode: RequestBody,
        @Part("LoginUserID") LoginUserID: RequestBody,
        @Part("BranchID") BranchID: RequestBody,
        @Part("TranType") TranType: RequestBody,
        @Part("TaskPriority") TaskPriority: RequestBody,
        @Part("TaskDescription") TaskDescription: RequestBody,
        @Part("Trandate") Trandate: RequestBody,
        @Part("AssignTo") AssignTo: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("TranID") TranID: RequestBody,
        @Part("Subject") Subject: RequestBody
    ): Response<JsonElement>

    /*@POST(Constants.ADD_TASK)
    @FormUrlEncoded
    fun doaddTaskEntry2(
        @Field("CompanyCode") CompanyCode: String,
        @Field("LoginUserID") LoginUserID: String,
        @Field("BranchID") BranchID: String,
        @Field("TranType") TranType: String,
        @Field("TaskPriority") TaskPriority: String,
        @Field("TaskDescription") TaskDescription: String,
        @Field("Trandate") Trandate: String,
        @Field("AssignTo") AssignTo: String,
        @Field("image") image: String,
        @Field("TranID") TranID: String,
        @Field("Subject") Subject: String
    ): Call<JsonElement>*/

    //@POST(Constants.ADD_PAYMENT)
    @POST()
    @FormUrlEncoded
    suspend fun doAddPayment(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("Trandate") Trandate: String,
        @Field("Amount") Amount: String,
        @Field("Remarks") Remarks: String,
        @Field("BranchID") BranchID: String,
        @Field("PaymentTo") PaymentTo: String,
        @Field("LoginUserID") LoginUserID: String,
        @Field("CurrencyType") currencyType: String
    ): Response<JsonElement>

    //@POST(Constants.EXPENSE_SEARCH)
    @POST()
    @FormUrlEncoded
    suspend fun doSearchExpense(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("FromDate") FromDate: String,
        @Field("ToDate") ToDate: String,
        @Field("ExpenseStatus") ExpenseStatus: String,
        @Field("ExpenseTypeKey") ExpenseTypeKey: String,
        @Field("BranchID") BranchID: String,
        @Field("UserID") UserID: String,
    ): Response<ModelSearchExpense>

    //@POST(Constants.TASK_SEARCH)
    @POST()
    @FormUrlEncoded
    suspend fun doSearchTask(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("FromDate") FromDate: String,
        @Field("ToDate") ToDate: String,
        @Field("TaskStatus") TaskStatus: String,
        @Field("TaskType") TaskType: String,
        @Field("BranchID") BranchID: String,
        @Field("UserID") UserID: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelSearchTask>

    //@POST(Constants.PAYMENT_SEARCH)
    @POST()
    @FormUrlEncoded
    suspend fun doSearchPayment(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("FromDate") FromDate: String,
        @Field("ToDate") ToDate: String,
        @Field("TaskStatus") TaskStatus: String,
        @Field("BranchID") BranchID: String,
        @Field("PaymentTo") PaymentTo: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelPaymentSearch>

    //@POST(Constants.GET_EXPENSE_ENTRY)
    @POST()
    @FormUrlEncoded
    suspend fun doExpenseEntryGet(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelEditExpense>

    //@POST(Constants.GET_TASK_ENTRY)
    @POST()
    @FormUrlEncoded
    suspend fun doTaskEntryGet(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelEditTask>

    //@POST(Constants.GET_PAYMENT_ENTRY)
    @POST()
    @FormUrlEncoded
    suspend fun doPaymentEntryGet(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelEditPayment>

    //@POST(Constants.UPDATE_TASK_STATUS)
    @POST()
    @FormUrlEncoded
    suspend fun doUpdateTaskStatus(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("TranStatus") TranStatus: String,
        @Field("LoginUserID") LoginUserID: String
    ): Response<JsonElement>

    //@POST(Constants.UPDATE_PAYMENT_STATUS)
    @POST()
    @FormUrlEncoded
    suspend fun doUpdatePaymentStatus(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: String,
        @Field("TranStatus") TranStatus: String,
        @Field("LoginUserID") LoginUserID: String
    ): Response<JsonElement>

    //@POST(Constants.REGULAR_EXPENSE_COMBO)
    @POST()
    @FormUrlEncoded
    suspend fun doRegularFill(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("UserID") UserID: String
    ): Response<ModelCombo>

    //@POST(Constants.ADD_REGULAR_TAK)
    @POST()
    @Multipart
    suspend fun doAddRegularTask(
        @Url url: String,
        @Part("CompanyCode") CompanyCode: RequestBody,
        @Part("TranId") tranId: RequestBody,
        @Part("Trandate") trandate: RequestBody,
        @Part("TranType") tranType: RequestBody,
        @Part("TaskDescription") taskDescription: RequestBody,
        @Part("BranchID") BranchID: RequestBody,
        @Part("AssignTo") assignTo: RequestBody,
        @Part("UserID") UserID: RequestBody,
        @Part image: MultipartBody.Part?,
    ): Response<JsonElement>

    //@POST(Constants.BALANCE_GET)
    @POST()
    @FormUrlEncoded
    suspend fun doBalaceGet(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("BranchID") branchId: Int,
        @Field("LoginUserID") userId: Int
    ): Response<JsonElement>

    //@POST(Constants.GET_TASK_ASSOSIATE)
    @POST()
    @FormUrlEncoded
    suspend fun doTaskAssosiateGet(
        @Url url: String,
        @Field("T2AKey") key: Int,
        @Field("CompanyCode") companyCode: String
    ): Response<ModelTaskAssosiate>

    //@POST(Constants.ACCEPT_TASK)
    @POST()
    @FormUrlEncoded
    suspend fun doAcceptTaskStatus(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("TranID") TranID: Int,
        @Field("TranStatus") TranStatus: String,
        @Field("LoginUserID") LoginUserID: Int
    ): Response<JsonElement>

    //@POST(Constants.SAVE_TASK_ASSOSIATE)
    @POST()
    @Multipart
    suspend fun doSaveAssosiateTask(
        @Url url: String,
        @Part("CompanyCode") companyCode: RequestBody,
        @Part("T2AKey") t2AKey: RequestBody,
        @Part("TaskKey") taskKey: RequestBody,
        @Part("Description") description: RequestBody,
        @Part("Progress") progress: RequestBody,
        @Part("TaskStatus") taskStatus: RequestBody,
        @Part image: MultipartBody.Part?,
    ): Response<JsonElement>

    //@POST(Constants.SAVE_TASK_ASSOSIATE)
    @POST()
    @Multipart
     fun doSaveAssosiateTask2(
        @Url url: String,
        @Part("CompanyCode") companyCode: RequestBody,
        @Part("T2AKey") t2AKey: RequestBody,
        @Part("TaskKey") taskKey: RequestBody,
        @Part("Description") description: RequestBody,
        @Part("Progress") progress: RequestBody,
        @Part("TaskStatus") taskStatus: RequestBody,
        @Part image: MultipartBody.Part?,
    ): Call<JsonElement>

    //@POST(Constants.REGULAR_TASK_LIST)
    @POST()
    @FormUrlEncoded
    suspend fun doRegularTaskGet(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("BranchID") branchId: Int,
        @Field("LoginUserID") userId: Int
    ): Response<ModelRegularTask>

    //@POST(Constants.REGULAR_TASK_SAVE)
    @POST()
    @FormUrlEncoded
    suspend fun doRegularTaskSave(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String,
        @Field("BranchID") branchId: Int,
        @Field("LoginUserID") userId: Int
    ): Response<JsonElement>

    //@POST(Constants.APP_ALL_EMPLOYEE)
    @POST()
    @FormUrlEncoded
    suspend fun doGetAllEmployee(
        @Url url: String,
        @Field("CompanyCode") CompanyCode: String): Response<DBModelEmployee>

    //@POST(Constants.GET_DELIVERY_TASK)
    @POST()
    @FormUrlEncoded
    suspend fun doGetDeliveryTask(
        @Url url: String,
        @Field("CompanyCode") companyCode: String,
        @Field("LoginUserID") loginUserID: Int
    ): Response<ModelDeliveryTask>

}