package com.emproto.hoabl.feature.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.emproto.core.BaseActivity
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.FragmentEditProfileBinding
import com.emproto.hoabl.di.HomeComponentProvider
import com.emproto.hoabl.feature.home.views.HomeActivity
import com.emproto.hoabl.feature.login.SucessDialogFragment
import com.emproto.hoabl.viewmodels.ProfileViewModel
import com.emproto.hoabl.viewmodels.factory.ProfileFactory
import com.emproto.networklayer.preferences.AppPreference
import com.emproto.networklayer.request.login.profile.EditUserNameRequest
import com.emproto.networklayer.request.login.profile.UploadProfilePictureRequest
import com.emproto.networklayer.response.BaseResponse
import com.emproto.networklayer.response.enums.Status
import com.emproto.networklayer.response.profile.Data
import com.emproto.networklayer.response.profile.ProfilePictureResponse
import com.emproto.networklayer.response.profile.States
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class EditProfileFragment : Fragment() {
    lateinit var binding: FragmentEditProfileBinding
    val bundle = Bundle()


    var charSequence1: Editable? = null
    var charSequence2: Editable? = null

    @Inject
    lateinit var profileFactory: ProfileFactory
    lateinit var profileViewModel: ProfileViewModel
    var hMobileNo = ""
    var hCountryCode = ""

    private val PICK_IMAGE = 1
    private val PICK_Camera_IMAGE = 2
    lateinit var bitmap: Bitmap
    lateinit var destinationFile: File

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadStorageGranted = false
    private var isCameraPermissionGranted = false
    val permissionRequest: MutableList<String> = ArrayList()
    private lateinit var statesData: List<States>
    private lateinit var cityData: List<String>
    val listStates = ArrayList<String>()
    val listStatesISO = ArrayList<String>()
    val listCities = ArrayList<String>()
    val countryIsoCode = "IN"
    lateinit var state: String
    lateinit var stateIso: String
    lateinit var city: String
    lateinit var gender: String


    @Inject
    lateinit var appPreference: AppPreference

    companion object {
        lateinit var data: Data
        fun newInstance(
        ): EditProfileFragment {
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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


        init()
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
                        listCities.add(cityData[i].toString())
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
            }
        }
    }

    private fun setStateSpinnersData() {
        val stateArrayAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, listStates)
        stateArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.autoState.setAdapter(stateArrayAdapter)

        binding.autoState.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                state = listStates[position]
                stateIso = listStatesISO[position]
                getCities(stateIso, countryIsoCode)
            }
    }

    private fun setCitiesSpinner() {
        val cityAdapter = ArrayAdapter(requireContext(), R.layout.spinner_text, listCities)
        cityAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        binding.autoCity.setAdapter(cityAdapter)

        binding.autoCity.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                city = listCities[position]
            }
    }

    private fun initView() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: isReadStorageGranted
                isCameraPermissionGranted =
                    permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted
            }
        requestPermission()

        binding.textviewEnterName.text = data.firstName + " " + data.lastName
        binding.enterPhonenumberTextview.text = data.phoneNumber

        if (!data.email.isNullOrEmpty()) {
            binding.emailTv.setText(data.email)
        } else {
            binding.emailTv.setText("")
        }
        val string = data.dateOfBirth
        val date = string.substring(0, 10)
        if (!data.dateOfBirth.isNullOrEmpty()) {
            binding.tvDatePicker.setText(date)
        } else {
            binding.tvDatePicker.setText("")
        }
        if (!data.gender.isNullOrEmpty()) {
            binding.autoGender.setText(data.gender)
            binding.autoGender.isCursorVisible = false;
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
            setuserNamePIC()
        } else {
            binding.profileImage.visibility = View.VISIBLE
            binding.profileUserLetters.visibility = View.GONE
            Glide.with(requireContext())
                .load(data.profilePictureUrl)
                .into(binding.profileImage)
        }
    }

    private fun setuserNamePIC() {
        val firstLetter: String = data.firstName.substring(0, 1)
        val lastLetter: String = data.lastName.substring(0, 1)
        if (data.lastName.isNullOrEmpty()) {
            binding.tvUserName.text = firstLetter
        } else {
            binding.tvUserName.text = firstLetter + "" + lastLetter

        }
    }

    private fun updateLable(myCalendar: Calendar) {
        val myCalendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
        binding.tvDatePicker.setText(sdf.format(myCalendar.timeZone))
    }


    ///*************ProfilePicture upload****************************//
    private fun init() {
        /* if (data.profilePictureUrl.isNotEmpty()) {
             binding.uploadNewPicture.setText(data.profilePictureUrl)
         }*/
        binding.tvremove.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                charSequence1 = p0
                if (p0.toString().isNullOrEmpty()) {
                    binding.uploadImage.isEnabled = false
                    binding.uploadImage.isClickable = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.uploadImage.background =
                            AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.unselect_button_bg
                            )
                    }
                } else {
                    binding.uploadImage.isEnabled = true
                    binding.uploadImage.isClickable = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.uploadImage.background =
                            AppCompatResources.getDrawable(requireContext(), R.drawable.button_bg)

                    }
                }
            }
        })

        binding.uploadImage.setOnClickListener(View.OnClickListener {
            val uploadProfilePictureRequest = UploadProfilePictureRequest(

                binding.uploadNewPicture.text.toString(),

                )
            profileViewModel.uploadProfilePicture(uploadProfilePictureRequest)
                .observe(
                    viewLifecycleOwner
                ) { t ->
                    when (t!!.status) {
                        Status.LOADING -> {

                            binding.uploadImage.visibility = View.GONE
                        }
                        Status.SUCCESS -> {
                            appPreference.saveLogin(true)

                            binding.uploadImage.visibility = View.VISIBLE
                            val dialog = SucessDialogFragment()
                            val bundle = Bundle()
                            bundle.putString(
                                "ProfilePictureUrl",
                                binding.uploadNewPicture.text.toString()
                            )
                            dialog.arguments = bundle
                            dialog.isCancelable = false
                            dialog.show(parentFragmentManager, "Welcome Card")
                        }
                        Status.ERROR -> {

                            binding.uploadImage.visibility = View.VISIBLE
                        }
                    }
                }
        })
    }

    /////**************************Create Profile***************************///
    private fun initClickListener() {

        binding.backAction.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.uploadNewPicture.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                selectImage()
            }
        })

        binding.tvremove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                profileViewModel.deleteProfilePicture().observe(viewLifecycleOwner, Observer {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.progressBaar.show()
                        }
                        Status.SUCCESS -> {
                            binding.progressBaar.hide()
                            if (it.data != null) {
                                Glide.with(requireContext())
                                    .load(R.drawable.img)
                                    .into(binding.profileImage)
                                Toast.makeText(
                                    requireContext(),
                                    it.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        Status.ERROR -> {
                            binding.progressBaar.hide()
                        }
                    }
                })
            }

        })

