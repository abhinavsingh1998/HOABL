package com.emproto.networklayer.response.bookingjourney

data class Investment(
    val agreementValue: Int,
    val allocationDate: String,
    val areaSqFt: Int,
    val bookingId: String,
    val bookingJourney: BookingJourneyX,
    val bookingStatus: String,
    val camCharges: Int,
    val camGST: Any,
    val corpusCharges: Int,
    val corpusGST: Any,
    val createdAt: String,
    val crmBookingId: String,
    val crmInventoryId: Any,
    val crmProjectId: String,
    val id: Int,
    val infraGST: Double,
    val infraValue: Int,
    val inventoryBucket: String,
    val inventoryId: String,
    val isBookingComplete: Boolean,
    val owners: String,
    val ownershipDate: Any,
    val paymentSchedules: List<PaymentSchedule>,
    val possesionDate: Any,
    val registrationCharges: Any,
    val registrationDate: Any,
    val registrationNumber: Any,
    val registryAmount: Int,
    val sdrCharges: Int,
    val updatedAt: String,
    val userId: String
)