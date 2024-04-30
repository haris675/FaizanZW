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
import com.app.faizanzw.databinding.FragmentSearchPaymentBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.adapter.PaymentOprationListener
import com.app.faizanzw.ui.postLogin.adapter.PendingPaymentAdapter
import com.app.faizanzw.ui.postLogin.fragments.FragmentPaymentEntry
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.model.NewSearchPayment
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.PaymentEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getFromDate
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.IntentParams
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentPaymentSearch : Fragment(), PaymentOprationListener {

    lateinit var binding: FragmentSearchPaymentBinding
    lateinit var paymentAdapter: PendingPaymentAdapter

    lateinit var branchList: ArrayList<DBBranch>
    lateinit var employeeList: ArrayList<DBEmployee>

    var FORMTYPE = 1

    val viewModel by viewModels<PaymentEntryViewModel>()

    var branch = "-1"
    var employee = "-1"
    var status = "SELECT ALL"

    @Inject
    lateinit var pref: PreferenceModule


    companion object {
        val TAG = FragmentPaymentSearch::class.java.name
        fun newInstance(
            formType: Int,
        ): FragmentPaymentSearch =
            FragmentPaymentSearch().apply {
                arguments = Bundle().apply {
                    putInt(IntentParams.FORM_TYPE, formType)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchPaymentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.txtTitle.text = resources.getString(R.string.search_payment)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        paymentAdapter =
            PendingPaymentAdapter(requireContext(), ArrayList(), this, pref.get(PrefEnum.USERID, 0))
        binding.rvList.adapter = paymentAdapter

        binding.llAllowToShow.isVisible = pref.get(PrefEnum.ALLOWTOSHOW, false)

        branch = pref.get(PrefEnum.BRANCHID, 0).toString()
        //employee = pref.get(PrefEnum.USERID, 0).toString()

        clickEvents()

        setupAllSpinnersAdapter()
        status = "PENDING"
        binding.spnStatus.setSelection(1)

        getData()

        observeViewModel()
    }

    private fun getData() {
        lifecycleScope.launch {
            delay(750)
            if (requireContext().isNetworkConnected())
                if (pref.get(PrefEnum.ALLOWTOSHOW, false)) {
                    viewModel.getMergedData2(
                        pref.get(PrefEnum.COMPANYCODE, ""),
                        binding.edtStDate.getTitle().trim(),
                        binding.edtEDate.getTitle().trim(),
                        status,
                        branch,
                        employee,
                        //pref.get(PrefEnum.USERID, 0).toString()
                    )
                } else {
                    viewModel.getSearchData(
                        pref.get(PrefEnum.COMPANYCODE, ""),
                        binding.edtStDate.getTitle().trim(),
                        binding.edtEDate.getTitle().trim(),
                        status,
                        branch,
                        employee,
                        //pref.get(PrefEnum.USERID, 0).toString()
                    )
                }
            else
                requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun observeViewModel() {
        viewModel.branchList.observe(requireActivity(),
            {
                when (it) {
                    is AppState.Loading -> {
                        isLoading(true)
                    }
                    is AppState.Success -> {
                        isLoading(false)
                        if (it.model.success) {
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
                        if (it.model.success) {
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

        viewModel.statePayment.observe(requireActivity(), {
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

        viewModel.modelSearchPayment.observe(requireActivity()) {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.status == 200) {
                        paymentAdapter.setData(it.model.data)
                    } else {
                        requireActivity().errorSnackBar(it.model.message)
                        paymentAdapter.setData(ArrayList())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                    paymentAdapter.setData(ArrayList())
                    Log.e("error : ", "${it.error}")
                }
            }
        }

        viewModel.statusData.observe(requireActivity())
        {
            when (it.first) {
                true -> {
                    isLoading(false)
                    requireActivity().successSnackBar(it.second)
                    paymentAdapter.updateStatus(it.third, clickPos)
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
        binding.btnSignIn.debouncedOnClick {
            viewModel.getSearchData(
                pref.get(PrefEnum.COMPANYCODE, ""),
                binding.edtStDate.getTitle().trim(),
                binding.edtEDate.getTitle().trim(),
                status,
                branch,
                employee,
                //pref.get(PrefEnum.USERID, 0).toString()
            )
        }

        binding.root.setOnClickListener { }
    }

    private fun setupAllSpinnersAdapter() {
        val peyeeList =
            ArrayList<String>(resources.getStringArray(R.array.arr_payment_status).asList())
        binding.spnStatus.initAdapter(
            peyeeList,
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val model = parent?.getItemAtPosition(position) as String
                    model.let {
                        status = it
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
                        branch = "-1"
                        if (this@FragmentPaymentSearch::employeeList.isInitialized) {
                            employee = "-1"
                            employeeList.clear()
                            setupEmployeeAdapter()
                        }
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBBranch
                    model.let {
                        branch = it.comboID.toString()
                        viewModel.getEmployeeData(branch)
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
                        employee = "-1"
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

    private fun openEditFragment(id: Int) {
        (requireActivity() as DashBoardActivity).addFragment(
            FragmentPaymentEntry.newInstance(id),
            "Payment_Search"
        )
    }

    var clickPos = 0
    override fun onEdit(data: NewSearchPayment, position: Int) {
        clickPos = position
        openEditFragment(data.tranID.toInt())
    }

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