/*
        binding.genderEditText.onValueChangeListner(object : OnValueChangedListener {
            override fun onValueChanged(value: String?, tvDrop: String) {
                hCountryCode = tvDrop
                binding.saveAndUpdate.visibility = View.VISIBLE
            }

            override fun afterValueChanges(value1: String?) {
                hMobileNo = value1!!
                if (value1.isNullOrEmpty()) {
                    binding.saveAndUpdate.isEnabled = false
                    binding.saveAndUpdate.isClickable = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.saveAndUpdate.background =
                            resources.getDrawable(R.drawable.unselect_button_bg)
                    }
                } else {
                    binding.saveAndUpdate.isEnabled = true
                    binding.saveAndUpdate.isClickable = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        binding.saveAndUpdate.background =
                            resources.getDrawable(R.drawable.button_bg)
                    }
                }
            }

        })
*/

        /* binding.city.onValueChangeListner(object : OnValueChangedListener {
             override fun onValueChanged(value: String?, tvDrop: String) {
                 hCountryCode = tvDrop
                 binding.saveAndUpdate.visibility = View.VISIBLE
             }

             override fun afterValueChanges(value1: String?) {
                 hMobileNo = value1!!
                 if (value1.isNullOrEmpty()) {
                     binding.saveAndUpdate.isEnabled = false
                     binding.saveAndUpdate.isClickable = false
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                         binding.saveAndUpdate.background =
                             resources.getDrawable(R.drawable.unselect_button_bg)
                     }
                 } else {
                     binding.saveAndUpdate.isEnabled = true
                     binding.saveAndUpdate.isClickable = true
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                         binding.saveAndUpdate.background =
                             resources.getDrawable(R.drawable.button_bg)
                     }
                 }
             }

         })

         binding.stateEditText.onValueChangeListner(object : OnValueChangedListener {
             override fun onValueChanged(value: String?, tvDrop: String) {
                 hCountryCode = tvDrop
                 binding.saveAndUpdate.visibility = View.VISIBLE
             }

             override fun afterValueChanges(value1: String?) {
                 hMobileNo = value1!!
                 getCities(value1.toString(), isoCode)
                 if (value1.isNullOrEmpty()) {
                     binding.saveAndUpdate.isEnabled = false
                     binding.saveAndUpdate.isClickable = false
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                         binding.saveAndUpdate.background =
                             resources.getDrawable(R.drawable.unselect_button_bg)
                     }
                 } else {
                     binding.saveAndUpdate.isEnabled = true
                     binding.saveAndUpdate.isClickable = true
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                         binding.saveAndUpdate.background =
                             resources.getDrawable(R.drawable.button_bg)
                     }
                 }
             }

         })*/

        /*    binding.saveAndUpdate.setOnClickListener {
                if (hMobileNo.isEmpty()) {
                    binding.genderEditText.showError()
                    return@setOnClickListener
                }
                if (data.dateOfBirth.isNotEmpty()) {
                    binding.tvDatePicker.setText(data.dateOfBirth)
                }
                if (data.email.isNotEmpty()) {
                    binding.emailTv.setText(data.email)

                }
                if (data.dateOfBirth.isNotEmpty()) {
                    binding.dob.setTag(data.dateOfBirth)

                }
                *//*if (data.gender.toString().isNotEmpty()) {
                binding.genderEditText.setTag(data.gender)

            }*//*
            if (data.houseNumber.isNotEmpty()) {
                binding.houseNo.setText(data.houseNumber)

            }
            if (data.locality.isNotEmpty()) {
                binding.locality.setText(data.locality)

            }

            *//* if (data.city.isNotEmpty()) {
                 binding.city.setTag(data.city)

             }
             if (data.state.isNotEmpty()) {
                 binding.stateEditText.setTag(data.state)
             }*//*

            if (data.pincode.toString().isNotEmpty()) {
                binding.pincodeEditText.setText(data.pincode)

            }


            binding.emailTv.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

            binding.tvDatePicker.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

            binding.houseNo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })
            binding.locality.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })
            binding.pincodeEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })

            binding.tvDatePicker.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {

                }


            })
        }*/

        binding.saveAndUpdate.setOnClickListener {
            binding.saveAndUpdate.text = "Save and Update"
            binding.updateProgressBar.visibility = View.VISIBLE
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
                data.city,
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
                            binding.updateProgressBar.visibility = View.GONE
                            binding.saveAndUpdate.text = "Updated"
                            binding.saveAndUpdate.visibility = View.VISIBLE
                            binding.emailTv.clearFocus()

                            binding.houseNo.clearFocus()
                            binding.completeAddress.clearFocus()
                            binding.tvLocality.clearFocus()
                            binding.pincodeEditText.clearFocus()


//                            val dialog = SucessDialogFragment()
//                            val bundle = Bundle()
//                            bundle.putString(
//                                "FirstName",
//                                binding.tvEnterName.text.toString()
//                            )
//                            dialog.arguments = bundle
//                            dialog.isCancelable = false
//                            dialog.show(parentFragmentManager, "Welcome Card")
                        }
                        Status.ERROR -> {
                            binding.saveAndUpdate.visibility = View.VISIBLE
                        }
                    }
                }
        }

    }


    /*----------upload picture--------------*/

    private fun requestPermission() {
        isReadStorageGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!isReadStorageGranted && !isCameraPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun onCaptureImageResult(data: Intent) {
        val thumbnail = data.extras!!["data"] as Bitmap?
        val bytes = ByteArrayOutputStream()
        thumbnail!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val destination = File(
            Environment.getExternalStorageDirectory(),
            "Profile_pic_" + System.currentTimeMillis() + ".jpg"
        )
        val fo: FileOutputStream
        try {
            destination.createNewFile()
            fo = FileOutputStream(destination)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        binding.profileImage.setImageBitmap(thumbnail)
        if ((requireActivity() as BaseActivity).isNetworkAvailable()) {
            callingUploadPicApi()
        } else {
            (requireActivity() as BaseActivity).showError(
                "Please check Internet Connections to upload image",
                binding.root
            )
        }
    }

    private fun callingUploadPicApi() {
        val url =
            "http://hoabl-backend-dev-306342355.ap-south-1.elb.amazonaws.com/" + destinationFile.name
        val uploadProfilePictureRequest = UploadProfilePictureRequest(url)
        profileViewModel.uploadProfilePicture(uploadProfilePictureRequest)
            .observe(viewLifecycleOwner,
                Observer {
                    when (it.status) {
                        Status.LOADING -> {
                            binding.progressBaar.show()
                        }
                        Status.SUCCESS -> {
                            binding.progressBaar.hide()
                            try {
                                Glide.with(requireContext())
                                    .load(it.data?.data?.profilePictureUrl)
                                    .into(binding.profileImage)
                            } catch (e: IOException) {
                                System.out.println(e)
                            }
                        }
                        Status.ERROR -> {
                            binding.progressBaar.hide()
                        }
                    }
                })
    }

    private fun onSelectFromGalleryResult(data: Intent) {
        val selectedImage = data.data
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().getContentResolver(),
                selectedImage
            )
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            Log.e("Activity", "Pick from Gallery::>>> ")
            destinationFile = File(
                Environment.getExternalStorageDirectory(),
                "Profile_pic_" + System.currentTimeMillis() + ".jpg"
            )
            val fo: FileOutputStream
            try {
                destinationFile.createNewFile()
                fo = FileOutputStream(destinationFile)
                fo.write(bytes.toByteArray())
                fo.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.profileImage.setImageBitmap(bitmap)

            //imgPath = getRealPathFromURI(selectedImage);
            // destination = new File(imgPath);
            //profile_image.setImageBitmap(bitmap);
            if ((requireActivity() as BaseActivity).isNetworkAvailable()) {
                callingUploadPicApi()
            } else {
                (requireActivity() as BaseActivity).showError(
                    "Please check Internet Connections to upload image",
                    binding.root
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor: Cursor = requireActivity().managedQuery(contentUri, proj, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(requireActivity())
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, PICK_Camera_IMAGE)

            } else if (options[item] == "Choose from Gallery") {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT //

                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    PICK_IMAGE
                )
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                onSelectFromGalleryResult(data!!)
            } else if (requestCode == PICK_Camera_IMAGE) {
                onCaptureImageResult(data!!)
            } else {
                (requireActivity() as BaseActivity).showError(
                    "Nothing Selected",
                    binding.root
                )
            }
        }
    }
}









