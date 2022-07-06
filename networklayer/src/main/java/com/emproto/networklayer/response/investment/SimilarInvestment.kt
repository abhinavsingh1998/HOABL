package com.emproto.networklayer.response.investment

data class SimilarInvestment(
    val address: AddressX?=null,
    val areaStartingFrom: String?=null,
    val createdAt: String?=null,
    val fomoContent: FomoContentX?=null,
    val generalInfoEscalationGraph: GeneralInfoEscalationGraph?=null,
    val id: Int?=null,
    val isEscalationGraphActive: Boolean?=null,
    val isInventoryBucketActive: Boolean?=null,
    val isKeyPillarsActive: Boolean?=null,
    val isLatestMediaGalleryActive: Boolean?=null,
    val isLocationInfrastructureActive: Boolean?=null,
    val isOffersAndPromotionsActive: Boolean?=null,
    val keyPillars: KeyPillars?=null,
    val latestMediaGalleryHeading: String?=null,
    val launchName: String?=null,
    val locationInfrastructure: LocationInfrastructure?=null,
    val numberOfSimilarInvestmentsToShow: Any?=null,
    val offersAndPromotions: OffersAndPromotions?=null,
    val priceStartingFrom: String?=null,
    val projectContentsAndSimilarinvestments: ProjectContentsAndSimilarinvestments?=null,
    val projectCoverImages: ProjectCoverImages?=null,
    val projectId: Int?=null,
    val reraDetails: ReraDetails?=null,
    val shortDescription: String?=null,
    val status: String?=null,
    val updatedAt: String?=null
)