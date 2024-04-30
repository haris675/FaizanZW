package com.app.faizanzw.ui.postLogin.fragments

import android.graphics.BitmapFactory
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
import coil.load
import com.app.faizanzw.R
import com.app.faizanzw.database.DBBranch
import com.app.faizanzw.database.DBEmployee
import com.app.faizanzw.database.DBTaskType
import com.app.faizanzw.databinding.FragmentCreateTaskBinding
import com.app.faizanzw.eventModel.SendImageData
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.eventmodel.EventTaskData
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.model.TaskItem
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.TaskEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension
import com.app.faizanzw.utils.Extension.asColor
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.currentDateInString
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.findBranch
import com.app.faizanzw.utils.Extension.findEmployee
import com.app.faizanzw.utils.Extension.findPosition
import com.app.faizanzw.utils.Extension.findStringListPosition
import com.app.faizanzw.utils.Extension.findTaskTypePosition
import com.app.faizanzw.utils.Extension.getDataMessage
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
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
import javax.inject.Inject

@AndroidEntryPoint
class FragmentCreateTask : Fragment() {

    lateinit var binding: FragmentCreateTaskBinding
    val viewModel by viewModels<TaskEntryViewModel>()
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

    companion object {
        val TAG = FragmentCreateTask::class.java.name
        fun newInstance(
            formType: Int,
            branchId: Int
        ): FragmentCreateTask = FragmentCreateTask().apply {
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
        binding = FragmentCreateTaskBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.FORM_TYPE, 0) != 0) {
            ENTRY_TASK = requireArguments().getInt(IntentParams.FORM_TYPE, 0)
            branch = requireArguments().getInt(IntentParams.BRANCH_ID, 0).toString()
        }

        binding.toolbar.txtTitle.text = resources.getString(R.string.create_task)
        clickEvents()
        setupAllSpinnersAdapter()
        //binding.edtRemarks.setTextWatcher(binding.tilRemarks)

        lifecycleScope.launch {
            delay(750)

            if (ENTRY_TASK != 0) {
                //binding.progressLayout.visibility = View.VISIBLE
                viewModel.getMergeData(
                    pref.get(PrefEnum.COMPANYCODE, "").toString(),
                    ENTRY_TASK.toString(),
                    pref.get(PrefEnum.USERID, 0),
                    false
                )
            } else {
                getTaskList()
            }
        }

