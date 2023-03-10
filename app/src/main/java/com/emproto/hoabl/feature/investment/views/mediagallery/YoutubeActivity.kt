package com.emproto.hoabl.feature.investment.views.mediagallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.emproto.core.Constants
import com.emproto.hoabl.databinding.ActivityYoutubeBinding
import com.emproto.hoabl.model.MediaViewItem
import com.emproto.networklayer.NetworkUtil
import com.emproto.networklayer.preferences.AppPreference
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import javax.inject.Inject

class YoutubeActivity : YouTubeBaseActivity() {

    lateinit var binding: ActivityYoutubeBinding
    private var videoId = ""
    var index = 0
    private var videoList = ArrayList<MediaViewItem>()
    private var videoTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        playYoutubeVideo(videoId)

        binding.ivCloseButton.setOnClickListener {
            onBackPressed()
        }

        when (videoList.size) {
            0 -> {
                binding.ivMediaRightArrow.visibility = View.GONE
                binding.ivMediaRightArrow.visibility = View.GONE
            }
        }
    }



    private fun initData() {
        videoId = intent.getStringExtra(Constants.YOUTUBE_VIDEO_ID).toString()
        videoTitle = intent.getStringExtra(Constants.VIDEO_TITLE).toString()
        binding.tvMediaImageName.text = videoTitle
    }

    private fun playYoutubeVideo(youtubeId: String) {
        binding.ytPlayer.initialize(
            NetworkUtil.decrypt(),
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubePlayer?,
                    p2: Boolean
                ) {
                    p1?.loadVideo(youtubeId)
//                p1?.play()
                }

                override fun onInitializationFailure(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubeInitializationResult?
                ) {
                    Toast.makeText(
                        applicationContext,
                        Constants.VIDEO_PLAYER_FAILED,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }
}