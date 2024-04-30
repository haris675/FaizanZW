package com.app.faizanzw.ui.postLogin.fragments.search

import android.os.Bundle
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
import com.app.faizanzw.R
import com.app.faizanzw.database.DBBranch
import com.app.faizanzw.database.DBEmployee
import com.app.faizanzw.database.DBTaskType
import com.app.faizanzw.databinding.FragmentSearchTaskBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.eventmodel.EventTaskData
import com.app.faizanzw.ui.postLogin.fragments.edit.FragmentEditAssosiate
import com.app.faizanzw.ui.postLogin.fragments.edit.FragmentEditTask
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.adapter.SearchTaskAdapter
import com.app.faizanzw.ui.postLogin.fragments.search.adapter.TaskOprationListener
import com.app.faizanzw.ui.postLogin.fragments.search.model.TaskItem
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.TaskEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getFromDate
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
class FragmentTaskSeach : Fragment(), TaskOprationListener {

    lateinit var binding: FragmentSearchTaskBinding
    lateinit var taskAdapter: SearchTaskAdapter

    val viewModel by viewModels<TaskEntryViewModel>()

    lateinit var typeList: ArrayList<DBTaskType>
    lateinit var branchList: ArrayList<DBBranch>
    lateinit var employeeList: ArrayList<DBEmployee>

    var status = ""
    var type = "SELECT ALL"
    var branchId: String? = null

    //var userId = ""
    var employee: String? = null
    var clickPosition = 0

    @Inject
    lateinit var pref: PreferenceModule

    companion object {
        val TAG = FragmentTaskSeach::class.java.name
        fun newInstance(
            formType: Int
        ): FragmentTaskSeach = FragmentTaskSeach().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.FORM_TYPE, formType)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchTaskBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = SearchTaskAdapter(requireContext(), ArrayList(), this,
        pref.get(PrefEnum.USERID,0))
        binding.rvList.adapter = taskAdapter

        binding.llAllowToShow.isVisible = pref.get(PrefEnum.ALLOWTOSHOW, false)

        clickEvents()
        setupAllSpinnersAdapter()

        branchId = pref.get(PrefEnum.BRANCHID,0).toString()

        when (requireArguments().getInt(IntentParams.FORM_TYPE, 0)) {
            2 -> {
                binding.toolbar.txtTitle.text = resources.getString(R.string.new_tasl)
                status = "NEW"
                binding.spnStatus.setSelection(1)
            }
            3 -> {
                binding.toolbar.txtTitle.text = resources.getString(R.string.in_progress_task)
                status = "INPROGRESS"
                binding.spnStatus.setSelection(2)
            }
        }

