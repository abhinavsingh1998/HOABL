package com.emproto.networklayer.response.insights

data class Media(
    val isActive: Boolean,
    val key: String,
    val mediaDescription: String,
    val name: String,
    val value: Value
)