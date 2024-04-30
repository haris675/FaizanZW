package com.app.faizanzw.network

import com.app.faizanzw.utils.AppState
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NodeApiRepo @Inject constructor(
    //@ApplicationContext val context: Context,
    @Qualifiers.nodeClientWithToken private val baseApiService: ApiServices,
    private val pref: PreferenceModule
) : BaseApiResponse() {

    fun codeGetApi(
        companyCode: String
    ): Flow<AppState<JsonElement>> = flow {
        emit(safeApiCall { baseApiService.doGetCode(companyCode) })
    }.flowOn(Dispatchers.IO)


}