package com.app.faizanzw.ui.prelogin

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.BaseActivity
import com.app.faizanzw.R
import com.app.faizanzw.databinding.ActivityLoginBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.isNetworkConnected
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    lateinit var binding: ActivityLoginBinding
    val loginViewModel by viewModels<LoginViewModel>()

    @Inject
    lateinit var pref: PreferenceModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickEvents()
        observeValidation()
        observeLogin()


        if (pref.get(PrefEnum.ISLOGIN, 0) == 1) {
            startActivity(Intent(this@LoginActivity, DashBoardActivity::class.java))
            this@LoginActivity.finish()
        }

        Log.e("dddddd : ", pref.get(PrefEnum.COMPANYURL, "nn"))
        binding.edtCode.setText(pref.get(PrefEnum.COMPANYCODE, ""))

    }

    private fun clickEvents() {
        binding.btnSignIn.setOnClickListener {
            performValidation()
        }
        binding.imgEye.setOnClickListener {
            val tag = binding.imgEye.tag != "1"
            Log.e("value tag : ", "${tag}")
            with(loginViewModel) { showHidePassword(tag) }
        }
        binding.txtChangeCode.setOnClickListener {
            pref.clear()
            startActivity(Intent(this, CodeGetActivity::class.java))
            finish();
        }
    }

    private fun observeValidation() {
        loginViewModel.stateValidate.observe(this)
        {
            errorSnackBar(it.second)
        }

        loginViewModel.stateShowHidePassword.observe(this, Observer {
            when (it) {
                true -> {
                    binding.edtPassword.setTransformationMethod(PasswordTransformationMethod())
                    binding.imgEye.tag = "1"
                }
                false -> {
                    binding.edtPassword.setTransformationMethod(null)
                    binding.imgEye.tag = "2"
                }
            }
        })
    }

    private fun performValidation() {
        if (this.isNetworkConnected()) {
            loginViewModel.validate(
                binding.edtCode.text.toString().trim(),
                binding.edtUserName.text.toString().trim(),
                binding.edtPassword.text.toString().trim()
            )
        } else {
            errorSnackBar(resources.getString(R.string.no_internet))
        }
    }

    private fun observeLogin() {
        loginViewModel.stateLogin.observe(this)
        {
            when (it) {
                is AppState.Loading -> {
                    isLoading(true)
                }
                is AppState.Success -> {
                    isLoading(false)
                    if (it.model.getStatus()) {
                        this.successSnackBar(it.model.getMessage())
                        lifecycleScope.launch {
                            pref.storeUserData(it.model.asJsonObject.get("data").asJsonArray[0].asJsonObject)
                            delay(1500)
                            startActivity(Intent(this@LoginActivity, DashBoardActivity::class.java))
                            this@LoginActivity.finish()
                        }
                    } else {
                        errorSnackBar(it.model.getMessage())
                    }
                }
                is AppState.Error -> {
                    isLoading(false)
                    errorSnackBar(it.error)
                }
            }
        }
    }

}