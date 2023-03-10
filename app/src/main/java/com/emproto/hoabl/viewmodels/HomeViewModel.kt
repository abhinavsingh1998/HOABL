package com.emproto.hoabl.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.emproto.core.Database.Dao.HomeSearchDao
import com.emproto.core.Database.TableModel.SearchModel
import com.emproto.hoabl.feature.home.data.LatesUpdatesPosition
import com.emproto.hoabl.repository.HomeRepository
import com.emproto.networklayer.request.chat.SendMessageBody
import com.emproto.networklayer.request.refernow.ReferralRequest
import com.emproto.networklayer.response.BaseResponse
import com.emproto.networklayer.response.chats.*
import com.emproto.networklayer.response.ddocument.DDocumentResponse
import com.emproto.networklayer.response.documents.DocumentsResponse
import com.emproto.networklayer.response.home.HomeResponse
import com.emproto.networklayer.response.home.Page
import com.emproto.networklayer.response.insights.InsightsResponse
import com.emproto.networklayer.response.marketingUpdates.Data
import com.emproto.networklayer.response.marketingUpdates.LatestUpdatesResponse
import com.emproto.networklayer.response.notification.dataResponse.NotificationResponse
import com.emproto.networklayer.response.notification.readStatus.ReadNotificationReponse
import com.emproto.networklayer.response.portfolio.fm.FMResponse
import com.emproto.networklayer.response.profile.AccountsResponse
import com.emproto.networklayer.response.promises.HomePagesOrPromise
import com.emproto.networklayer.response.promises.PromisesResponse
import com.emproto.networklayer.response.refer.ReferalResponse
import com.emproto.networklayer.response.search.SearchResponse
import com.emproto.networklayer.response.terms.TermsConditionResponse
import com.emproto.networklayer.response.testimonials.TestimonialsResponse
import javax.inject.Inject

