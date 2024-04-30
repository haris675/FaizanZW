package com.app.faizanzw.network

import com.app.faizanzw.database.*
import com.app.faizanzw.ui.postLogin.fragments.edit.model.ModelTaskAssosiate
import com.app.faizanzw.ui.postLogin.fragments.model.ModelCombo
import com.app.faizanzw.ui.postLogin.fragments.model.ModelRegularTask
import com.app.faizanzw.ui.postLogin.fragments.search.ModelSearchExpense
import com.app.faizanzw.ui.postLogin.fragments.search.model.*
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Constants
import com.app.faizanzw.utils.Extension.getImageBody
import com.app.faizanzw.utils.Extension.getRequestBody
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseApiRepo @Inject constructor(
    //@ApplicationContext val context: Context,
    @Qualifiers.baseClientWithToken private val baseApiService: ApiServices,
    private val pref: PreferenceModule
) : BaseApiResponse() {

    fun loginApi(
        companyCode: String, username: String, password: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doLogin(
                pref.get(
                    PrefEnum.COMPANYURL,
                    ""
                ) + Constants.LOGIN, companyCode, username, password
            )
        })
    }.flowOn(Dispatchers.IO)

    fun getDashboardData(companyCode: String, loginUserID: Int): Flow<AppState<JsonElement>> =
        flow {
            emit(safeApiCall {
                baseApiService.doGetDashboardData(
                    pref.get(
                        PrefEnum.COMPANYURL,
                        ""
                    ) + Constants.DASHBOARD, companyCode, loginUserID
                )
            })
        }.flowOn(Dispatchers.IO)

    fun getComboFillList(comapanyCode: String, comboName: String): Flow<AppState<ModelCombo>> =
        flow<AppState<ModelCombo>> {
            emit(safeApiCall {
                baseApiService.doComboFill(
                    pref.get(
                        PrefEnum.COMPANYURL,
                        ""
                    ) + Constants.COMBO_FILL, comapanyCode, comboName
                )
            })
        }.flowOn(Dispatchers.IO)

    fun addExpenseEntry(
        companyCode: String,
        branchID: String,
        tranPartyKey: String,
        tranID: String,
        //tranAccountKey: String,
        trandate: String,
        isPaymetClear: String,
        accGroupKey: String,
        netAmount: String,
        oneDollarValue: String,
        paymentType: String,
        cardChecqueNo: String,
        tranremarks: String,
        image: String,
        currencyType: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doaddExpenseEntry(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.ADD_EXPENSE,
                companyCode.getRequestBody(),
                branchID.getRequestBody(),
                tranPartyKey.getRequestBody(),
                tranID.getRequestBody(),
                //tranAccountKey.getRequestBody(),
                trandate.getRequestBody(),
                isPaymetClear.getRequestBody(),
                accGroupKey.getRequestBody(),
                netAmount.getRequestBody(),
                oneDollarValue.getRequestBody(),
                paymentType.getRequestBody(),
                cardChecqueNo.getRequestBody(),
                tranremarks.getRequestBody(),
                image.getImageBody("DocByte"),
                currencyType.getRequestBody()
            )
        })
    }.flowOn(Dispatchers.IO)

    /*fun getEmployeeFillList(comapanyCode: String, branchId: String): Flow<AppState<ModelCombo>> =
        flow<AppState<ModelCombo>> {
            emit(safeApiCall { baseApiService.doEmployeeFill(comapanyCode, branchId) })
        }.flowOn(Dispatchers.IO)*/

    fun addTaskApi(
        companyCode: String,
        loginUserID: String,
        branchID: String,
        tranType: String,
        taskPriority: String,
        taskDescription: String,
        tranDate: String,
        assignTo: String,
        image: String,
        tranID: String,
        subject: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doaddTaskEntry(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.ADD_TASK,
                companyCode.getRequestBody(),
                loginUserID.getRequestBody(),
                branchID.getRequestBody(),
                tranType.getRequestBody(),
                taskPriority.getRequestBody(),
                taskDescription.getRequestBody(),
                tranDate.getRequestBody(),
                assignTo.getRequestBody(),
                image.getImageBody("DocByte"),
                tranID.getRequestBody(),
                subject.getRequestBody()
            )
        })
    }.flowOn(Dispatchers.IO)

    fun addPaymentApi(
        tranID: String,
        trandate: String,
        amount: String,
        remarks: String,
        branchID: String,
        paymentTo: String,
        currencyType: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doAddPayment(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.ADD_PAYMENT,
                pref.get(PrefEnum.COMPANYCODE, ""),
                tranID,
                trandate,
                amount,
                remarks,
                //currencyKey,
                branchID,
                paymentTo,
                pref.get(PrefEnum.USERID, 0).toString(),
                currencyType,
            )
        })
    }

    suspend fun doSearchExpense(
        //companyCode: String,
        fromDate: String,
        toDate: String,
        expenseStatus: String,
        expenseTypeKey: String,
        branchID: String,
        userID: String
    ): AppState<ModelSearchExpense> {
        return safeApiCall {
            baseApiService.doSearchExpense(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.EXPENSE_SEARCH,
                pref.get(PrefEnum.COMPANYCODE, ""), fromDate,
                toDate, expenseStatus, expenseTypeKey, branchID,
                userID
            )
        }
    }

    fun doSearchTask(
        fromDate: String,
        toDate: String,
        status: String,
        typeKey: String,
        branchID: String,
        userID: String,
        loginUserID: Int
    ): Flow<AppState<ModelSearchTask>> = flow {
        emit(safeApiCall {
            baseApiService.doSearchTask(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.TASK_SEARCH,
                pref.get(PrefEnum.COMPANYCODE, ""), fromDate,
                toDate, status, typeKey, branchID,
                userID, loginUserID
            )
        })
    }.flowOn(Dispatchers.IO)

    suspend fun doSearchTask2(
        fromDate: String,
        toDate: String,
        status: String,
        typeKey: String,
        branchID: String,
        userID: String,
        loginUserID: Int
    ): AppState<ModelSearchTask> {
        return safeApiCall {
            baseApiService.doSearchTask(
                pref.get(PrefEnum.COMPANYURL, ""),
                pref.get(PrefEnum.COMPANYCODE, ""), fromDate,
                toDate, status, typeKey, branchID,
                userID, loginUserID
            )
        }
    }

    suspend fun doSearchPaymentFlow(
        fromDate: String,
        toDate: String,
        status: String,
        branchID: String,
        paymentTo: String,
        loginUserID: Int
    ): Flow<AppState<ModelPaymentSearch>> = flow {
        emit(safeApiCall {
            baseApiService.doSearchPayment(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.PAYMENT_SEARCH,
                pref.get(PrefEnum.COMPANYCODE, ""), fromDate,
                toDate, status, branchID, paymentTo,
                loginUserID
            )
        })
    }.flowOn(Dispatchers.IO)

    suspend fun doSearchPayment(
        fromDate: String,
        toDate: String,
        status: String,
        branchID: String,
        paymentTo: String,
        //userID: String,
        loginUserID: Int
    ): AppState<ModelPaymentSearch> {
        return safeApiCall {
            baseApiService.doSearchPayment(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.PAYMENT_SEARCH,
                pref.get(PrefEnum.COMPANYCODE, ""), fromDate,
                toDate, status, branchID, paymentTo,
                /*userID,*/ loginUserID
            )
        }
    }

    suspend fun doExpenseEntryGet(
        tranID: String,
        loginUserID: Int
    ): AppState<ModelEditExpense> {
        return safeApiCall {
            baseApiService.doExpenseEntryGet(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.GET_EXPENSE_ENTRY,
                pref.get(PrefEnum.COMPANYCODE, ""), tranID, loginUserID
            )
        }
    }

    suspend fun getTaskType(comapanyCode: String, comboName: String): AppState<DBModelTaskType> {
        return safeApiCall {
            baseApiService.doTaskType(
                pref.get(
                    PrefEnum.COMPANYURL,
                    ""
                ) + Constants.COMBO_FILL, comapanyCode, comboName
            )
        }
    }

    suspend fun getExpenseType(comapanyCode: String): AppState<DBModelExpenseType> {
        return safeApiCall {
            baseApiService.doExpenseType(
                pref.get(
                    PrefEnum.COMPANYURL,
                    ""
                ) + Constants.COMBO_FILL, comapanyCode, "ExpenseType"
            )
        }
    }

    suspend fun getComboBranch(comapanyCode: String, comboName: String): AppState<DBModelBranch> {
        return safeApiCall {
            baseApiService.doComboBranch(
                pref.get(
                    PrefEnum.COMPANYURL,
                    ""
                ) + Constants.COMBO_FILL, comapanyCode, comboName
            )
        }
    }

    /*suspend fun getBranchFlow(
        comapanyCode: String,
        comboName: String
    ): Flow<AppState<DBModelBranch>> = flow {
        emit(safeApiCall {
            baseApiService.doComboBranch(comapanyCode, comboName)
        })
    }.flowOn(Dispatchers.IO)*/

    /*suspend fun getEmployeeFillList2(comapanyCode: String, branchId: String): AppState<ModelCombo> {
        return safeApiCall { baseApiService.doEmployeeFill(comapanyCode, branchId) }
    }*/

    suspend fun doTastEntryGet(
        companyCode: String,
        tranID: String,
        loginUserID: Int
    ): AppState<ModelEditTask> {
        return safeApiCall {
            baseApiService.doTaskEntryGet(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.GET_TASK_ENTRY,
                companyCode, tranID, loginUserID
            )
        }
    }

    suspend fun doPaymentEntryGet(
        companyCode: String,
        tranID: String,
        loginUserID: Int
    ): AppState<ModelEditPayment> {
        return safeApiCall {
            baseApiService.doPaymentEntryGet(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.GET_PAYMENT_ENTRY,
                companyCode, tranID, loginUserID
            )
        }
    }

    suspend fun doUpdateTaskStatus(
        companyCode: String,
        tranID: String,
        tranStatus: String,
        loginUserID: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doUpdateTaskStatus(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.UPDATE_TASK_STATUS,
                companyCode, tranID, tranStatus, loginUserID
            )
        })
    }.flowOn(Dispatchers.IO)

    suspend fun doUpdatePaymentStatus(
        companyCode: String,
        tranID: String,
        tranStatus: String,
        loginUserID: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doUpdatePaymentStatus(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.UPDATE_PAYMENT_STATUS,
                companyCode, tranID, tranStatus, loginUserID
            )
        })
    }.flowOn(Dispatchers.IO)

    fun getRegularFillList(comapanyCode: String, userId: String): Flow<AppState<ModelCombo>> =
        flow<AppState<ModelCombo>> {
            emit(safeApiCall {
                baseApiService.doRegularFill(
                    pref.get(PrefEnum.COMPANYURL, "") + Constants.REGULAR_EXPENSE_COMBO,
                    comapanyCode, userId
                )
            })
        }.flowOn(Dispatchers.IO)

    fun addRegularTaskApi(
        companyCode: String,
        tranId: Int,
        trandate: String,
        tranType: String,
        taskDescription: String,
        branchID: Int,
        assignTo: Int,
        userID: Int,
        image: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doAddRegularTask(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.ADD_REGULAR_TAK,
                companyCode.getRequestBody(),
                tranId.toString().getRequestBody(),
                trandate.getRequestBody(),
                tranType.getRequestBody(),
                taskDescription.getRequestBody(),
                branchID.toString().getRequestBody(),
                assignTo.toString().getRequestBody(),
                userID.toString().getRequestBody(),
                image.getImageBody("DocByte")
            )
        })
    }.flowOn(Dispatchers.IO)

    suspend fun getBalanceApi(
        companyCode: String,
        branchId: Int,
        userId: Int
    ): AppState<JsonElement> {
        return safeApiCall {
            baseApiService.doBalaceGet(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.BALANCE_GET,
                companyCode, branchId, userId
            )
        }
    }

    fun getAssosiateTask(key: Int, companyCode: String): Flow<AppState<ModelTaskAssosiate>> = flow {
        emit(
            safeApiCall {
                baseApiService.doTaskAssosiateGet(
                    pref.get(PrefEnum.COMPANYURL, "") + Constants.GET_TASK_ASSOSIATE,
                    key, companyCode
                )
            }
        )
    }.flowOn(Dispatchers.IO)

    fun doAcceptTaskStatus(
        companyCode: String,
        tranID: Int,
        tranStatus: String,
        loginUserID: Int
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doAcceptTaskStatus(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.ACCEPT_TASK,
                companyCode, tranID, tranStatus, loginUserID
            )
        })
    }.flowOn(Dispatchers.IO)

    fun doSaveAssosiateTask(
        companyCode: String,
        t2AKey: String,
        taskKey: String,
        description: String,
        progress: String,
        taskStatus: String,
        image: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doSaveAssosiateTask(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.SAVE_TASK_ASSOSIATE,
                companyCode.getRequestBody(),
                t2AKey.getRequestBody(),
                taskKey.getRequestBody(),
                description.getRequestBody(),
                progress.getRequestBody(),
                taskStatus.getRequestBody(),
                image.getImageBody("DocByte")
            )
        })
    }.flowOn(Dispatchers.IO)

    fun getRegularTaskApi(
        companyCode: String,
        branchId: Int,
        userId: Int
    ): Flow<AppState<ModelRegularTask>> = flow {
        emit(safeApiCall {
            baseApiService.doRegularTaskGet(
                pref.get(
                    PrefEnum.COMPANYURL,
                    ""
                ) + Constants.REGULAR_TASK_LIST, companyCode, branchId, userId
            )
        })
    }.flowOn(Dispatchers.IO)

    fun getRegularTaskSaveApi(
        companyCode: String,
        branchId: Int,
        userId: Int
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall {
            baseApiService.doRegularTaskSave(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.REGULAR_TASK_SAVE,
                companyCode, branchId, userId
            )
        })
    }.flowOn(Dispatchers.IO)

    suspend fun getAllEmployee(companyCode: String): AppState<DBModelEmployee> {
        return safeApiCall {
            baseApiService.doGetAllEmployee(
                pref.get(PrefEnum.COMPANYURL, "") + Constants.APP_ALL_EMPLOYEE,
                companyCode
            )
        }
    }

    /*fun getDeliveryTaskApi(
        companyCode: String,
        userId: Int
    ): Flow<AppState<ModelDeliveryTask>> = flow {
        emit(safeApiCall {
            baseApiService.doGetDeliveryTask(companyCode, userId)
        })
    }.flowOn(Dispatchers.IO)*/

    suspend fun getDeliveryTaskApi2(
        companyCode: String,
        userId: Int
    ): AppState<ModelDeliveryTask> {
        return safeApiCall { baseApiService.doGetDeliveryTask(
            pref.get(PrefEnum.COMPANYURL,"")+Constants.GET_DELIVERY_TASK,
            companyCode, userId) }
    }

}