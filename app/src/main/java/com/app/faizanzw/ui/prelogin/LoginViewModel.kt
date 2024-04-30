package com.app.faizanzw.ui.prelogin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.faizanzw.network.BaseApiRepo
import com.app.faizanzw.network.NodeApiRepo
import com.app.faizanzw.utils.AppState
import com.google.gson.JsonElement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private var apiRepo: BaseApiRepo,private var nodeApiRepo:NodeApiRepo) : ViewModel() {

    private val _stateValidate = MutableLiveData<Pair<Int, String>>()
    val stateValidate: MutableLiveData<Pair<Int, String>> get() = _stateValidate

    private val _stateSignup = MutableLiveData<AppState<JsonElement>>()
    val stateSignup: MutableLiveData<AppState<JsonElement>> get() = _stateSignup

    val stateShowHidePassword = MutableLiveData<Boolean>()

    fun validate(companyCode: String, userName: String, password: String = "") {


        if (companyCode.isEmpty()) {
            _stateValidate.value = Pair(1, "Please enter company code")
            return
        }
        if (companyCode.trim().length < 2) {
            _stateValidate.value = Pair(1, "Comapny code must be 2 digit long")
            return
        }
        if (userName.isEmpty()) {
            _stateValidate.value = Pair(1, "Please enter username")
            return
        }
        if (userName.length < 4) {
            _stateValidate.value = Pair(1, "Username must be 4 digit long")
            return
        }
        if (password!!.isEmpty()) {
            _stateValidate.value = Pair(1, "Please enter username")
            return
        }
        if (password.length < 4) {
            _stateValidate.value = Pair(1, "Password must be 4 digit long")
            return
        }
        loginApi(companyCode, userName, password)
    }

    fun validateForCode(companyCode: String) {
        if (companyCode.isEmpty()) {
            _stateValidate.value = Pair(1, "Please enter company code")
            return
        }
        if (companyCode.trim().length < 2) {
            _stateValidate.value = Pair(1, "Compony code must be 2 digit long")
            return
        }
        codeGetApi(companyCode)
    }

    fun showHidePassword(value: Boolean) {
        stateShowHidePassword.value = value
    }

    private val _stateLogin = MutableLiveData<AppState<JsonElement>>()
    val stateLogin: MutableLiveData<AppState<JsonElement>> get() = _stateLogin
    fun loginApi(companyCode: String, userName: String, password: String = "") =
        viewModelScope.launch {
            _stateLogin.value = AppState.Loading()
            apiRepo.loginApi(companyCode, userName, password).collect {
                _stateLogin.value = it
            }
        }

    fun codeGetApi(companyCode: String) =
        viewModelScope.launch {
            _stateLogin.value = AppState.Loading()
            nodeApiRepo.codeGetApi(companyCode).collect {
                _stateLogin.value = it
            }
        }

}