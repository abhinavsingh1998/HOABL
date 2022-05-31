package com.emproto.hoabl.feature.portfolio.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.DocumentsBottomSheetBinding
import com.emproto.hoabl.databinding.FragmentPortfolioSpecificViewBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.home.views.fragments.ReferralDialog
import com.emproto.hoabl.feature.investment.dialogs.ApplicationSubmitDialog
import com.emproto.hoabl.feature.investment.views.CategoryListFragment
import com.emproto.hoabl.feature.investment.views.FaqDetailFragment
import com.emproto.hoabl.feature.investment.views.ProjectDetailFragment
import com.emproto.hoabl.feature.portfolio.adapters.DocumentsAdapter
import com.emproto.hoabl.feature.portfolio.adapters.PortfolioSpecificViewAdapter
import com.emproto.hoabl.feature.promises.PromisesDetailsFragment
import com.emproto.hoabl.model.RecyclerViewItem
import com.emproto.hoabl.viewmodels.HomeViewModel
import com.emproto.hoabl.viewmodels.PortfolioViewModel
import com.emproto.hoabl.viewmodels.factory.HomeFactory
import com.emproto.hoabl.viewmodels.factory.PortfolioFactory
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.portfolio.fm.FMResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.Serializable
import javax.inject.Inject
import android.content.Intent
import android.net.Uri
import com.emproto.hoabl.feature.investment.views.mediagallery.MediaGalleryFragment
import com.emproto.hoabl.feature.investment.views.mediagallery.MediaViewFragment
import com.emproto.hoabl.model.MediaViewItem


class PortfolioSpecificProjectView : BaseFragment() {

    lateinit var binding: FragmentPortfolioSpecificViewBinding
    lateinit var portfolioSpecificViewAdapter: PortfolioSpecificViewAdapter
    lateinit var documentBinding: DocumentsBottomSheetBinding
    lateinit var docsBottomSheet: BottomSheetDialog

    @Inject
    lateinit var portfolioFactory: PortfolioFactory
    lateinit var portfolioviewmodel: PortfolioViewModel

    //only for promises details screen
    @Inject
    lateinit var homeFactory: HomeFactory
    lateinit var homeViewModel: HomeViewModel

    val list = ArrayList<RecyclerViewItem>()
    lateinit var fmData: FMResponse
    var crmId: Int = 0
    var projectId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        binding = FragmentPortfolioSpecificViewBinding.inflate(layoutInflater)

        portfolioviewmodel = ViewModelProvider(
            requireActivity(),
            portfolioFactory
        )[PortfolioViewModel::class.java]
        //getting data from arguments
        arguments?.let {
            crmId = it.getInt("IVID")
            projectId = it.getInt("PID")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setUpRecyclerView()
        initView()
        initObserver()
    }

    private fun initView() {
        (requireActivity() as HomeActivity).showHeader()
        (requireActivity() as HomeActivity).showBackArrow()
        (requireActivity() as HomeActivity).hideBottomNavigation()

        documentBinding = DocumentsBottomSheetBinding.inflate(layoutInflater)
        docsBottomSheet =
            BottomSheetDialog(this.requireContext(), R.style.BottomSheetDialogTheme)
        docsBottomSheet.setContentView(documentBinding.root)

        documentBinding.ivDocsClose.setOnClickListener {
            docsBottomSheet.dismiss()
        }
    }

