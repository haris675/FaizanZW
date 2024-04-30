package com.app.faizanzw.ui.postLogin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.R
import com.app.faizanzw.database.DBBranch
import com.app.faizanzw.database.DBEmployee
import com.app.faizanzw.databinding.FragmentPaymentEntryBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.model.DataItem
import com.app.faizanzw.ui.postLogin.fragments.search.model.NewSearchPayment
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.PaymentEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.currentDateInString
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.findBranch
import com.app.faizanzw.utils.Extension.findBranchValue
import com.app.faizanzw.utils.Extension.findEmployee
import com.app.faizanzw.utils.Extension.findPosition
import com.app.faizanzw.utils.Extension.findValue
import com.app.faizanzw.utils.Extension.getDataArray
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
import javax.inject.Inject

interface PaymentUpdateListener {
    fun onProductSelected(payment: NewSearchPayment)
}

@AndroidEntryPoint
class FragmentPaymentEntry : Fragment() {

    lateinit var binding: FragmentPaymentEntryBinding
    val viewModel by viewModels<PaymentEntryViewModel>()

    var branch = ""
    var employee = ""
    var currency = ""
    var ENTRY_TASK = 0
    var TRAN_ID = 0

    //lateinit var currenyList: ArrayList<DataItem>
    lateinit var branchList: ArrayList<DBBranch>
    lateinit var employeeList: ArrayList<DBEmployee>

    @Inject
    lateinit var pref: PreferenceModule

    var isButtonPress = false

    var amountUSD = 0.0
    var amountOther = 0.0

    companion object {
        fun newInstance(
            formType: Int
        ): FragmentPaymentEntry = FragmentPaymentEntry().apply {
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
        binding = FragmentPaymentEntryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.FORM_TYPE, 0) != 0) {
            ENTRY_TASK = requireArguments().getInt(IntentParams.FORM_TYPE, 0)
        }

        binding.toolbar.txtTitle.text = resources.getString(R.string.payment_entry)
        clickEvents()

        observeViewModel()

