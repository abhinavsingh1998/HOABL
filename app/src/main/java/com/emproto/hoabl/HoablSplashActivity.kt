package com.emproto.hoabl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.emproto.core.BaseActivity
import com.emproto.hoabl.databinding.ActivityHoablSplashBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.login.IntroSliderActivity
import com.emproto.networklayer.preferences.AppPreference
import javax.inject.Inject


class HoablSplashActivity : BaseActivity() {

    lateinit var mBinding: ActivityHoablSplashBinding

    @Inject
    lateinit var appPreference: AppPreference

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as HomeComponentProvider).homeComponent().inject(this)

        mBinding = ActivityHoablSplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        Handler().postDelayed(Runnable { // Show white background behind floating label
//            startActivity(Intent(this, HomeActivity::class.java))
//            finish()
            if (appPreference.isUserLoggedIn()) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, IntroSliderActivity::class.java))
                finish()
            }
        }, 1000)

    }

}

