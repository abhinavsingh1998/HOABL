package com.emproto.networklayer.response.portfolio.ivdetails

data class DataX(
    val createdAt: String,
    val createdBy: Any,
    val crmPromiseId: String,
    val description: String,
    val displayMedia: DisplayMedia,
    val howToApply: HowToApply,
    val id: Int,
    val isHowToApplyActive: Boolean,
    val isTermsAndConditionsActive: Boolean,
    val name: String,
    val priority: Any,
    val promiseIconType: String,
    val promiseMedia: PromiseMedia,
    val promiseType: String,
    val shortDescription: String,
    val status: String,
    val termsAndConditions: TermsAndConditions,
    val updatedAt: String,
    val updatedBy: Any
)