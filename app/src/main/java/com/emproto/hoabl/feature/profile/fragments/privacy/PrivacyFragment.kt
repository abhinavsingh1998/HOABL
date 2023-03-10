package com.emproto.hoabl.feature.profile.fragments.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.hoabl.databinding.FragmentPrivacyBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.Mixpanel
import com.emproto.hoabl.viewmodels.ProfileViewModel
import com.emproto.hoabl.viewmodels.factory.ProfileFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.response.enums.Status
import javax.inject.Inject

class PrivacyFragment : BaseFragment() {
    lateinit var binding: FragmentPrivacyBinding

    @Inject
    lateinit var factory: ProfileFactory
    lateinit var profileViewModel: ProfileViewModel
    @Inject
    lateinit var appPreference: AppPreference
    val bundle = Bundle()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyBinding.inflate(inflater, container, false)
        (requireActivity() as HomeActivity).hideBottomNavigation()
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        profileViewModel =
            ViewModelProvider(requireActivity(), factory)[ProfileViewModel::class.java]
        initObserver()
        trackEvent()
        initClickListener()
        return binding.root
    }

    private fun trackEvent() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(),Mixpanel.PRIVACYPOLICY)
    }
    private fun initClickListener() {
        binding.backAction.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
    private fun initObserver() {
        profileViewModel.getPrivacyAndPolicy(5005).observe(viewLifecycleOwner) { baseResponse ->
            when (baseResponse.status) {
                Status.SUCCESS -> {
                    binding.rootView.show()
                    binding.loader.hide()
                    baseResponse.data?.let {
                        binding.tvHeading.text =
                            showHTMLText(it.data.page.termsAndConditions.displayName)
                        binding.tvDescription.text =
                            showHTMLText(it.data.page.termsAndConditions.description)
                    }
                }
                Status.ERROR -> {
                    binding.rootView.hide()
                    binding.loader.show()
                    (requireActivity() as HomeActivity).showErrorToast(
                        baseResponse.message!!
                    )
                }
                Status.LOADING -> {
                    binding.rootView.hide()
                    binding.loader.show()

                }
            }
        }
    }
}



