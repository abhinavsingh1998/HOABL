package com.emproto.hoabl.feature.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.emproto.core.BaseActivity
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.FragmentEditProfileBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.viewmodels.factory.ProfileFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.request.login.profile.EditUserNameRequest
import com.emproto.networklayer.request.login.profile.UploadProfilePictureRequest
import com.emproto.networklayer.response.BaseResponse
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.profile.Data
import com.emproto.networklayer.response.profile.ProfilePictureResponse
import com.emproto.networklayer.response.profile.States
import okhttp3.MultipartBody
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.annotation.SuppressLint
import android.content.Context
import android.provider.DocumentsContract

import android.content.ContentUris
import android.text.InputFilter
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.FileProvider
import com.emproto.core.BaseFragment
import com.emproto.hoabl.viewmodels.ProfileViewModel
import java.util.regex.Pattern


class EditProfileFragment : BaseFragment() {
    val bundle = Bundle()
    var charSequence1: Editable? = null
    var charSequence2: Editable? = null
    lateinit var filePart: MultipartBody.Part

    @Inject
    lateinit var profileFactory: ProfileFactory
    lateinit var profileViewModel: ProfileViewModel
    lateinit var binding: FragmentEditProfileBinding

    var email = ""
    val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

    var houseNo = ""
    val houseNoPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    var address = ""
    val addressPattern = Pattern.compile("\\d{1,5}\\s\\w.\\s(\\b\\w*\\b\\s){1,2}\\w*\\.")
    var locality = ""
    val localityPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    var pinCode = ""
    val pinCodePattern = Pattern.compile("^[1-9][0-9]{5}\$")
    var hMobileNo = ""
    var hCountryCode = ""

    private val PICK_GALLERY_IMAGE = 1
    private val PICK_CAMERA_IMAGE = 2
    lateinit var bitmap: Bitmap
    lateinit var destinationFile: File

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadStorageGranted = false
    private var isCameraPermissionGranted = false
    private var isWriteStorageGranted = false

    val permissionRequest: MutableList<String> = ArrayList()
    private lateinit var statesData: List<States>
    private lateinit var cityData: List<String>
    private val listStates = ArrayList<String>()
    private val listStatesISO = ArrayList<String>()
    private val listCities = ArrayList<String>()
    private val countryIsoCode = "IN"
    lateinit var state: String
    lateinit var stateIso: String
    lateinit var city: String
    lateinit var gender: String
    lateinit var uploadProfilePictureRequest: UploadProfilePictureRequest

    lateinit var cameraFile: File

    @Inject
    lateinit var appPreference: AppPreference
    lateinit var data: Data

