package com.app.faizanzw.database

import android.util.Log
import io.realm.Realm
import io.realm.kotlin.delete

interface DatabaseDeletedListener {
    fun onDataBaseDelete()
}

object DataBaseUtils {

    fun addOrUpdateBranch(
        item: ArrayList<DBBranch>
    ) {
        /*if (uniqueId == 0) {
            val maxId: Number? = realm.where(DBBranch::class.java).max("comboID")
            if (maxId == null) {
                item.comboID = 1
            }
        } */


        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun addOrUpdateExpense(
        item: ArrayList<DBExpenseType>
    ) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun addOrUpdateDeliveries(
        item: ArrayList<DBDeliveryTasK>
    ) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            for (i in item) {
                Log.e("count : ", "count + ${i.tranID}")
                val result =
                    realm.where(DBDeliveryTasK::class.java).equalTo("tranID", i.tranID.toLong())
                        .and().equalTo("isOnline", "0".toLong()).findFirst()
                Log.e("aaaa : ", "${result?.tranID} nn ")
                if (result == null) {
                    realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(i) })
                }
            }
        } finally {
            realm?.close()
        }
    }

    fun updateDelivery(
        item: DBDeliveryTasK
    ) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun getDeliveriesById(id: String): DBDeliveryTasK? {
        var dataList: DBDeliveryTasK? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result = realm.where(DBDeliveryTasK::class.java).equalTo("tranID", id.toLong())
                    .findFirst()
                Log.e("aaaa : ", result?.subject + " nn ")
                if (result != null)
                    dataList = realm.copyFromRealm(result)
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getOfflineDeliveries(): ArrayList<DBDeliveryTasK>? {
        var dataList: ArrayList<DBDeliveryTasK>? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result =
                    realm.where(DBDeliveryTasK::class.java).equalTo("isOnline", "0".toLong())
                        .findAll()
                Log.e("aaaa : ", result.asJSON() + " nn ")
                dataList = realm.copyFromRealm(result) as ArrayList<DBDeliveryTasK>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun updateDeliveriesToOnline(
        item: DBDeliveryTasK
    ) {
        var realm: Realm? = null
        item.tranStatus = "COMPLETED"
        item.isOnline = 1
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun addOrUpdateTaskType(
        item: ArrayList<DBTaskType>
    ) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun addOrUpdateEmployee(item: ArrayList<DBEmployee>) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm -> realm.insertOrUpdate(item) })
        } finally {
            realm?.close()
        }
    }

    fun getAllBranch(): ArrayList<DBBranch>? {
        var dataList: ArrayList<DBBranch>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result = realm.where(DBBranch::class.java).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBBranch>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getAllExpenseType(): ArrayList<DBExpenseType>? {
        var dataList: ArrayList<DBExpenseType>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result = realm.where(DBExpenseType::class.java).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBExpenseType>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getAllTaskType(): ArrayList<DBTaskType>? {
        var dataList: ArrayList<DBTaskType>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result = realm.where(DBTaskType::class.java).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBTaskType>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getEmployeeByBranch(branchID: Int): ArrayList<DBEmployee>? {
        var dataList: ArrayList<DBEmployee>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result =
                    realm.where(DBEmployee::class.java).equalTo("branchID", branchID).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBEmployee>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getAllEmployee(): ArrayList<DBEmployee>? {
        var dataList: ArrayList<DBEmployee>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result =
                    realm.where(DBEmployee::class.java).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBEmployee>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getAllDeliveries(): ArrayList<DBDeliveryTasK>? {
        var dataList: ArrayList<DBDeliveryTasK>? = null
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result =
                    realm.where(DBDeliveryTasK::class.java).findAll()
                dataList = realm.copyFromRealm(result) as ArrayList<DBDeliveryTasK>
            })
        } finally {
            realm?.close()
        }
        return dataList
    }

    fun getLocalDeliveriesCount(): Int {
        var count = 0
        var realm: Realm? = null
        try { // I could use try-with-resources here
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm ->
                val result =
                    realm.where(DBDeliveryTasK::class.java).count()
                count = result.toInt()
            })
        } finally {
            realm?.close()
        }
        return count
    }

    fun deleteAllData(
        listener: DatabaseDeletedListener
    ) {

        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.executeTransaction(Realm.Transaction { realm2 ->
                realm2.delete(DBBranch::class.java)
                realm2.delete(DBEmployee::class.java)
                realm2.delete(DBExpenseType::class.java)
                realm2.delete(DBTaskType::class.java)
            })
        } finally {
            realm?.close()
            listener.onDataBaseDelete()
        }
    }

    fun deleteDeliveryData(id: String
    ) {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val data = realm.where(DBDeliveryTasK::class.java).equalTo("tranID",id.toLong()).findAll()
            realm.executeTransaction(Realm.Transaction { realm2 ->
                data.deleteAllFromRealm()
            })
        } finally {
            realm?.close()
        }
    }

    fun deleteOnlineDelivery() {
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val data = realm.where(DBDeliveryTasK::class.java).equalTo("tranStatus", "COMPLETED")
                .findAll()
            realm.executeTransaction(Realm.Transaction { realm2 ->
                data.deleteAllFromRealm()
            })
        } finally {
            realm?.close()
        }
    }

    /*fun addOrUpdateCuisineItems2(
        uniqueId: Int,
        quantity: Int,
        items: DBCuisineData
    ) {

        var realm = Realm.getDefaultInstance()

        if (uniqueId == 0) {
            val maxId: Number? = realm.where(DBCuisineData::class.java).max("primary_id")
            if (maxId == null) {
                items.primary_id = 1
            } else {
                items.primary_id = maxId.toInt() + 1
            }
        } else {
            items.primary_id = uniqueId
        }

        items.quantity = quantity

        realm.executeTransaction {
            it.copyToRealmOrUpdate(items)
        }
    }


    fun addOrUpdateRepeatedItems(
        uniqueId: Int,
        items: DBCuisineData,
        realm: Realm
    ) {

        if (uniqueId == 0) {
            val maxId: Number? = realm.where(DBCuisineData::class.java).max("primary_id")
            if (maxId == null) {
                items.primary_id = 1
            } else {
                items.primary_id = maxId.toInt() + 1
            }
        } else {
            items.primary_id = uniqueId
        }

        realm.executeTransaction {
            it.copyToRealmOrUpdate(items)
        }
    }

    fun updateQuantity(uniqueId: Int, realm: Realm, mRestaurantCuisineItemId: Int) {
        val result = realm.where(DBCuisineData::class.java)
            .equalTo("restaurantCuisineItemId", mRestaurantCuisineItemId)
            .findAll()

        var items: DBCuisineData? = null
        if (result != null) {
            items = result.last()
            realm.executeTransaction {
                if (items != null) {
                    val quantity = items.quantity!! + 1
                    items.quantity = quantity
                    it.copyToRealmOrUpdate(items)
                }
            }
        }
    }

    fun updateParticularItemQuantity(uniqueId: Int, realm: Realm) {
        val result =
            realm.where(DBCuisineData::class.java).equalTo("primary_id", uniqueId)
                .findFirst()

        realm.executeTransaction {
            if (result != null) {
                val quantity = result.quantity + 1
                result.quantity = quantity
                it.copyToRealmOrUpdate(result)
            }
        }
    }

    fun removeQuantity(uniqueId: Int, realm: Realm) {

        val result =
            realm.where(DBCuisineData::class.java)
                .equalTo("restaurantCuisineItemId", uniqueId)
                .findAll()

        Log.e("ddd : ", result.asJSON())


        if (result != null) {
            var items: DBCuisineData? = null
            if (result.size > 1)
                items = result.last()
            else
                items = result.first()

            if (items != null) {
                realm.executeTransaction {
                    if (items.quantity > 1) {
                        val quantity = items.quantity - 1
                        items.quantity = quantity
                        it.copyToRealmOrUpdate(items)
                    } else {
                        for (i in items.addon_groups!!)
                            i.groupItems!!.deleteAllFromRealm()

                        items.addon_groups!!.deleteAllFromRealm()
                        items.deleteFromRealm()
                    }
                }
            }
        }
    }


    fun removeFromCart(uniqueId: Int, realm: Realm) {

        val result =
            realm.where(DBCuisineData::class.java)
                .equalTo("primary_id", uniqueId)
                .findAll()

        Log.e("ddd : ", result.asJSON())


        if (result != null) {
            var items: DBCuisineData? = null
            if (result.size > 1)
                items = result.last()
            else
                items = result.first()

            if (items != null) {
                realm.executeTransaction {
                    if (items.quantity > 1) {
                        val quantity = items.quantity - 1
                        items.quantity = quantity
                        it.copyToRealmOrUpdate(items)
                    } else {
                        for (i in items.addon_groups!!)
                            i.groupItems!!.deleteAllFromRealm()

                        items.addon_groups!!.deleteAllFromRealm()
                        items.deleteFromRealm()
                    }
                }
            }
        }
    }


    fun checkIfRestaurantItemAdded(
        realm: Realm, restaurantId: Int, uniqueId: Int,
        quantity: Int,
        items: DBCuisineData
    ) {

        val allResult = realm.where(DBCuisineData::class.java).findAll()

        if (allResult.size == 0) {
            addOrUpdateCuisineItems2(uniqueId, quantity, items)
        } else {
            val result =
                realm.where(DBCuisineData::class.java).equalTo("restaurantId", restaurantId)
                    .findAll()
            if (result.size != 0) {
                addOrUpdateCuisineItems2(uniqueId, quantity, items)
            } else {
                val realm1 = Realm.getDefaultInstance()
                try {
                    realm1.executeTransactionAsync { realm1 ->
                        realm1.deleteAll()
                        addOrUpdateCuisineItems2(uniqueId, quantity, items)
                    }
                } finally {
                    realm1.close()
                }
            }
        }
    }

    fun deleteAllData(
        realm: Realm,
        listener: DatabaseDeletedListener
    ) {
        realm.beginTransaction()
        val result =
            realm.where(DBCuisineData::class.java)
                .findAll()

        for (i in result) {
            for (j in i.addon_groups!!) {
                j.groupItems!!.deleteAllFromRealm()
            }
            i.addon_groups!!.deleteAllFromRealm()
            i.deleteFromRealm()
        }
        realm.commitTransaction()
        listener.onDataDeletedFromRealm()
    }

    fun removeRestaurentCart(uniqueId: Int, realm: Realm) {
        val result =
            realm.where(DBCuisineData::class.java)
                .equalTo("restaurantId", uniqueId)
                .findAll()
        Log.e("ddd : ", result.asJSON())

        realm.executeTransaction {
            for (i in result) {
                for (j in i.addon_groups!!) {
                    j.groupItems!!.deleteAllFromRealm()
                }
                i.addon_groups!!.deleteAllFromRealm()
                i.deleteFromRealm()
            }
        }
    }

    fun removeRestaurentCart2(uniqueId: Int, realm: Realm) {
        val result =
            realm.where(DBCuisineData::class.java)
                .findAll()

        realm.executeTransaction {
            for (i in result) {
                for (j in i.addon_groups!!) {
                    j.groupItems!!.deleteAllFromRealm()
                }
                i.addon_groups!!.deleteAllFromRealm()
                i.deleteFromRealm()
            }
        }
    }*/
}