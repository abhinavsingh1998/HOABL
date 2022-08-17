package com.emproto.hoabl.feature.home.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.emproto.core.BaseFragment
import com.emproto.core.Constants
import com.emproto.hoabl.databinding.FragmentLatestUpdatesBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.adapters.AllLatestUpdatesAdapter
import com.emproto.hoabl.feature.home.data.LatesUpdatesPosition
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.viewmodels.HomeViewModel
import com.emproto.hoabl.viewmodels.factory.HomeFactory
import com.emproto.networklayer.response.BaseResponse
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.marketingUpdates.LatestUpdatesResponse
import javax.inject.Inject

class LatestUpdatesFragment : BaseFragment() {

    lateinit var mBinding: FragmentLatestUpdatesBinding
    lateinit var latestUpatesAdapter: AllLatestUpdatesAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    val appURL = Constants.APP_URL
    var updatesListCount = 0
    lateinit var latestHeading: String
    lateinit var latestSubHeading: String

    @Inject
    lateinit var factory: HomeFactory
    lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentLatestUpdatesBinding.inflate(layoutInflater)

        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)

        (requireActivity() as HomeActivity).hideBottomNavigation()

        homeViewModel = ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java]

        (requireActivity() as HomeActivity).showHeader()

        (requireActivity() as HomeActivity).showBackArrow()
        initView()
        initObserver(false)
        initClickListner()
        return mBinding.root
    }

    private fun initView(){
        homeViewModel.getHeaderAndList().observe(viewLifecycleOwner, Observer{
            updatesListCount= it.totalUpdatesOnListView
            latestHeading= it.latestUpdates.heading
            latestSubHeading= it.latestUpdates.subHeading

        })
    }

    private fun initObserver(refresh: Boolean) {
        if (isNetworkAvailable()){
            homeViewModel.getLatestUpdatesData(refresh, true)
                .observe(viewLifecycleOwner, object : Observer<BaseResponse<LatestUpdatesResponse>> {

                    override fun onChanged(it: BaseResponse<LatestUpdatesResponse>?) {
                        when (it?.status) {
                            Status.LOADING -> {
                                mBinding.parentScroll.hide()
                                mBinding.loader.show()
                                mBinding.noInternetView.mainContainer.hide()
                            }
                            Status.SUCCESS -> {
                                mBinding.parentScroll.show()
                                mBinding.loader.hide()

                                mBinding.headerText.text = latestHeading
                                mBinding.subHeaderTxt.text = latestSubHeading


                                it.data.let {
                                    if (it != null) {
                                        homeViewModel.setLatestUpdatesData(it.data)

                                    }

                                    //loading List
                                    it?.data!!.size
                                    latestUpatesAdapter = AllLatestUpdatesAdapter(requireActivity(),
                                        it.data,
                                        updatesListCount,
                                        object : AllLatestUpdatesAdapter.UpdatesItemsInterface {
                                            override fun onClickItem(position: Int) {
                                                homeViewModel.setSeLectedLatestUpdates(it.data[position])
                                                homeViewModel.setSelectedPosition(
                                                    LatesUpdatesPosition(
                                                        position,
                                                        updatesListCount
                                                    )
                                                )

//                                        val fragment = LatestUpdatesFragment()
//                                        val bundle = Bundle()
//                                        bundle.putInt("UpdateList", updatesListCount)
//                                        fragment.arguments = bundle
//                                        (requireActivity() as HomeActivity).addFragment(fragment, false)


                                                (requireActivity() as HomeActivity).addFragment(
                                                    LatestUpdatesDetailsFragment(),
                                                    false
                                                )
                                            }

                                        }
                                    )
                                    linearLayoutManager = LinearLayoutManager(
                                        requireContext(),
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    mBinding.recyclerLatestUpdates.layoutManager = linearLayoutManager
                                    mBinding.recyclerLatestUpdates.adapter = latestUpatesAdapter
                                }

                            }
                            Status.ERROR -> {
                                mBinding.loader.hide()
                                (requireActivity() as HomeActivity).showErrorToast(
                                    it.message!!
                                )
                                mBinding.rootView.show()
                            }
                        }
                    }
                })
        } else{
            mBinding.refressLayout.isRefreshing = false
            mBinding.loader.hide()
            mBinding.parentScroll.hide()
            mBinding.noInternetView.mainContainer.show()
            mBinding.noInternetView.textView6.setOnClickListener(View.OnClickListener {
                initObserver(true)
                (requireActivity() as HomeActivity).activityHomeActivity.searchLayout.rotateText.hide()
            })
        }


    }

    private fun initClickListner() {

        mBinding.appShareBtn.setOnClickListener(View.OnClickListener {
            share_app()
        })

        mBinding.refressLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            mBinding.loader.show()
            initObserver(true)

            mBinding.refressLayout.isRefreshing = false

        })
    }

    private fun share_app() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "The House Of Abhinandan Lodha $appURL")
        startActivity(shareIntent)
    }
}


