package com.emproto.hoabl.feature.profile.fragments.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emproto.hoabl.databinding.FragmentFaqBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.profile.adapter.faq.ProfileFaqCategoryAdapter
import com.emproto.hoabl.viewmodels.ProfileViewModel
import com.emproto.hoabl.viewmodels.factory.HomeFactory
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.profile.ProfileFaqResponse
import javax.inject.Inject


class ProfileFaqFragment : Fragment() {

    @Inject
    lateinit var homeFactory: HomeFactory
    private lateinit var profileViewModel: ProfileViewModel
    private var faqCategory = ArrayList<ProfileFaqResponse.ProfileFaqData>()
    lateinit var binding: FragmentFaqBinding
    private lateinit var profileFaqCategoryAdapter: ProfileFaqCategoryAdapter
    val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaqBinding.inflate(layoutInflater, container, false)
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        profileViewModel =
            ViewModelProvider(requireActivity(), homeFactory)[ProfileViewModel::class.java]
        initClickListener()
        (requireActivity() as HomeActivity).hideBottomNavigation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProfileFaqData()
    }

    private fun getProfileFaqData() {
        val typeOfFAQ = "3001"
        profileViewModel.getFaqList(typeOfFAQ).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.loader.show()
                }
                Status.SUCCESS -> {
                    binding.loader.hide()
                    if (it.data?.data != null) {
                        setAdapter()
                        profileFaqCategoryAdapter.notifyDataSetChanged()
                    }
                }
                Status.ERROR -> {
                    binding.loader.hide()
                    (requireActivity() as HomeActivity).showErrorToast(it.message!!)
                }
            }
        }

    }

    private fun setAdapter() {
        binding.rvHelpCenterCategory.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        profileFaqCategoryAdapter = ProfileFaqCategoryAdapter(context, faqCategory)
        binding.rvHelpCenterCategory.adapter = profileFaqCategoryAdapter
    }


    private fun initClickListener() {
        binding.backAction.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
    }
}