        obserViewModel()
    }

    private fun getTaskList() {
        viewModel.getMergedData(pref.get(PrefEnum.COMPANYCODE, ""))
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
                            setupTaskAdapter()
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
                            setupBranchAdapter()
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
                                add(DBEmployee(0,0, "Select Employee"))
                            }
                            employeeList.addAll(it.model.data)
                            setupEmployeeAdapter()
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
                    if (it.model.getStatus()) {
                        requireActivity().successSnackBar(it.model.getMessage())
                        passDataToSearchList()
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
    }

    private fun passDataToSearchList() {
        if (ENTRY_TASK != 0) {
            EventBus.getDefault().post(
                EventTaskData(
                    strEmployee,
                    priority
                )
            )
        }
    }

    private fun setDataToView(data: TaskItem) {

        binding.edtExpenseNo.isVisible = true
        binding.edtDate.isVisible = true
        binding.edtExpenseNo.setTitle(data.tranNo)
        binding.edtDate.setTitle(data.trandate.convertTodate())
        binding.edtSubject.setTitle(data.subject)
        binding.btnSignIn.isVisible = data.IsAllowToChange

        if (this::taskList.isInitialized) {
            if (data.TranType.isNotEmpty()) {
                val position = taskList.findTaskTypePosition(data.TranType)
                binding.spnTaskType.setSelection(position)
            }
        }
        if (this::branchList.isInitialized) {
            binding.spnBranch.setSelection(branchList.findBranch(data.BranchID))
        }
        if (this::employeeList.isInitialized) {
            binding.spnEmpolyee.setSelection(employeeList.findEmployee(data.AssignTo))
        }
        binding.spnTaskPrioroty.setSelection(priorityList.findStringListPosition(data.taskPriority))
        binding.edtRemarks.setTitle(data.taskDescription)
        branch = data.BranchID
        employee = data.AssignTo

       /* binding.progress.progress = data.taskPercentage
        binding.txtPercentage.text = data.taskPercentage.toString()+"%"
        binding.txtUserName.text = "Assigned To: "+data.assignToName*/

        if (!data.DocByte.isNullOrEmpty()) {
            byteArr = Base64.decode(data.DocByte, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            binding.imgView.setImageBitmap(decodedImage)
        }
        if (!data.IsAllowToChange)
            disableAllViews()
    }

    private fun disableAllViews() {
        binding.spnTaskType.disableSpinner()
        binding.spnTaskPrioroty.disableSpinner()
        binding.spnBranch.disableSpinner()
        binding.spnEmpolyee.disableSpinner()
        binding.llUploadImage.isEnabled = false
        binding.edtRemarks.edtTitle?.isEnabled = false
        binding.edtRemarks.edtTitle?.isFocusable = false
        binding.edtSubject.edtTitle?.isFocusable = false
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.btnSignIn.setOnClickListener {
            if (validate()) {
                val date =
                    if (ENTRY_TASK != 0) binding.edtDate.getTitle()
                        .trim() else requireContext().currentDateInString()

                viewModel.addTask(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    pref.get(PrefEnum.USERID, 0).toString(),
                    branch,
                    taskType,
                    priority,
                    binding.edtRemarks.getTitle(),
                    date,
                    employee,
                    getImageInEditMode(),
                    ENTRY_TASK.toString(),
                    binding.edtSubject.getTitle()
                )
            }
        }
        binding.llUploadImage.setOnClickListener {
            (requireActivity() as DashBoardActivity).attachmentPicker.showDialogForCameraAndGallery()
        }

        binding.root.setOnClickListener { }

    }

    private fun validate(): Boolean {
        var isValid = true

        if (binding.edtSubject.getTitle().isEmpty()) {
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
        }
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
        imagePath = data.image
        binding.imgView.load(data.image)
        binding.txtImageError.setText("Upload Image")
        binding.txtImageError.setTextColor(R.color.color_primary.asColor(requireContext()))
    }

    private fun setupTaskAdapter() {
        binding.spnTaskType.initAdapter(
            taskList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        taskType = ""
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBTaskType
                    model.let {
                        Log.e("selected statusList : ", "${model}")
                        taskType = model.comboID.toString()
                        binding.spnTaskType.hideError()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
    }

    var count = 0
    private fun setupBranchAdapter() {
        binding.spnBranch.initAdapter(
            branchList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    count++
                    if (position == 0) {
                        branch = ""
                        if (this@FragmentCreateTask::employeeList.isInitialized) {
                            employeeList.clear()
                            setupEmployeeAdapter()
                        }
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBBranch
                    model.let {
                        Log.e("selected statusList : ", "${model}")
                        branch = model.comboID.toString()
                        if (ENTRY_TASK != 0) {
                            viewModel.getEmployeeData(
                                model.comboID.toString()
                            )
                            return
                        } else {
                            viewModel.getEmployeeData(
                                model.comboID.toString()
                            )
                        }
                        binding.spnBranch.hideError()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
    }

    var strEmployee = ""
    private fun setupEmployeeAdapter() {
        binding.spnEmpolyee.initAdapter(
            employeeList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        employee = ""
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBEmployee
                    model.let {
                        Log.e("selected statusList : ", "${model}")
                        employee = model.userMasterKey.toString()
                        strEmployee = model.userFullName!!
                        binding.spnEmpolyee.hideError()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
        if (ENTRY_TASK != 0 && count == 1) {
            //count = 0
            binding.spnEmpolyee.setSelection(employeeList.findEmployee(employee))
        }
    }

    val priorityList by lazy {
        ArrayList<String>(resources.getStringArray(R.array.arr_priority).asList())
    }

    private fun setupAllSpinnersAdapter() {
        binding.spnTaskPrioroty.initAdapter(
            priorityList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (position == 0) {
                        priority = ""
                        return
                    }

                    val model = parent?.getItemAtPosition(position) as String
                    model.let {
                        priority = model
                        Log.e("selected type : ", "${model}")
                        binding.spnTaskPrioroty.hideError()
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
            if (this@FragmentCreateTask::byteArr.isInitialized)
                if (byteArr.size > 0)
                    image = getImageFile
        } else {
            image = imagePath
        }
        return image
    }

}