        observeViewModel()
        getTaskType()
    }

    private fun getTaskType() {
        lifecycleScope.launch {
            delay(750)
                viewModel.getTaskData(
                    pref.get(
                        PrefEnum.COMPANYCODE, ""
                    ), binding.edtStDate.getTitle().trim(),
                    binding.edtEDate.getTitle().trim(),
                    status, type, branchId, employee, pref.get(PrefEnum.ALLOWTOSHOW, false)
                )
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun observeViewModel() {
        viewModel.taskList.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.success) {
                        typeList = ArrayList<DBTaskType>().apply {
                            add(DBTaskType(0, "Select ALL"))
                        }
                        typeList.addAll(it.model.data)
                        setTypeAdapter()
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

        viewModel.stateSearch.observe(requireActivity()) {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.status == 200) {
                        taskAdapter.setData(it.model.data)
                    } else {
                        requireActivity().errorSnackBar(it.model.message)
                        taskAdapter.setData(ArrayList())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                    taskAdapter.setData(ArrayList())
                }
            }
        }
        viewModel.statusData.observe(requireActivity())
        {
            when (it.first) {
                true -> {
                    isLoading(false)
                    requireActivity().successSnackBar(it.second)
                    taskAdapter.updateStatus(it.third, clickPos)
                }
                false -> {
                    if (it.second.equals("Loading")) {
                        isLoading(true)
                    } else {
                        isLoading(false)
                        requireActivity().errorSnackBar(it.second)
                    }
                }
            }
        }
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.edtStDate.setOnClickListener {
            requireActivity().getFromDate(true, { value, date ->
                binding.edtStDate.setTitle(date)
            })
        }
        binding.edtEDate.setOnClickListener {
            requireActivity().getFromDate(true, { value, date ->
                binding.edtEDate.setTitle(date)
            })
        }
        binding.btnSearch.setOnClickListener {
            viewModel.searchTask(
                binding.edtStDate.getTitle().trim(),
                binding.edtEDate.getTitle().trim(),
                status, type, branchId, employee
            )
        }

        binding.root.setOnClickListener { }
    }

    private fun setupAllSpinnersAdapter() {

        val peyeeList =
            ArrayList<String>(resources.getStringArray(R.array.arr_task_status).asList())
        binding.spnStatus.initAdapter(peyeeList, object : AdapterView.OnItemSelectedListener {
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
        //val position = data.species.indexOfFirst { it.isSelected }
        //binding.spnStatus.setSelection(position)
    }

    private fun setupBranchAdapter() {
        binding.spnBanch.initAdapter(
            branchList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        branchId = ""
                        if (this@FragmentTaskSeach::employeeList.isInitialized) {
                            employee = null
                            employeeList.clear()
                            setupEmployeeAdapter()
                        }
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBBranch
                    model.let {
                        branchId = it.comboID.toString()
                        viewModel.getEmployeeData(branchId)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
    }

    private fun setupEmployeeAdapter() {
        binding.spnEmployee.initAdapter(
            employeeList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        employee = pref.get(PrefEnum.USERID, 0).toString()
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBEmployee
                    model.let {
                        employee = it.userMasterKey.toString()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
    }

    private fun setTypeAdapter() {
        binding.spnType.initAdapter(typeList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    type = "SELECT ALL"
                    return
                }
                val model = parent?.getItemAtPosition(position) as DBTaskType
                model.let {
                    Log.e("selected payee : ", "${model}")
                    type = it.comboID.toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
    }

    override fun onEdit(data: TaskItem, position: Int) {
        if (isAbleToRedirect(data)) {
            clickPosition = position
            val isAssigned = data.AssignTo.toInt() == pref.get(PrefEnum.USERID, 0)
            openEditFragment(data.TranID, data.BranchID.toInt(), isAssigned)
        } else {
            requireActivity().errorSnackBar("This task is not Created by you or Assigned to you")
        }
    }

    private fun isAbleToRedirect(data: TaskItem): Boolean {
        if (data.AssignTo.toInt() == pref.get(PrefEnum.USERID, 0) || data.createdBy == pref.get(
                PrefEnum.USERID,
                0
            )
        )
            return true
        return false
    }

    private fun openEditFragment(id: Int, bid: Int, isAssigned: Boolean) {
        if (isAssigned) {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentEditAssosiate.newInstance(id, bid, isAssigned),
                "TASK_ASSOSIATE"
            )
        } else {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentEditTask.newInstance(id, bid, isAssigned),
                "TASK_ENTRY"
            )
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
    fun updateListItem(data: EventTaskData) {
        taskAdapter.updateItem(data, clickPosition)
    }

    var clickPos = 0
    override fun onInProgress(id: String, position: Int, status: String) {
        clickPos = position
        callUpdateStatusApi(id, status)
    }

    override fun onComplete(id: String, position: Int, status: String) {
        clickPos = position
        callUpdateStatusApi(id, status)
    }

    private fun callUpdateStatusApi(id: String, status: String) {
        if (requireActivity().isNetworkConnected()) {
            viewModel.updateStatus(
                pref.get(PrefEnum.COMPANYCODE, ""),
                id, status, pref.get(PrefEnum.USERID, 0).toString()
            )
        } else {
            requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
        }
    }

}