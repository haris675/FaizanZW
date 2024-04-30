package com.app.faizanzw.ui.postLogin.fragments.edit

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
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.app.faizanzw.R
import com.app.faizanzw.databinding.FrgmentEditAssosiateBinding
import com.app.faizanzw.eventModel.SendImageData
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.adapter.TaskProgressAdapter
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.model.TaskItem
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.TaskEntryViewModel
import com.app.faizanzw.utils.*
import com.app.faizanzw.utils.Extension.asColor
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.getStatusCode
import com.app.faizanzw.utils.Extension.successSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class FragmentEditAssosiate : Fragment() {

    lateinit var binding: FrgmentEditAssosiateBinding
    val viewModel by viewModels<TaskEntryViewModel>()
    var taskType = ""
    var priority = ""
    var branch = ""
    var employee = ""
    var imagePath = ""
    var ENTRY_TASK = 0

    //lateinit var taskList: ArrayList<DataItem>
    //lateinit var branchList: ArrayList<DataItem>
    //lateinit var employeeList: ArrayList<DataItem>

    @Inject
    lateinit var pref: PreferenceModule

    lateinit var byteArr: ByteArray

    lateinit var assosiateList: ArrayList<AssosiateData>
    var assigneeName = ""
    var isAssignedToYou = false
    var decodeImage: Bitmap? = null

    companion object {
        fun newInstance(
            formType: Int,
            branchId: Int,
            isAssigned: Boolean
        ): FragmentEditAssosiate = FragmentEditAssosiate().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.FORM_TYPE, formType)
                putInt(IntentParams.BRANCH_ID, branchId)
                putBoolean(IntentParams.ISASSIGNED, isAssigned)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FrgmentEditAssosiateBinding.inflate(layoutInflater)
        return binding.root
    }

    lateinit var taskAdapter: TaskProgressAdapter

    var clickPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.FORM_TYPE, 0) != 0) {
            ENTRY_TASK = requireArguments().getInt(IntentParams.FORM_TYPE, 0)
            branch = requireArguments().getInt(IntentParams.BRANCH_ID, 0).toString()
        }

        binding.toolbar.txtTitle.text = "Assigned Task"
        clickEvents()
        setupAllSpinnersAdapter()
        //binding.edtRemarks.setTextWatcher(binding.tilRemarks)

        lifecycleScope.launch {
            delay(750)
            binding.progressLayout.visibility = View.VISIBLE
            viewModel.getMergeData(
                pref.get(PrefEnum.COMPANYCODE, "").toString(),
                ENTRY_TASK.toString(),
                pref.get(PrefEnum.USERID, 0),
                true
            )
        }

        taskAdapter = TaskProgressAdapter(requireContext(), ArrayList(), true, { position, data ->
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentSubTask.newInstance(data.t2AKey),
                "SUBTASK"
            )
        })

        binding.rvProgressList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        obserViewModel()
        //disableAllViews()

    }

    private fun obserViewModel() {

        viewModel.acceptTask.observe(this)
        {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatusCode() == 200) {
                        Log.e("Accept Success: ", "Accept Success")
                        requireActivity().successSnackBar(it.model.getMessage())
                        lifecycleScope.launch {
                            delay(750)
                            viewModel.getMergeData(
                                pref.get(PrefEnum.COMPANYCODE, "").toString(),
                                ENTRY_TASK.toString(),
                                pref.get(PrefEnum.USERID, 0),
                                true
                            )
                        }

                    } else {
                        requireActivity().equals(it.model.getMessage())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                }
            }
        }

        viewModel.stateAddTask.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatus()) {
                        requireActivity().successSnackBar(it.model.getMessage())
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
                    if (it.model.status == 200) {
                        setDataToView(it.model.data)
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

        viewModel.assosiateDate.observe(this) {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    setDataforSave(it.model.getMessage())
                }
                is AppState.Error -> {
                    isLoading(false)
                }
            }
        }
    }

    private fun setDataforSave(message: String) {
        requireActivity().successSnackBar(message)
        binding.imgView.isVisible = false
        imagePath = ""
        binding.edtRemarks.setTitle("")
        binding.rangeSlider.setValues(0.0f)
        binding.spnTaskStatus.setSelection(1)
        progressValues = 0

        viewModel.getMergeData(
            pref.get(PrefEnum.COMPANYCODE, "").toString(),
            ENTRY_TASK.toString(),
            pref.get(PrefEnum.USERID, 0),
            true
        )

    }

    private var webUrl = ""
    private fun setDataToView(data: TaskItem) {
        //binding.lableUi.edtExpenseNo.isVisible = true
        //binding.lableUi.edtDate.isVisible = true
        binding.lableUi.edtExpenseNo.setTitle(data.tranNo)
        binding.lableUi.edtDate.setTitle(data.trandate.convertTodate())
        binding.lableUi.edtSubject.setTitle(data.subject)
        binding.btnSignIn.isVisible = data.IsAllowToChange
        binding.lableUi.spnTaskType.setTitle(data.tranTypeName)
        binding.lableUi.txtPriority.setTitle(data.taskPriority)
        binding.lableUi.txtStatus.setTitle(data.tranStatus)

        //binding.spnTaskStatus.setSelection(statusList.findStringListPosition(data.tranStatus))

        //binding.edtRemarks.setTitle(data.taskDescription)
        branch = data.BranchID
        employee = data.AssignTo

        binding.progress.progress = data.taskPercentage
        binding.txtPercentage.text = data.taskPercentage.toString() + "%"
        //binding.txtUserName.text = "Assigned To: " + data.assignToName
        binding.lableUi.txtRemarks.setTitle(data.taskDescription)
        assigneeName = data.assignToName

        if (!data.DocByte.isNullOrEmpty()) {
            val byteArr = Base64.decode(data.DocByte, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            binding.imgViewFix.setImageBitmap(decodedImage)
        }

        var isAssigned = true
        //if (data.IsAllowToChange) {
        if (data.tranStatus.equals("NEW")) {
            binding.lableUi.btnAccept.isVisible = true
            binding.btnSignIn.isVisible = false
            binding.cardEditable.isVisible = false
        } else if (data.tranStatus.equals(Constants.COMPLETED)) {
            binding.lableUi.btnAccept.isVisible = false
            binding.btnSignIn.isVisible = false
            binding.cardEditable.isVisible = false
            isAssigned = false
        } else {
            binding.cardEditable.isVisible = true
            binding.btnSignIn.isVisible = true
            binding.lableUi.btnAccept.isVisible = false
        }
        /*}else{
            binding.btnSignIn.isVisible = true
            binding.btnAccept.isVisible = false
            binding.cardEditable.isVisible = false
            isAssigned = false
        }*/

        assosiateList = data.lstAssociate!!
        isAssignedToYou = data.AssignTo.toInt() == pref.get(PrefEnum.USERID, 0)
        taskAdapter.setData(assosiateList, isAssigned)
        webUrl = data.WebView
        binding.lableUi.txtWebView.edtTitle?.setTextColor(Color.BLUE)
        //disableAllViews()
    }

    /*private fun disableAllViews() {
        binding.spnTaskType.disable()
        binding.txtPriority.disable()
        binding.edtSubject.disable()
        binding.edtExpenseNo.disable()
        binding.edtDate.disable()
        binding.txtStatus.disable()
        binding.txtRemarks.disable()
    }*/

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    var progressValues = 0
    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.lableUi.btnAccept.debouncedOnClick {
            viewModel.callAcceptTask(
                pref.get(PrefEnum.COMPANYCODE, ""),
                ENTRY_TASK,
                "INPROGRESS",
                pref.get(PrefEnum.USERID, 0)
            )
        }

        binding.btnSignIn.debouncedOnClick {
            if (validate()) {
                viewModel.saveAssosiate(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    "0", ENTRY_TASK.toString(), binding.edtRemarks.getTitle(),
                    progressValues.toString(), status, getImageInEditMode()
                )
            }
        }
        binding.llUploadImage.setOnClickListener {
            (requireActivity() as DashBoardActivity).attachmentPicker.showDialogForCameraAndGallery()
        }

        binding.root.setOnClickListener { }

        binding.rangeSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            binding.txtRange.text = value.toInt().toString() + "%"
            progressValues = value.toInt()
        }

        binding.lableUi.txtWebView.debouncedOnClick {
            if (!webUrl.isNullOrEmpty()) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(webUrl)
                startActivity(i)
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        /*if (binding.edtSubject.getTitle().isEmpty()) {
            binding.edtSubject.setError("Please entr Subject")
            isValid = false
        }

        if (taskType.isEmpty()) {
            binding.spnTaskType.setError("Please select Task Type")
            isValid = false
        }
        if (priority.isEmpty()) {
            binding.spnTaskPrioroty.setError("Please select priority")
            isValid = false
        }
        if (branch.isEmpty()) {
            binding.spnBranch.setError("Please Select Branch")
            isValid = false
        }

        if (employee.isEmpty()) {
            binding.spnEmpolyee.setError("Please select Employee")
            isValid = false
        }*/


        /*if (getImageInEditMode().isEmpty()) {
            binding.txtImageError.text = "Please Upload Image"
            binding.txtImageError.setTextColor(R.color.light_red.asColor(requireContext()))
            isValid = false
        }*/
        if (binding.edtRemarks.getTitle().trim().isEmpty()) {
            binding.edtRemarks.setError("Please enter Remarks")
            isValid = false
        }
        return isValid
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
        binding.imgView.isVisible = true
        imagePath = data.image
        binding.imgView.load(data.image)
        binding.txtImageError.setText("Upload Image")
        binding.txtImageError.setTextColor(R.color.color_primary.asColor(requireContext()))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateListItem(data: AssosiateData) {
        taskAdapter.updateItem(clickPosition, data)
    }

    val statusList by lazy {
        ArrayList<String>(
            resources.getStringArray(R.array.arr_edit_task_status).asList()
        )
    }


    var status = ""
    private fun setupAllSpinnersAdapter() {
        binding.spnTaskStatus.initAdapter(statusList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val model = parent?.getItemAtPosition(position) as String
                model.let {
                    Log.e("selected payee : ", "${model}")
                    status = it
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
    }

    private val getImageFile by lazy {
        Extension.saveFile(requireActivity(), byteArr, "savedFile.jpg")
    }

    private fun getImageInEditMode(): String {
        var image = ""
        if (imagePath.isEmpty()) {
            if (this@FragmentEditAssosiate::byteArr.isInitialized)
                if (byteArr.size > 0)
                    image = getImageFile
        } else {
            image = imagePath
        }
        return image
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}