    private fun initObserver() {
        portfolioviewmodel.getInvestmentDetails(crmId, projectId)
            .observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.loader.show()
                        binding.rvPortfolioSpecificView.hide()
                    }
                    Status.SUCCESS -> {
                        binding.loader.hide()
                        binding.rvPortfolioSpecificView.show()
                        it.data?.let {
                            list.clear()
                            it.data.projectExtraDetails = portfolioviewmodel.getprojectAddress()
                            list.add(
                                RecyclerViewItem(
                                    PortfolioSpecificViewAdapter.PORTFOLIO_TOP_SECTION,
                                    it.data
                                )
                            )
                            list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_FACILITY_CARD))
                            list.add(
                                RecyclerViewItem(
                                    PortfolioSpecificViewAdapter.PORTFOLIO_TRENDING_IMAGES,
                                    it.data.projectInformation.latestMediaGalleryOrProjectContent[0]
                                )
                            )
                            list.add(
                                RecyclerViewItem(
                                    PortfolioSpecificViewAdapter.PORTFOLIO_PROMISES,
                                    it.data.projectPromises
                                )
                            )
                            list.add(
                                RecyclerViewItem(
                                    PortfolioSpecificViewAdapter.PORTFOLIO_GRAPH,
                                    it.data.projectExtraDetails.graphData
                                )
                            )
                            list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_REFERNOW))
                            if (it.data.projectInformation.projectContentsAndFaqs.isNotEmpty()) {
                                list.add(
                                    RecyclerViewItem(
                                        PortfolioSpecificViewAdapter.PORTFOLIO_FAQ,
                                        it.data.projectInformation.projectContentsAndFaqs
                                    )
                                )
                            }
                            if (it.data.projectInformation.similarInvestments.isNotEmpty()) {
                                list.add(
                                    RecyclerViewItem(
                                        PortfolioSpecificViewAdapter.PORTFOLIO_SIMILER_INVESTMENT,
                                        it.data.projectInformation.similarInvestments
                                    )
                                )
                            }
                            portfolioSpecificViewAdapter =
                                PortfolioSpecificViewAdapter(
                                    this.requireContext(),
                                    list,
                                    object :
                                        PortfolioSpecificViewAdapter.InvestmentScreenInterface {
                                        override fun onClickFacilityCard() {
                                            (requireActivity() as HomeActivity).addFragment(
                                                FmFragment.newInstance(
                                                    fmData.data.web_url,
                                                    ""
                                                ), false
                                            )
                                        }

                                        override fun seeAllCard() {
                                            docsBottomSheet.show()
                                        }

                                        override fun seeProjectTimeline() {
                                            (requireActivity() as HomeActivity).addFragment(
                                                ProjectTimelineFragment.newInstance(
                                                    "",
                                                    ""
                                                ), false
                                            )
                                        }

                                        override fun seeBookingJourney() {
                                            (requireActivity() as HomeActivity).addFragment(
                                                BookingjourneyFragment.newInstance(
                                                    "",
                                                    ""
                                                ), false
                                            )
                                        }

                                        override fun referNow() {
                                            val dialog = ReferralDialog()
                                            dialog.isCancelable = true
                                            dialog.show(parentFragmentManager, "Refrral card")
                                        }

                                        override fun seeAllSimilarInvestment() {
                                            val list = CategoryListFragment()
                                            val bundle = Bundle()
                                            bundle.putString("Category", "Similar Investment")
                                            bundle.putSerializable(
                                                "SimilarData",
                                                it.data.projectInformation.similarInvestments as Serializable
                                            )
                                            list.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                list,
                                                false
                                            )
                                        }

                                        override fun onClickSimilarInvestment(project: Int) {
                                            val bundle = Bundle()
                                            bundle.putInt("ProjectId", projectId)
                                            val fragment = ProjectDetailFragment()
                                            fragment.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                fragment, false
                                            )
                                        }

                                        override fun onApplySinvestment(projectId: Int) {
                                        }

                                        override fun readAllFaq(position: Int) {
                                            val fragment = FaqDetailFragment()
                                            val bundle = Bundle()
                                            bundle.putInt("ProjectId", projectId)
                                            if (position != -1) {
                                                bundle.putInt("SelectedPosition", position)
                                            }
                                            fragment.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                fragment,
                                                false
                                            )

                                        }

                                        override fun seePromisesDetails(position: Int) {
                                            homeViewModel =
                                                ViewModelProvider(
                                                    requireActivity(),
                                                    homeFactory
                                                ).get(HomeViewModel::class.java)
                                            val details = it.data.projectPromises.data[position]
                                            homeViewModel.setSelectedPromise(details)
                                            (requireActivity() as HomeActivity).addFragment(
                                                PromisesDetailsFragment(),
                                                false
                                            )

                                        }

                                        override fun moreAboutPromises() {
                                            val applicationSubmitDialog = ApplicationSubmitDialog(
                                                "Request Sent.",
                                                "A relationship manager will get back to you to discuss more about it.",
                                                false
                                            )
                                            applicationSubmitDialog.show(
                                                parentFragmentManager,
                                                "ApplicationSubmitDialog"
                                            )
                                        }

                                        override fun seeProjectDetails(projectId: Int) {
                                            val bundle = Bundle()
                                            bundle.putInt("ProjectId", projectId)
                                            val fragment = ProjectDetailFragment()
                                            fragment.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                fragment, false
                                            )
                                        }

                                        override fun seeOnMap(latitude: String, longitude: String) {
                                            val mapUri: Uri =
                                                Uri.parse("geo:0,0?q=$latitude,$longitude(Hoabl)")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            startActivity(mapIntent)
                                        }

                                        override fun onClickImage(url: String) {
                                            val mediaViewItem =
                                                MediaViewItem("Photo", url)
                                            val bundle = Bundle()
                                            bundle.putSerializable("Data", mediaViewItem)
                                            val fragment = MediaViewFragment()
                                            fragment.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                fragment, false
                                            )
                                        }

                                        override fun seeAllImages(imagesList: ArrayList<MediaViewItem>) {
                                            val fragment = MediaGalleryFragment()
                                            val bundle = Bundle()
                                            bundle.putSerializable("Data", imagesList)
                                            fragment.arguments = bundle
                                            (requireActivity() as HomeActivity).addFragment(
                                                fragment, false
                                            )
                                        }

                                        override fun shareApp() {
                                            (requireActivity() as HomeActivity).share_app()
                                        }

                                        override fun onClickAsk() {
                                            showError("Chat is in Development", binding.root)
                                        }

                                    })
                            binding.rvPortfolioSpecificView.adapter = portfolioSpecificViewAdapter

                            fetchDocuments(it.data.investmentInformation.crmProjectId)
                        }


                    }
                    Status.ERROR -> {
                        binding.loader.hide()
                        (requireActivity() as HomeActivity).showErrorToast(
                            it.message!!
                        )
                    }

                }
            })

    }

    private fun fetchDocuments(id: String) {
        portfolioviewmodel.getFacilityManagment().observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data.let {
                        fmData = it!!
                    }
                }
            }
        })
        portfolioviewmodel.getDocumentList(id).observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (it.data!!.data.isNotEmpty()) {
                        list.removeAt(2)
                        list.add(
                            2,
                            RecyclerViewItem(
                                PortfolioSpecificViewAdapter.PORTFOLIO_DOCUMENTS,
                                it.data!!.data
                            )
                        )
                        portfolioSpecificViewAdapter.notifyItemChanged(3)
                        it.data?.let {
                            val adapter = DocumentsAdapter(it.data, true)
                            documentBinding.rvDocsItemRecycler.adapter = adapter
                        }
                    }
                }
                Status.ERROR -> {

                }

            }
        })
    }

    private fun setUpRecyclerView() {

        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_TOP_SECTION))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_PENDINGCARD))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_FACILITY_CARD))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_DOCUMENTS))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_TRENDING_IMAGES))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_PROMISES))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_GRAPH))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_REFERNOW))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_FAQ))
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_SIMILER_INVESTMENT))
        //portfolioSpecificViewAdapter = PortfolioSpecificViewAdapter(this.requireContext(), list)
        binding.rvPortfolioSpecificView.adapter = portfolioSpecificViewAdapter
        //portfolioSpecificViewAdapter.setItemClickListener(onPortfolioSpecificItemClickListener)
    }
}