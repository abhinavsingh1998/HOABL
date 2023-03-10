package com.emproto.hoabl.feature.portfolio.views

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.emproto.core.BaseFragment
import com.emproto.core.Constants
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.FragmentPortfolioBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.Mixpanel
import com.emproto.hoabl.feature.home.views.fragments.ReferralDialog
import com.emproto.hoabl.feature.investment.views.CategoryListFragment
import com.emproto.hoabl.feature.investment.views.LandSkusFragment
import com.emproto.hoabl.feature.investment.views.ProjectDetailFragment
import com.emproto.hoabl.feature.portfolio.adapters.ExistingUsersPortfolioAdapter
import com.emproto.hoabl.feature.portfolio.models.PortfolioModel
import com.emproto.hoabl.viewmodels.PortfolioViewModel
import com.emproto.hoabl.viewmodels.factory.PortfolioFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.portfolio.dashboard.InvestmentHeadingDetails
import com.emproto.networklayer.response.portfolio.dashboard.PortfolioData
import com.emproto.networklayer.response.portfolio.dashboard.Project
import com.emproto.networklayer.response.portfolio.ivdetails.ProjectExtraDetails
import com.emproto.networklayer.response.watchlist.Data
import com.example.portfolioui.databinding.DailogLockPermissonBinding
import com.example.portfolioui.databinding.DailogSecurePinConfirmationBinding
import com.example.portfolioui.databinding.DialogAllowPinBinding
import com.example.portfolioui.databinding.DialogSecurePinBinding
import java.io.Serializable
import java.util.concurrent.Executor
import javax.inject.Inject


