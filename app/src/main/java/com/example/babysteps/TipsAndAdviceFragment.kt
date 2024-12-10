package com.example.babysteps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class TipsAndAdviceFragment : Fragment() {

    data class TipsItem(
        val title: String,
        val videoId: String
    )

    private val tipsItemList = listOf(
        TipsItem("Healthy Diet During Pregnancy", "3GTK6MLPJ9g"),
        TipsItem("Exercise During Pregnancy", "WhNNoL6shg4"),
        TipsItem("Mental State During Pregnancy", "lv4xrmvdamY"),
        TipsItem("Sex During Pregnancy", "vO21IHmfA0Q"),
        TipsItem("Travelling During Pregnancy", "aCx0Pgb8X0Q"),
        TipsItem("Work During Pregnancy", "F_ssj7-8rYg"),
        TipsItem("Baby Care Tips", "UKqN9o87-oc"),
        TipsItem("Common Pregnancy Myths", "RpnR6NAsBjY"),
        TipsItem("Postpartum Care", "xMctsH-FNsU"),
        TipsItem("Signs of Labor", "jF9Rz9flE8I")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tips_and_advice, container, false)

        // Bind each LinearLayout with respective video
        val linearLayoutItem1: View = rootView.findViewById(R.id.linearLayoutItem1)
        val linearLayoutItem2: View = rootView.findViewById(R.id.linearLayoutItem2)
        val linearLayoutItem3: View = rootView.findViewById(R.id.linearLayoutItem3)
        val linearLayoutItem4: View = rootView.findViewById(R.id.linearLayoutItem4)
        val linearLayoutItem5: View = rootView.findViewById(R.id.linearLayoutItem5)
        val linearLayoutItem6: View = rootView.findViewById(R.id.linearLayoutItem6)
        val linearLayoutItem7: View = rootView.findViewById(R.id.linearLayoutItem7)
        val linearLayoutItem8: View = rootView.findViewById(R.id.linearLayoutItem8)
        val linearLayoutItem9: View = rootView.findViewById(R.id.linearLayoutItem9)
        val linearLayoutItem10: View = rootView.findViewById(R.id.linearLayoutItem10)
        val linearLayoutBabyNames: View = rootView.findViewById(R.id.linearLayoutBabyNames)

        linearLayoutItem1.setOnClickListener { showDialog(tipsItemList[0].videoId) }
        linearLayoutItem2.setOnClickListener { showDialog(tipsItemList[1].videoId) }
        linearLayoutItem3.setOnClickListener { showDialog(tipsItemList[2].videoId) }
        linearLayoutItem4.setOnClickListener { showDialog(tipsItemList[3].videoId) }
        linearLayoutItem5.setOnClickListener { showDialog(tipsItemList[4].videoId) }
        linearLayoutItem6.setOnClickListener { showDialog(tipsItemList[5].videoId) }
        linearLayoutItem7.setOnClickListener { showDialog(tipsItemList[6].videoId) }
        linearLayoutItem8.setOnClickListener { showDialog(tipsItemList[7].videoId) }
        linearLayoutItem9.setOnClickListener { showDialog(tipsItemList[8].videoId) }
        linearLayoutItem10.setOnClickListener { showDialog(tipsItemList[9].videoId) }

        linearLayoutBabyNames.setOnClickListener {
            // Navigate to Baby Names Fragment
            val babyNamesFragment = BabyNamesFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, babyNamesFragment)
                .addToBackStack(null)
                .commit()
        }

        return rootView
    }

    private fun showDialog(videoId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_full_text, null)

        val youTubePlayerView = dialogView.findViewById<YouTubePlayerView>(R.id.youtubePlayerView)
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle2)
            .setView(dialogView)

        val dialog = dialogBuilder.create()
        dialog.show()

        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        closeButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}
