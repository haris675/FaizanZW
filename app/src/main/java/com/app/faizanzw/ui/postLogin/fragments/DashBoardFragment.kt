package com.app.faizanzw.ui.postLogin.fragments

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.app.faizanzw.R
import com.app.faizanzw.database.DataBaseUtils
import com.app.faizanzw.database.DatabaseDeletedListener
import com.app.faizanzw.databinding.FragmentDashboardBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.ui.postLogin.fragments.search.FragmentExpenseSearch
import com.app.faizanzw.ui.postLogin.fragments.search.FragmentPaymentSearch
import com.app.faizanzw.ui.postLogin.fragments.search.FragmentTaskSeach
import com.app.faizanzw.ui.postLogin.viewmodel.DashboardViewModel
import com.app.faizanzw.ui.prelogin.CodeGetActivity
import com.app.faizanzw.ui.prelogin.LoginActivity
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.debouncedOnClick
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getDataMessage
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.showLogoutPopUp
import com.app.faizanzw.utils.PrefEnum
import com.app.faizanzw.utils.ScreenType
import com.google.gson.JsonElement
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashBoardFragment : Fragment() {

    lateinit var binding: FragmentDashboardBinding
    val dashboadViewModel by viewModels<DashboardViewModel>()


    @Inject
    lateinit var pref: PreferenceModule

    lateinit var realm: Realm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickEvents()

        binding.txtUserName.text = "Welcome ${pref.get(PrefEnum.USERNAME, "")}"

        realm = Realm.getDefaultInstance()

        lifecycleScope.launch {
            delay(750)
            getData()
        }

        dashboadViewModel.dashboardData.observe(requireActivity(), Observer {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    setupDataToView(it.model)
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                }
            }
        })

        dashboadViewModel.allSyncData.observe(requireActivity(), {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    DataBaseUtils.deleteAllData(object : DatabaseDeletedListener {
                        override fun onDataBaseDelete() {
                            if (it.model.branch!=null)
                                DataBaseUtils.addOrUpdateBranch(it.model.branch!!)
                            if (it.model.expenseType!=null)
                                DataBaseUtils.addOrUpdateExpense(it.model.expenseType!!)
                            if (it.model.taskType != null)
                                DataBaseUtils.addOrUpdateTaskType(it.model.taskType!!)
                            if (it.model.employeeType!=null)
                                DataBaseUtils.addOrUpdateEmployee(it.model.employeeType!!)
                            if (!it.model.deliveryData.isNullOrEmpty()) {
                                DataBaseUtils.addOrUpdateDeliveries(it.model.deliveryData)
                            }
                        }
                    })
                }
                is AppState.Error -> {
                    isLoading(false)
                    requireActivity().errorSnackBar(it.error)
                }
            }
        })

        /* val result = realm.where(DBDeliveryTasK::class.java).findAll()
         Log.e("DB Data : ", result.asJSON().toString() + " nn ")*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {

                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(
                        context,
                        "Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(
                    Manifest.permission.POST_NOTIFICATIONS,
                )
                .check()
        }
    }

    fun getData() {
        if (requireActivity().isNetworkConnected())
            dashboadViewModel.getDashboardData(
                pref.get(PrefEnum.COMPANYCODE, ""),
                pref.get(PrefEnum.USERID, 0)
            )
        else
            requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
    }

    private fun setupDataToView(jsonElement: JsonElement) {
        if (jsonElement.getStatus()) {
            with(binding) {
                txtPendingExpense.text =
                    jsonElement.getDataMessage().get("PendingExpForApprove").asString
                txtNewTask.text = jsonElement.getDataMessage().get("NewTask").asString
                txtProgressTask.text = jsonElement.getDataMessage().get("InProgressTask").asString
                txtPendingPayment.text =
                    jsonElement.getDataMessage().get("PendingPaymentForApprove").asString
            }
        } else {
            requireActivity().errorSnackBar(jsonElement.getMessage())
        }
    }

    private fun isLoading(value: Boolean) {
        (requireActivity() as DashBoardActivity).isLoading(value)
    }


    private fun clickEvents() {
        binding.cardPendingExpense.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentExpenseSearch.newInstance(ScreenType.PENDING_EXPENSE, "PENDING"),
                "Pending"
            )
        }
        binding.cardNewTask.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentTaskSeach.newInstance(ScreenType.NEW_TASK),
                "New"
            )
        }
        binding.cardINProgressTask.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentTaskSeach.newInstance(ScreenType.INPROGRESS_TASK),
                "Inprogress"
            )
        }
        binding.cardPendingPayment.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentPaymentSearch.newInstance(ScreenType.PENDING_PAYMENT),
                "Pending_Payment"
            )
        }
        binding.cardExpenseEntry.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentExpenseEntry.newInstance(0, null, 0),
                "Expense_Entry"
            )
        }
        binding.cardCreateTask.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentCreateTask.newInstance(0, 0),
                "Create_Task"
            )
        }
        binding.cardPaymentEntry.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentPaymentEntry.newInstance(0),
                "Payment_Entry"
            )
        }
        binding.imgSearch.debouncedOnClick {
            showLogoutPopUp(requireActivity(), {
                performSignOut()
            })
        }
        binding.cardRegularEntry.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentRegularTask.newInstance(),
                "Regular_Entry"
            )
        }
        binding.cardDelivery.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentDeliveryTask(),
                "Delivery_Task"
            )
        }
        binding.cardSync.debouncedOnClick {
            if (requireActivity().isNetworkConnected()) {
                //val result = realm.where(DBBranch::class.java).findAll()
                //Log.e("Total Count : ", result.size.toString() + " nn ")
                dashboadViewModel.getAllSyncData(
                    pref.get(PrefEnum.COMPANYCODE, ""),
                    pref.get(PrefEnum.USERID, 0)
                )
            } else {
                requireActivity().errorSnackBar(resources.getString(R.string.no_internet))
            }
        }
        binding.cardUnSync.debouncedOnClick {
            (requireActivity() as DashBoardActivity).addFragment(
                FragmentUnSync(),
                "UnSync_Task"
            )
        }
    }

    private fun performSignOut() {
        clearRealm()
        pref.clear()
        WorkManager.getInstance(requireActivity()).cancelAllWorkByTag("uniqueWorkName")
        requireActivity().finish()
        val intent = Intent(context, CodeGetActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun clearRealm() {
        realm.close()
        Realm.deleteRealm(realm.configuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.close()
    }

    private fun showNotification(title: String, description: String) {

        val notificationManager =
            requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var intent = Intent(requireContext(), DashBoardActivity::class.java)
        val builder: NotificationCompat.Builder

        /*when (notificationType) {
            1 -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
            2 -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
            3 -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
            4 -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
            5 -> {
                intent = Intent(this, DashboardActivity::class.java)
            }
        }*/

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel: NotificationChannel = notificationManager.getNotificationChannel("101")
            builder = NotificationCompat.Builder(requireContext(), "101")

            builder.setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(description)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
        } else {
            builder = NotificationCompat.Builder(requireContext())

            builder.setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(description)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .setPriority(NotificationCompat.PRIORITY_MAX)
        }

        val notification: Notification = builder.build()
        notificationManager.notify(101, notification)
    }

}