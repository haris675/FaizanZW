package com.app.faizanzw

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.faizanzw.databinding.LoaderLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    private var loader: Dialog? = null
    private var loaderBinding: LoaderLayoutBinding? = null

    fun setStatusBar() {
        window.apply {
            // clear FLAG_TRANSLUCENT_STATUS flag:
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            // finally change the color
            statusBarColor = Color.WHITE
            //setup system status bar visible text
            if (Build.VERSION.SDK_INT >= 23) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


    fun isLoading(shown: Boolean) {
        if (!shown) {
            if (loader != null && loader!!.isShowing) {
                loader!!.dismiss()
            }
        } else {
            if (loader == null) {
                loaderBinding = LoaderLayoutBinding.inflate(layoutInflater)
                loader = Dialog(this, R.style.Loader).also {
                    it.setCancelable(false)
                    it.setContentView(loaderBinding!!.root)
                }
                loader?.window?.let {
                    it.setLayout(MATCH_PARENT, MATCH_PARENT)
                    it.setBackgroundDrawable(ColorDrawable(0))
                }
                loader?.show()
            } else {
                if (!loader?.isShowing!!) {
                    loader!!.show()
                }
            }
        }
    }
}