package com.emproto.networklayer.response.watchlist

import java.io.Serializable

data class TargetTime(
    val hours: Int,
    val minutes: Int,
    val seconds: Int
) : Serializable