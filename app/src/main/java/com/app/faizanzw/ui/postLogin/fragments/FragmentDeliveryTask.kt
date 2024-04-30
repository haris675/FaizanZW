package com.app.faizanzw.ui.postLogin.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import com.app.faizanzw.database.DBDeliveryTasK
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.databinding.FragmentDeliveryTaskBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.adapter.DeliveryAdapter
import com.app.faizanzw.ui.postLogin.fragments.adapter.OprationListener
import com.app.faizanzw.ui.postLogin.fragments.viewmodel.DeliveryViewModel
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.BackupWorker
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FragmentDeliveryTask : Fragment(), OprationListener {

    lateinit var binding: FragmentDeliveryTaskBinding
    val viewModel by viewModels<DeliveryViewModel>()
    lateinit var deliveryAdapter: DeliveryAdapter

    @Inject
    lateinit var pref: PreferenceModule

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeliveryTaskBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolBar.txtTitle.text = "Delivery Task"

        deliveryAdapter = DeliveryAdapter(requireContext(), ArrayList(), this)

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())

        //val realm = Realm.getDefaultInstance()
        //val reamData = realm.where(DBDeliveryTasK::class.java).findAll()
        //Log.e("all data : ", reamData.asJSON())


        lifecycleScope.launch {
            delay(750)
            viewModel.getDeliveryData(
                pref.get(PrefEnum.COMPANYCODE, ""),
                pref.get(PrefEnum.USERID, 0),
                requireActivity().isNetworkConnected()
            )
        }

        viewModel.deliveryData.observe(this)
        {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    it.model.data?.let { it2 ->
                        if (it2.size > 0) {
                            deliveryAdapter = DeliveryAdapter(requireContext(), it2, this)
                            binding.rvList.adapter = deliveryAdapter
                            DataBaseUtils.addOrUpdateDeliveries(it2)
                        } else {
                            binding.rvList.isVisible = false
                            binding.txtNoData.isVisible = true
                            binding.txtNoData.text = it.model.message
                        }
                    }
                    /*if (deliveryAdapter.itemCount==0)
                    {
                        binding.rvList.isVisible = false
                        binding.txtNoData.isVisible = true
                        binding.txtNoData.text = it.model.message
                    }*/
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                    binding.rvList.isVisible = false
                    binding.txtNoData.isVisible = true
                    binding.txtNoData.text = it.error
                }
            }
        }

        clickEvents()

        val workManager = WorkManager.getInstance(requireActivity())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            //.setRequiresDeviceIdle(true)
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<BackupWorker>(
                18,
                TimeUnit.MINUTES
            ).setConstraints(
                constraints
            ).build()

        /*val periodicWorkRequest =
            OneTimeWorkRequestBuilder<BackupWorker>().setConstraints(
                constraints
            ).build()*/

        workManager.enqueueUniquePeriodicWork(
            "uniqueWorkName",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        //workManager.enqueueUniqueWork("uniqueWork",ExistingWorkPolicy.KEEP,periodicWorkRequest)

        Log.e("ccccc : ","${DataBaseUtils.getOfflineDeliveries()?.size} nn ")
    }

    private fun clickEvents() {
        binding.toolBar.imgBack.debouncedOnClick {
            requireActivity().onBackPressed()
        }
    }

    var clickPosition = 0
    override fun onEdit(data: Any, position: Int) {
        var itemData = data as DBDeliveryTasK
        if (isAbleToRedirect(itemData)) {
            clickPosition = position
            val isAssigned = itemData.assignTo == pref.get(PrefEnum.USERID, 0)
            openEditFragment(itemData.tranID, data.branchID, isAssigned)
        } else {
            requireActivity().errorSnackBar("This task is not Created by you or Assigned to you")
        }
    }

    private fun isAbleToRedirect(data: DBDeliveryTasK): Boolean {
        if (data.assignTo == pref.get(PrefEnum.USERID, 0) || data.createdBy == pref.get(
                PrefEnum.USERID,
                0
            )
        )
            return true
        return false
    }

    private fun openEditFragment(id: Int, bid: Int, isAssigned: Boolean) {
        (requireActivity() as DashBoardActivity).addFragment(
            FragmentDeliveryEntry.newInstance(id, bid),
            "TASK_ENTRY"
        )
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun updateList(data: DBDeliveryTasK) {
        deliveryAdapter.updateData(data, clickPosition)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onStop() {
        super.onStop()
    }
}