        getData()

    }

    private fun getData() {
        lifecycleScope.launch {
            delay(750)
            //if (requireContext().isNetworkConnected()) {
                if (ENTRY_TASK != 0) {
                    viewModel.getEditDataData(
                        pref.get(PrefEnum.COMPANYCODE, ""),
                        ENTRY_TASK.toString(),
                        pref.get(PrefEnum.BRANCHID, 0),
                        pref.get(PrefEnum.USERID, 0),
                        requireContext().isNetworkConnected()
                    )
                } else {
                    viewModel.getMergedData(
                        pref.get(PrefEnum.COMPANYCODE, ""),
                        pref.get(PrefEnum.BRANCHID, 0),
                        pref.get(PrefEnum.USERID, 0)
                    )
                }
           /* } else
                requireActivity().errorSnackBar(resources.getString(R.string.no_internet))*/
        }
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.btnSignIn.setOnClickListener {
            if (validate(
                    binding.edtAmount.getTitle().trim(),
                    binding.edtRemarks.getTitle().trim()
                )
            ) {

                if (binding.chkCurrency.isChecked) {

                    if (amountOther == 0.0) {
                        requireActivity().errorSnackBar("Other balance is 0")
                        return@setOnClickListener
                    }

                    if (binding.edtAmount.getTitle().toDouble() > amountOther) {
                        requireActivity().errorSnackBar("Amount must be less than Other Balance")
                        return@setOnClickListener
                    }

                    viewModel.addPayment(
                        TRAN_ID.toString(),
                        requireContext().currentDateInString(),
                        binding.edtAmount.getTitle().trim(),
                        binding.edtRemarks.getTitle().trim(),
                        //currency,
                        branch, employee, binding.chkCurrency.isChecked
                    )
                } else {
                    if (amountUSD == 0.0) {
                        requireActivity().errorSnackBar("USD balance is 0")
                        return@setOnClickListener
                    }

                    if (binding.edtAmount.getTitle().toDouble() > amountUSD) {
                        requireActivity().errorSnackBar("Amount must be less than USD Balance")
                        return@setOnClickListener
                    }
                    viewModel.addPayment(
                        TRAN_ID.toString(),
                        requireContext().currentDateInString(),
                        binding.edtAmount.getTitle().trim(),
                        binding.edtRemarks.getTitle().trim(),
                        //currency,
                        branch, employee, binding.chkCurrency.isChecked
                    )
                }
            }
        }
        binding.root.setOnClickListener { }
    }

    private fun validate(amount: String, remarks: String): Boolean {
        var isValid = true
        isButtonPress = true
        if (branch.isEmpty()) {
            binding.spnBranch.setError("Please select Branch")
            isValid = false
        }

        if (employee.isEmpty()) {
            binding.spnEmployee.setError("Please select Employee")
            isValid = false
        }

        if (amount.isEmpty()) {
            binding.edtAmount.setError("Please enter Amount")
            isValid = false
        }
        if (amount.isNotEmpty()) {
            if (amount.toDouble() <= 0) {
                binding.edtAmount.setError("Please enter valid Amount")
                isValid = false
            }
        }
        if (remarks.isEmpty()) {
            binding.edtRemarks.setError("Please enter Remarks")
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {

        viewModel.balanceData.observe(requireActivity())
        {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatusCode() == 200) {
                        var USD = "0.0"
                        var OTHER = "0.0"
                        if (it.model.getDataArray().size() > 0) {
                            for (currency in it.model.getDataArray()) {
                                if (currency.asJsonObject.get("CurrencyType").asString.equals("USD"))
                                    USD = currency.asJsonObject.get("Amount").asString
                                else
                                    OTHER = currency.asJsonObject.get("Amount").asString
                            }
                            binding.btnUSD.text =
                                "USD\n(" + USD + ")"
                            binding.btnOther.text =
                                "Other \n(" + OTHER + ")"
                            amountUSD = USD.toDouble()
                            amountOther = OTHER.toDouble()
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
        }

        viewModel.branchList.observe(requireActivity(),
            {
                when (it) {
                    is AppState.Loading -> {
                        isLoading(true)
                    }
                    is AppState.Success -> {
                        isLoading(false)
                        if (it.model.status == 200) {
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

        viewModel.paymentData.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.status == 200) {
                        if (it.model.data.isAllowToChange)
                            setDataToView(it.model.data)
                        else
                            setDisableDataToView(it.model.data)
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

    private fun setDataToView(data: NewSearchPayment) {
        TRAN_ID = data.tranID.toInt()
        binding.edtTranNo.isVisible = true
        binding.edtDate.isVisible = true
        binding.edtTranNo.setTitle(data.tranNo)
        binding.edtDate.setTitle(data.trandate.convertTodate())
        binding.btnSignIn.isVisible = data.isAllowToChange

        if (this::branchList.isInitialized) {
            //binding.spnBranch.setSelection(branchList.findPosition(data.branchID.toString()))
            binding.spnBranch.setSelection(branchList.findBranch(data.branchID.toString()))
        }
        if (this::employeeList.isInitialized) {
            binding.spnEmployee.setSelection(employeeList.findEmployee(data.paymentTo.toString()))
        }
        binding.edtAmount.setTitle(data.amount)
        binding.edtRemarks.setTitle(data.remarks)
        branch = data.branchID.toString()
        employee = data.paymentTo.toString()
        binding.chkCurrency.isChecked = if (data.currencyType.equals("OTHER")) true else false
        if (!data.isAllowToChange || data.tranStatus.equals("ACCEPT", true))
            disableViews()
    }

    private fun setDisableDataToView(data: NewSearchPayment) {
        binding.llEdilable.isVisible = false
        binding.llDisable.isVisible = true
        binding.lableUi.txtPaymentNo.setTitle(data.tranNo)
        binding.lableUi.txtDate.setTitle(data.trandate.convertTodate())

        if (this::branchList.isInitialized) {
            //binding.lableUi.txtBranch.setTitle(branchList.findValue(dat a.branchID.toString()))
            binding.lableUi.txtBranch.setTitle(branchList.findBranchValue(data.branchID.toString()))
        }

        binding.lableUi.txtEmployee.setTitle(data.reciveByName)
        binding.lableUi.txtSentBy.setTitle(data.sentByName)

        binding.lableUi.txtAmount.setTitle(data.amount)
        binding.lableUi.txtRemarks.setTitle(data.remarks)
        binding.lableUi.txtCurrency.setTitle(data.currencyType)
    }

    private fun disableViews() {
        binding.spnBranch.disableSpinner()
        binding.spnEmployee.disableSpinner()
        binding.edtAmount.edtTitle?.isEnabled = false
        binding.edtAmount.edtTitle?.isFocusable = false
        binding.chkCurrency.isEnabled = false
        binding.edtRemarks.edtTitle?.isEnabled = false
        binding.edtRemarks.edtTitle?.isFocusable = false
        binding.btnSignIn.isVisible = false
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
                        if (this@FragmentPaymentEntry::employeeList.isInitialized) {
                            employeeList.clear()
                            setupEmployeeAdapter()
                        }
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBBranch
                    model.let {
                        Log.e("selected payee : ", "${model}")
                        branch = it.comboID.toString()
                        if (isButtonPress)
                            binding.spnBranch.hideError()
                        viewModel.getEmployeeData(it.comboID.toString())
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
                        employee = ""
                        return
                    }
                    val model = parent?.getItemAtPosition(position) as DBEmployee
                    model.let {
                        Log.e("selected payee : ", "${model}")
                        employee = it.userMasterKey.toString()
                        if (isButtonPress)
                            binding.spnEmployee.hideError()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            })
        if (ENTRY_TASK != 0 && count == 1) {
            binding.spnEmployee.setSelection(employeeList.findEmployee(employee))
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }
}