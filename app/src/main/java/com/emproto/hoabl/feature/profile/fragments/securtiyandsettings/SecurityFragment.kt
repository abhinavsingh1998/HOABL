package com.emproto.hoabl.feature.profile.fragments.securtiyandsettings

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.core.Constants
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.FragmentSecurityBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.Mixpanel
import com.emproto.hoabl.feature.investment.dialogs.ApplicationSubmitDialog
import com.emproto.hoabl.feature.login.AuthActivity
import com.emproto.hoabl.feature.profile.adapter.SecurityAdapter
import com.emproto.hoabl.feature.profile.adapter.SettingsAdapter
import com.emproto.hoabl.model.RecyclerViewItem
import com.emproto.hoabl.utils.ItemClickListener
import com.emproto.hoabl.viewmodels.ProfileViewModel
import com.emproto.hoabl.viewmodels.factory.ProfileFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.request.profile.ReportSecurityRequest
import com.emproto.networklayer.request.profile.WhatsappConsentBody
import com.emproto.networklayer.response.enums.Status
import com.example.portfolioui.databinding.LogoutAllConfirmationBinding
import javax.inject.Inject


class SecurityFragment : BaseFragment() {
    @Inject
    lateinit var profileFactory: ProfileFactory
    private lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var appPreference: AppPreference

    lateinit var binding: FragmentSecurityBinding
    lateinit var adapter: SettingsAdapter
    lateinit var logoutDialog: Dialog

    val bundle = Bundle()

    private var isSecurityTipsActive = true

