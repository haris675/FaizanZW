package com.app.faizanzw.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.faizanzw.R
import com.app.faizanzw.database.DBDeliveryTasK
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.network.ApiClient
import com.app.faizanzw.network.OnApiResponseListener
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.utils.Extension.getStatusCode
import com.google.gson.JsonElement
import java.io.File
import java.io.FileOutputStream

class BackupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Perform your long-running backup operation here.
        //createForegroundInfo(applicationContext)
        Log.e("upload data bbb : ", "Uploaded Data")
        uploadDataToServer()
        // Return SUCCESS if the task was successful.
        // Return FAILURE if the task failed and you don’t want to retry it.
        // Return RETRY if the task failed and you want to retry it.

        return success()
    }

    var count = 0
    var apiCount = 0
    var isFirst = false

    private fun uploadDataToServer() {
        val pref = PreferenceModule(this.applicationContext)
        if (pref.get(PrefEnum.ISLOGIN, 0) == 1) {
            val dataList = DataBaseUtils.getOfflineDeliveries()
            dataList?.let {
                if (it.size == 0) {
                    isFirst = false
                    count = 0
                    apiCount = 0
                    DataBaseUtils.deleteOnlineDelivery()
                    showNotification("Sync Complete", "All deliveries data synded")
                    return
                }
                if (!isFirst) {
                    count = it.size
                }

                val deliveryData = it[0]
                apiCount++
                addTask(
                    pref.get(PrefEnum.COMPANYURL,"")+Constants.SAVE_TASK_ASSOSIATE,
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    deliveryData
                )
            }
        }
    }

    fun saveFile(data: ByteArray?, fileName: String?): String {
        val destinyDir = applicationContext.cacheDir
        if (!destinyDir.exists() && !destinyDir.mkdirs()) {
            return ""
        }
        val mainPicture: File? = File(destinyDir, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mainPicture)
            fos.write(data)
            fos.close()
            if (mainPicture != null)
                return mainPicture.absolutePath
            return ""
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun addTask(
        url:String,
        companyCode: String,
        data: DBDeliveryTasK
    ) {
        var docType = ""
        if (data.docByte != null) {
            val byteArr = Base64.decode(data.docByte, Base64.DEFAULT)
            docType = saveFile(byteArr, "${System.currentTimeMillis()}_savedFile.jpg")
        }

        ApiClient.updateDeliveryTask(
            url,
            companyCode,
            data.tranID,
            data.taskDescription,
            docType,
            object : OnApiResponseListener<JsonElement> {
                override fun onResponseComplete(clsGson: JsonElement?, requestCode: Int) {
                    clsGson?.let {
                        Log.e("uploaded response : ", "${it}")
                    }
                    if (clsGson?.getStatusCode() == 200) {
                        //DataBaseUtils.deleteDeliveryData(data.tranID.toString())
                        DataBaseUtils.updateDeliveriesToOnline(data)
                        uploadDataToServer()
                    }
                }

                override fun onResponseError(
                    errorMessage: String?,
                    requestCode: Int,
                    responseCode: Int
                ) {
                    Log.e("errpr : ", "${errorMessage} nn ")
                }
            })
    }

    private fun showNotification(title: String, description: String) {

        val notificationManager =
            this.applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var intent = Intent(applicationContext, DashBoardActivity::class.java)
        val builder: NotificationCompat.Builder

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("101", "101")
            var mChannel = notificationManager.getNotificationChannel("101")
            builder = NotificationCompat.Builder(applicationContext, "101")
            builder.setContentTitle(title)
                .setChannelId(mChannel.id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(description)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        } else {
            builder = NotificationCompat.Builder(applicationContext)
            builder.setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(description)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        }
        val notification: Notification = builder.build()
        notificationManager.notify(101, notification)
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_HIGH
        ).also { channel ->
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}