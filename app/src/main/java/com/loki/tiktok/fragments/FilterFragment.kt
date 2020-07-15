

package com.loki.tiktok.fragments

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
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
import com.loki.tiktok.adapter.OptiFilterAdapter
import com.loki.tiktok.interfaces.FFMpegCallback
import com.loki.tiktok.interfaces.FilterListener
import com.loki.tiktok.utils.Constant
import com.loki.tiktok.utils.Utils
import java.io.File


class FilterFragment : BottomSheetDialogFragment(), FilterListener, FFMpegCallback {

    private var tagName: String = FilterFragment::class.java.simpleName
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvFilter: RecyclerView
    private lateinit var ivClose: ImageView
    private lateinit var ivDone: ImageView
    private var videoFile: File? = null
    private var helper: BaseCreatorDialogFragment.CallBacks? = null
    private var filterList: ArrayList<String> = ArrayList()
    private lateinit var optiFilterAdapter: OptiFilterAdapter
    private var selectedFilter: String? = null
    private var bmThumbnail: Bitmap? = null
    private var mContext: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_filter_dialog, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFilter = rootView.findViewById(R.id.rvFilter)
        ivClose = rootView.findViewById(R.id.iv_close)
        ivDone = rootView.findViewById(R.id.iv_done)
        linearLayoutManager =
            LinearLayoutManager(activity!!.applicationContext)

        mContext = context

        ivClose.setOnClickListener {
            dismiss()
        }

        ivDone.setOnClickListener {
            optiFilterAdapter.setFilter()

            if (selectedFilter != null) {
                dismiss()

                when (selectedFilter) {
                    "Black and White" -> {
                        applyFilterAction("hue=s=0")
                    }

                    "Vertigo" -> {
                        //showing error
                        applyFilterAction("frei0r=vertigo:0.2")
                    }

                    "Vignette" -> {
                        applyFilterAction("vignette=PI/4")
                    }

                    "Sobel" -> {
                        //showing error
                        applyFilterAction("hwupload, sobel_opencl=scale=2:delta=10, hwdownload")
                    }

                    "Sepia" -> {
                        //video not playing
                        applyFilterAction("colorchannelmixer=.393:.769:.189:0:.349:.686:.168:0:.272:.534:.131")
                    }

                    "Grayscale" -> {
                        //video not playing
                        applyFilterAction("colorchannelmixer=.3:.4:.3:0:.3:.4:.3:0:.3:.4:.3")
                    }
                }
            } else {
                Utils.showGlideToast(activity!!, getString(R.string.error_select_filter))
            }
        }

        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvFilter.layoutManager = linearLayoutManager

        filterList.add("Black and White")
        filterList.add("Vignette")
        /*filterList.add("Vertigo")
        filterList.add("Sobel")
        filterList.add("Sepia")
        filterList.add("Grayscale")*/

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(
            videoFile!!.absolutePath,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )

        optiFilterAdapter = OptiFilterAdapter(filterList, bmThumbnail!!, activity!!.applicationContext, this)
        rvFilter.adapter = optiFilterAdapter
        optiFilterAdapter.notifyDataSetChanged()
    }

    private fun applyFilterAction(command: String) {
        //output file is generated and send to video processing
        val outputFile = Utils.createVideoFile(context!!)
        Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

        VideoEditor.with(context!!)
            .setType(Constant.VIDEO_FLIRT)
            .setFile(videoFile!!)
            .setFilter(command)
            .setOutputPath(outputFile.path)
            .setCallback(this)
            .main()

        helper?.showLoading(true)
    }

    override fun selectedFilter(filter: String) {
        selectedFilter = filter
    }

    fun setHelper(helper: BaseCreatorDialogFragment.CallBacks) {
        this.helper = helper
    }

    fun setFilePathFromSource(file: File) {
        videoFile = file
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        helper?.showLoading(false)
        helper?.onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_LONG).show()
        helper?.showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        helper?.showLoading(false)
    }
}