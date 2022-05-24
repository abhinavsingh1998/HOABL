package com.emproto.networklayer.feature

import android.app.Application
import com.emproto.networklayer.di.DataAppModule
import com.emproto.networklayer.di.DataComponent
import com.emproto.networklayer.di.DataModule
import com.emproto.networklayer.ApiService
import com.emproto.networklayer.di.DaggerDataComponent
import com.emproto.networklayer.response.home.HomeResponse
import com.emproto.networklayer.response.promises.PromisesResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

/**
 * Home Data Source.
 * All the api in home modules
 * @property application
 */
public class HomeDataSource(val application: Application) : BaseDataSource(application) {
    @Inject
    lateinit var apiService: ApiService
    @Named("dummy")

    private var dataComponent: DataComponent =
        DaggerDataComponent.builder().dataAppModule(DataAppModule(application))
            .dataModule(DataModule(application)).build()

    init {
        dataComponent.inject(this)
    }

    // home modules apis
    suspend fun getDashboardData(pageType: Int): Response<HomeResponse> {
        return apiService.getDashboardData(pageType)
    }

    //promises modules apis
    suspend fun getPromisesData(pageType: Int): Response<PromisesResponse> {
        return apiService.getPromises(pageType)
    }

}
