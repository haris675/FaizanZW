package com.app.faizanzw.ui.postLogin.fragments.search

import android.app.DatePickerDialog
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
import com.app.faizanzw.database.DBExpenseType
import com.app.faizanzw.databinding.FragmentSearchExpenseBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.eventmodel.EventExpenseData
import com.app.faizanzw.ui.postLogin.fragments.FragmentExpenseEntry
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.adapter.ExpenseAdapter
import com.app.faizanzw.ui.postLogin.fragments.search.adapter.ExpenseOprationListener
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.ExpenseEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.IntentParams
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import javax.inject.Inject

interface ExpenseUpdateListener {
    fun onExpenseUpdate(payment: ExpenseSearch)
}

@AndroidEntryPoint
class FragmentExpenseSearch : Fragment(), ExpenseOprationListener {

    lateinit var binding: FragmentSearchExpenseBinding
    lateinit var expenseAdapter: ExpenseAdapter

    var expenseStatus = "PENDING"
    var expenseType = "-1"
    var employee: String? = null
    var user = ""
    var branchId: String? = null
    var clickPos = 0

    val viewModel by viewModels<ExpenseEntryViewModel>()

    @Inject
    lateinit var pref: PreferenceModule
    lateinit var typeList: ArrayList<DBExpenseType>
    lateinit var branchList: ArrayList<DBBranch>
    lateinit var employeeList: ArrayList<DBEmployee>

    companion object {
        val TAG = FragmentExpenseSearch::class.java.name
        fun newInstance(
            formType: Int,
            type: String
        ): FragmentExpenseSearch = FragmentExpenseSearch().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.FORM_TYPE, formType)
                putString(IntentParams.DATA, type)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchExpenseBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.txtTitle.text = resources.getString(R.string.search_expense)

        employee = pref.get(PrefEnum.USERID, 0).toString()

        Log.e("current Tag", "${requireArguments().getString(IntentParams.SCREEN_NAME)}")

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter =
            ExpenseAdapter(requireContext(), ArrayList(), this, pref.get(PrefEnum.USERID, 0))
        binding.rvList.adapter = expenseAdapter

        binding.llAllowToShow.isVisible = pref.get(PrefEnum.ALLOWTOSHOW, false)


        clickEvents()
        setupAllSpinnersAdapter()
        expenseStatus = requireArguments().getString(IntentParams.DATA, "SELECT ALL")
        Log.e("status : ", expenseStatus)
        binding.spnStatus.setSelection(1)

        observeViewModel()
        getExpenseType()
    }

    private fun getExpenseType() {
        lifecycleScope.launch {
            delay(750)
            if (requireContext().isNetworkConnected()) {

                viewModel.getFillSearchData(
                    pref.get(
                        PrefEnum.COMPANYCODE,
                        ""
                    ),
                    binding.edtStDate.getTitle().trim(),
                    binding.edtEDate.getTitle().trim(),
                    expenseStatus,
                    expenseType,
                    branchId,
                    employee,
                    pref.get(PrefEnum.ALLOWTOSHOW, false)
                )
            } else requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun observeViewModel() {
        viewModel.typeList.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.success) {
                        typeList = ArrayList<DBExpenseType>().apply {
                            add(DBExpenseType(0, "Select ALL"))
                        }
                        typeList.addAll(it.model.data)
                        setupTypeAdapter()
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

        viewModel.stateSearch.observe(requireActivity())
        {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.status == 200) {
                        expenseAdapter.setData(it.model.data)
                    } else {
                        requireActivity().errorSnackBar(it.model.message)
                        expenseAdapter.setData(ArrayList())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                    expenseAdapter.setData(ArrayList())
                }
            }
        }
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.edtStDate.setOnClickListener {
            getFromDate(true)
        }
        binding.edtEDate.setOnClickListener {
            getFromDate(false)
        }
        binding.btnSignIn.setOnClickListener {

            viewModel.searchExpense(
                binding.edtStDate.getTitle().trim(),
                binding.edtEDate.getTitle().trim(),
                expenseStatus,
                expenseType,
                branchId,
                employee
            )
        }

        binding.root.setOnClickListener { }

    }


    private fun setupTypeAdapter() {
        binding.spnType.initAdapter(typeList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    expenseType = "-1"
                    return
                }
                val model = parent?.getItemAtPosition(position) as DBExpenseType
                model.let {
                    expenseType = it.comboID.toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
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
                        branchId = null
                        if (this@FragmentExpenseSearch::employeeList.isInitialized) {
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
                        employee = null
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

    private fun setupAllSpinnersAdapter() {

        val statusList =
            ArrayList<String>(resources.getStringArray(R.array.arr_expense_status).asList())
        binding.spnStatus.initAdapter(statusList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val model = parent?.getItemAtPosition(position) as String
                model.let {
                    Log.e("selected payee : ", "${model}")
                    expenseStatus = it
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
        //val position = data.species.indexOfFirst { it.isSelected }
        //binding.spnStatus.setSelection(position)
    }

    private fun getFromDate(fromDate: Boolean) {
        val c = Calendar.getInstance()
        var mYear = c.get(Calendar.YEAR)
        var mMonth = c.get(Calendar.MONTH)
        var mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                var strDay = dayOfMonth.toString()
                var strMonth = (monthOfYear + 1).toString()

                if (dayOfMonth <= 9) strDay = "0" + dayOfMonth

                if ((monthOfYear + 1) <= 9) strMonth = "0" + (monthOfYear + 1)


                if (fromDate) {
                    binding.edtStDate.setTitle(strDay + "/" + strMonth + "/" + year)
                } else {
                    binding.edtEDate.setTitle(strDay + "/" + strMonth + "/" + year)
                }
            },
            mYear,
            mMonth,
            mDay
        )
        datePickerDialog.show()
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private fun openEditFragment(id: Int, data: ExpenseSearch, position: Int) {
        (requireActivity() as DashBoardActivity).addFragment(
            FragmentExpenseEntry.newInstance(id, data, position),
            "Expense_Entry"
        )
    }

    override fun onEdit(data: ExpenseSearch, position: Int) {
        clickPos = position
        openEditFragment(data.tranID, data, position)
    }


    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateExpenseItem(data: EventExpenseData) {
        expenseAdapter.updateItem(data.position, data.data)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }
}