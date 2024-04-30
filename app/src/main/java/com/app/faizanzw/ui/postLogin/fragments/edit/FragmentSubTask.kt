package com.app.faizanzw.ui.postLogin.fragments.edit

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.R
import com.app.faizanzw.databinding.FragmentSubTaskBinding
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.findStringListPosition
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
import javax.inject.Inject

@AndroidEntryPoint
class FragmentSubTask : Fragment() {

    lateinit var binding: FragmentSubTaskBinding
    var tranKey = 0
    var taskKey = 0
    lateinit var byteArr: ByteArray

    @Inject
    lateinit var apiRepo: BaseApiRepo

    @Inject
    lateinit var pref: PreferenceModule

    lateinit var assosiateData: AssosiateData

    companion object {
        fun newInstance(
            data: Int,
        ): FragmentSubTask = FragmentSubTask().apply {
            arguments = Bundle().apply {
                putInt(IntentParams.DATA, data)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubTaskBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getInt(IntentParams.DATA, 0) != 0) {
            tranKey = requireArguments().getInt(IntentParams.DATA, 0)
        }

        clickEvents()

        getData()
    }

    private fun setDataToView(data: AssosiateData) {
        assosiateData = data
        taskKey = data.taskKey
        binding.txtRange.text = data.progress.toString()
        binding.rangeSlider.setValues(data.progress.toFloat())
        progressValues = data.progress
        binding.edtRemarks.setTitle(data.description)
        setupAllSpinnersAdapter()
        binding.spnTaskStatus.setSelection(statusList.findStringListPosition(data.taskStatus))

        if (!data.docByte.isNullOrEmpty()) {
            byteArr = Base64.decode(data.docByte, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
            binding.imgView.setImageBitmap(decodedImage)
        }
    }

    private val getImageFile by lazy {
        Extension.saveFile(requireActivity(), byteArr, "savedFile.jpg")
    }

    var imagePath = ""
    private fun getImageInEditMode(): String {
        var image = ""
        if (imagePath.isEmpty()) {
            if (this@FragmentSubTask::byteArr.isInitialized)
                if (byteArr.size > 0)
                    image = getImageFile
        } else {
            image = imagePath
        }
        return image
    }

    var progressValues = 0
    private fun clickEvents() {
        binding.btnSignIn.debouncedOnClick {
            if (validate()) {
                if (this::assosiateData.isInitialized) {
                    saveAssosiate(
                        pref.get(PrefEnum.COMPANYCODE, ""),
                        assosiateData.t2AKey.toString(),
                        assosiateData.taskKey.toString(),
                        binding.edtRemarks.getTitle(),
                        progressValues.toString(),
                        status, getImageInEditMode()
                    )
                }
            }
        }

        binding.toolbar.imgBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.rangeSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            binding.txtRange.text = value.toInt().toString() + "%"
            progressValues = value.toInt()
        }

        binding.root.setOnClickListener {

        }
    }


    fun saveAssosiate(
        companyCode: String,
        t2AKey: String,
        taskKey: String,
        description: String,
        progress: String,
        taskStatus: String,
        image: String
    ) = lifecycleScope.launch {
        isLoading(false)
        apiRepo.doSaveAssosiateTask(
            companyCode,
            t2AKey,
            taskKey,
            description,
            progress,
            taskStatus,
            image
        ).collect {
            when (it) {
                is AppState.Loading -> {

                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatusCode() == 200) {
                        requireActivity().successSnackBar(it.model.getMessage())
                        lifecycleScope.launch {
                            delay(1500)
                            assosiateData.progress = progress.toInt()
                            assosiateData.description = description
                            assosiateData.taskStatus = status
                            EventBus.getDefault().post(assosiateData)
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
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    private fun getData() {
        if (requireActivity().isNetworkConnected()) {
            fetchDataFromApi()
        } else {
            requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
        }
    }

    fun fetchDataFromApi() {
        lifecycleScope.launch {
            delay(750)
            isLoading(true)
            apiRepo.getAssosiateTask(tranKey, pref.get(PrefEnum.COMPANYCODE, ""))
                .collect {
                    when (it) {
                        is AppState.Loading -> {

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
                }
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        if (binding.edtRemarks.getTitle().trim().isEmpty()) {
            binding.edtRemarks.setError("Please enter Remarks")
            isValid = false
        }
        return isValid
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

}