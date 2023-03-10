package com.emproto.networklayer.response.investment

data class Values(
    val dateOfCompletion: String,
    val displayName: String,
    val medias: Media,
    val percentage: Double,
    val status: String,
    val toolTipDetails: String,
    val webLink: String
)