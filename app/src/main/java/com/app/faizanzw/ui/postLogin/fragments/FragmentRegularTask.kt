package com.app.faizanzw.ui.postLogin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.faizanzw.R
import com.app.faizanzw.databinding.FragmentRegularTaskListBinding
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.adapter.RegularOprationListener
import com.app.faizanzw.ui.postLogin.fragments.adapter.RegularTaskAdapter
import com.app.faizanzw.ui.postLogin.fragments.edit.FragmentEditAssosiate
import com.app.faizanzw.ui.postLogin.fragments.edit.FragmentEditTask
import com.app.faizanzw.ui.postLogin.fragments.model.RegularData
import com.app.faizanzw.ui.postLogin.fragments.search.model.TaskItem
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.RegularTaskViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatusCode
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class FragmentRegularTask : Fragment(),RegularOprationListener {

    lateinit var binding: FragmentRegularTaskListBinding

    val viewModel by viewModels<RegularTaskViewModel>()

    lateinit var regularAdapter: RegularTaskAdapter

    @Inject
    lateinit var pref: PreferenceModule

    @Inject
    lateinit var apiRepo: BaseApiRepo

    companion object {
        fun newInstance(
        ): FragmentRegularTask = FragmentRegularTask().apply {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegularTaskListBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun getData() {
        if (requireActivity().isNetworkConnected())
            viewModel.getTaskData(
                pref.get(PrefEnum.COMPANYCODE, ""),
                pref.get(PrefEnum.BRANCHID, 0),
                pref.get(PrefEnum.USERID, 0)
            )
        else
            requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
    }

    private fun refreshData() {
        if (requireActivity().isNetworkConnected())
            viewModel.getTaskSave(
                pref.get(PrefEnum.COMPANYCODE, ""),
                pref.get(PrefEnum.BRANCHID, 0),
                pref.get(PrefEnum.USERID, 0)
            )
        else
            requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.txtTitle.text = resources.getString(R.string.regular_entry)

        regularAdapter = RegularTaskAdapter(requireContext(), ArrayList(),this)
        binding.rvTaskList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = regularAdapter
        }

        lifecycleScope.launch {
            delay(750)
            getData()
        }

        observeData()

        clickEvents()
    }

    private fun clickEvents() {
        binding.toolbar.imgBack.debouncedOnClick {
            requireActivity().onBackPressed()
        }


        binding.btnRefresh.debouncedOnClick {
            refreshData()
        }
    }

    private fun observeData() {
        viewModel.taskList.observe(requireActivity(), Observer {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.status == 200) {
                        regularAdapter.setData(it.model.data)
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

        viewModel.taskSave.observe(requireActivity(), Observer {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatusCode() == 200) {
                        requireActivity().successSnackBar(it.model.getMessage())
                        lifecycleScope.launch {
                            delay(750)
                            getData()
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
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        //EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        //EventBus.getDefault().unregister(this)
    }

    override fun onEdit(data: RegularData, position: Int) {
        if (isAbleToRedirect(data)) {
            clickPosition = position
            val isAssigned = data.assignTo == pref.get(PrefEnum.USERID, 0)
            openEditFragment(data.tranID, data.branchID, isAssigned)
        } else {
            requireActivity().errorSnackBar("This task is not Created by you or Assigned to you")
        }
    }

    var clickPosition = 0

    private fun isAbleToRedirect(data: RegularData): Boolean {
        if (data.assignTo.toInt() == pref.get(PrefEnum.USERID, 0) || data.createdBy == pref.get(
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

}