package com.app.faizanzw.ui.postLogin.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.app.faizanzw.R
import com.app.faizanzw.database.*
import com.app.faizanzw.databinding.FragmentDeliveryEntryBinding
import com.app.faizanzw.eventModel.SendImageData
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.DeliveryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension
import com.app.faizanzw.utils.Extension.asColor
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatusCode
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.IntentParams
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class FragmentDeliveryEntry : Fragment() {

    lateinit var binding: FragmentDeliveryEntryBinding
    val viewModel by viewModels<DeliveryViewModel>()
    var taskType = ""
    var priority = ""
    var branch = ""
    var employee = ""
    var imagePath = ""
    var ENTRY_TASK = 0

    lateinit var taskList: ArrayList<DBTaskType>
    lateinit var branchList: ArrayList<DBBranch>
    lateinit var employeeList: ArrayList<DBEmployee>

    @Inject
    lateinit var pref: PreferenceModule
    lateinit var byteArr: ByteArray

    lateinit var deliveryData: DBDeliveryTasK

    companion object {
        val TAG = FragmentDeliveryEntry::class.java.name
        fun newInstance(
            formType: Int,
            branchId: Int
        ): FragmentDeliveryEntry = FragmentDeliveryEntry().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.FORM_TYPE, formType)
                putInt(IntentParams.BRANCH_ID, branchId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeliveryEntryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.FORM_TYPE, 0) != 0) {
            ENTRY_TASK = requireArguments().getInt(IntentParams.FORM_TYPE, 0)
            branch = requireArguments().getInt(IntentParams.BRANCH_ID, 0).toString()
        }

        binding.toolbar.txtTitle.text = resources.getString(R.string.delivery_task)
        //binding.llUploadImage.isVisible = false
        clickEvents()

        lifecycleScope.launch {
            delay(750)
            viewModel.getMergeData(
                ENTRY_TASK.toString(),
            )
        }
        obserViewModel()
    }

    private fun obserViewModel() {
        viewModel.taskList.observe(requireActivity(),
            {
                when (it) {
                    is AppState.Loading -> {
                        isLoading(true)
                    }
                    is AppState.Success -> {
                        isLoading(false)
                        if (it.model.success && it.model.status == 200) {
                            taskList = ArrayList<DBTaskType>().apply {
                                add(DBTaskType(0, "Select Task Type"))
                            }
                            taskList.addAll(it.model.data)
                        } else {
                            requireActivity().errorSnackBar(it.model.message)
                        }
                    }
                    is AppState.Error -> {
                        isLoading(false)
                        requireActivity().errorSnackBar(it.error)
                    }
                }
            })

        viewModel.branchList.observe(requireActivity(),
            {
                when (it) {
                    is AppState.Loading -> {
                        isLoading(true)
                    }
                    is AppState.Success -> {
                        isLoading(false)
                        if (it.model.success && it.model.status == 200) {
                            branchList = ArrayList<DBBranch>().apply {
                                add(DBBranch(0, "Select Branch"))
                            }
                            branchList.addAll(it.model.data)
                        } else {
                            requireActivity().errorSnackBar(it.model.message)
                        }
                    }
                    is AppState.Error -> {
                        isLoading(false)
                        requireActivity().errorSnackBar(it.error)
                    }
                }
            })

        viewModel.employeeList.observe(requireActivity(),
            {
                when (it) {
                    is AppState.Loading -> {
                        isLoading(true)
                    }
                    is AppState.Success -> {
                        isLoading(false)
                        if (it.model.success && it.model.status == 200) {
                            employeeList = ArrayList<DBEmployee>().apply {
                                add(DBEmployee(0, 0, "Select Employee"))
                            }
                            employeeList.addAll(it.model.data)
                        } else {
                            requireActivity().errorSnackBar(it.model.message)
                        }
                    }
                    is AppState.Error -> {
                        isLoading(false)
                        requireActivity().errorSnackBar(it.error)
                    }
                }
            })

        viewModel.stateAddTask.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatusCode() == 200) {
                        requireActivity().successSnackBar(it.model.getMessage())
                        upDataDatabase(deliveryData, 1)

                        lifecycleScope.launch {
                            delay(1800)
                            requireActivity().onBackPressed()
                        }
                    } else {
                        requireActivity().errorSnackBar(it.model.getMessage())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                }
            }
        })

        viewModel.taskData.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    Log.e("ddddddd : ", it.model.assignToName + " nn ")
                    deliveryData = it.model
                    setDataToView(it.model)
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                }
            }
        })
    }

    private fun passDataToSearchList(data: DBDeliveryTasK) {
        if (ENTRY_TASK != 0) {
            EventBus.getDefault().post(
                data
            )
        }
    }

    var webURL = ""
    private fun setDataToView(data: DBDeliveryTasK) {
        binding.lableUi.edtExpenseNo.setTitle(data.tranNo)
        Log.e("parse data : ", "${data.trandate} nn")
        binding.lableUi.edtDate.setTitle(data.trandate.convertTodate())
        binding.lableUi.edtSubject.setTitle(data.subject)
        binding.lableUi.spnTaskType.setTitle(data.tranTypeName)
        binding.lableUi.txtPriority.setTitle(data.taskPriority)
        binding.lableUi.txtStatus.setTitle(data.tranStatus)
        webURL = data.webView
        if (!webURL.isNullOrEmpty())
        {
            binding.lableUi.txtWebView.edtTitle?.setTextColor(Color.BLUE)
        }else{
            binding.lableUi.txtWebView.setTitle("")
        }
        binding.lableUi.txtRemarks.setTitle(data.taskDescription)
        branch = data.branchID.toString()
        employee = data.assignTo.toString()

        if (!data.docByte.isNullOrEmpty()) {
            byteArr = Base64.decode(data.docByte, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            binding.imgView.setImageBitmap(decodedImage)
            binding.imgViewFix.setImageBitmap(decodedImage)
        }
        binding.txtUserName.text = deliveryData.assignToName

        /*if (data.tranStatus.equals("COMPLETED", true))
            disableAllViews()*/
    }

    private fun disableAllViews() {
        binding.cardEditable.isVisible = false
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.btnSignIn.setOnClickListener {

            if (binding.edtRemarks.getTitle().toString().trim().isEmpty()) {
                requireActivity().errorSnackBar("Please enter remarks")
                return@setOnClickListener
            }

            if (requireActivity().isNetworkConnected()) {
                viewModel.addTask(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    binding.edtRemarks.getTitle(),
                    getImageInEditMode(),
                    ENTRY_TASK.toString()
                )
            } else {
                upDataDatabase(deliveryData, 0)
                requireActivity().successSnackBar("Data Saved in Local")
                lifecycleScope.launch {
                    delay(1500)
                    requireActivity().onBackPressed()
                }
            }
        }
        binding.llUploadImage.setOnClickListener {
            (requireActivity() as DashBoardActivity).attachmentPicker.showDialogForCameraAndGallery()
        }
        binding.root.setOnClickListener { }

        binding.lableUi.txtWebView.debouncedOnClick {
            if (!webURL.isNullOrEmpty()) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(webURL)
                startActivity(i)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMediaImageReceived(data: SendImageData) {
        imagePath = data.image
        binding.imgView.load(data.image)
        binding.txtImageError.setText("Upload Image")
        binding.txtImageError.setTextColor(R.color.color_primary.asColor(requireContext()))
    }

    private val getImageFile by lazy {
        Extension.saveFile(
            requireActivity(),
            byteArr,
            "${System.currentTimeMillis()}_savedFile.jpg"
        )
    }

    private fun getImageInEditMode(): String {
        var image = ""
        if (imagePath.isEmpty()) {
            if (this@FragmentDeliveryEntry::byteArr.isInitialized)
                if (byteArr.size > 0)
                    image = getImageFile
        } else {
            image = imagePath
        }
        return image
    }

    private fun upDataDatabase(data: DBDeliveryTasK, online: Int) {
        val deliveryData = data
        deliveryData.taskDescription = binding.edtRemarks.getTitle()
        if (online == 0 && imagePath.isNotEmpty()) {
            val bm = BitmapFactory.decodeFile(imagePath)
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos) // bm is the bitmap object

            val b: ByteArray = baos.toByteArray()
            val base64 = Base64.encodeToString(b,Base64.DEFAULT)
            deliveryData.docByte = base64
        }
        deliveryData.isOnline = online
        deliveryData.tranStatus = "COMPLETED"
        DataBaseUtils.updateDelivery(deliveryData)
        passDataToSearchList(deliveryData)
    }
}