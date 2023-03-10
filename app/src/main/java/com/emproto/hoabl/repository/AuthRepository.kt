package com.emproto.hoabl.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.emproto.core.BaseRepository
import com.emproto.core.Constants
import com.emproto.networklayer.ApiConstants
import com.emproto.networklayer.feature.RegistrationDataSource
import com.emproto.networklayer.request.login.AddNameRequest
import com.emproto.networklayer.request.login.OtpRequest
import com.emproto.networklayer.request.login.OtpVerifyRequest
import com.emproto.networklayer.request.login.TroubleSigningRequest
import com.emproto.networklayer.response.BaseResponse
import com.emproto.networklayer.response.login.AddNameResponse
import com.emproto.networklayer.response.login.OtpResponse
import com.emproto.networklayer.response.login.TroubleSigningResponse
import com.emproto.networklayer.response.login.VerifyOtpResponse
import com.emproto.networklayer.response.terms.TermsConditionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthRepository @Inject constructor(application: Application) : BaseRepository(application) {
    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + parentJob)


    /**
     * Method to send otp to mobile number
     *
     * @param otpRequest(contain mobile no)
     * @return OtpResponse
     */
    fun getOtpOnMobile(otpRequest: OtpRequest): LiveData<BaseResponse<OtpResponse>> {
        val loginResponse = MutableLiveData<BaseResponse<OtpResponse>>()
        loginResponse.postValue(BaseResponse.loading());
        coroutineScope.launch {
            try {
                val request = RegistrationDataSource(application).getOtp(otpRequest)
                if (request.isSuccessful) {
                    loginResponse.postValue(BaseResponse.success(request.body()!!))
                } else {
                    loginResponse.postValue(
                        BaseResponse.error(
                            getErrorMessage(
                                request.errorBody()!!.string()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                loginResponse.postValue(BaseResponse.error(getErrorMessage(e)))
            }
        }
        return loginResponse;
    }

    /**
     * verifying otp sent to mobile no
     *
     * @param otpVerifyRequest
     * @return if otp is valid it will return user information
     */
    fun verifyOtp(otpVerifyRequest: OtpVerifyRequest): LiveData<BaseResponse<VerifyOtpResponse>> {
        val mVerifyOtpResponse = MutableLiveData<BaseResponse<VerifyOtpResponse>>()
        mVerifyOtpResponse.postValue(BaseResponse.loading())

        coroutineScope.launch {
            try {
                val request = RegistrationDataSource(application).verifyOtp(otpVerifyRequest)
                if (request.isSuccessful) {
                    mVerifyOtpResponse.postValue(BaseResponse.success(request.body()!!))
                } else {
                    mVerifyOtpResponse.postValue(
                        BaseResponse.Companion.error(
                            getErrorMessage(
                                request.errorBody()!!.string()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                mVerifyOtpResponse.postValue(BaseResponse.error(getErrorMessage(e)))
            }
        }

        return mVerifyOtpResponse
    }

    /**
     * Adding user details like first name and lastname
     *
     * @param addNameRequest//firstname and lastname in request
     * @return status of api request
     */
    fun addUsernameDetails(addNameRequest: AddNameRequest): LiveData<BaseResponse<AddNameResponse>> {
        val mAddUsernameResponse = MutableLiveData<BaseResponse<AddNameResponse>>()
        mAddUsernameResponse.postValue(BaseResponse.loading())
        coroutineScope.launch {
            try {
                val request = RegistrationDataSource(application).addUserDetails(addNameRequest)
                if (request.isSuccessful) {
                    mAddUsernameResponse.postValue(BaseResponse.success(request.body()!!))
                } else {
                    mAddUsernameResponse.postValue(
                        BaseResponse.Companion.error(
                            getErrorMessage(
                                request.errorBody()!!.string()
                            )
                        )
                    )
                }

            } catch (e: Exception) {
                mAddUsernameResponse.postValue(BaseResponse.error(getErrorMessage(e)))
            }
        }
        return mAddUsernameResponse
    }

    /**
     * api to submit trouble signin request
     *
     * @param signingRequest
     * @return success/failure response.
     */
    fun submitTroubleCase(signingRequest: TroubleSigningRequest): LiveData<BaseResponse<TroubleSigningResponse>> {
        val mCaseResponse = MutableLiveData<BaseResponse<TroubleSigningResponse>>()

        mCaseResponse.postValue(BaseResponse.loading())
        coroutineScope.launch {
            try {
                val request = RegistrationDataSource(application).submitTroubleCase(signingRequest)
                if (request.isSuccessful) {
                    mCaseResponse.postValue(BaseResponse.success(request.body()!!))
                } else {
                    mCaseResponse.postValue(BaseResponse.Companion.error(request.message()))
                }

            } catch (e: Exception) {
                mCaseResponse.postValue(BaseResponse.Companion.error(e.message!!))
            }
        }
        return mCaseResponse
    }

    fun getTermsCondition(pageType: Int): LiveData<BaseResponse<TermsConditionResponse>> {
        val mAddUsernameResponse = MutableLiveData<BaseResponse<TermsConditionResponse>>()
        mAddUsernameResponse.postValue(BaseResponse.loading())
        coroutineScope.launch {
            try {
                val request = RegistrationDataSource(application).getTermsCondition(pageType)
                if (request.isSuccessful) {
                    mAddUsernameResponse.postValue(BaseResponse.success(request.body()!!))
                } else {
                    mAddUsernameResponse.postValue(
                        BaseResponse.Companion.error(
                            getErrorMessage(
                                request.errorBody()!!.string()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                if (e.localizedMessage == ApiConstants.NO_INTERNET) {
                    mAddUsernameResponse.postValue(BaseResponse.error(ApiConstants.NO_INTERNET))
                } else {
                    mAddUsernameResponse.postValue(BaseResponse.Companion.error(e.message!!))
                }
            }
        }
         return mAddUsernameResponse
    }
}