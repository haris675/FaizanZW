package com.app.faizanzw.ui.prelogin

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.app.faizanzw.BaseActivity
import com.app.faizanzw.databinding.ActivityUrlGetBinding
import com.app.faizanzw.network.PreferenceModule
import com.app.faizanzw.ui.postLogin.DashBoardActivity
import com.app.faizanzw.utils.AppState
import com.app.faizanzw.utils.Extension.errorSnackBar
import com.app.faizanzw.utils.Extension.getDataMessage
import com.app.faizanzw.utils.Extension.getMessage
import com.app.faizanzw.utils.Extension.getStatus
import com.app.faizanzw.utils.Extension.successSnackBar
import com.app.faizanzw.utils.PrefEnum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CodeGetActivity : BaseActivity() {

    lateinit var binding : ActivityUrlGetBinding
    val loginViewModel by viewModels<LoginViewModel>()

    @Inject
    lateinit var pref: PreferenceModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlGetBinding.inflate(layoutInflater);
        setContentView(binding.root)

        if (pref.get(PrefEnum.ISLOGIN, 0) == 1) {
            startActivity(Intent(this@CodeGetActivity, DashBoardActivity::class.java))
            this@CodeGetActivity.finish()
        }

        observeValidation()
        observeLogin()
        clickEvents()
    }

    private fun clickEvents() {
        binding.btnSignIn.setOnClickListener {
            loginViewModel.validateForCode(binding.edtCode.text.toString().trim())
        }

    }

    private fun observeValidation() {
        loginViewModel.stateValidate.observe(this)
        {
            errorSnackBar(it.second)
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
                        if (it.model.asJsonObject.has("data"))
                        {
                            this.successSnackBar(it.model.getMessage())
                            var baseURL = it.model.getDataMessage().get("BaseURL").asString
                            pref.set(PrefEnum.COMPANYURL,baseURL)
                            pref.set(PrefEnum.COMPANYCODE,binding.edtCode.text.toString().trim())
                            lifecycleScope.launch {
                                //pref.storeUserData(it.model.asJsonObject.get("data").asJsonArray[0].asJsonObject)
                                delay(1500)
                                startActivity(Intent(this@CodeGetActivity, LoginActivity::class.java))
                                this@CodeGetActivity.finish()
                            }
                        }else{
                            errorSnackBar(it.model.getMessage())
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