package com.emproto.hoabl.feature.home.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.AbsListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.emproto.core.BaseActivity
import com.emproto.core.tourguide.Overlay
import com.emproto.core.tourguide.Pointer
import com.emproto.core.tourguide.TourGuide
import com.emproto.core.Constants
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ActivityHomeBinding
import com.emproto.hoabl.databinding.FragmentNotificationBottomSheetBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.chat.views.fragments.ChatsFragment
import com.emproto.hoabl.feature.home.views.fragments.HomeFragment
import com.emproto.hoabl.feature.home.views.fragments.InsightsFragment
import com.emproto.hoabl.feature.home.views.fragments.LatestUpdatesFragment
import com.emproto.hoabl.feature.investment.views.InvestmentFragment
import com.emproto.hoabl.feature.login.AuthActivity
import com.emproto.hoabl.feature.notification.adapter.NotificationAdapter
import com.emproto.hoabl.feature.portfolio.views.FmFragment
import com.emproto.hoabl.feature.portfolio.views.PortfolioFragment
import com.emproto.hoabl.feature.profile.fragments.about_us.AboutUsFragment
import com.emproto.hoabl.feature.profile.fragments.profile.ProfileFragment
import com.emproto.hoabl.feature.promises.HoablPromises
import com.emproto.hoabl.feature.promises.PromisesDetailsFragment
import com.emproto.hoabl.viewmodels.HomeViewModel
import com.emproto.hoabl.viewmodels.ProfileViewModel
import com.emproto.hoabl.viewmodels.factory.HomeFactory
import com.emproto.hoabl.viewmodels.factory.ProfileFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.notification.dataResponse.Data
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import kotlin.properties.Delegates

class HomeActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val ScreenHome = 0
    private val ScreenInvestment = 1
    private val ScreenPortfolio = 2
    private val ScreenPromises = 3
    private val ScreenProfile = 4
    private val ScreenFM = 5
    lateinit var fragmentNotificationBottomSheetBinding: FragmentNotificationBottomSheetBinding
    lateinit var bottomSheetDialog: BottomSheetDialog
    var CurrentScreen = -1
    private var contentFrame = 0
    private var mContext: Context? = null
    private var closeApp = false
    lateinit var activityHomeActivity: ActivityHomeBinding
    lateinit var tourGuide: TourGuide
    lateinit var manager: LinearLayoutManager
    var pageIndex = 1
    var pageSize = 20
    var isScrolling = false
    var currentItem by Delegates.notNull<Int>()
    var totalItem by Delegates.notNull<Int>()
    var scrolledItem by Delegates.notNull<Int>()
    private var totalNotification = 0
    var toatalPageSize = 0
    private lateinit var customAdapter: NotificationAdapter
    var notificationList = ArrayList<Data>()

    private lateinit var handler: Handler
    private var runnable: Runnable? = null


    @Inject
    lateinit var factory: HomeFactory
    lateinit var homeViewModel: HomeViewModel

    @Inject
    lateinit var profileFactory: ProfileFactory
    private lateinit var profileViewModel: ProfileViewModel

    private var added = false
    private val appURL = Constants.PLAY_STORE
    private var oneTimeValidation = false

    @Inject
    lateinit var appPreference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        activityHomeActivity = ActivityHomeBinding.inflate(layoutInflater)
        (application as HomeComponentProvider).homeComponent().inject(this)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        profileViewModel =
            ViewModelProvider(this, profileFactory)[ProfileViewModel::class.java]
        activityHomeActivity.searchLayout.rotateText.text = " "
        activityHomeActivity.searchLayout.rotateText.isSelected = true
        setContentView(activityHomeActivity.root)
        mContext = this
        contentFrame = R.id.container
        activityHomeActivity.includeNavigation.bottomNavigation.setOnNavigationItemSelectedListener(
            this
        )

        if (savedInstanceState == null) {
            navigate(R.id.navigation_hoabl) // change to whichever id should be default
        }

        bottomSheetDialog = BottomSheetDialog(this)
        fragmentNotificationBottomSheetBinding =
            FragmentNotificationBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(fragmentNotificationBottomSheetBinding.root)
        activityHomeActivity.searchLayout.imageBack.setOnClickListener { onBackPressed() }
        handler = Handler(Looper.getMainLooper())

        initData()
        trackEvent()
    }

    fun createTourGuide() {
        if (appPreference.isTourGuideCompleted()) {
            initClickListener()
        } else {
            initTourGuide()
        }
    }

    val enterAnimation = AlphaAnimation(0f, 1f)
        .apply {
            duration = 600
            fillAfter = true
        }
    val exitAnimation = AlphaAnimation(1f, 0f)
        .apply {
            duration = 600
            fillAfter = true
        }

    private fun initTourGuide() {

        tourGuide = TourGuide.create(this) {
            toolTip {
                title { "Home" }
                description { "The home page tells you about your investment opportunities, updates and insights." }
                gravity { Gravity.TOP }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener {
                    showInvestmentOverlay()
                }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
                setHoleRadius(75)
            }
        }
        tourGuide.playOn((activityHomeActivity.includeNavigation.bottomNavigation[0] as BottomNavigationMenuView)[0])

        activityHomeActivity.searchLayout.notificationView.setOnClickListener {
            chatOverlay()
        }
        activityHomeActivity.searchLayout.headsetView.setOnClickListener {
            cleanupTour()
        }
        activityHomeActivity.searchLayout.rotateText.setOnClickListener {
            searchOverlay()
        }
        activityHomeActivity.searchLayout.search.setOnClickListener {
            notificationOverlay()
        }
    }

    private fun notificationOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Notification" }
                description { "Get all updates and \nalerts in real time." }
                gravity { Gravity.BOTTOM }
                backgroundColor { R.color.black }
            }
            pointer {
                Pointer()
            }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener { chatOverlay() }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
            }
        }.playOn(activityHomeActivity.searchLayout.notificationView)
    }

    private fun cleanupTour() {
        tourGuide.cleanUp()
        //for tour guide
        appPreference.setTourGuide(true)
        //resetting tour guide events
        activityHomeActivity.searchLayout.rotateText.setOnClickListener(null)
        activityHomeActivity.searchLayout.search.setOnClickListener(null)
        initClickListener()
        Handler().postDelayed({
            activityHomeActivity.includeNavigation.bottomNavigation.menu[0].isChecked =
                true
        }, 100)
    }

    private fun chatOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Chat Support" }
                description { "Reach out to us for any questions you may have." }
                gravity { Gravity.BOTTOM }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener { cleanupTour() }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
            }
        }.playOn(activityHomeActivity.searchLayout.headsetView)
    }

    private fun searchOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Search" }
                description { "Search for specific information or\n investments within the app." }
                gravity { Gravity.BOTTOM }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.RECTANGLE }
                setOnClickListener { notificationOverlay() }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
            }
        }.playOn(activityHomeActivity.searchLayout.search)
    }

    private fun showInvestmentOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Investment" }
                description { "Click here to invest with us\n in New Generation Land." }
                gravity { Gravity.TOP }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener { showPortfolioOverlay() }
                setHoleRadius(75)
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)

            }
        }.playOn(
            (activityHomeActivity.includeNavigation.bottomNavigation[0] as BottomNavigationMenuView)[1]
        )
    }

    private fun trackEvent() {
        Mixpanel(this).identifyFunction(appPreference.getMobilenum(), Mixpanel.HOME)
    }

    @SuppressLint("NewApi")
    private fun initClickListener() {

        activityHomeActivity.searchLayout.headsetView.setOnClickListener {
            val bundle = Bundle()
            val chatsFragment = ChatsFragment()
            chatsFragment.arguments = bundle
            replaceFragment(
                chatsFragment.javaClass, "", true, bundle, null, 0, true
            )
        }

        activityHomeActivity.searchLayout.notificationView.setOnClickListener {
            notificationList.clear()
            callNotificationApi(20, 1, true)
            launchBottomSheet()
        }

        activityHomeActivity.searchLayout.layout.setOnClickListener {
            val fragment = AboutUsFragment()
            addFragment(fragment, true)
        }

    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(contentFrame)
    }

    fun navigate(id: Int) {
        activityHomeActivity.includeNavigation.bottomNavigation.selectedItemId = id
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_hoabl -> {
                openScreen(ScreenHome, "", false)
                return true
            }
            R.id.navigation_investment -> {
                if (appPreference.isTourGuideCompleted()) {
                    openScreen(ScreenInvestment, "", false)
                } else {
                    showPortfolioOverlay()
                }
                return true
            }
            R.id.navigation_portfolio -> {
                if (appPreference.isTourGuideCompleted()) {
                    openScreen(ScreenPortfolio, "", false)
                } else if (!appPreference.isTourGuideCompleted() && appPreference.isFacilityCard()) {
                    //My service overlay
                    showPromiseOverlay(
                        "My Services",
                        "Manage your land at \nthe click of a button."
                    )
                } else {
                    showPromiseOverlay("Promises", "Learn more about the \npromises made by HOABL.")
                }
                return true
            }
            R.id.navigation_promises -> {
                if (appPreference.isTourGuideCompleted() && !appPreference.isFacilityCard()) {
                    openScreen(ScreenPromises, "", false)
                } else if (appPreference.isTourGuideCompleted() && appPreference.isFacilityCard()) {
                    openScreen(ScreenFM, "", false)
                } else {
                    showProfileOverlay()
                }
                return true
            }
            R.id.navigation_profile -> {
                if (appPreference.isTourGuideCompleted()) {
                    openScreen(ScreenProfile, "", false)
                } else {
                    showMastOverlay()
                }
                return true
            }
        }
        return false
    }

    private fun showMastOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "HOABL Updates" }
                description { "Know more about us and our achievements." }
                gravity { Gravity.BOTTOM }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.RECTANGLE }
                setOnClickListener { searchOverlay() }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
            }
        }.playOn(activityHomeActivity.searchLayout.rotateText)
    }

    private fun showProfileOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Profile" }
                description { "See and edit your personal information and account details." }
                gravity { Gravity.TOP }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener { showMastOverlay() }
                setHoleRadius(75)
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
            }
        }.playOn(
            (activityHomeActivity.includeNavigation.bottomNavigation[0] as BottomNavigationMenuView)[4]
        )
    }

    private fun showPromiseOverlay(title: String, subTitle: String) {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { title }
                description { subTitle }
                gravity { Gravity.TOP }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener {
                    showProfileOverlay()
                }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
                setHoleRadius(75)
            }
        }.playOn(
            (activityHomeActivity.includeNavigation.bottomNavigation[0] as BottomNavigationMenuView)[3]
        )
    }

    private fun showPortfolioOverlay() {
        tourGuide.apply {
            tourGuide.cleanUp()
            toolTip {
                title { "Portfolio" }
                description { "Here you can see the summary of your \ninvestments and monitor your portfolio." }
                gravity { Gravity.TOP }
                backgroundColor { R.color.black }
            }
            pointer { Pointer() }
            overlay {
                backgroundColor { R.color.text_light_grey_color }
                style { Overlay.Style.CIRCLE }
                setOnClickListener {
                    if (!appPreference.isTourGuideCompleted() && appPreference.isFacilityCard()) {
                        //My service overlay
                        showPromiseOverlay(
                            "My Services",
                            "Manage your land at \nthe click of a button."
                        )
                    } else {
                        showPromiseOverlay(
                            "Promises",
                            "Learn more about the\n promises made by HOABL."
                        )
                    }
                }
                setEnterAnimation(enterAnimation)
                setExitAnimation(exitAnimation)
                setHoleRadius(75)
            }
        }.playOn(
            (activityHomeActivity.includeNavigation.bottomNavigation[0] as BottomNavigationMenuView)[2]
        )
    }

    private fun openScreen(screen: Int, metaData: String, isInit: Boolean) {
        val bundle = Bundle()
        bundle.putString("Type", metaData)

        CurrentScreen = screen
        when (screen) {
            ScreenHome -> {
                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle
                replaceFragment(homeFragment.javaClass, "", true, bundle, null, 0, true)
            }
            ScreenInvestment -> {
                val favouriteFragment = InvestmentFragment()
//                val favouriteFragment = Testimonials()
                favouriteFragment.arguments = bundle
                replaceFragment(favouriteFragment.javaClass, "", true, bundle, null, 0, true)
            }
            ScreenPortfolio -> {
                val portfolioFragment = PortfolioFragment()
                portfolioFragment.arguments = bundle
                replaceFragment(portfolioFragment.javaClass, "", true, bundle, null, 0, true)
            }
            ScreenPromises -> {
                val cartFragment = HoablPromises()
                cartFragment.arguments = bundle
                replaceFragment(cartFragment.javaClass, "", true, bundle, null, 0, true)
            }
            ScreenProfile -> {
                val profileFragment = ProfileFragment()
                profileFragment.arguments = bundle
                replaceFragment(profileFragment.javaClass, "", true, bundle, null, 0, true)
            }
            ScreenFM -> {
                val fmFragment = FmFragment()
                val mbundle = Bundle()
                mbundle.putString("param1", appPreference.getFmUrl())
                fmFragment.arguments = bundle
                replaceFragment(fmFragment.javaClass, "", true, mbundle, null, 0, true)

            }
        }
    }

    fun replaceFragment(
        fragmentClass: Class<*>,
        extraTag: String?,
        addToBackStack: Boolean,
        bundle: Bundle?,
        fragmentForResult: Fragment?,
        targetRequestCode: Int,
        replaceWithAnimation: Boolean
    ) {
        try {
            val finalTag: String
            if (extraTag != null && extraTag == "")
                finalTag = fragmentClass.simpleName + extraTag
            else
                finalTag = fragmentClass.simpleName

            val isPopBackStack = supportFragmentManager.popBackStackImmediate(finalTag, 0)
            if (!isPopBackStack) {
                var fragment = supportFragmentManager.findFragmentByTag(finalTag)
                if (fragment == null) try {
                    fragment = fragmentClass.newInstance() as Fragment
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                if (fragment != null) {
                    if (bundle != null) {
                        fragment.arguments = bundle
                    }
                    if (fragmentForResult != null) {
                        fragment.setTargetFragment(fragmentForResult, targetRequestCode)
                    }
                    val fragmentTransaction: FragmentTransaction =
                        supportFragmentManager.beginTransaction()
//                    fragmentTransaction.setCustomAnimations()
                    fragmentTransaction.replace(contentFrame, fragment, finalTag)
                    if (addToBackStack) fragmentTransaction.addToBackStack(finalTag)
                    supportFragmentManager.executePendingTransactions()
                    fragmentTransaction.setCustomAnimations(
                        R.anim.enter,
                        R.anim.exit,
                        R.anim.enter_right,
                        R.anim.exit_left
                    ).commit()


                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }


    override fun onBackPressed() {
        added = false
        if (getCurrentFragment() is HomeFragment) {
            if (closeApp) {
                finishAffinity()
            } else {
                runnable = Runnable { closeApp = true }
                runnable?.let { it1 -> handler.postDelayed(it1, 500) }
                Toast.makeText(mContext, "Please press again to exit", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onBackPressed()
            if (getCurrentFragment() is HomeFragment) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[0].isChecked = true
            } else if (getCurrentFragment() is InvestmentFragment) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[1].isChecked = true
            } else if (getCurrentFragment() is PortfolioFragment) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[2].isChecked = true
            } else if (!appPreference.isFacilityCard() && (getCurrentFragment() is HoablPromises || getCurrentFragment() is PromisesDetailsFragment)) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[3].isChecked = true
            } else if (getCurrentFragment() is ProfileFragment) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[4].isChecked = true
            } else if (getCurrentFragment() is FmFragment) {
                activityHomeActivity.includeNavigation.bottomNavigation.menu[3].isChecked = true
            }

        }
    }


    fun addFragment(fragment: Fragment, showAnimation: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (showAnimation) {
            false -> {
                fragmentTransaction.replace(R.id.container, fragment, fragment.javaClass.name)
                    .addToBackStack(fragment.javaClass.name).commit()
            }
            true -> {
                fragmentTransaction.setCustomAnimations(
                    R.anim.enter,
                    R.anim.exit,
                    R.anim.enter_right,
                    R.anim.exit_left
                )
                    .replace(R.id.container, fragment, fragment.javaClass.name)
                    .addToBackStack(fragment.javaClass.name).commit()
            }
        }
    }

    fun showErrorToast(message: String) {
        showErrorView(activityHomeActivity.root, message)
    }

    fun showHeader() {
        activityHomeActivity.searchLayout.toolbarLayout.show()
    }

    fun hideHeader() {
        activityHomeActivity.searchLayout.toolbarLayout.hide()
    }

    fun hideBackArrow() {
        activityHomeActivity.searchLayout.imageBack.hide()
    }

    fun showBackArrow() {
        activityHomeActivity.searchLayout.imageBack.show()
    }

    fun showBottomNavigation() {
        activityHomeActivity.includeNavigation.bottomNavigation.show()
    }

    fun hideBottomNavigation() {
        activityHomeActivity.includeNavigation.bottomNavigation.hide()
    }

    fun fingerprintValidation(status: Boolean) {
        oneTimeValidation = status
    }

    fun isFingerprintValidate(): Boolean {
        return oneTimeValidation
    }

    @SuppressLint("SetTextI18n")

    fun initData() {
        homeViewModel.gethomeData().observe(this, Observer {

        })
    }

    private fun callNotificationApi(pageSize: Int, pageIndex: Int, refresh: Boolean) {
        homeViewModel.getNotification(pageSize, pageIndex, refresh)
            .observe(
                this
            ) {
                when (it!!.status) {
                    Status.LOADING -> {
                        fragmentNotificationBottomSheetBinding.progressBar.isVisible = true
                        fragmentNotificationBottomSheetBinding.rv.isVisible = false
                        fragmentNotificationBottomSheetBinding.noNotificationText.visibility =
                            View.GONE
                    }
                    Status.SUCCESS -> {
                        fragmentNotificationBottomSheetBinding.rv.isVisible = true
                        fragmentNotificationBottomSheetBinding.progressBar.isVisible = false
                        fragmentNotificationBottomSheetBinding.markAllRead.isVisible = true
                        fragmentNotificationBottomSheetBinding.noNotificationText.visibility =
                            View.GONE

                        totalNotification = it!!.data!!.totalCount
                        toatalPageSize = it!!.data!!.totalPages

                        for (i in 0 until it!!.data!!.data?.size!!) {
                            notificationList.add(it!!.data!!.data[i]!!)
                        }

                        if (notificationList.isEmpty()) {
                            fragmentNotificationBottomSheetBinding.rv.isVisible = false
                            fragmentNotificationBottomSheetBinding.noNotificationText.visibility =
                                View.VISIBLE
                        }

                        notificationNavigation()

                    }
                    Status.ERROR -> {
                        when (it.message) {
                            Constants.ACCESS_DENIED -> {
                                showErrorToast(it.message!!)
                                appPreference.saveLogin(false)
                                appPreference.setToken("")
                                startActivity(Intent(mContext, AuthActivity::class.java))
                                this@HomeActivity.finish()
                            }
                            else -> {
                                showErrorToast(it.message!!)
                            }
                        }
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun launchBottomSheet() {
        bottomSheetDialog.show()
        pageIndex = 1
        pageSize = 20
        pagination()
    }

    fun setReadStatus(id: Int) {
        homeViewModel.setReadStatus(id).observe(
            this@HomeActivity
        ) {
            when (it!!.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                }
                Status.ERROR -> {

                }

            }
        }
    }

    fun logoutFromAllDevice() {
        appPreference.saveLogin(false)
        appPreference.setToken("")
        startActivity(Intent(mContext, AuthActivity::class.java))
        this@HomeActivity.finish()

    }

    fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "The House Of Abhinandan Lodha $appURL")
        startActivity(shareIntent)
    }

    override fun onResume() {
        super.onResume()
        hideSoftKeyboard()
        callApiForChecking()
    }

    private fun callApiForChecking() {
        profileViewModel.getUserProfile(true).observe(
            this@HomeActivity
        ) {
            when (it!!.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    Log.i("success", it.message.toString())
                }
                Status.ERROR -> {
                    when (it.message) {
                        Constants.ACCESS_DENIED -> {
                            showErrorToast(it.message!!)
                            appPreference.saveLogin(false)
                            appPreference.setToken("")
                            startActivity(Intent(mContext, AuthActivity::class.java))
                            this@HomeActivity.finish()
                        }
                        else -> {
                            //showErrorToast(it.message!!)
                        }
                    }
                }

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun pagination() {

        fragmentNotificationBottomSheetBinding.rv.setOnScrollListener(object : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                currentItem = manager.childCount
                totalItem = manager.itemCount
                scrolledItem = manager.findFirstVisibleItemPosition()

                if (isScrolling && currentItem + scrolledItem == totalItem - 2 && pageIndex < toatalPageSize) {

                    refreshNotification(pageSize, ++pageIndex, true)
                }
            }
        })
    }

    private fun notificationNavigation() {
        var unreadNotificationList = ArrayList<Int>()


        for (i in 0 until notificationList.size) {
            if (!notificationList[i].readStatus) {
                unreadNotificationList.add(notificationList[i]?.id)
            }
        }

        fragmentNotificationBottomSheetBinding.markAllRead.setOnClickListener(
            View.OnClickListener {
                setReadStatus(0)
                activityHomeActivity.searchLayout.notification.setImageDrawable(
                    resources.getDrawable(R.drawable.ic_notification_inactive)
                )
                fragmentNotificationBottomSheetBinding.markAllRead.setTextColor(
                    resources.getColor(R.color.color_text_normal)
                )
                unreadNotificationList.clear()
                bottomSheetDialog.dismiss()
            })
        if (unreadNotificationList.isEmpty()) {
            activityHomeActivity.searchLayout.notification.setImageDrawable(
                resources.getDrawable(R.drawable.ic_notification_inactive)
            )

            fragmentNotificationBottomSheetBinding.markAllRead.setTextColor(
                resources.getColor(R.color.color_text_normal)
            )

            fragmentNotificationBottomSheetBinding.markAllRead.isClickable =
                false
        } else {
            activityHomeActivity.searchLayout.notification.setImageDrawable(
                resources.getDrawable(R.drawable.ic_notification)
            )

            fragmentNotificationBottomSheetBinding.markAllRead.isClickable =
                true
            fragmentNotificationBottomSheetBinding.markAllRead.setTextColor(
                resources.getColor(R.color.black)
            )
        }

        bottomSheetDialog.findViewById<RecyclerView>(R.id.rv)?.apply {

            customAdapter = NotificationAdapter(
                this@HomeActivity,
                notificationList,
                object : NotificationAdapter.ItemsClickInterface {
                    override fun onClickItem(id: Int, posittion: Int) {

                        if (unreadNotificationList.size == 1) {
                            activityHomeActivity.searchLayout.notification.setImageDrawable(
                                resources.getDrawable(R.drawable.ic_notification_inactive)
                            )
                            unreadNotificationList.clear()
                        }
                        setReadStatus(id)
                        when (notificationList[posittion].notification.targetPage) {
                            1 -> {

                                bottomSheetDialog.dismiss()
                            }
                            2 -> {
                                val bundle = Bundle()
                                val latestUpdatesFragment =
                                    LatestUpdatesFragment()
                                latestUpdatesFragment.arguments = bundle
                                replaceFragment(
                                    latestUpdatesFragment.javaClass,
                                    "",
                                    true,
                                    bundle,
                                    null,
                                    0,
                                    true
                                )
                                bottomSheetDialog.dismiss()

                            }
                            3 -> {
                                val bundle = Bundle()
                                val insightsFragment = InsightsFragment()
                                insightsFragment.arguments = bundle
                                replaceFragment(
                                    insightsFragment.javaClass,
                                    "",
                                    true,
                                    bundle,
                                    null,
                                    0,
                                    true
                                )
                                bottomSheetDialog.dismiss()

                            }
                            4 -> {

                                (this@HomeActivity).navigate(R.id.navigation_investment)
                                bottomSheetDialog.dismiss()

                            }
                            5 -> {
                                (this@HomeActivity).navigate(R.id.navigation_portfolio)
                                bottomSheetDialog.dismiss()

                            }
                            6 -> {
                                if (!appPreference.isFacilityCard()) {
                                    (this@HomeActivity).navigate(R.id.navigation_promises)
                                } else {
                                    val bundle = Bundle()
                                    val promisesFragment = HoablPromises()
                                    promisesFragment.arguments = bundle
                                    replaceFragment(
                                        promisesFragment.javaClass,
                                        "",
                                        true,
                                        bundle,
                                        null,
                                        0,
                                        true
                                    )
                                }

                                bottomSheetDialog.dismiss()

                            }
                            7 -> {
                                (this@HomeActivity).navigate(R.id.navigation_profile)
                                bottomSheetDialog.dismiss()
                            }
                            else -> {
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                }
            )
            manager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            layoutManager = manager

            adapter = customAdapter
        }

    }

    fun refreshNotification(pageSize: Int, pageIndex: Int, refresh: Boolean) {
        homeViewModel.getNotification(pageSize, pageIndex, refresh)
            .observe(
                this
            ) {
                when (it!!.status) {
                    Status.LOADING -> {
                        fragmentNotificationBottomSheetBinding.progressBar.isVisible = true
                        fragmentNotificationBottomSheetBinding.markAllRead.isVisible = true

                    }
                    Status.ERROR -> {
                        fragmentNotificationBottomSheetBinding.progressBar.isVisible = false
                        Toast.makeText(
                            this@HomeActivity,
                            it.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Status.SUCCESS -> {
                        fragmentNotificationBottomSheetBinding.progressBar.isVisible = false
                        fragmentNotificationBottomSheetBinding.markAllRead.isVisible = true

                        totalNotification = it.data!!.totalCount
                        toatalPageSize = it.data!!.totalPages

                        for (i in 0 until it?.data?.data?.size!!) {
                            notificationList.add(it?.data?.data!![i])
                        }

                        customAdapter.notifyItemInserted(notificationList.size - 1)
                    }
                }
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }
    }

}