package com.app.faizanzw.ui.postLogin.fragments

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.faizanzw.R
import com.app.faizanzw.database.DBDeliveryTasK
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.databinding.FragmentUnsyncBinding
import com.app.faizanzw.network.ApiClient
import com.app.faizanzw.network.OnApiResponseListener
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.utils.Constants
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getStatusCode
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.PrefEnum
import com.google.gson.JsonElement
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class FragmentUnSync : Fragment() {

    lateinit var binding: FragmentUnsyncBinding

    @Inject
    lateinit var pref: PreferenceModule

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUnsyncBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.txtTitle.text = "UnSync Data"

        getCount()

        binding.btnSync.setOnClickListener {
            if (requireActivity().isNetworkConnected()) {

                val list2 = DataBaseUtils.getOfflineDeliveries()

                if (!list2.isNullOrEmpty()) {
                    uploadDataToServer()
                } else {
                    requireActivity().successSnackBar("All Data is Synced With Server")
                }
            } else {
                requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
            }
        }

        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    private fun getCount() {
        val UnSynclist = DataBaseUtils.getOfflineDeliveries()

        if (!UnSynclist.isNullOrEmpty()) {
            binding.txtCount.text = "${UnSynclist.size}"
        } else {
            binding.txtCount.text = "0"
        }

        val syncList = DataBaseUtils.getAllDeliveries()
        if (!syncList.isNullOrEmpty()) {
            binding.txtTotalCount.text = "${syncList.size}"
        } else {
            binding.txtTotalCount.text = "0"
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun uploadDataToServer() {
        isLoading(true)
        if (pref.get(PrefEnum.ISLOGIN, 0) == 1) {
            val dataList = DataBaseUtils.getOfflineDeliveries()
            dataList?.let {
                if (it.size == 0) {
                    isLoading(false)
                    DataBaseUtils.deleteOnlineDelivery()
                    requireActivity().successSnackBar("All Data is Synced With Server\"")
                    getCount()
                    return
                }

                val deliveryData = it[0]
                addTask(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    deliveryData
                )
            }
        }
    }

    fun saveFile(data: ByteArray?, fileName: String?): String {
        val destinyDir = requireActivity().cacheDir
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
        companyCode: String,
        data: DBDeliveryTasK
    ) {
        var docType = ""
        if (data.docByte != null) {
            val byteArr = Base64.decode(data.docByte, Base64.DEFAULT)
            docType = saveFile(byteArr, "${System.currentTimeMillis()}_savedFile.jpg")
        }

        ApiClient.updateDeliveryTask(
            pref.get(PrefEnum.COMPANYURL, "") + Constants.SAVE_TASK_ASSOSIATE,
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
                        DataBaseUtils.updateDeliveriesToOnline(data)
                        uploadDataToServer()
                    } else {
                        isLoading(false)
                    }
                }

                override fun onResponseError(
                    errorMessage: String?,
                    requestCode: Int,
                    responseCode: Int
                ) {
                    isLoading(false)
                    requireActivity().errorSnackBar(errorMessage + "")
                    Log.e("errpr : ", "${errorMessage} nn ")
                }
            })
    }

}