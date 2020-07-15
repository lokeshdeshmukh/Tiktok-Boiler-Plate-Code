

package com.loki.tiktok.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.loki.tiktok.R
import com.loki.tiktok.interfaces.OptiVideoOptionListener
import com.loki.tiktok.utils.Constant

class OptiVideoOptionsAdapter(videoOptions: ArrayList<String>, val context: Context, optiVideoOptionListener: OptiVideoOptionListener, orientationLand: Boolean) :
    RecyclerView.Adapter<OptiVideoOptionsAdapter.MyPostViewHolder>() {

    private var myVideoOptions = videoOptions
    private var myVideoOptionListener = optiVideoOptionListener
    var myOrientationLand = orientationLand

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyPostViewHolder {
        return if(myOrientationLand) {
            MyPostViewHolder(LayoutInflater.from(context).inflate(R.layout.option_view_land, p0, false))
        } else {
            MyPostViewHolder(LayoutInflater.from(context).inflate(R.layout.option_view, p0, false))
        }
    }

    override fun onBindViewHolder(p0: MyPostViewHolder, p1: Int) {

        //set image based on video option
        when(myVideoOptions[p1]){
            Constant.FLIRT -> {
                p0.ivOption.setImageResource(R.drawable.video_conference_24)
            }

            Constant.TRIM -> {
                p0.ivOption.setImageResource(R.drawable.video_trimming_24)
            }

            Constant.MUSIC -> {
                p0.ivOption.setImageResource(R.drawable.music_video_24)
            }

            Constant.PLAYBACK -> {
                p0.ivOption.setImageResource(R.drawable.speed_skating_24)
            }

            Constant.TEXT -> {
                p0.ivOption.setImageResource(R.drawable.text_width_24)
            }

            Constant.OBJECT -> {
                p0.ivOption.setImageResource(R.drawable.sticker_24)
            }

            Constant.MERGE -> {
                p0.ivOption.setImageResource(R.drawable.merge_vertical_24)
            }

            Constant.TRANSITION -> {
                p0.ivOption.setImageResource(R.drawable.transition_24)
            }
        }

        p0.ivOption.setOnClickListener {
            myVideoOptionListener.videoOption(myVideoOptions[p1])
        }
    }

    class MyPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivOption: ImageView = itemView.findViewById(R.id.iv_option)
    }

    override fun getItemCount(): Int {
        return myVideoOptions.size
    }
}