@Suppress("DEPRECATION")
class PortfolioFragment : BaseFragment(), View.OnClickListener,
    ExistingUsersPortfolioAdapter.ExistingUserInterface {

    companion object {
        const val mRequestCode = 300
        const val SETTING_REQUEST_CODE = 301
    }

    lateinit var binding: FragmentPortfolioBinding
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @Inject
    lateinit var portfolioFactory: PortfolioFactory
    lateinit var portfolioViewModel: PortfolioViewModel

    @Inject
    lateinit var appPreference: AppPreference
    private lateinit var pinPermissionDialog: DailogLockPermissonBinding
    private lateinit var pinAllowDialog: DialogAllowPinBinding

    private lateinit var dialogSecurePinBinding: DialogSecurePinBinding
    private lateinit var dialogSecurePinConfirmationBinding: DailogSecurePinConfirmationBinding

    lateinit var securePinDialog: CustomDialog
    private lateinit var securePinConfirmationDialog: CustomDialog

    private lateinit var pinDialog: CustomDialog
    private lateinit var pinAllowD: CustomDialog
    var watchList = ArrayList<Data>()

    private lateinit var adapter: ExistingUsersPortfolioAdapter
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadPermissionGranted: Boolean = false
    private var investmentId = 0
    private var doNotMissOutId = 0
    private var mPositionCompleted = 0
    private var mPositionOngoing = 0
    private lateinit var mFirstOngoingProject: Project


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        binding = FragmentPortfolioBinding.inflate(layoutInflater)
        portfolioViewModel = ViewModelProvider(
            requireActivity(), portfolioFactory
        )[PortfolioViewModel::class.java]

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: isReadPermissionGranted
            }

        initViews()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as HomeActivity).showHeader()
    }

    private fun initViews() {
        (requireActivity() as HomeActivity).showBottomNavigation()
        (requireActivity() as HomeActivity).hideBackArrow()

        pinPermissionDialog = DailogLockPermissonBinding.inflate(layoutInflater)
        pinAllowDialog = DialogAllowPinBinding.inflate(layoutInflater)
        dialogSecurePinBinding = DialogSecurePinBinding.inflate(layoutInflater)
        dialogSecurePinConfirmationBinding =
            DailogSecurePinConfirmationBinding.inflate(layoutInflater)

        pinDialog = CustomDialog(requireContext())
        pinAllowD = CustomDialog(requireContext())
        securePinDialog = CustomDialog(requireContext())
        securePinConfirmationDialog = CustomDialog(requireContext())

        pinDialog.setContentView(pinPermissionDialog.root)
        pinDialog.setCancelable(false)

        pinAllowD.setContentView(pinAllowDialog.root)
        pinAllowD.setCancelable(false)

        securePinDialog.setContentView(dialogSecurePinBinding.root)
        securePinDialog.setCancelable(false)

        securePinConfirmationDialog.setContentView(dialogSecurePinConfirmationBinding.root)
        securePinConfirmationDialog.setCancelable(false)

        pinPermissionDialog.tvActivate.setOnClickListener {
            //activate pin
            appPreference.activatePin(true)
            appPreference.savePinDialogStatus(true)
            pinDialog.dismiss()
            setUpAuthentication()
            //setUpUI(true)
        }

        pinPermissionDialog.tvDontallow.setOnClickListener {
            pinDialog.dismiss()
            pinAllowD.show()

        }
        pinAllowDialog.tvActivate.setOnClickListener {
            pinAllowD.dismiss()
            pinDialog.show()
        }



        if (appPreference.isPinDialogShown()) {
            // if dialog is shown already and pin is activated show pin screen.
            if (appPreference.getPinActivationStatus() && !(requireActivity() as HomeActivity).isFingerprintValidate()) {
                setUpInitialUI()
                setUpAuthentication()
            } else {
                setUpUI()
            }

        } else {
            pinDialog.show()
        }
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.btnExploreNewInvestmentProject.setOnClickListener(this)

        //secure pin dialog actions
        dialogSecurePinBinding.acitionSecure.setOnClickListener {
            startActivityForResult(
                Intent(android.provider.Settings.ACTION_SETTINGS), SETTING_REQUEST_CODE
            )
            securePinDialog.dismiss()
        }

        dialogSecurePinBinding.actionDontsecure.setOnClickListener {
            securePinDialog.dismiss()
            securePinConfirmationDialog.show()
        }

        dialogSecurePinConfirmationBinding.actionNo.setOnClickListener {
            securePinConfirmationDialog.dismiss()
            securePinDialog.show()
        }

        dialogSecurePinConfirmationBinding.actionYes.setOnClickListener {
            securePinConfirmationDialog.dismiss()
            setUpUI()
            (requireActivity() as HomeActivity).fingerprintValidation(true)
        }
    }

    private fun setUpInitialUI() {
        //setUpUI(false)
    }

    private fun setUpAuthentication() {
        executor = ContextCompat.getMainExecutor(this.requireContext())
        //Biometric dialog
        promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(Constants.HOABL)
            .setSubtitle(Constants.LOG_IN_USING_BIOMETRIC_CREDENTIAL)
            .setNegativeButtonText(Constants.USE_PATTERN).build()

        biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            setUpKeyGuardManager()
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            //user doesn't have any biometric
                            //show dialog here.
                            //setUpUI(true)
                            securePinDialog.show()
                        }
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            (requireActivity() as HomeActivity).onBackPressed()
                        }
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            //no enrollment

                        }
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                            //setUpUI(true)
                            setUpKeyGuardManager()
                        }
                        else -> {
                            setUpUI()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    (requireActivity() as HomeActivity).fingerprintValidation(true)
                    setUpUI()
                }

            })

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setUpKeyGuardManager()
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun setUpKeyGuardManager() {
        keyguardManager =
            (activity as HomeActivity).getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                Constants.HI_USER, Constants.VERIFY_YOUR_SERCURITY_PIN_PATTERN
            )
            if (intent != null) startActivityForResult(intent, mRequestCode)
            else {
                setUpUI()
            }
        } else {

        }
    }

    private fun setUpUI() {
        fetchUserPortfolio(false)
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            fetchUserPortfolio(true)
        }
    }

    private fun fetchUserPortfolio(refresh: Boolean) {
        if (isNetworkAvailable()) {
            portfolioViewModel.getPortfolioDashboard(refresh)
                .observe(viewLifecycleOwner, Observer { it ->
                    when (it.status) {
                        Status.LOADING -> {
                            binding.progressBaar.show()
                            binding.noInternetView.mainContainer.hide()
                        }

                        Status.SUCCESS -> {
                            binding.noInternetView.mainContainer.hide()
                            binding.refreshLayout.isRefreshing = false
                            binding.progressBaar.hide()
                            it.data?.let {
                                //load data in listview
                                binding.financialRecycler.show()
                                if (it.data.isInvestor) observePortFolioData(it)
                                else {
                                    eventTrackingExploreNewinvestment()
                                    binding.noUserView.show()
                                    binding.portfolioTopImg.visibility = View.VISIBLE
                                    binding.addYouProject.visibility = View.VISIBLE
                                    binding.instriction.visibility = View.VISIBLE
                                    binding.btnExploreNewInvestmentProject.visibility = View.VISIBLE
                                }
                            }
                        }

                        Status.ERROR -> {
                            binding.refreshLayout.isRefreshing = false
                            binding.progressBaar.hide()
                            //show error dialog
                            (requireActivity() as HomeActivity).showErrorToast(
                                it.message!!
                            )
                        }
                    }
                })
        } else {
            binding.refreshLayout.isRefreshing = false
            binding.progressBaar.hide()
            binding.financialRecycler.hide()
            binding.noInternetView.mainContainer.show()
            binding.noInternetView.textView6.setOnClickListener {
                fetchUserPortfolio(true)
            }
        }
    }

    private fun eventTrackingExploreNewinvestment() {
        Mixpanel(requireContext()).identifyFunction(
            appPreference.getMobilenum(), Mixpanel.EXPLORENEWINVESTMENT
        )
    }

    private fun observePortFolioData(portfolioData: PortfolioData) {

        portfolioData.let { data ->
            val list = ArrayList<PortfolioModel>()
            list.add(
                PortfolioModel(
                    ExistingUsersPortfolioAdapter.TYPE_HEADER, portfolioData.data.pageManagement
                )
            )
            //rearranging logic
            if (data.data.summary.completed.count > 0 && data.data.summary.ongoing.count > 0) {
                //show as normal
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_SUMMARY_COMPLETED, data.data.summary
                    )
                )
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_SUMMARY_ONGOING,
                        data.data.summary.ongoing
                    )
                )
                list.add(PortfolioModel(ExistingUsersPortfolioAdapter.TYPE_COMPLETED_INVESTMENT,
                    data.data.projects.filter { it.investment.isBookingComplete }))
                val onGoingProjects = data.data.projects.filter { !it.investment.isBookingComplete }
                if (onGoingProjects.isNotEmpty()) {
                    investmentId = onGoingProjects[0].investment.id
                    mFirstOngoingProject = onGoingProjects[0]
                }
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_ONGOING_INVESTMENT, onGoingProjects
                    )
                )
                mPositionCompleted = 3
                mPositionOngoing = 4
            } else if (data.data.summary.completed.count > 0) {
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_SUMMARY_COMPLETED, data.data.summary
                    )
                )
                list.add(PortfolioModel(ExistingUsersPortfolioAdapter.TYPE_COMPLETED_INVESTMENT,
                    data.data.projects.filter { it.investment.isBookingComplete }))
                val onGoingProjects = data.data.projects.filter { !it.investment.isBookingComplete }
                if (onGoingProjects.isNotEmpty()) {
                    investmentId = onGoingProjects[0].investment.id
                }
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_ONGOING_INVESTMENT, onGoingProjects
                    )
                )
                mPositionCompleted = 2
            } else if (data.data.summary.ongoing.count > 0) {
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_SUMMARY_ONGOING,
                        data.data.summary.ongoing
                    )
                )
                val onGoingProjects = data.data.projects.filter { !it.investment.isBookingComplete }
                if (onGoingProjects.isNotEmpty()) {
                    investmentId = onGoingProjects[0].investment.id
                    mFirstOngoingProject = onGoingProjects[0]
                }
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_ONGOING_INVESTMENT, onGoingProjects
                    )
                )
                list.add(PortfolioModel(ExistingUsersPortfolioAdapter.TYPE_COMPLETED_INVESTMENT,
                    data.data.projects.filter { it.investment.isBookingComplete }))
                mPositionOngoing = 2
            } else {
                list.add(PortfolioModel(ExistingUsersPortfolioAdapter.TYPE_COMPLETED_INVESTMENT,
                    data.data.projects.filter { it.investment.isBookingComplete }))
                val onGoingProjects = data.data.projects.filter { !it.investment.isBookingComplete }
                if (onGoingProjects.isNotEmpty()) {
                    investmentId = onGoingProjects[0].investment.id
                }
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_ONGOING_INVESTMENT, onGoingProjects
                    )
                )
            }


            if (portfolioData.data.pageManagement != null && portfolioData.data.pageManagement.data != null && portfolioData.data.pageManagement.data.page.isPromotionAndOfferActive) {
                doNotMissOutId =
                    portfolioData.data.pageManagement.data.page.promotionAndOffersProjectContentId
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_NUDGE_CARD,
                        portfolioData.data.pageManagement.data.page.promotionAndOffersMedia.value.url
                    )
                )
            }

            if (data.data.watchlist != null && data.data.watchlist.isNotEmpty()) {
                watchList.clear()
                watchList.addAll(data.data.watchlist)
                list.add(
                    PortfolioModel(
                        ExistingUsersPortfolioAdapter.TYPE_WATCHLIST, data.data.watchlist
                    )
                )
            }
            list.add(
                PortfolioModel(
                    ExistingUsersPortfolioAdapter.TYPE_REFER
                )
            )

            binding.financialRecycler.layoutManager = LinearLayoutManager(requireActivity())
            adapter = ExistingUsersPortfolioAdapter(
                requireActivity(), list, this@PortfolioFragment, appPreference
            )
            binding.financialRecycler.adapter = adapter
            binding.financialRecycler.setHasFixedSize(true)
            binding.financialRecycler.setItemViewCacheSize(10)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            mRequestCode -> {
                when (resultCode) {
                    RESULT_OK -> {
                        (requireActivity() as HomeActivity).fingerprintValidation(true)
                        setUpUI()
                    }
                }
            }
            SETTING_REQUEST_CODE -> {
                setUpAuthentication()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_explore_new_investment_project -> {
                (requireActivity() as HomeActivity).navigate(R.id.navigation_investment)
            }
        }
    }

    override fun manageProject(
        crmId: Int,
        projectId: Int,
        otherDetails: ProjectExtraDetails,
        iea: String?,
        ea: Double,
        headingDetails: InvestmentHeadingDetails,
        customerGuideLinesValueUrl: String?,
        isBookingComplete: Boolean
    ) {
        val portfolioSpecificProjectView = PortfolioSpecificProjectView()
        val arguments = Bundle()
        arguments.putInt("IVID", crmId)
        arguments.putInt("PID", projectId)
        arguments.putString("IEA", iea)
        arguments.putDouble("EA", ea)
        arguments.putBoolean("isBookingComplete", isBookingComplete)
        arguments.putString("customerGuideLinesValueUrl", customerGuideLinesValueUrl)
        portfolioSpecificProjectView.arguments = arguments
        portfolioViewModel.setprojectAddress(otherDetails)
        portfolioViewModel.saveHeadingDetails(headingDetails)
        (requireActivity() as HomeActivity).addFragment(portfolioSpecificProjectView, true)
    }

    override fun referNow() {
        val dialog = ReferralDialog()
        dialog.isCancelable = true
        dialog.show(parentFragmentManager, Constants.REFERRAL_CARD)
    }

    override fun seeAllWatchlist() {
        val list = CategoryListFragment()
        val bundle = Bundle()
        bundle.putString("Category", "Watchlist")
        bundle.putSerializable(
            "WatchlistData", watchList as Serializable
        )
        list.arguments = bundle
        (requireActivity() as HomeActivity).addFragment(list, true)
    }

    override fun investNow() {
        (requireActivity() as HomeActivity).navigate(R.id.navigation_investment)
    }

    override fun onGoingDetails() {

        val projectExtraDetails = ProjectExtraDetails(
            mFirstOngoingProject.project.address,
            mFirstOngoingProject.project.projectIcon,
            mFirstOngoingProject.project.generalInfoEscalationGraph,
            mFirstOngoingProject.project.launchName,
            mFirstOngoingProject.investment.pendingAmount,
            mFirstOngoingProject.investment.isBookingComplete,
            mFirstOngoingProject.investment.paidAmount
        )
        val headingDetails = InvestmentHeadingDetails(
            mFirstOngoingProject.project.isSimilarInvestmentActive,
            mFirstOngoingProject.project.numberOfSimilarInvestmentsToShow,
            mFirstOngoingProject.project.similarInvestmentSectionHeading,
            mFirstOngoingProject.project.isEscalationGraphActive,
            mFirstOngoingProject.project.isLatestMediaGalleryActive,
            mFirstOngoingProject.project.latestMediaGallerySectionHeading ?: "",
            mFirstOngoingProject.project.otherSectionHeadings
        )
//        project.investment.id,
//        project.project.id,
//        projectExtraDetails,
//        project.investment.projectIea,
//        project.project.generalInfoEscalationGraph.estimatedAppreciation, headingDetails,
//        project.project.customerGuideLines?.value?.url,
//        project.investment.isBookingComplete

        val portfolioSpecificProjectView = PortfolioSpecificProjectView()
        val arguments = Bundle()
        arguments.putInt("IVID", mFirstOngoingProject.investment.id)
        arguments.putInt("PID", mFirstOngoingProject.project.id)
        arguments.putString("IEA", mFirstOngoingProject.investment.projectIea)
        arguments.putDouble(
            "EA", mFirstOngoingProject.project.generalInfoEscalationGraph.estimatedAppreciation
        )
        arguments.putBoolean("isBookingComplete", mFirstOngoingProject.investment.isBookingComplete)
        arguments.putString(
            "customerGuideLinesValueUrl",
            mFirstOngoingProject.project.customerGuideLines?.value?.url
        )
        portfolioSpecificProjectView.arguments = arguments
        portfolioViewModel.setprojectAddress(projectExtraDetails)
        portfolioViewModel.saveHeadingDetails(headingDetails)
        (requireActivity() as HomeActivity).addFragment(portfolioSpecificProjectView, true)
    }

    override fun onClickOfWatchlist(projectId: Int) {
        val bundle = Bundle()
        bundle.putInt(Constants.PROJECT_ID, projectId)
        val fragment = ProjectDetailFragment()
        fragment.arguments = bundle
        (requireActivity() as HomeActivity).addFragment(
            fragment, true
        )
    }

    override fun onClickApplyNow(projectId: Int) {
        //open sku screen
        val fragment = LandSkusFragment()
        val bundle = Bundle()
        bundle.putInt(Constants.PROJECT_ID, projectId)
        fragment.arguments = bundle
        (requireActivity() as HomeActivity).addFragment(fragment, true)
    }

    override fun onClickShare() {
        (requireActivity() as HomeActivity).shareApp()
    }

    override fun doNotMissOutCard() {
        val bundle = Bundle()
        bundle.putInt(Constants.PROJECT_ID, doNotMissOutId)
        val fragment = ProjectDetailFragment()
        fragment.arguments = bundle
        (requireActivity() as HomeActivity).addFragment(
            fragment, true
        )
    }

    override fun onClickCompletedSummary() {
        binding.financialRecycler.scrollToPosition(mPositionCompleted)
    }

    override fun onClickOngoingSummary() {
        binding.financialRecycler.scrollToPosition(mPositionOngoing)
    }

}