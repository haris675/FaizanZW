package com.app.faizanzw.utils

import com.app.faizanzw.database.*

object CommonFunctions {

       suspend fun getBranchData(): AppState<DBModelBranch> {
        val result = DataBaseUtils.getAllBranch()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Branch Found")
            } else {
                return AppState.Success(DBModelBranch(result, true, "", 200))
            }
        }
        return AppState.Error("No Branch Found")
    }

     suspend fun getExpenseData(): AppState<DBModelExpenseType> {
        val result = DataBaseUtils.getAllExpenseType()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Expense Found")
            } else {
                return AppState.Success(DBModelExpenseType(result, true, "", 200))
            }
        }
        return AppState.Error("No Expense Found")
    }

    suspend fun getTaskTypeData(): AppState<DBModelTaskType> {
        val result = DataBaseUtils.getAllTaskType()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Task Type Found")
            } else {
                return AppState.Success(DBModelTaskType(result, true, "", 200))
            }
        }
        return AppState.Error("No Task Type Found")
    }

    fun extractTaskTypeData(data: AppState<DBModelTaskType>): ArrayList<DBTaskType>? {
        var expenseData: ArrayList<DBTaskType>? = null
        when (data) {
            is AppState.Success -> {
                if (data.model.status == 200)
                    expenseData = data.model.data
                else
                    expenseData = null
            }
            is AppState.Error -> {
                expenseData = null
            }
            is AppState.Loading -> {
                expenseData = null
            }
        }
        return expenseData
    }

    fun extractEmployeeData(data: AppState<DBModelEmployee>): ArrayList<DBEmployee>? {
        var expenseData: ArrayList<DBEmployee>? = null
        when (data) {
            is AppState.Success -> {
                if (data.model.status == 200)
                    expenseData = data.model.data
                else
                    expenseData = null
            }
            is AppState.Error -> {
                expenseData = null
            }
            is AppState.Loading -> {
                expenseData = null
            }
        }
        return expenseData
    }

    fun extractDeliveryData(data: AppState<ModelDeliveryTask>): ArrayList<DBDeliveryTasK>? {
        var expenseData: ArrayList<DBDeliveryTasK>? = null
        when (data) {
            is AppState.Success -> {
                if (data.model.status == 200)
                    expenseData = data.model.data
                else
                    expenseData = null
            }
            is AppState.Error -> {
                expenseData = null
            }
            is AppState.Loading -> {
                expenseData = null
            }
        }
        return expenseData
    }

    suspend fun getEmployeeDataByBranch(branchId:Int): AppState<DBModelEmployee> {
        val result = DataBaseUtils.getEmployeeByBranch(branchId)
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Employee Found")
            } else {
                return AppState.Success(DBModelEmployee(result, true, "", 200))
            }
        }
        return AppState.Error("No Employee Found")
    }

    suspend fun getDeliveryData(): AppState<ModelDeliveryTask> {
        val result = DataBaseUtils.getAllDeliveries()
        result?.let {
            if (it.size == 0) {
                return AppState.Error("No Employee Found")
            } else {
                return AppState.Success(ModelDeliveryTask(result, true, "", 200))
            }
        }
        return AppState.Error("No Employee Found")
    }

    suspend fun getDeliveryTask(id:String): AppState<DBDeliveryTasK> {
        val result = DataBaseUtils.getDeliveriesById(id)
        result?.let {
            return AppState.Success(it)
        }
        return AppState.Error("No Employee Found")
    }

}