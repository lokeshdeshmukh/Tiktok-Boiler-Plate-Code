/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */



package com.loki.tiktok.fragments

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.loki.tiktok.VideoEditor
import com.loki.tiktok.R
import com.loki.tiktok.adapter.OptiPlaybackSpeedAdapter
import com.loki.tiktok.interfaces.DialogueHelper
import com.loki.tiktok.interfaces.FFMpegCallback
import com.loki.tiktok.interfaces.PlaybackSpeedListener
import com.loki.tiktok.utils.Constant
import com.loki.tiktok.utils.Utils
import java.io.File

class PlaybackSpeedDialogFragment : BottomSheetDialogFragment(), DialogueHelper,
    FFMpegCallback, PlaybackSpeedListener {

    private var tagName: String = PlaybackSpeedDialogFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvPlaybackSpeed: RecyclerView
    private lateinit var optiPlaybackSpeedAdapter: OptiPlaybackSpeedAdapter
    private var playbackSpeed: ArrayList<String> = ArrayList()
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var masterFile: File? = null
    private var isHavingAudio = true
    private var helper: BaseCreatorDialogFragment.CallBacks? = null
    private var mContext: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_playback_speed_dialog, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPlaybackSpeed = rootView.findViewById(R.id.rvPlaybackSpeed)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager =
            LinearLayoutManager(activity!!.applicationContext)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            optiPlaybackSpeedAdapter.setPlayback()
        }

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvPlaybackSpeed.layoutManager = linearLayoutManager

        playbackSpeed.add(Constant.SPEED_0_25)
        playbackSpeed.add(Constant.SPEED_0_5)
        playbackSpeed.add(Constant.SPEED_0_75)
        playbackSpeed.add(Constant.SPEED_1_0)
        playbackSpeed.add(Constant.SPEED_1_25)
        playbackSpeed.add(Constant.SPEED_1_5)

        optiPlaybackSpeedAdapter = OptiPlaybackSpeedAdapter(playbackSpeed, activity!!.applicationContext, this)
        rvPlaybackSpeed.adapter = optiPlaybackSpeedAdapter
        optiPlaybackSpeedAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance() = PlaybackSpeedDialogFragment()
    }

    override fun setHelper(helper: BaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    override fun setMode(mode: Int) {
    }

    override fun setFilePathFromSource(file: File) {
        masterFile = file
        isHavingAudio = Utils.isVideoHaveAudioTrack(file.absolutePath)
        Log.d(tagName, "isHavingAudio $isHavingAudio")
    }

    override fun setDuration(duration: Long) {

    }

    override fun onProgress(progress: String) {
        Log.d(tagName, "onProgress() $progress")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.d(tagName, "onSuccess()")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.d(tagName, "onFailure() " + error.localizedMessage)
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_LONG).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.d(tagName,"onNotAvailable() " + error.message)
        helper?.showLoading(false)
    }

    override fun onFinish() {
        Log.d(tagName, "onFinish()")
        helper?.showLoading(false)
    }

    /*private fun processFSVideo(ffMpegCommand: String) {
        val outputFile = OptiUtils.createVideoFile(context!!)
        Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

        VideoSpeed.with(context!!)
            .setFile(masterFile!!)
            .setIsHavingAudio(isHavingAudio)
            .setCallback(this@OptiPlaybackSpeedDialogFragment)
            .setOutputPath(outputFile.absolutePath)
            .setOutputFileName(outputFile.name)
            .processFS(ffMpegCommand)
    }*/

    public override fun processVideo(playbackSpeed: String, tempo: String) {
        if(playbackSpeed != "0.0") {
            //output file is generated and send to video processing
            val outputFile = Utils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            VideoEditor.with(context!!)
                .setType(Constant.VIDEO_PLAYBACK_SPEED)
                .setFile(masterFile!!)
                .setOutputPath(outputFile.absolutePath)
                .setIsHavingAudio(isHavingAudio)
                .setSpeedTempo(playbackSpeed, tempo)
                .setCallback(this)
                .main()

            helper?.showLoading(true)
            dismiss()
        } else {
            Utils.showGlideToast(activity!!, getString(R.string.error_select_speed))
        }
    }
    public fun processVideowithcontext(playbackSpeed: String, tempo: String,context1: Context,masterfile:File,opticontext:FFMpegCallback) {
        if(playbackSpeed != "0.0") {
            //output file is generated and send to video processing
            val outputFile = Utils.createVideoFile(context1)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

            VideoEditor.with(context1)
                .setType(Constant.VIDEO_PLAYBACK_SPEED)
                .setFile(masterfile)
                .setOutputPath(outputFile.absolutePath)
                .setIsHavingAudio(false)
                .setSpeedTempo(playbackSpeed, tempo)
                .setCallback(opticontext)
                .main()


        } else {
            Utils.showGlideToast(activity!!, getString(R.string.error_select_speed))
        }
    }
}