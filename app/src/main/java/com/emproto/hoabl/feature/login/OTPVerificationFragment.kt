package com.emproto.hoabl.feature.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.emproto.core.BaseFragment
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.FragmentVerifyOtpBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.viewmodels.AuthViewmodel
import com.emproto.hoabl.viewmodels.factory.AuthFactory
import com.emproto.networklayer.request.login.OtpRequest
import com.emproto.networklayer.request.login.OtpVerifyRequest
import com.emproto.networklayer.response.enums.Status
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject
import com.emproto.hoabl.smsverificatio.SmsBroadcastReceiver
import com.emproto.networklayer.preferences.AppPreference


class OTPVerificationFragment : BaseFragment() {

    private lateinit var mBinding: FragmentVerifyOtpBinding
    lateinit var smsBroadcastReceiver: SmsBroadcastReceiver

    var countOtp: Int = 3

    /// lateinit var dialog: Dialog
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadSMSGranted = false
    val permissionRequest: MutableList<String> = ArrayList()

    @Inject
    lateinit var authFactory: AuthFactory
    lateinit var authViewModel: AuthViewmodel

    lateinit var authActivity: AuthActivity

    @Inject
    lateinit var appPreference: AppPreference
    lateinit var bottomSheetDialog: BottomSheetDialog

    var attempts_num= 0

    companion object {
        const val TAG = "SMS_USER_CONSENT"
        const val REQ_USER_CONSENT = 100
        var mobileno: String = ""
        var countryCode: String = ""

        fun newInstance(mobileNumber: String, cCode: String): OTPVerificationFragment {
            val fragment = OTPVerificationFragment()
            val bundle = Bundle()
            mobileno = bundle.getString("mobilenumber", mobileNumber)
            countryCode = bundle.getString("countrycode", cCode)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        authViewModel = ViewModelProvider(requireActivity(), authFactory)[AuthViewmodel::class.java]
        mBinding = FragmentVerifyOtpBinding.inflate(layoutInflater)
        authActivity= AuthActivity()
        initView()
        initClickListener()
        otpTimerCount()
        edit_number()

        return mBinding.root
    }

    private fun startUserConsent() {
        val client = SmsRetriever.getClient(requireContext())
        client.startSmsUserConsent(null)
    }

    private fun initView() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadSMSGranted = permissions[Manifest.permission.READ_SMS] ?: isReadSMSGranted
            }

        requestPermission()
        mBinding.tvMobileNumber.text = "$countryCode-$mobileno"
        mBinding.tvMobileNumber.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        startSmsUserConsent()

    }

    private fun initClickListener() {
        mBinding.etOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    if (isNetworkAvailable(mBinding.root)) {
                        hideSoftKeyboard()
                        val otpVerifyRequest =
                            OtpVerifyRequest(s.toString(), mobileno, false, "+91")

                        authViewModel.verifyOtp(otpVerifyRequest).observe(viewLifecycleOwner,
                            Observer {
                                when (it.status) {
                                    Status.LOADING -> {
                                        mBinding.loader.visibility = View.VISIBLE
                                    }
                                    Status.ERROR -> {
                                        mBinding.loader.visibility = View.INVISIBLE
                                        (requireActivity() as AuthActivity).showErrorToast(
                                            it.message!!
                                        )
                                    }
                                    Status.SUCCESS -> {
                                        //save token to preference
                                        mBinding.loader.visibility = View.INVISIBLE
                                        it.data?.let {
                                            appPreference.setToken(it.token)
                                            if (it.user.contactType == "prelead" &&
                                                it.user.firstName.isNullOrBlank()
                                            ) {
                                                requireActivity().supportFragmentManager.popBackStack()
                                                (requireActivity() as AuthActivity).replaceFragment(
                                                    NameInputFragment.newInstance(
                                                        if (it.user.firstName != null) it.user.firstName else "",
                                                        if (it.user.lastName != null) it.user.lastName else ""
                                                    ), true
                                                )
                                            } else {
                                                appPreference.saveLogin(true)
                                                startActivity(
                                                    Intent(
                                                        requireContext(),
                                                        HomeActivity::class.java
                                                    )
                                                )
                                                requireActivity().finish()
                                            }
                                        }

                                    }
                                }
                            })
                    } else {
                        mBinding.layout1.setBackgroundColor(resources.getColor(R.color.background_grey))
                        showSnackBar(mBinding.root)
                    }
                }
            }

        })

    }

    private fun resentOtp(){
            val otpRequest = OtpRequest(mobileno, "+91", "IN")
            authViewModel.getOtp(otpRequest).observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.SUCCESS -> {
                        mBinding.loader.visibility = View.INVISIBLE
                       // Toast.makeText(requireContext(), "resend OTP successfully", Toast.LENGTH_LONG).show()
                    }
                    Status.ERROR -> {
                        mBinding.loader.visibility = View.INVISIBLE
                        it.data
                        (requireActivity() as AuthActivity).showErrorToast(
                            it.message!!
                        )
                    }
                    Status.LOADING -> {
                        mBinding.loader.visibility = View.VISIBLE
                    }
                }
            })
    }
    private fun startSMSRetrieverClient() {
        val client = SmsRetriever.getClient(requireActivity())
        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener { aVoid -> }
        task.addOnFailureListener { e -> }
    }

