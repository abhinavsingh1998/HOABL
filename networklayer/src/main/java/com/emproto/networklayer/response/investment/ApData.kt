package com.emproto.networklayer.response.investment

import java.io.Serializable

data class ApData(         
    val address: Address,
    val areaStartingFrom: String,
    val createdAt: String,
    val crmLaunchPhaseId: Int,
    val crmProjectId: Int,
    val fomoContent: FomoContent,
    val generalInfoEscalationGraph: GeneralInfoEscalationGraph,
    val id: Int,
    val isSoldOut:Boolean,
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
    val priceStartingFrom: String,
    val projectCoverImages: ProjectCoverImages,
    val reraDetails: ReraDetails,
    val shortDescription: String,
    val status: String,
    val updatedAt: String
):Serializable