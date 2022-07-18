package com.emproto.hoabl.feature.portfolio.views

import android.Manifest
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
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.emproto.core.Utility
import com.emproto.hoabl.feature.chat.views.fragments.ChatsFragment
import com.emproto.hoabl.feature.investment.views.LandSkusFragment
import com.emproto.hoabl.feature.investment.views.mediagallery.MediaGalleryFragment
import com.emproto.hoabl.feature.investment.views.mediagallery.MediaViewFragment
import com.emproto.hoabl.feature.portfolio.adapters.DocumentInterface
import com.emproto.hoabl.model.MediaViewItem
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.hoabl.viewmodels.InvestmentViewModel
import com.emproto.hoabl.viewmodels.factory.InvestmentFactory
import com.emproto.networklayer.request.login.TroubleSigningRequest
import com.emproto.networklayer.response.bookingjourney.BJHeader
import com.emproto.networklayer.response.portfolio.ivdetails.InvestmentDetailsResponse


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

    @Inject
    lateinit var investmentFactory: InvestmentFactory
    lateinit var investmentViewModel: InvestmentViewModel

    val list = ArrayList<RecyclerViewItem>()
    var crmId: Int = 0
    var projectId: Int = 0
    var iea: String = ""
    var ea: Double = 0.0
    var allMediaList = ArrayList<MediaViewItem>()

    @Inject
    lateinit var appPreference: AppPreference

    private var isReadPermissonGranted: Boolean = false
    private var isWritePermissonGranted: Boolean = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val permissionRequest: MutableList<String> = ArrayList()
    var base64Data: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        binding = FragmentPortfolioSpecificViewBinding.inflate(layoutInflater)

        portfolioviewmodel =
            ViewModelProvider(requireActivity(), portfolioFactory)[PortfolioViewModel::class.java]
        investmentViewModel =
            ViewModelProvider(requireActivity(), investmentFactory)[InvestmentViewModel::class.java]
        //getting data from arguments
        arguments?.let {
            crmId = it.getInt("IVID")
            projectId = it.getInt("PID")
            it.getString("IEA")?.let {
                iea = it
            }
            it.getDouble("EA")?.let {
                ea = it
            }
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

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadPermissonGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: isReadPermissonGranted
                isWritePermissonGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: isWritePermissonGranted

                if (isReadPermissonGranted && isWritePermissonGranted) {
                    openPdf(base64Data)
                }
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
                            loadInvestmentDetails(it)
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

    private fun loadInvestmentDetails(it: InvestmentDetailsResponse) {
        //saving project name and address for booking journey screen
        val headingDetails = portfolioviewmodel.getHeadingDetails()
        portfolioviewmodel.setInvestmentInfo(it.data.investmentInformation)
        it.data.investmentInformation.launchName = it.data.projectInformation.launchName
        it.data.investmentInformation.address = portfolioviewmodel.getprojectAddress().address

        val bjHeader = BJHeader(
            it.data.investmentInformation.launchName,
            it.data.investmentInformation.address?.let { it.city } + " , " + it.data.investmentInformation.address?.let { it.state },
            it.data.investmentInformation.bookingStatus,
            it.data.investmentInformation.owners[0],
            "Hoabl/${it.data.investmentInformation.crmInventory.name}"
        )
        portfolioviewmodel.saveBookingHeader(bjHeader)
        var itemId = 0

        list.clear()
        it.data.projectExtraDetails = portfolioviewmodel.getprojectAddress()

        //top section in investment details
        list.add(
            RecyclerViewItem(
                PortfolioSpecificViewAdapter.PORTFOLIO_TOP_SECTION,
                it.data,
                iea
            )
        )

        //adding pending cards
        if (!it.data.investmentInformation.isBookingComplete) {

            val filteredpayments = it.data.investmentInformation.paymentSchedules.filter {
                it.targetDate != null && !it.isPaymentDone && Utility.compareDates(it.targetDate)
            }
            if (filteredpayments.isNotEmpty()) {
                list.add(
                    RecyclerViewItem(
                        PortfolioSpecificViewAdapter.PORTFOLIO_PENDINGCARD,
                        filteredpayments, iea, 0.0, it.data.investmentInformation.id
                    )
                )
            }
        }

        // facility card
        if (appPreference.isFacilityCard())
            list.add(
                RecyclerViewItem(
                    PortfolioSpecificViewAdapter.PORTFOLIO_FACILITY_CARD,
                    appPreference.getOfferUrl()
                ),
            )

        //adding document
        if (it.data.documentList != null) {
            list.add(
                RecyclerViewItem(
                    PortfolioSpecificViewAdapter.PORTFOLIO_DOCUMENTS,
                    it.data.documentList
                )
            )
        }

        //adding trending video and images
        if (headingDetails.isLatestMediaGalleryActive) {
            allMediaList.clear()
            if (it.data.projectInformation.latestMediaGalleryOrProjectContent != null
                && it.data.projectInformation.latestMediaGalleryOrProjectContent.isNotEmpty()
            ) {
                for (item in it.data.projectInformation.latestMediaGalleryOrProjectContent[0].images) {
                    itemId++
                    allMediaList.add(
                        MediaViewItem(
                            item.mediaContentType,
                            item.mediaContent.value.url,
                            title = "Images",
                            id = itemId,
                            name = item.name
                        )
                    )
                }

                for (item in it.data.projectInformation.latestMediaGalleryOrProjectContent[0].threeSixtyImages) {
                    itemId++
                    allMediaList.add(
                        MediaViewItem(
                            item.mediaContentType,
                            item.mediaContent.value.url,
                            title = "ThreeSixtyImages",
                            id = itemId,
                            name = item.name
                        )
                    )
                }
                list.add(
                    RecyclerViewItem(
                        PortfolioSpecificViewAdapter.PORTFOLIO_TRENDING_IMAGES,
                        it.data.projectInformation.latestMediaGalleryOrProjectContent[0]
                    )
                )

                //setting media visibility
                investmentViewModel.setImageActive(it.data.projectInformation.latestMediaGalleryOrProjectContent[0].isImagesActive)
                investmentViewModel.setVideoActive(it.data.projectInformation.latestMediaGalleryOrProjectContent[0].isVideosActive)
                investmentViewModel.setDroneActive(it.data.projectInformation.latestMediaGalleryOrProjectContent[0].isDroneShootsActive)
                investmentViewModel.setThreeSixtyActive(it.data.projectInformation.latestMediaGalleryOrProjectContent[0].isThreeSixtyImagesActive)
            }
        }
        //adding promises
        list.add(
            RecyclerViewItem(
                PortfolioSpecificViewAdapter.PORTFOLIO_PROMISES,
                it.data.projectPromises
            )
        )

        //adding graph
        if (headingDetails.isEscalationGraphActive) {
            list.add(
                RecyclerViewItem(
                    PortfolioSpecificViewAdapter.PORTFOLIO_GRAPH,
                    it.data.projectExtraDetails.graphData, "", ea

                )
            )
        }

        //adding refer now
        list.add(RecyclerViewItem(PortfolioSpecificViewAdapter.PORTFOLIO_REFERNOW))

        //adding faq section
        if (it.data.projectInformation.projectContentsAndFaqs != null) {
            if (it.data.projectInformation.projectContentsAndFaqs.isNotEmpty()) {
                list.add(
                    RecyclerViewItem(
                        PortfolioSpecificViewAdapter.PORTFOLIO_FAQ,
                        it.data.projectInformation.projectContentsAndFaqs
                    )
                )
            }
        }

        //adding similar investment
        if (headingDetails.isSimilarInvestmentActive && it.data.projectInformation.similarInvestments != null) {
            if (it.data.projectInformation.similarInvestments.isNotEmpty()) {
                list.add(
                    RecyclerViewItem(
                        PortfolioSpecificViewAdapter.PORTFOLIO_SIMILER_INVESTMENT,
                        it.data.projectInformation.similarInvestments
                    )
                )
            }
        }
        portfolioSpecificViewAdapter =
            PortfolioSpecificViewAdapter(
                this.requireContext(),
                list,
                object :
                    PortfolioSpecificViewAdapter.InvestmentScreenInterface {
                    override fun onClickFacilityCard() {

                        portfolioviewmodel.getFacilityManagment("", "")
                            .observe(viewLifecycleOwner, Observer {
                                when (it.status) {
                                    Status.SUCCESS -> {
                                        it.data.let {
                                            if (it != null) {
                                                (requireActivity() as HomeActivity).addFragment(
                                                    FmFragment.newInstance(
                                                        it.data.web_url,
                                                        ""
                                                    ), false
                                                )
                                            } else {
                                                (requireActivity() as HomeActivity).showErrorToast(
                                                    "Something Went Wrong"
                                                )
                                            }
                                        }
                                    }
                                }
                            })

                    }

                    override fun seeAllCard() {
                        docsBottomSheet.show()
                    }

                    override fun seeProjectTimeline(id: Int) {
                        (requireActivity() as HomeActivity).addFragment(
                            ProjectTimelineFragment.newInstance(
                                id,
                                ""
                            ), true
                        )
                    }

                    override fun seeBookingJourney(id: Int) {
                        (requireActivity() as HomeActivity).addFragment(
                            BookingjourneyFragment.newInstance(
                                id,
                                ""
                            ), true
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
                        bundle.putInt("ProjectId", project)
                        val fragment = ProjectDetailFragment()
                        fragment.arguments = bundle
                        (requireActivity() as HomeActivity).addFragment(
                            fragment, false
                        )
                    }

                    override fun onApplySinvestment(project: Int) {
                        val fragment = LandSkusFragment()
                        val bundle = Bundle()
                        bundle.putInt("ProjectId", project)
                        fragment.arguments = bundle
                        (requireActivity() as HomeActivity).addFragment(fragment, false)
                    }

                    override fun readAllFaq(position: Int, faqId: Int) {
                        val fragment = FaqDetailFragment()
                        val bundle = Bundle()
                        bundle.putInt("ProjectId", projectId)
                        if (position != -1) {
                            bundle.putInt("SelectedPosition", position)
                            bundle.putInt("FaqId", faqId)
                        }
                        bundle.putBoolean("isFromInvestment", true)
                        bundle.putString("ProjectName", "")
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
                        val troubleSigningRequest = TroubleSigningRequest(
                            "1002",
                            "91",
                            "I want to know more about - promises",
                            "",
                            "",
                            appPreference.getMobilenum()
                        )
                        portfolioviewmodel.submitTroubleCase(troubleSigningRequest)
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
                        try {
                            val mapUri: Uri =
                                Uri.parse("geo:0,0?q=$latitude,$longitude(Hoabl)")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            startActivity(mapIntent)
                        } catch (e: Exception) {

                        }

                    }

                    override fun onClickImage(mediaViewItem: MediaViewItem, position: Int) {
                        investmentViewModel =
                            ViewModelProvider(
                                requireActivity(),
                                investmentFactory
                            ).get(InvestmentViewModel::class.java)
//                        val mediaViewItem = MediaViewItem(media.mediaContentType, item.mediaContent.value.url,title = "Images", id = itemId, name = item.name)
                        val bundle = Bundle()
                        Log.d(
                            "kjdkjds",
                            "${mediaViewItem.toString()}, tyyt== ${allMediaList.toString()}, pos = $position"
                        )
                        bundle.putSerializable("Data", mediaViewItem)
                        bundle.putInt("ImagePosition", position)
                        Log.d("kjdjdsj", allMediaList.toString())
                        investmentViewModel.setMediaListItem(allMediaList)
                        val fragment = MediaViewFragment()
                        fragment.arguments = bundle
                        (requireActivity() as HomeActivity).addFragment(
                            fragment, false
                        )
                    }

                    override fun seeAllImages(imagesList: ArrayList<MediaViewItem>) {
                        investmentViewModel =
                            ViewModelProvider(
                                requireActivity(),
                                investmentFactory
                            ).get(InvestmentViewModel::class.java)
                        val fragment = MediaGalleryFragment()
                        val bundle = Bundle()
                        bundle.putSerializable("Data", imagesList)
                        investmentViewModel.setMediaListItem(imagesList)
                        fragment.arguments = bundle
                        (requireActivity() as HomeActivity).addFragment(
                            fragment, false
                        )
                    }

                    override fun shareApp() {
                        (requireActivity() as HomeActivity).share_app()
                    }

                    override fun onClickAsk() {
                        (requireActivity() as HomeActivity).addFragment(ChatsFragment(), false)
                    }

                    override fun onDocumentView(name: String, path: String) {
                        openDocumentScreen(name, path)
                    }

                }, allMediaList, headingDetails
            )
        binding.rvPortfolioSpecificView.adapter = portfolioSpecificViewAdapter
        binding.rvPortfolioSpecificView.setHasFixedSize(true)
        binding.rvPortfolioSpecificView.setItemViewCacheSize(20)

        //for document bottom sheet
        if (it.data.documentList != null) {
            val adapter =
                DocumentsAdapter(it.data.documentList, true, object : DocumentInterface {
                    override fun onclickDocument(name: String, path: String) {
                        docsBottomSheet.dismiss()
                        openDocumentScreen(name, path)
                    }

                })
            documentBinding.rvDocsItemRecycler.adapter = adapter
        }
    }

    private fun openDocumentScreen(name: String, path: String) {
        val strings = name.split(".")
        if (strings[1] == "png" || strings[1] == "jpg") {
            //open image loading screen
            openDocument(name, path)
        } else if (strings[1] == "pdf") {
            getDocumentData(path)
        } else {

        }
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

    private fun openDocument(name: String, path: String) {
        (requireActivity() as HomeActivity).addFragment(
            DocViewerFragment.newInstance(true, name, path),
            false
        )
    }

    fun getDocumentData(path: String) {
        portfolioviewmodel.downloadDocument(path)
            .observe(viewLifecycleOwner,
                androidx.lifecycle.Observer {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.loader.show()
                        }
                        Status.SUCCESS -> {
                            binding.loader.hide()
                            requestPermisson(it.data!!.data)
                        }
                        Status.ERROR -> {
                            (requireActivity() as HomeActivity).showErrorToast(
                                it.message!!
                            )
                        }
                    }
                })
    }

    private fun requestPermisson(base64: String) {
        isReadPermissonGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWritePermissonGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (!isReadPermissonGranted || !isWritePermissonGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            openPdf(base64)
        }
        if (permissionRequest.isNotEmpty()) {
            base64Data = base64
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

    private fun openPdf(stringBase64: String) {
        val file = Utility.writeResponseBodyToDisk(stringBase64)
        if (file != null) {
            val path = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file!!
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(path, "application/pdf")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("Error:openPdf: ", e.localizedMessage)
            }
        } else {
            (requireActivity() as HomeActivity).showErrorToast("Something Went Wrong")
        }


    }

}