    companion object {
        const val SPEECH_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        (requireActivity() as HomeActivity).hideBottomNavigation()
        profileViewModel =
            ViewModelProvider(requireActivity(), profileFactory)[ProfileViewModel::class.java]
        binding = FragmentSecurityBinding.inflate(layoutInflater)
        arguments?.let {
            it.getBoolean(Constants.IS_SECURITY_TIPS_ACTIVE).also { isSecurityTipsActive = it }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataList: ArrayList<RecyclerViewItem> = ArrayList()
        dataList.add(RecyclerViewItem(SecurityAdapter.VIEW_REPORT))
        dataList.add(RecyclerViewItem(SecurityAdapter.VIEW_SECURITY_WHATSAPP_COMMUNICATION))
        when (isSecurityTipsActive) {
            true -> {
                dataList.add(RecyclerViewItem(SecurityAdapter.VIEW_SECURITY_TIPS))
            }
            else -> {}
        }
        dataList.add(RecyclerViewItem(SecurityAdapter.VIEW_SIGN_OUT_ALL))
        dataList.add(RecyclerViewItem(SecurityAdapter.VIEW_SETTINGS_ALL_OPTIONS))

        eventTrackingSecuritySettings()

        val logoutDialogLayout = LogoutAllConfirmationBinding.inflate(layoutInflater)
        logoutDialog = Dialog(requireContext())
        logoutDialog.setCancelable(false)
        logoutDialog.setContentView(logoutDialogLayout.root)

        val adapter = SecurityAdapter(
            this.requireContext(),
            dataList,
            itemClickListener,
            appPreference.getWhatsappStatus(),
            appPreference
        )
        binding.rvHelpCenter.adapter = adapter

        binding.arrowimage.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        logoutDialogLayout.actionYes.setOnClickListener {
            logOutFromAllDevices()
        }

        logoutDialogLayout.actionNo.setOnClickListener {
            logoutDialog.dismiss()
        }

    }

    private fun eventTrackingSecuritySettings() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(), Mixpanel.SECURITYANDSETTINGS)
    }

    val itemClickListener = object : ItemClickListener {
        override fun onItemClicked(
            view: View,
            position: Int,
            item: String) {
            when (view.id) {
                R.id.switch1 -> {
                    when (item) {
                        Constants.TRUE -> {
                            appPreference.whatsappStatus(true)
                            callWhatsAppConsentApi(appPreference.getWhatsappStatus(), appPreference.getPushNotificationStatus())
                        }
                        Constants.FALSE -> {
                            appPreference.whatsappStatus(false)
                            callWhatsAppConsentApi(appPreference.getWhatsappStatus(), appPreference.getPushNotificationStatus())
                        }
                    }
                }
                R.id.cl_security_tips -> {

                    eventTrackingSecurityTips()
                    when (item) {
                        Constants.SECURITY_TIPS -> {
                            val securityTipsFragment = SecurityTipsFragment()
                            (requireActivity() as HomeActivity).addFragment(
                                securityTipsFragment,
                                false
                            )
                        }
                        Constants.SIGN_OUT_FROM_ALL_DEVICES -> {
                            logoutDialog.show()
                        }
                    }

                }

                R.id.setting_switch ->{
                    when (item) {
                        Constants.TRUE -> {
                            eventTrackingSendPushNotifications()
                            appPreference.pushNotificationStatus(true)
                            callWhatsAppConsentApi(appPreference.getWhatsappStatus(), appPreference.getPushNotificationStatus())
                        }
                        Constants.FALSE -> {
                            appPreference.pushNotificationStatus(false)
                            callWhatsAppConsentApi(appPreference.getWhatsappStatus(), appPreference.getPushNotificationStatus())
                        }
                        "Voice Command" -> {
//                            displaySpeechRecognizer()
                        }
                    }
                }
                R.id.button_view -> {
                    profileViewModel.submitTroubleCase(
                        ReportSecurityRequest(
                            caseType = "1005",
                            description = Constants.I_WANT_TO_RAISE_A_SECURITY_EMERGENCY
                        )
                    ).observe(viewLifecycleOwner) {
                        when (it.status) {
                            Status.LOADING -> {
                                binding.progressBar.show()
                            }
                            Status.SUCCESS -> {
                                binding.progressBar.hide()
                                if (it.data != null) {
                                    it.data?.let {
                                        val applicationSubmitDialog = ApplicationSubmitDialog(
                                            Constants.APPLICATION_SUBMIT_DIALOG_TITLE,
                                            Constants.APPLICATION_SUBMIT_DIALOG_DESCRIPTION,
                                            false
                                        )
                                        applicationSubmitDialog.show(
                                            parentFragmentManager,
                                            Constants.APPLICATION_SUBMIT_DIALOG
                                        )
                                    }
                                }
                            }
                            Status.ERROR -> {
                                binding.progressBar.hide()
                                it.data
                                (requireActivity() as HomeActivity).showErrorToast(
                                    it.message!!
                                )
                            }
                        }
                    }
                }

            }
        }
    }

    private fun eventTrackingSendPushNotifications() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(), Mixpanel.SENDPUSHNOTIFICATIONS)
    }

    private fun eventTrackingSecurityTips() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(), Mixpanel.READSECURITYTIPS)
    }


    private fun logOutFromAllDevices() {
        profileViewModel.logOutFromAll().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.show()
                }
                Status.SUCCESS -> {
                    binding.progressBar.hide()
                    if (it.data != null) {
                        it.data?.let {
                            logoutDialog.dismiss()
                            appPreference.saveLogin(false)
                            startActivity(Intent(context, AuthActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
                Status.ERROR -> {
                    logoutDialog.dismiss()
                    binding.progressBar.hide()
                    (requireActivity() as HomeActivity).showErrorToast(
                        it.message!!
                    )
                }
            }
        }
    }

    private fun callWhatsAppConsentApi(status: Boolean, showPushNotifications: Boolean) {
        profileViewModel.putWhatsappconsent(
            WhatsappConsentBody(
                whatsappConsent = status,
                showPushNotifications = showPushNotifications

            )
        ).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.show()
                }
                Status.SUCCESS -> {
                    binding.progressBar.hide()
                    if (it.data != null) {
                        it.data?.let {

                        }
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.hide()
                    it.data
                    (requireActivity() as HomeActivity).showErrorToast(
                        it.message!!
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results!![0]
                }
            // Do something with spokenText.
            Toast.makeText(this.requireContext(), spokenText.toString(), Toast.LENGTH_SHORT).show()
            when {
                spokenText.toString().contains(Constants.INVESTMENT, ignoreCase = true) -> {
                    (requireActivity() as HomeActivity).navigate(R.id.navigation_investment)
                }
                spokenText.toString().contains(Constants.PROMISE, ignoreCase = true) -> {
                    (requireActivity() as HomeActivity).navigate(R.id.navigation_promises)
                }
                spokenText.toString().contains(Constants.PORTFOLIO, ignoreCase = true) -> {
                    (requireActivity() as HomeActivity).navigate(R.id.navigation_portfolio)
                }
                spokenText.toString().contains(Constants.HOME, ignoreCase = true) -> {
                    (requireActivity() as HomeActivity).navigate(R.id.navigation_hoabl)
                }
                spokenText.toString().contains(Constants.PROFILE, ignoreCase = true) -> {
                    (requireActivity() as HomeActivity).navigate(R.id.navigation_profile)
                }
            }
        }
    }

}


