package com.emproto.networklayer.feature

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.emproto.networklayer.ApiConstants
import com.emproto.networklayer.di.DataAppModule
import com.emproto.networklayer.di.DataComponent
import com.emproto.networklayer.di.DataModule
import com.emproto.networklayer.ApiService
import com.emproto.networklayer.di.DaggerDataComponent
import com.emproto.networklayer.request.refernow.ReferralRequest
import com.emproto.networklayer.response.ddocument.DDocumentResponse
import com.emproto.networklayer.response.refer.ReferalResponse
import retrofit2.Response
import javax.inject.Inject

/**
 * Base Data Source.
 * All the common end point used accross the sub modules.
 * @property application
 */
public abstract class BaseDataSource(private val baseApplication: Application) {
    @Inject
    lateinit var baseService: ApiService

    private var dataComponent: DataComponent =
        DaggerDataComponent.builder().dataAppModule(DataAppModule(baseApplication))
            .dataModule(DataModule(baseApplication)).build()

    init {
        dataComponent.inject(this)
        if (!isNetworkAvailable()) {
            throw Exception(ApiConstants.NO_INTERNET)
        }
    }

    suspend fun getRefer(referralRequest: ReferralRequest): Response<ReferalResponse> {
        return baseService.referNow(referralRequest)
    }

    suspend fun downloadDocument(path: String): Response<DDocumentResponse> {
        return baseService.downloadDocument(path)
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            baseApplication.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network =
                connectivityManager.activeNetwork // network is currently in a high power state for performing data transmission.
            Log.d("Network", "active network $network")
            network ?: return false // return false if network is null
            val actNetwork = connectivityManager.getNetworkCapabilities(network)
                ?: return false // return false if Network Capabilities is null
            return when {
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> { // check if wifi is connected
                    Log.d("Network", "wifi connected")
                    true
                }
                actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> { // check if mobile dats is connected
                    Log.d("Network", "cellular network connected")
                    true
                }
                else -> {
                    Log.d("Network", "internet not connected")
                    false
                }
            }
        }
        return false
    }


}
