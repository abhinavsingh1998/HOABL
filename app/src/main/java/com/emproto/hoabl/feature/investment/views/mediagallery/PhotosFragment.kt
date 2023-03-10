package com.emproto.hoabl.feature.investment.views.mediagallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.core.Constants
import com.emproto.hoabl.databinding.FragmentPhotosBinding
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

class PhotosFragment : BaseFragment() {

    @Inject
    lateinit var investmentFactory: InvestmentFactory
    lateinit var investmentViewModel: InvestmentViewModel
    lateinit var binding: FragmentPhotosBinding
    private lateinit var mediaPhotosAdapter: MediaPhotosAdapter
    @Inject
    lateinit var appPreference: AppPreference
    private var allImageList = ArrayList<MediaViewItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotosBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        eventTrackingMediaGallerySectionSelection()
        initObserver()
    }

    private fun eventTrackingMediaGallerySectionSelection() {
        Mixpanel(requireContext()).identifyFunction(appPreference.getMobilenum(), Mixpanel.MEDIAGALLERYSECTIONSELECTION)
    }

    private fun initViewModel() {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        investmentViewModel =
            ViewModelProvider(
                requireActivity(),
                investmentFactory
            )[InvestmentViewModel::class.java]
        (requireActivity() as HomeActivity).showBackArrow()
        (requireActivity() as HomeActivity).showHeader()
    }

    private fun initObserver() {
        val list1 = investmentViewModel.getMediaContent().filter { it.title == "Images" }
        setUpRecyclerView(list1)
    }

    private fun setUpRecyclerView(list1: List<MediaViewItem>) {
        val list = ArrayList<MediaGalleryItem>()
        list.add(MediaGalleryItem(2, Constants.PHOTOS))

        allImageList.clear()
        for (item in list1) {
            allImageList.add(item)
        }
        investmentViewModel.getImageActive().observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.tvNoData.visibility = View.GONE
                    binding.ivNoData.visibility = View.GONE
                    binding.rvMainPhotos.visibility = View.VISIBLE
                }
                false -> {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.ivNoData.visibility = View.VISIBLE
                    binding.rvMainPhotos.visibility = View.GONE
                }
            }
        }

        mediaPhotosAdapter =
            MediaPhotosAdapter(
                this.requireContext(),
                list,
                itemClickListener,
                list1,
                youtubeItemClickListener
            )
        binding.rvMainPhotos.adapter = mediaPhotosAdapter
    }

    val youtubeItemClickListener = object : YoutubeItemClickListener {
        override fun onItemClicked(view: View, position: Int, url: String, title: String) {

        }

    }

    private val itemClickListener = object : MediaItemClickListener {
        override fun onItemClicked(view: View, position: Int, item: MediaViewItem) {
            investmentViewModel.setMediaListItem(allImageList)
            val mediaViewFragment = MediaViewFragment()
            val bundle = Bundle()
            bundle.putSerializable(Constants.DATA, item)
            bundle.putInt(Constants.IMAGE_POSITION, position)
            mediaViewFragment.arguments = bundle
            (requireActivity() as HomeActivity).addFragment(mediaViewFragment, true)
        }
    }

}