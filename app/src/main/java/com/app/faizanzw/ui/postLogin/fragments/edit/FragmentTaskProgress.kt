package com.app.faizanzw.ui.postLogin.fragments.edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.faizanzw.databinding.FragmentTaskProgressBinding
import com.app.faizanzw.ui.postLogin.fragments.adapter.TaskProgressAdapter
import com.app.faizanzw.ui.postLogin.fragments.edit.model.AssosiateData
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.IntentParams

class FragmentTaskProgress : Fragment() {

    lateinit var binding: FragmentTaskProgressBinding

    lateinit var assosiateList: java.util.ArrayList<AssosiateData>

    lateinit var taskAdapter: TaskProgressAdapter

    var isAssignedToYou = false

    companion object {
        fun newInstance(
            lstAssosiate: ArrayList<AssosiateData>,
            name: String,
            isAssigned: Boolean
        ): FragmentTaskProgress = FragmentTaskProgress().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(IntentParams.DATA, lstAssosiate)
                putString(IntentParams.NAME, name)
                putBoolean(
                    IntentParams.ISASSIGNED, isAssigned
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskProgressBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getParcelableArrayList<AssosiateData>(IntentParams.DATA) != null) {
            assosiateList =
                requireArguments().getParcelableArrayList<AssosiateData>(IntentParams.DATA)!!
            Log.e("size : ", "${assosiateList.size}")
            binding.toolbar.txtTitle.text = requireArguments().getString(IntentParams.NAME, "")
            isAssignedToYou = requireArguments().getBoolean(IntentParams.ISASSIGNED, false)
        }

        Log.e("Assigned : ","${isAssignedToYou}")

        if (this::assosiateList.isInitialized) {
            taskAdapter = TaskProgressAdapter(requireContext(), assosiateList,isAssignedToYou,{ position, data ->  })

            binding.rvProgressList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = taskAdapter
            }
        }

        clickEvents()
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.debouncedOnClick {
            requireActivity().onBackPressed()
        }
        binding.root.setOnClickListener {

        }
    }

}