/*    private fun getTimerCount() {
        activityOtpVerifyBinding.timerLayout.isVisible = true
        activityOtpVerifyBinding.resendLayout.isVisible = false
        activityOtpVerifyBinding.otpLeft.isVisible = false
        object : CountDownTimer(30000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val time:String= (millisUntilFinished/1000).toString()
                activityOtpVerifyBinding.timerText.text=time+" sec"
            }

            override fun onFinish() {
                activityOtpVerifyBinding.timerLayout.isVisible=false
                activityOtpVerifyBinding.resendLayout.isVisible=true
                activityOtpVerifyBinding.otpLeft.isVisible = true
                activityOtpVerifyBinding.otpLeft.text = "[" + count_otp.toString() + " more attempts left]"
            }

        }.start()
    }*/

    private fun edit_number(){
        mBinding.etEdit.setOnClickListener(View.OnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        })
    }

    private fun requestPermission() {
        isReadSMSGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED


        if (!isReadSMSGranted) {
            permissionRequest.add(Manifest.permission.READ_SMS)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    override fun onStart() {
        super.onStart()
        registerToSmsBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_USER_CONSENT -> {
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val code = message?.let { fetchVerificationCode(it) }
                    mBinding.etOtp.setText(code!!.toString())
                    //etVerificationCode.setText(code)
                }
            }
        }
    }

    private fun startSmsUserConsent() {
        SmsRetriever.getClient(requireActivity()).also {
            //We can add user phone number or leave it blank
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    Log.d("", "LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    Log.d("", "LISTENING_FAILURE")
                }
        }
    }

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener =
                object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                    override fun onSuccess(intent: Intent?) {
                        intent?.let { context -> startActivityForResult(context, REQ_USER_CONSENT) }
                    }

                    override fun onFailure() {
                    }
                }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    private fun fetchVerificationCode(message: String): String {
        return Regex("(\\d{6})").find(message)?.value ?: ""
    }



    private fun otpTimerCount(){
        attempts_num = 2
        mBinding.resentOtp.setOnClickListener(View.OnClickListener {
            if(attempts_num > 0){
                resentOtp()
                mBinding.loginEdittext.setHint("Enter OTP ($attempts_num attempts left)")
            }
            else{

                mBinding.loginEdittext.setHint("Enter OTP")
                Toast.makeText(requireContext(), "You have reached maximum attempts", Toast.LENGTH_LONG).show()

            }
            --attempts_num
        })

    }
}