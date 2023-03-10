package com.emproto.hoabl.feature.investment.views.mediagallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.core.Constants
import com.emproto.hoabl.databinding.FragmentVideosBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.Mixpanel
import com.emproto.hoabl.feature.investment.adapters.MediaPhotosAdapter
import com.emproto.hoabl.model.MediaGalleryItem
import com.emproto.hoabl.model.MediaViewItem
import com.emproto.hoabl.utils.MediaItemClickListener
import com.emproto.hoabl.utils.YoutubeItemClickListener
import com.emproto.hoabl.viewmodels.InvestmentViewModel
import com.emproto.hoabl.viewmodels.factory.InvestmentFactory
import com.emproto.networklayer.preferences.AppPreference
import javax.inject.Inject

class DroneFragment : BaseFragment() {

    @Inject
    lateinit var appPreference: AppPreference
    @Inject
    lateinit var investmentFactory: InvestmentFactory
    lateinit var binding: FragmentVideosBinding
    lateinit var investmentViewModel: InvestmentViewModel
    private lateinit var mediaPhotosAdapter: MediaPhotosAdapter

    private var isYoutubeVideo = true





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initObserver()
        eventTrackingMediaGallery()
    }

    private fun eventTrackingMediaGallery() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(), Mixpanel.MEDIAGALLERYSECTIONSELECTION)
    }

    private fun initObserver() {
        val mediaList =
            investmentViewModel.getMediaContent().filter { it.title == Constants.DRONE_SHOOT }
        val list = ArrayList<MediaGalleryItem>()
//        list.add(MediaGalleryItem(1, "Videos"))
        list.add(MediaGalleryItem(2, Constants.VIDEOS))

        val videoList = arrayListOf<String>()
        for (item in mediaList) {
            videoList.add(item.media)
        }
        investmentViewModel.getDroneActive().observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.tvNoData.visibility = View.GONE
                    binding.ivNoData.visibility = View.GONE
                    binding.rvMainVideos.visibility = View.VISIBLE
                }
                false -> {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.ivNoData.visibility = View.VISIBLE
                    binding.rvMainVideos.visibility = View.GONE
                }
            }
        }
        mediaPhotosAdapter =
            MediaPhotosAdapter(
                this.requireContext(),
                list,
                mediaItemClickListener,
                mediaList,
                itemClickListener
            )
        binding.rvMainVideos.adapter = mediaPhotosAdapter
    }

    private fun initViewModel() {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        investmentViewModel =
            ViewModelProvider(requireActivity(), investmentFactory)[InvestmentViewModel::class.java]
        (requireActivity() as HomeActivity).showBackArrow()
        (requireActivity() as HomeActivity).showHeader()

    }

    private val mediaItemClickListener = object : MediaItemClickListener {
        override fun onItemClicked(view: View, position: Int, item: MediaViewItem) {

        }
    }

    private val itemClickListener = object : YoutubeItemClickListener {
        override fun onItemClicked(view: View, position: Int, url: String, title: String) {
            when (isYoutubeVideo) {
                true -> {
                    val intent =
                        Intent(this@DroneFragment.requireActivity(), YoutubeActivity::class.java)
                    intent.putExtra(Constants.YOUTUBE_VIDEO_ID, url)
                    intent.putExtra(Constants.VIDEO_TITLE, title)
                    startActivity(intent)
                }
                else -> {}
            }
        }
    }

}