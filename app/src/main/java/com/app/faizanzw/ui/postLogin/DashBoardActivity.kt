package com.app.faizanzw.ui.postLogin

import android.app.Activity
import android.app.appsearch.AppSearchResult
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.BaseActivity
import com.app.faizanzw.R
import com.app.faizanzw.databinding.ActivityDashboardBinding
import com.app.faizanzw.eventModel.SendImageData
import com.app.faizanzw.ui.postLogin.fragments.DashBoardFragment
import com.app.faizanzw.utils.AttachmentPicker
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.FileUtil
import com.app.faizanzw.utils.OnPickAttachmentListener
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class DashBoardActivity : BaseActivity(), OnPickAttachmentListener {

    lateinit var binding: ActivityDashboardBinding
    lateinit var attachmentPicker: AttachmentPicker
    lateinit var dashBoardFragment: DashBoardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        attachmentPicker = AttachmentPicker((this), this)

        dashBoardFragment = DashBoardFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, dashBoardFragment)
            .commitAllowingStateLoss()

    }

    fun addFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.frame_container,
                fragment
            )
            .addToBackStack(tag)
            .commitAllowingStateLoss()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUri = UCrop.getOutput(data!!)
            if (imageUri != null) {
                val imagePath = FileUtil.from(this, imageUri)
                Log.e("paath : ", "${imagePath.absolutePath}")
                lifecycleScope.launch {
                    delay(1000)
                    EventBus.getDefault().post(SendImageData(imagePath.absolutePath))
                }
            }
        } else if (requestCode == 100) {
            if (resultCode == AppSearchResult.RESULT_OK) {
                errorSnackBar("${data?.getStringExtra("message")}")
            }
        }
    }

    override fun onPicked(path: String) {
        lifecycleScope.launch {
            delay(1000)
            EventBus.getDefault().post(SendImageData(path))
        }
    }

    override fun onBackPressed() {

        val count = supportFragmentManager.backStackEntryCount

        /*if (count == 1) {
            Log.e("in if : ", "in if")
            if (this::dashBoardFragment.isInitialized)
                dashBoardFragment.getData()
        } else {
            Log.e("in else : ", "in else")
        }*/

        super.onBackPressed()
    }

}