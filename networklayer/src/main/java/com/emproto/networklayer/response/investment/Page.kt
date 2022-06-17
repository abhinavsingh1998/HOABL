package com.emproto.networklayer.response.investment

data class Page(
    val aboutUs: Any,
    val bookingJourney: Any,
    val collectionOne: CollectionOne,
    val collectionOneCollectionId: Int,
    val collectionTwo: CollectionTwo,
    val collectionTwoCollectionId: Int,
    val createdAt: String,
    val facilityManagement: Any,
    val footerTabs: Any,
    val hoablPromiseDisplayName: Any,
    val id: Int,
    val insightsHeading: Any,
    val insightsSubHeading: Any,
    val isAboutUsActive: Any,
    val isCollectionOneActive: Boolean,
    val isCollectionTwoActive: Boolean,
    val isFacilityManagementActive: Any,
    val isHoablPromiseActive: Any,
    val isInsightsActive: Any,
    val isLatestUpdatesActive: Any,
    val isMastheadActive: Any,
    val isNewInvestmentsActive: Boolean,
    val isPromisesActive: Any,
    val isPublished: Boolean,
    val isSecurityTipsActive: Any,
    val isTermsActive: Any,
    val isTestimonialsActive: Any,
    val latestUpdates: Any,
    val mastheadSection: Any,
    val mythBuster: Any,
    val newInvestments: NewInvestments,
    val numberOfScreens: Any,
    val pageManagementsOrCollectionOneModels: List<Any>,
    val pageManagementsOrCollectionTwoModels: List<Any>,
    val pageName: String,
    val pageType: String,
    val playTimeInSeconds: Any,
    val promiseSection: Any,
    val promisesHeading: Any,
    val promotionAndOffersMedia: PromotionAndOffersMedia,
    val promotionAndOffersProjectContentId: Int,
    val propositionScreens: Any,
    val sectionName: Any,
    val securityTips: Any,
    val shortDescription: Any,
    val splashScreenImage: Any,
    val subHeading: Any,
    val termsAndConditions: Any,
    val testimonialsHeading: Any,
    val testimonialsSubHeading: Any,
    val totalInsightsOnHomeScreen: Any,
    val totalInsightsOnListView: Any,
    val totalProjectsOnHomeScreen: Any,
    val totalPromisesOnHomeScreen: Any,
    val totalTestimonialsOnHomeScreen: Any,
    val totalTestimonialsOnListView: Any,
    val totalUpdatesOnHomeScreen: Any,
    val totalUpdatesOnListView: Any,
    val updatedAt: String
)