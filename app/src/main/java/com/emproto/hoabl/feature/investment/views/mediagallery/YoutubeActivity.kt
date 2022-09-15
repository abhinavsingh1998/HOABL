package com.emproto.hoabl.feature.investment.views.mediagallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.emproto.core.Constants
import com.emproto.core.Utility
import com.emproto.hoabl.R
import com.emproto.hoabl.databinding.ActivityYoutubeBinding
import com.emproto.hoabl.model.MediaViewItem
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer

class YoutubeActivity : YouTubeBaseActivity() {

    lateinit var binding: ActivityYoutubeBinding
    private var videoId = ""
    var index = 0
    private var videoList = ArrayList<MediaViewItem>()
    private var videoTitle = ""
    private val encryptedMapKey = "3j+SjzloU3Keq7Fk+lbKW9W2kdodK3M59ZmL10v+AOctU/oems5E2ANOrD73gB59"

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
        binding.ytPlayer.initialize(Utility.decrypt(encryptedMapKey),
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