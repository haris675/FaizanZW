package com.app.faizanzw.ui.postLogin.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import coil.load
import com.app.faizanzw.R
import com.app.faizanzw.database.DBExpenseType
import com.app.faizanzw.databinding.FragmentEntryBinding
import com.app.faizanzw.eventModel.SendImageData
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.eventmodel.EventExpenseData
import com.app.faizanzw.ui.postLogin.fragments.search.ExpenseSearch
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.ExpenseEntryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension
import com.app.faizanzw.utils.Extension.asColor
import com.app.faizanzw.utils.Extension.convertTodate
import com.app.faizanzw.utils.Extension.currentDateInString
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.findExpenseType
import com.app.faizanzw.utils.Extension.findStringListPosition
import com.app.faizanzw.utils.Extension.getDataArray
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.getStatusCode
import com.app.faizanzw.utils.Extension.saveFile
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.IntentParams
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class FragmentExpenseEntry : Fragment() {

    lateinit var binding: FragmentEntryBinding

    var expenseStatus = ""
    var expenseType = ""

    //var currency = "-1"
    var payType = ""
    var imagePath = ""
    var editDate = ""
    lateinit var byteArr: ByteArray

    val viewModel by viewModels<ExpenseEntryViewModel>()
    //lateinit var currencyList: ArrayList<DataItem>
    lateinit var typeList: ArrayList<DBExpenseType>

    @Inject
    lateinit var pref: PreferenceModule
    var ENTRY_TYPE = 0
    lateinit var expenseData: ExpenseSearch
    var editPosition = 0

    var amountUSD = 0.0
    var amountOther = 0.0

    companion object {
        val TAG = FragmentExpenseEntry::class.java.name
        fun newInstance(
            formType: Int,
            data: ExpenseSearch?,
            position: Int
        ): FragmentExpenseEntry = FragmentExpenseEntry().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.FORM_TYPE, formType)
                putParcelable(IntentParams.DATA, data)
                putInt(IntentParams.POSITION, position)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEntryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.FORM_TYPE, 0) != 0) {
            ENTRY_TYPE = requireArguments().getInt(IntentParams.FORM_TYPE, 0)
            editPosition = requireArguments().getInt(IntentParams.POSITION, 0)
            if (requireArguments().getParcelable<ExpenseSearch>(IntentParams.DATA) != null)
                expenseData = requireArguments().getParcelable<ExpenseSearch>(IntentParams.DATA)!!
        }

        binding.toolbar.txtTitle.text = resources.getString(R.string.expense_entry)
        binding.spnCurrency.isVisible = false

        setupAllSpinnersAdapter()
        clickEvents()
        //setWatchers()

        observeViewModel()

        lifecycleScope.launch {
            delay(500)
            if (ENTRY_TYPE == 0) {
                //viewModel.getCurrencyData(pref.get(PrefEnum.COMPANYCODE, ""))
                viewModel.getTypeData(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    pref.get(PrefEnum.BRANCHID, 0),
                    pref.get(PrefEnum.USERID, 0)
                )
            } else {
                viewModel.getMergeData(
                    pref.get(PrefEnum.COMPANYCODE, ""), ENTRY_TYPE.toString(),
                    pref.get(PrefEnum.BRANCHID, 0),
                    pref.get(PrefEnum.USERID, 0)
                )
            }
        }
    }

    /* private fun setWatchers() {
         binding.edtAmount.setTextWatcher(binding.tilAmount)
         binding.edtDollar.setTextWatcher(binding.tilDollar)
         binding.edtReference.setTextWatcher(binding.tilReference)
         binding.edtRemarks.setTextWatcher(binding.tilRemarks)
     }*/

    private fun validate(
        //currency: String,
        type: String,
        amount: String,
        payType: String,
        challan: String,
        image: String,
        remarks: String,
        dollar: String
    ): Boolean {
        var isValid = true
        /*if (currency.isEmpty()) {
            binding.spnCurrency.setError("Please select Currency")
            isValid = false
        }*/
        if (type.isEmpty()) {
            binding.spntype.setError("Please select Expense Type")
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

        if (payType.isEmpty()) {
            binding.spnPayType.setError("Please select Pay Type")
            isValid = false
        }

        if (dollar.isEmpty()) {
            binding.edtDollar.setError("Please enter Dollar value")
            isValid = false
        }

        if (dollar.isNotEmpty()) {
            if (dollar.toDouble() <= 0) {
                binding.edtDollar.setError("Please enter valid Dollar value")
                isValid = false
            }
        }

        if (!payType.equals("CASH", true)) {
            if (challan.isEmpty()) {
                binding.edtReference.setError("Please enter refenrece")
                isValid = false
            }
        }

        if (remarks.isEmpty()) {
            binding.edtRemarks.setError("Please enter Remarks")
            isValid = false
        }

        /*if (image.isEmpty()) {
            binding.txtImageError.text = "Please Upload Image"
            binding.txtImageError.setTextColor(R.color.light_red.asColor(requireContext()))
            isValid = false
        }*/
        return isValid
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.llUploadImage.setOnClickListener {
            (requireActivity() as DashBoardActivity).attachmentPicker.showDialogForCameraAndGallery()
        }
        binding.imgView.setOnClickListener {
            Extension.openImageFileInExternal(File(getImageInEditMode()))
        }
        binding.btnSave.setOnClickListener {
            validate()
        }

        binding.chkCurrency.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (buttonView!!.isPressed) {
                    if (!isChecked) {
                        binding.spnPayType.setSelection(0)
                    }
                }
            }
        })

        binding.root.setOnClickListener { }
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

        viewModel.typeList.observe(requireActivity(), Observer {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.success) {
                        typeList = ArrayList<DBExpenseType>().apply {
                            add(DBExpenseType(0, "Select Expense Type"))
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

        viewModel.stateAddEntry.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatus()) {
                        requireActivity().successSnackBar(it.model.getMessage())
                        passDataToFrag(payType, binding.edtAmount.getTitle().trim(), status)
                        lifecycleScope.launch {
                            delay(800)
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

        viewModel.dataExpense.observe(requireActivity(), {
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

    private fun passDataToFrag(type: String, amount: String, status: String) {
        if (this::expenseData.isInitialized) {
            if (expenseData != null) {
                expenseData.paymentType = type
                expenseData.netAmount = amount
                expenseData.expenseStatus = status
                EventBus.getDefault().post(EventExpenseData(editPosition, expenseData))
            }
        }
    }

    private fun setDataToView(data: ExpenseSearch) {
        binding.edtExpenseNo.isVisible = true
        binding.edtDate.isVisible = true
        binding.edtExpenseNo.setTitle(data.tranNo)
        binding.edtDate.setTitle(data.trandate.convertTodate())
        binding.edtDollar.setTitle(data.oneDollarValue)

        binding.btnSave.isVisible = data.IsAllowToChange

        //currency = data.TranAccountKey

        /*if (this::currencyList.isInitialized) {
            if (data.TranAccountKey.isNotEmpty()) {
                val position = currencyList.findPosition(data.TranAccountKey)
                binding.spnCurrency.setSelection(position)
            }
        }*/
        if (this::typeList.isInitialized) {
            //binding.spntype.setSelection(typeList.findPosition(data.AccGroupKey))
            binding.spntype.setSelection(typeList.findExpenseType(data.AccGroupKey))
        }
        binding.spnPayType.setSelection(payTypeList.findStringListPosition(data.paymentType))
        binding.spnStatus.setSelection(statusList.findStringListPosition(data.isPaymetClear))
        binding.edtAmount.setTitle(data.netAmount)
        binding.edtReference.setTitle(data.CardChecqueNo)
        binding.edtRemarks.setTitle(data.tranremarks)
        binding.chkCurrency.isChecked = if (data.currencyType.equals("OTHER")) true else false

        if (!data.DocByte.isNullOrEmpty()) {
            byteArr = Base64.decode(data.DocByte, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            binding.imgView.setImageBitmap(decodedImage)
            //binding.imgView.text = "Image Selected"
        }
        if (!data.IsAllowToChange || data.isPaymetClear.equals("APPROVED")) {
            disableAllView()
        } else {
            binding.spnStatus.isVisible = true
        }
    }

    private fun disableAllView() {
        binding.spntype.disableSpinner()
        binding.spnPayType.disableSpinner()
        binding.spnCurrency.disableSpinner()
        binding.edtAmount.edtTitle?.isEnabled = false
        binding.edtAmount.edtTitle?.isFocusable = false
        binding.edtReference.edtTitle?.isEnabled = false
        binding.edtReference.edtTitle?.isFocusable = false
        binding.edtDollar.edtTitle?.isEnabled = false
        binding.edtDollar.edtTitle?.isFocusable = false
        binding.edtRemarks.edtTitle?.isEnabled = false
        binding.edtRemarks.edtTitle?.isFocusable = false
        binding.llUploadImage.isEnabled = false
        binding.btnSave.isVisible = false
        binding.chkCurrency.isEnabled = false
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun validate() {
        with(binding) {
            if (validate(
                    //currency,
                    expenseType,
                    edtAmount.getTitle().trim(),
                    payType,
                    edtReference.getTitle().trim(),
                    getImageInEditMode(),
                    edtRemarks.getTitle().trim(),
                    edtDollar.getTitle().trim(),
                )
            ) {
                val date =
                    if (ENTRY_TYPE != 0) binding.edtDate.getTitle()
                        .trim() else requireContext().currentDateInString()

                if (binding.chkCurrency.isChecked) {

                    if (amountOther == 0.0) {
                        requireActivity().errorSnackBar("Other balance is 0")
                        return
                    }

                    if (binding.edtAmount.getTitle().toDouble() > amountOther) {
                        requireActivity().errorSnackBar("Amount must be less than Other Balance")
                        return
                    }

                    if (payType.equals("CASH")) {
                        requireActivity().errorSnackBar("Amount must be in USD, please change toggle")
                        return
                    }

                    viewModel.addExpenseEntry(
                        pref,
                        ENTRY_TYPE.toString(),
                        //currency,
                        date,
                        status,
                        expenseType,
                        binding.edtAmount.getTitle().trim(),
                        binding.edtDollar.getTitle().trim(),
                        payType,
                        binding.edtReference.getTitle().trim(),
                        binding.edtRemarks.getTitle().trim(),
                        getImageInEditMode(),
                        binding.chkCurrency.isChecked
                    )
                } else {
                    if (amountUSD == 0.0) {
                        requireActivity().errorSnackBar("USD balance is 0")
                        return
                    }

                    if (binding.edtAmount.getTitle().toDouble() > amountUSD) {
                        requireActivity().errorSnackBar("Amount must be less than USD Balance")
                        return
                    }

                    viewModel.addExpenseEntry(
                        pref,
                        ENTRY_TYPE.toString(),
                        //currency,
                        date,
                        status,
                        expenseType,
                        binding.edtAmount.getTitle().trim(),
                        binding.edtDollar.getTitle().trim(),
                        payType,
                        binding.edtReference.getTitle().trim(),
                        binding.edtRemarks.getTitle().trim(),
                        getImageInEditMode(),
                        binding.chkCurrency.isChecked
                    )
                }
            }
        }
    }

    private val getImageFile by lazy {
        saveFile(requireActivity(), byteArr, "savedFile.jpg")
    }

    private fun getImageInEditMode(): String {
        var image = ""
        if (imagePath.isEmpty()) {
            if (this@FragmentExpenseEntry::byteArr.isInitialized)
                if (byteArr.size > 0)
                    image = getImageFile
        } else {
            image = imagePath
        }
        return image
    }

    private fun setupTypeAdapter() {
        binding.spntype.initAdapter(typeList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    expenseType = ""
                    return
                }
                val model = parent?.getItemAtPosition(position) as DBExpenseType
                model.let {
                    expenseType = model.comboID.toString()
                    binding.spntype.hideError()
                    Log.e("selected type : ", "${model.comboID}")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })
    }

    val payTypeList by lazy {
        ArrayList<String>(resources.getStringArray(R.array.arr_pay_type).asList())
    }

    val statusList by lazy {
        ArrayList<String>(resources.getStringArray(R.array.arr_add_expense_status).asList())
    }

    var status = "PENDING"
    private fun setupAllSpinnersAdapter() {
        binding.spnPayType.initAdapter(payTypeList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                /*if (position == 0) {
                    payType = ""
                    binding.edtReference.isVisible = false
                    return
                }*/
                val model = parent?.getItemAtPosition(position) as String
                model.let {
                    payType = it
                    if (!it.equals("CASH")) {
                        binding.edtReference.isVisible = true
                    } else {
                        binding.edtReference.isVisible = false
                        binding.chkCurrency.isChecked = false
                    }
                    binding.spnPayType.hideError()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })

        binding.spnStatus.initAdapter(statusList, object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val model = parent?.getItemAtPosition(position) as String
                model.let {
                    status = it
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        })

        //val position = data.species.indexOfFirst { it.isSelected }
        //binding.spnStatus.setSelection(position)
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

        val extension = File(data.image).extension
        if (extension.trim().lowercase().equals("pdf") || extension.trim().lowercase()
                .equals("doc") || extension.trim().lowercase().equals("docx")
        ) {
            imagePath = data.image
            //binding.imgView.text = File(data.image).name
        } else {
            imagePath = data.image
            //binding.imgView.text = File(data.image).name
            binding.imgView.load(data.image)
            binding.txtImageError.text = "Upload Image"
            binding.txtImageError.setTextColor(R.color.color_primary.asColor(requireContext()))
        }
    }

}
