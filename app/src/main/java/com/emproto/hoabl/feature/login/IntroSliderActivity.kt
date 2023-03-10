package com.emproto.hoabl.feature.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.emproto.core.BaseActivity
import com.emproto.core.button.OnButtonClickListener
import com.emproto.core.storyViewMaker.StoriesProgressView
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ActivityIntrosliderBinding


class IntroSliderActivity : BaseActivity(), StoriesProgressView.StoriesListener {

    private lateinit var activityIntrosliderBinding: ActivityIntrosliderBinding
    var context: Context? = null

    private var resources: IntArray = intArrayOf(
        R.drawable.ic_intro_first,
        R.drawable.ic_intro_second,
        R.drawable.ic_intro_third,
    )
    private var txtResource: IntArray = intArrayOf(
        R.string.intro1,
        R.string.intro2,
        R.string.intro3
    )

    private var pressTime = 0L
    private var limit = 500L


    private lateinit var storiesProgressView: StoriesProgressView
    private lateinit var image: ImageView
    private var textview: TextView? = null
    private var counter = 0

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityIntrosliderBinding = ActivityIntrosliderBinding.inflate(layoutInflater)
        setContentView(activityIntrosliderBinding.root)
        context = this

        storiesProgressView = activityIntrosliderBinding.progressBaar
        textview = activityIntrosliderBinding.qotesTxt
        image = activityIntrosliderBinding.image

        storyStart()
    }

    private fun storyStart() {
        storiesProgressView.setStoriesCount(resources.size, txtResource.size)
        storiesProgressView.setStoryDuration(3000)
        storiesProgressView.setStoriesListener(this as StoriesProgressView.StoriesListener)
        storiesProgressView.startStories(counter)

        image.setImageResource(resources[counter])
        textview!!.setText(txtResource[counter])
        btnTxtChnage()

        activityIntrosliderBinding.buttonSkip.setOnButtonClickListener(object :
            OnButtonClickListener {
            override fun OnButtonClicked(view: View?) {
                startActivity(Intent(applicationContext, AuthActivity::class.java))
                finish()
            }
        })

        activityIntrosliderBinding.reverse.setOnClickListener {
            storiesProgressView.reverse()
        }

        activityIntrosliderBinding.reverse.setOnTouchListener(onTouchListener)

        activityIntrosliderBinding.skip.setOnClickListener {
            storiesProgressView.skip()
        }

        activityIntrosliderBinding.skip.setOnTouchListener(onTouchListener)
    }

    override fun onNext() {
        try {
            ++counter
            image.setImageResource(resources[counter])
            textview?.setText(txtResource[counter])
            btnTxtChnage()
        } catch (e: Exception) {
        }
    }

    fun btnTxtChnage() {
        if (counter == 2) {
            activityIntrosliderBinding.buttonSkip.setText("Get started")
            activityIntrosliderBinding.tmTxt.isVisible = false
        } else if (counter == 1) {
            activityIntrosliderBinding.tmTxt.isVisible = false
            activityIntrosliderBinding.buttonSkip.setText("Skip & get started")
        } else {
            activityIntrosliderBinding.buttonSkip.setText("Skip & get started")
            activityIntrosliderBinding.tmTxt.isVisible = true
        }
    }

    override fun onPrev() {

        try {
            if ((counter - 1) < 0)
                return
            --counter
            image.setImageResource(resources[counter])
            textview!!.setText(txtResource[counter])
            btnTxtChnage()
        } catch (e: Exception) {

        }
    }

    override fun onComplete() {
        counter = 0
        storyStart()
    }

    class SliderViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)
}