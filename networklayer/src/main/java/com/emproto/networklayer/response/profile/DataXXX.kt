package com.emproto.networklayer.response.profile

data class DataXXX(
    val address: Address,
    val areaStartingFrom: Any,
    val createdAt: String,
    val createdBy: Int,
    val crmLaunchPhaseId: Int,
    val crmProjectId: Int,
    val customerGuideLines: Any,
    val fomoContent: FomoContent,
    val generalInfoEscalationGraph: GeneralInfoEscalationGraph,
    val id: Int,
    val isCustomerGuideLinesActive: Any,
    val isEscalationGraphActive: Boolean,
    val isInventoryBucketActive: Boolean,
    val isKeyPillarsActive: Boolean,
    val isLatestMediaGalleryActive: Boolean,
    val isLocationInfrastructureActive: Boolean,
    val isOffersAndPromotionsActive: Boolean,
    val keyPillars: KeyPillars,
    val latestMediaGalleryHeading: String,
    val launchName: String,
    val locationInfrastructure: LocationInfrastructure,
    val numberOfSimilarInvestmentsToShow: Any,
    val offersAndPromotions: OffersAndPromotions,
    val priceStartingFrom: Any,
    val projectCompletionDate: Any,
    val projectContentCreatedBy: ProjectContentCreatedBy,
    val projectCoverImages: ProjectCoverImages,
    val reraDetails: ReraDetails,
    val shortDescription: String,
    val status: String,
    val updatedAt: String,
    val updatedBy: Int
)