    companion object {
        fun newInstance():
                EditProfileFragment {
            val fragment = EditProfileFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            data = requireArguments().getSerializable("profileData") as Data
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        (requireActivity().application as HomeComponentProvider).homeComponent().inject(this)
        profileViewModel =
            ViewModelProvider(requireActivity(), profileFactory)[ProfileViewModel::class.java]
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        (requireActivity() as HomeActivity).activityHomeActivity.includeNavigation.bottomNavigation.isVisible =
            false

        val myCalender = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayofMonth ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH, month)
            myCalender.set(Calendar.DAY_OF_MONTH, dayofMonth)
            updateLable(myCalender)
        }
        binding.tvDatePicker.onFocusChangeListener =
            View.OnFocusChangeListener { p0, p1 ->
                if (p1) {
                    context?.let {
                        DatePickerDialog(
                            it,
                            datePicker,
                            myCalender.get(Calendar.YEAR),
                            myCalender.get(Calendar.MONTH),
                            myCalender.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                }
            }
        binding.tvDatePicker.setOnClickListener {
            context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    datePicker,
                    myCalender.get(Calendar.YEAR),
                    myCalender.get(Calendar.MONTH),
                    myCalender.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        initView()
        setGenderSpinnersData()
        getStates()
        initClickListener()




        return binding.root
    }

    private fun getStates() {
        profileViewModel.getStates(countryIsoCode).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.show()
                }
                Status.SUCCESS -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.hide()
                    it.data?.data?.let { data ->
                        statesData = data
                    }

                    for (i in statesData.indices) {
                        listStates.add(statesData[i].name)
                        listStatesISO.add(statesData[i].isoCode)
                    }
                    setStateSpinnersData()
                }
                Status.ERROR -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.hide()
                }
            }
        }
    }

    private fun getCities(value1: String, isoCode: String) {
        profileViewModel.getCities(value1, isoCode).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.show()
                }
                Status.SUCCESS -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.hide()
                    it.data?.data.let { data ->
                        cityData = data!!
                    }
                    for (i in cityData.indices) {
                        listCities.add(cityData[i])
                    }
                    setCitiesSpinner()
                }
                Status.ERROR -> {
                    (requireActivity() as HomeActivity).activityHomeActivity.loader.hide()
                }
            }
        }
    }

    private fun setGenderSpinnersData() {
        val list3 = ArrayList<String>()
        list3.add("Male")
        list3.add("Female")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_text, list3)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        binding.autoGender.setAdapter(adapter)

        binding.autoGender.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                gender = parent?.adapter?.getItem(position).toString().substring(0, 1)
                binding.autoGender.isCursorVisible = false
            }
        }
    }

    private fun setStateSpinnersData() {
        val stateArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.spinner_text, listStates)
        stateArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.autoState.setAdapter(stateArrayAdapter)

        binding.autoState.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                state = listStates[position]
                stateIso = listStatesISO[position]
                getCities(stateIso, countryIsoCode)
            }
        binding.autoState.isCursorVisible = false

    }

    private fun setCitiesSpinner() {
        val cityAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, listCities)
        cityAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.autoCity.setAdapter(cityAdapter)

        binding.autoCity.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                city = listCities[position]
            }
        binding.autoCity.isCursorVisible = false
    }

    private fun initView() {
        binding.emailTv.setText("")
        binding.completeAddress.setText("")
        binding.houseNo.setText("")
        binding.pincodeEditText.setText("")


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: isReadStorageGranted
                isWriteStorageGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: isWriteStorageGranted
            }
        requestPermission()

        binding.textviewEnterName.text = data.firstName + " " + data.lastName
        Log.i("name", data.firstName + " " + data.lastName + data.email)
        binding.enterPhonenumberTextview.text = data.phoneNumber
        if (!data.email.isNullOrEmpty()) {
            binding.emailTv.setText(data.email)
        } else {
            binding.emailTv.setText("")
        }
        if (!data.dateOfBirth.isNullOrEmpty()) {
            binding.tvDatePicker.setText(data.dateOfBirth.substring(0, 10))
        } else {
            binding.tvDatePicker.setText("")
        }
        if (!data.gender.isNullOrEmpty()) {
            binding.autoGender.setText(data.gender)
            binding.autoGender.isCursorVisible = false
        } else {
            binding.autoGender.setText("")
        }
        if (!data.houseNumber.isNullOrEmpty()) {
            binding.houseNo.setText(data.houseNumber)
        } else {
            binding.houseNo.setText("")
        }
        if (!data.streetAddress.isNullOrEmpty()) {
            binding.completeAddress.setText(data.streetAddress)
        } else {
            binding.completeAddress.setText("")
        }
        if (!data.locality.isNullOrEmpty()) {
            binding.locality.setText(data.locality)
        } else {
            binding.locality.setText("")
        }
        if (!data.country.isNullOrEmpty()) {
            binding.autoCountry.setText(data.country)
            binding.autoCountry.isCursorVisible = false;
        } else {
            binding.autoCountry.setText("")

        }
        if (!data.state.isNullOrEmpty()) {
            binding.autoState.setText(data.state)
            binding.autoState.isCursorVisible = false;
        } else {
            binding.autoState.setText("")

        }
        if (!data.city.isNullOrEmpty()) {
            binding.autoCity.setText(data.city)
            binding.autoCity.isCursorVisible = false;
        } else {
            binding.autoCity.setText("")

        }
        if (!data.pincode.toString().isNullOrEmpty()) {
            binding.pincodeEditText.setText(data.pincode.toString())
        } else {
            binding.pincodeEditText.setText("")
        }
        if (data.profilePictureUrl.isNullOrEmpty()) {
            binding.profileImage.visibility = View.GONE
            binding.profileUserLetters.visibility = View.VISIBLE
            setUserNamePIC(data)
        } else {
            binding.profileImage.visibility = View.VISIBLE
            binding.profileUserLetters.visibility = View.GONE
            Glide.with(requireContext())
                .load(data.profilePictureUrl)
                .into(binding.profileImage)

        }
    }

    private fun setUserNamePIC(data: Data) {

        if (this.data.lastName.isNullOrEmpty()) {
            val firstLetter: String = this.data.firstName.substring(0, 2)

            binding.tvUserName.text = firstLetter
        } else {
            val firstLetter: String = this.data.firstName.substring(0, 1)

            val lastLetter: String = this.data.lastName.substring(0, 1)
            binding.tvUserName.text = firstLetter + "" + lastLetter

        }
    }

    private fun updateLable(myCalendar: Calendar) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
        var dateSelected = sdf.format(myCalendar.time)
        binding.tvDatePicker.setText(dateSelected.substring(0, 10))
    }


    private fun initClickListener() {
        binding.backAction.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.emailTv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.tvEmail.isErrorEnabled = false
            }
        })
        binding.houseNo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.floorHouseNum.isErrorEnabled = false
            }
        })
        binding.completeAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.comAdd.isErrorEnabled = false
            }
        })
        binding.locality.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.tvLocality.isErrorEnabled = false
            }
        })
        binding.pincodeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.pincode.isErrorEnabled = false
            }
        })


        binding.uploadNewPicture.setOnClickListener { selectImage() }



        binding.tvremove.setOnClickListener {
//                binding.profileImage.visibility = View.GONE
//                binding.profileUserLetters.visibility = View.VISIBLE
//                setUserNamePIC()
            callDeletePic(data)
        }
        binding.saveAndUpdate.setOnClickListener {
            binding.saveAndUpdate.text = "Save and Update"
            email = binding.emailTv.text.toString()
            if (!email.isNullOrEmpty() && email.isValidEmail()) {
                var validEmail=email
                binding.tvEmail.isErrorEnabled = false
                sendProfileDetail(validEmail)
            } else {
                binding.tvEmail.error = "Please enter valid email"
                email = binding.emailTv.text.toString()
                if (email.length == 150) {
                    binding.tvEmail.error = "You have reached the max characters limit"
                    Toast.makeText(
                        context,
                        "You have reached the max characters limit",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            houseNo = binding.houseNo.text.toString()
            Log.i("houseNo", houseNo)
            if (!houseNo.isNullOrEmpty() && houseNo.isValidHouseNo()) {
                var validHouseNo=houseNo
                binding.floorHouseNum.isErrorEnabled = false
                sendProfileDetail(validHouseNo)
            } else {
                binding.floorHouseNum.error = "Please enter valid floor and house number"
                houseNo = binding.houseNo.text.toString()
                if (houseNo.length == 150) {
                    binding.floorHouseNum.error = "You have reached the max characters limit"
                    Toast.makeText(
                        context,
                        "You have reached the max characters limit",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            address = binding.completeAddress.text.toString()
            if (address.isValidAddress()) {
                var validAdd=address
                binding.comAdd.isErrorEnabled = false

                sendProfileDetail(validAdd)
            } else {
                binding.comAdd.error = "Please enter valid address"
                address = binding.completeAddress.text.toString()
                if (address.length == 150) {
                    binding.comAdd.error = "You have reached the max characters limit"
                    Toast.makeText(
                        context,
                        "You have reached the max characters limit",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            locality = binding.locality.text.toString()
            if (locality.isValidAddress()) {
                binding.tvLocality.isErrorEnabled = false
                var validLocality=locality
                sendProfileDetail(validLocality)
            } else {
                binding.tvLocality.error = "Please enter valid locality"
                locality = binding.locality.text.toString()
                if (locality.length == 150) {
                    binding.tvLocality.error = "You have reached the max characters limit"
                    Toast.makeText(
                        context,
                        "You have reached the max characters limit",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            pinCode = binding.pincodeEditText.text.toString()
            if (pinCode.isValidPinCode()) {
                var validPinCode=pinCode
                binding.pincode.isErrorEnabled = false
                sendProfileDetail(validPinCode)
            } else {
                binding.pincode.error = "Please enter valid pincode"
                pinCode = binding.pincodeEditText.text.toString()
                if (pinCode.length == 150) {
                    binding.pincode.error = "You have reached the max characters limit"
                    Toast.makeText(
                        context,
                        "You have reached the max characters limit",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (!email.isNullOrEmpty() && email.isValidEmail() && houseNo.isValidHouseNo() && address.isValidAddress() && locality.isValidLocality() && pinCode.isValidPinCode()) {
                binding.saveAndUpdate.text = "Updated"
            }

        }

    }


    private fun sendProfileDetail(validPinCode: String) {
        val editUserNameRequest = EditUserNameRequest(
            data.firstName,
            data.lastName,
            binding.emailTv.text.toString(),
            binding.tvDatePicker.text.toString(),
            binding.autoGender.text.toString(),
            binding.houseNo.text.toString(),
            binding.completeAddress.text.toString(),
            binding.locality.text.toString(),
            binding.pincodeEditText.text.toString(),
            binding.autoCity.text.toString(),
            binding.autoState.text.toString(),
            "India"
        )
        profileViewModel.editUserNameProfile(editUserNameRequest)
            .observe(
                viewLifecycleOwner
            ) { t ->
                when (t!!.status) {
                    Status.LOADING -> {
                        binding.saveAndUpdate.visibility = View.GONE

                    }
                    Status.SUCCESS -> {
                        appPreference.saveLogin(true)
                        binding.saveAndUpdate.visibility = View.VISIBLE
                        binding.emailTv.clearFocus()
                        binding.houseNo.clearFocus()
                        binding.completeAddress.clearFocus()
                        binding.tvLocality.clearFocus()
                        binding.pincodeEditText.clearFocus()
                    }
                    Status.ERROR -> {
                        binding.saveAndUpdate.visibility = View.VISIBLE
                    }
                }
            }

    }


    fun CharSequence?.isValidEmail() =
        emailPattern.matcher(this).matches()

    fun CharSequence?.isValidAddress() =
        addressPattern.matcher(this).matches()

    fun CharSequence?.isValidHouseNo() =
        houseNoPattern.matcher(this).matches()

    fun CharSequence?.isValidLocality() =
        localityPattern.matcher(this).matches()

    fun CharSequence?.isValidPinCode() =
        pinCodePattern.matcher(this).matches()

    /*----------upload picture--------------*/
    private fun requestPermission() {
        isReadStorageGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteStorageGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!isReadStorageGranted && !isWriteStorageGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun onCaptureImageResult() {
        val selectedImage = cameraFile.path
        destinationFile=cameraFile
        val thumbnail = BitmapFactory.decodeFile(selectedImage)
        binding.profileImage.visibility = View.VISIBLE
        binding.profileUserLetters.visibility = View.GONE
        binding.profileImage.setImageBitmap(thumbnail)
        if ((requireActivity() as BaseActivity).isNetworkAvailable()) {
            callingUploadPicApi(cameraFile)

        } else {
            (requireActivity() as BaseActivity).showError(
                "Please check Internet Connections to upload image",
                binding.root

            )
        }
    }
    private fun onSelectFromGalleryResult(data: Intent) {
        val selectedImage = data.data
        var inputStream =
            requireContext().contentResolver.openInputStream(selectedImage!!)
        try {
            bitmap = BitmapFactory.decodeStream(inputStream)
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

            try {
                val filePath = getRealPathFromURI_API19(requireContext(), selectedImage)
                if ((requireActivity() as BaseActivity).isNetworkAvailable()) {
                    destinationFile = File(filePath)
                    callingUploadPicApi(destinationFile)
                } else {
                    (requireActivity() as BaseActivity).showError(
                        "Please check Internet Connections to upload image",
                        binding.root
                    )
                }
            } catch (e: Exception) {
                Log.e("Error", "onSelectFromGalleryResult: " + e.localizedMessage)
            }

            binding.profileImage.visibility = View.VISIBLE
            binding.profileUserLetters.visibility = View.GONE
            binding.profileImage.setImageBitmap(bitmap)


        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun callingUploadPicApi(destinationFile: File) {
        profileViewModel.uploadProfilePicture(destinationFile, destinationFile.name)
            .observe(
                viewLifecycleOwner,
                object : Observer<BaseResponse<ProfilePictureResponse>> {
                    override fun onChanged(it: BaseResponse<ProfilePictureResponse>?) {
                        when (it?.status) {
                            Status.LOADING -> {
                                binding.progressBaar.show()
                            }
                            Status.SUCCESS -> {
                                binding.progressBaar.hide()
                            }
                            Status.ERROR -> {
                                binding.progressBaar.hide()
                            }
                        }
                    }


                })
    }
    private fun callDeletePic(data: Data) {
        val fileName :String=  data.profilePictureUrl.toString().substring( data.profilePictureUrl.toString().lastIndexOf('/') + 1)
        Log.i("profileUrl", fileName)
        profileViewModel.deleteProfileImage(fileName)
            .observe(viewLifecycleOwner, Observer {
                when (it.status) {
                    Status.LOADING -> {
                        binding.progressBaar.show()
                    }
                    Status.SUCCESS -> {
                        binding.progressBaar.hide()
                        if (data.profilePictureUrl == null) {
                            binding.profileImage.visibility = View.GONE
                            binding.profileUserLetters.visibility = View.VISIBLE
                            setUserNamePIC(data)
                        }

                    }

                    Status.ERROR -> {
                        binding.progressBaar.hide()
                        Toast.makeText(
                            this.requireContext(),
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.i("delete api errpr", it.message.toString())
                    }
                }
            })
    }

    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        getPhotoFile(requireContext())
                    )
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    cameraLauncher.launch(intent)

                }
                options[item] == "Choose from Gallery" -> {
                    val intent =
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    resultLauncher.launch(intent)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    var cameraLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode === Activity.RESULT_OK) {
            onCaptureImageResult()
        }
    }

    var resultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result != null && result.resultCode === Activity.RESULT_OK) {
            if (result.data != null) {
                onSelectFromGalleryResult(result.data!!)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_GALLERY_IMAGE) {
                onSelectFromGalleryResult(data!!)
            } else {
                (requireActivity() as BaseActivity).showError(
                    "Nothing Selected",
                    binding.root
                )
            }
        }
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs!!)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getPhotoFile(context: Context): Uri? {
        val fileSuffix = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        cameraFile = File(context.externalCacheDir, "$fileSuffix.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".provider",
            cameraFile
        )
    }

}








