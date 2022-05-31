package com.emproto.hoabl.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.emproto.networklayer.response.investment.PmData
import com.emproto.networklayer.response.home.HomePagesOrPromise
import com.emproto.networklayer.response.home.PageManagementOrLatestUpdate
import com.emproto.networklayer.response.marketingUpdates.Data
import com.emproto.networklayer.response.marketingUpdates.DetailedInfo
import com.emproto.networklayer.response.marketingUpdates.MarketingUpdateCreatedBy
import com.emproto.networklayer.response.marketingUpdates.MarketingUpdateUpdatedBy

object Extensions{

    fun saveToPreferences(activity:Activity,key:String,value:String){
        val sharedPref: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getFromPreferences(activity:Activity,key:String,value:String):String{
        val sharedPref: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString(key,"").toString()
    }

    fun PmData.toHomePagesOrPromise() = com.emproto.networklayer.response.promises.HomePagesOrPromise(
        createdAt = createdAt.toString(),
        createdBy = createdBy.toString(),
        crmPromiseId = crmPromiseId.toString(),
        description = listOf(description.toString()),
        displayMedia = displayMedia,
        howToApply = howToApply,
        id = id,
        isHowToApplyActive = isHowToApplyActive,
        isTermsAndConditionsActive = isTermsAndConditionsActive,
        name = name,
        priority = priority.toString(),
        promiseIconType = promiseIconType,
        promiseMedia = promiseMedia,
        promiseType = promiseType.toString(),
        shortDescription = shortDescription.toString(),
        status = status,
        termsAndConditions = termsAndConditions,
        updatedAt = updatedAt,
        updatedBy = updatedBy.toString()
    )

    fun HomePagesOrPromise.toHomePagesOrPromise() = com.emproto.networklayer.response.promises.HomePagesOrPromise(
        createdAt = createdAt.toString(),
        createdBy = createdBy.toString(),
        crmPromiseId = crmPromiseId.toString(),
        description = listOf(description.toString()),
        displayMedia = displayMedia,
        howToApply = howToApply,
        id = id,
        isHowToApplyActive = isHowToApplyActive,
        isTermsAndConditionsActive = isTermsAndConditionsActive,
        name = name,
        priority = priority.toString(),
        promiseIconType = promiseIconType,
        promiseMedia = promiseMedia,
        promiseType = promiseType.toString(),
        shortDescription = shortDescription.toString(),
        status = status,
        termsAndConditions = termsAndConditions,
        updatedAt = updatedAt,
        updatedBy = updatedBy.toString()
    )

    fun PageManagementOrLatestUpdate.toData() = Data(
        createdAt = createdAt,
        createdBy = createdBy,
        detailedInfo = detailedInfo,
        displayTitle = displayTitle,
        formType = formType,
        id = id,
        marketingUpdateCreatedBy = MarketingUpdateCreatedBy(firstName = "",id = 0),
        marketingUpdateUpdatedBy = MarketingUpdateUpdatedBy(firstName = "",id=0),
        priority = 0,
        shouldDisplayDate = false,
        status = status,
        subTitle = subTitle,
        updateType = updateType,
        updatedAt = updatedAt,
        updatedBy = updatedBy
    )
}