class HomeViewModel(
    private var mapplication: Application,
    private var mhomeRepository: HomeRepository
) :
    ViewModel() {

    private var homeData = MutableLiveData<HomeResponse>()

    private var application: Application = mapplication
    private var homeRepository: HomeRepository = mhomeRepository
    private var promise = MutableLiveData<HomePagesOrPromise>()


    private var testimonial = MutableLiveData<TestimonialsResponse>()
    private var selectedlatestUpdates = MutableLiveData<Data>()
    private var selectedInsights =
        MutableLiveData<com.emproto.networklayer.response.insights.Data>()
    private var latestUpdates = MutableLiveData<List<Data>>()
    private var insights = MutableLiveData<List<com.emproto.networklayer.response.insights.Data>>()

    private var position = MutableLiveData<LatesUpdatesPosition>()
    private var latUpdPosition = MutableLiveData<LatesUpdatesPosition>()
    private var notificationData = MutableLiveData<NotificationResponse>()
    private var listData = MutableLiveData<Page>()
    private var searchText = MutableLiveData<String>()
    private var investmentId = MutableLiveData<Int>()

    @Inject
    lateinit var homeSearchDao: HomeSearchDao

    private var allSearchList: MutableLiveData<List<SearchModel>> = MutableLiveData()

    fun getRecordsObserver(): MutableLiveData<List<SearchModel>> {
        return allSearchList
    }

    fun getAllRecords() {
        val list = homeSearchDao.getAllSearchDetails()
        allSearchList.postValue(list)
    }

    fun insertRecord(searchModel: SearchModel) {
        homeSearchDao.insert(searchModel)
        getAllRecords()
    }

    fun getPromises(pageType: Int, refresh: Boolean): LiveData<BaseResponse<PromisesResponse>> {
        return homeRepository.getPromises(pageType, refresh)
    }

    fun setSelectedPromise(promise: HomePagesOrPromise) {
        this.promise.postValue(promise)
    }

    fun getSelectedPromise(): LiveData<HomePagesOrPromise> {
        return promise
    }

    fun getDashBoardData(pageType: Int, refresh: Boolean): LiveData<BaseResponse<HomeResponse>> {
        return homeRepository.getDashboardData(pageType, refresh)
    }

    fun setDashBoardData(data: HomeResponse) {
        homeData.postValue(data)
    }

    fun gethomeData(): LiveData<HomeResponse> {
        return homeData
    }

    fun getTermsCondition(pageType: Int): LiveData<BaseResponse<TermsConditionResponse>> {
        return mhomeRepository.getTermsCondition(pageType)
    }

    fun getLatestUpdatesData(
        refresh: Boolean,
        byPrority: Boolean
    ): LiveData<BaseResponse<LatestUpdatesResponse>> {
        return homeRepository.getlatestUpdatesData(refresh, byPrority)
    }

    fun setLatestUpdatesData(data: List<Data>) {
        latestUpdates.postValue(data)
    }

    fun getLatestUpdates(): LiveData<List<Data>> {
        return latestUpdates
    }

    fun setSeLectedLatestUpdates(selectedlatestUpdates: Data) {
        this.selectedlatestUpdates.postValue(selectedlatestUpdates)
    }

    fun setHeaderAndList(listData: Page) {
        this.listData.postValue(listData)
    }

    fun getHeaderAndList(): LiveData<Page> {
        return listData
    }

    fun getSelectedLatestUpdates(): LiveData<Data> {
        return selectedlatestUpdates
    }

    fun setSelectedPosition(position: LatesUpdatesPosition) {
        this.position.postValue(position)
    }

    fun getSelectedPosition(): LiveData<LatesUpdatesPosition> {
        return position
    }

    fun getInsightsData(
        refresh: Boolean,
        byPrority: Boolean
    ): LiveData<BaseResponse<InsightsResponse>> {
        return homeRepository.getInsightsData(refresh, byPrority)
    }

    fun setInsightsData(data: List<com.emproto.networklayer.response.insights.Data>) {
        insights.postValue(data)
    }

    fun setSeLectedInsights(insights: com.emproto.networklayer.response.insights.Data) {
        this.selectedInsights.postValue(insights)
    }

    fun getSelectedInsights(): LiveData<com.emproto.networklayer.response.insights.Data> {
        return selectedInsights
    }

    fun getTestimonialsData(refresh: Boolean): LiveData<BaseResponse<TestimonialsResponse>> {
        return homeRepository.getTestimonialsData(refresh)
    }

    fun setTestimonials(data: TestimonialsResponse) {
        testimonial.postValue(data)
    }

    fun getChatsList(): LiveData<BaseResponse<ChatResponse>> {
        return homeRepository.getChatsList()
    }

    fun chatInitiate(topicId:String, isInvested:Boolean): LiveData<BaseResponse<ChatDetailResponse>> {
        return homeRepository.chatInitiate(topicId,isInvested)
    }

    fun getChatHistory(
        projectId: String,
        isInvested: Boolean
    ): LiveData<BaseResponse<ChatHistoryResponse>> {
        return homeRepository.getChatHistory(projectId, isInvested)
    }

    fun sendMessage(sendMessageBody: SendMessageBody): LiveData<BaseResponse<SendMessageResponse>> {
        return homeRepository.sendMessage(sendMessageBody)
    }

    fun getReferNow(referralRequest: ReferralRequest): LiveData<BaseResponse<ReferalResponse>> {
        return homeRepository.addReferral(referralRequest)
    }

    fun getFacilityManagment(): LiveData<BaseResponse<FMResponse>> {
        return homeRepository.getFacilitymanagment()
    }

    fun getSearchResult(searchWord: String): LiveData<BaseResponse<SearchResponse>> {
        return homeRepository.getSearchResult(searchWord)
    }

    fun getSearchDocResult(searchWord: String): LiveData<BaseResponse<DocumentsResponse>> {
        return homeRepository.getSearchDocResult(searchWord)
    }

    fun downloadDocument(path: String): LiveData<BaseResponse<DDocumentResponse>> {
        return homeRepository.downloadDocument(path)
    }

    fun getNotification(
        size: Int,
        index: Int,
        refresh: Boolean
    ): LiveData<BaseResponse<NotificationResponse>> {
        return homeRepository.getNotificationList(size, index, refresh)
    }

    fun getNewNotification(
        size: Int,
        index: Int,
        refresh: Boolean
    ): LiveData<BaseResponse<NotificationResponse>> {
        return homeRepository.getNewNotificationList(size, index, refresh)
    }

    fun setReadStatus(id: Int): LiveData<BaseResponse<ReadNotificationReponse>> {
        return homeRepository.setReadStatus(id)
    }

    fun setSearchedText(searchText: String) {
        this.searchText.postValue(searchText)
    }

    fun getSearchedText(): LiveData<String> {
        return searchText
    }

}