package com.emproto.networklayer.response.responsee

import com.emproto.networklayer.response.portfolio.ivdetails.MediaContent

data class CoverImage(
    val mediaContent: MediaContent,
    val mediaContentType: String,
    val name: String,
    val status: String
)