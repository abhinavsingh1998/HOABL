package com.emproto.core.customedittext

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.emproto.core.R

import androidx.core.content.ContextCompat

import android.graphics.drawable.ShapeDrawable

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList


class CustomEdittext : ConstraintLayout {

    private lateinit var view: View
    private lateinit var dropdownView: RelativeLayout
    private lateinit var countryCodeText: TextView
    private lateinit var editText: EditText
    private lateinit var arrowDrop: ImageView

    private lateinit var hintTextView: TextView
    private lateinit var backGroundView: View
    private lateinit var appContext: Context
    private var textValue = ""
    private var dropDownValue = ""


    private var hintText = ""
    private var placeholderText = ""
    private var value = ""

    var onValueChangeListner: OnValueChangedListener? = null
    private lateinit var textWatcher: TextWatcher
    private lateinit var alertDialog: AlertDialog
    private lateinit var dialogView: View
    private var dropDownValues = ArrayList<String>()
    private val onItemClickListener: OnItemClickListener? = null
    private lateinit var listItemAdapter: ListItemAdapter


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
        appContext = context
    }


    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
        appContext = context
    }

    fun init() {
        view = inflate(context, R.layout.custom_edittext, this)
        dropdownView = view.findViewById(R.id.dropdown_view)
        countryCodeText = view.findViewById(R.id.country_code)
        arrowDrop = view.findViewById(R.id.dropArrow)

        editText = view.findViewById(R.id.et_phonenumber)
        hintTextView = view.findViewById(R.id.hint_text)

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (onValueChangeListner != null) {
                    onValueChangeListner!!.onValueChanged(p0.toString(), dropDownValue)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                {
                    if (onValueChangeListner != null) {
                        onValueChangeListner!!.afterValueChanges(p0.toString())
                    }
                }
            }

        }
        editText.addTextChangedListener(textWatcher)
    }

    private fun init(attrs: AttributeSet?) {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomEdittext, 0, 0)
        hintText = attributes.getString(R.styleable.CustomEdittext_hintText)!!
        placeholderText = attributes.getString(R.styleable.CustomEdittext_placeholdertext)!!


        view = inflate(context, R.layout.custom_edittext, this)
        dropdownView = view.findViewById(R.id.dropdown_view)
        countryCodeText = view.findViewById(R.id.country_code)
        arrowDrop = view.findViewById(R.id.dropArrow)

        editText = view.findViewById(R.id.et_phonenumber)
        hintTextView = view.findViewById(R.id.hint_text)
        backGroundView = view.findViewById(R.id.background_view)
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_option, null)
        dropDownValues = ArrayList<String>()
        listItemAdapter = ListItemAdapter(context, dropDownValues, object : OnItemClickListener {
            override fun onItemClicked(value: String, index: Int) {
                dropDownValue = value
                if (onValueChangeListner != null) {
                    onValueChangeListner!!.onValueChanged(textValue, value)
                }
                countryCodeText.text = "+91"
                alertDialog!!.cancel()
                refreshView()
            }
        })
        dialogView.findViewById<RecyclerView>(R.id.recyclerViewList).adapter = listItemAdapter
        alertDialog = AlertDialog.Builder(context)
            .setCancelable(true)
            .setView(dialogView)
            .create()

        dropdownView.setOnClickListener {
            alertDialog.show()
        }
        countryCodeText.setOnClickListener {
            alertDialog.show()
        }


        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //show default color for edittext
                textValue = p0.toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    editText.setTextColor(appContext.getColor(R.color.black))
                    countryCodeText.setTextColor(appContext.getColor(R.color.black))
                    hintTextView.setTextColor(appContext.getColor(R.color.app_color))
                    arrowDrop.setImageResource(R.drawable.ic_arrow_down)
                    backGroundView.background = appContext.getDrawable(R.drawable.custom_et_bg)
                }
                if (onValueChangeListner != null) {
                    onValueChangeListner!!.onValueChanged(p0.toString(), dropDownValue)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                textValue = p0.toString()
                if (onValueChangeListner != null) {
                    onValueChangeListner!!.afterValueChanges(p0.toString())
                }
            }

        }
        editText.addTextChangedListener(textWatcher)
        editText.onFocusChangeListener = OnFocusChangeListener { p0, hasFocus ->
            if (hasFocus) {
                Handler().postDelayed(Runnable { // Show white background behind floating label
                    hintTextView.visibility = View.VISIBLE
                    editText.hint = placeholderText
                }, 100)
            } else {
                // Required to show/hide white background behind floating label during focus change
                if (editText.text.toString()
                        .isNotEmpty()
                )
                    hintTextView.visibility = View.VISIBLE
                else {
                    hintTextView.visibility = View.INVISIBLE
                    editText.hint =
                        hintText
                }

            }
        }

        refreshView()
    }

    private fun refreshView() {
        editText.hint = hintText
        hintTextView.text = hintText
        listItemAdapter.values = dropDownValues
    }

    public fun showError() {
        //change edittext color//hint color//country color//view background color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            editText.setTextColor(appContext.getColor(R.color.error_color))
            countryCodeText.setTextColor(appContext.getColor(R.color.error_color))
           hintTextView.setTextColor(appContext.getColor(R.color.error_color))
           // arrowDrop.background=appContext.getDrawable(R.drawable.ic_error)
            arrowDrop.setImageResource(R.drawable.ic_error);

            backGroundView.background = appContext.getDrawable(R.drawable.custom_et_error_bg)
//            editText.error= ""
        }
    }


    public fun onValueChangeListner(onValueChangedListener: OnValueChangedListener) {
        this.onValueChangeListner = onValueChangedListener
    }

    public fun setValue(value: String){
        editText.setText(value)
    }

    public fun addDropDownValues(list: List<String>) {
        dropDownValue = list[0]
        dropDownValues.clear()
        dropDownValues.addAll(list)
    }


}