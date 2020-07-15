

package com.loki.tiktok.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Util
import com.loki.tiktok.VideoEditor
import com.loki.tiktok.utils.CommonMethods
import com.loki.tiktok.utils.Constant
import com.loki.tiktok.R
import com.loki.tiktok.TrimmerActivity
import com.loki.tiktok.utils.SessionManager
import com.loki.tiktok.adapter.OptiVideoOptionsAdapter
import com.loki.tiktok.fragments.AddMusicFragment.Companion.masterAudioFile
import com.loki.tiktok.interfaces.FFMpegCallback
import com.loki.tiktok.interfaces.OptiVideoOptionListener
import com.loki.tiktok.utils.Utils
import com.loki.tiktok.utils.VideoUtils
import com.loki.tiktok.utils.VideoFrom
import com.loki.tiktok.videorecorder.VideoRecorder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MasterProcessorFragment : Fragment(), BaseCreatorDialogFragment.CallBacks, OptiVideoOptionListener,
    FFMpegCallback  {

    private var tagName: String = MasterProcessorFragment::class.java.simpleName
    private lateinit var rootView: View
    private var videoUri: Uri? = null
    private var videoFile: File? = null
    private var permissionList: ArrayList<String> = ArrayList()
    private lateinit var preferences: SharedPreferences
    private lateinit var progressBar: ProgressBar
    private var tvVideoProcessing: TextView? = null
    private var handler: Handler = Handler()
    private var playdefault=true
    companion object{
        public var masterVideoFile: File? = null
    }

    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var ePlayer: PlayerView? = null
    private var pbLoading: ProgressBar? = null
    private var exoPlayer: SimpleExoPlayer? = null
    private var playWhenReady: Boolean? = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var rvVideoOptions: RecyclerView
    private lateinit var optiVideoOptionsAdapter: OptiVideoOptionsAdapter
    private var videoOptions: ArrayList<String> = ArrayList()
    private var orientationLand: Boolean = false
    private var tvSave: ImageView? = null
    private var isLargeVideo: Boolean? = false
    private var mContext: Context? = null
    private var tvInfo: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.video_processor_fragment, container, false)
        initView(rootView)
        return rootView
    }

    private fun initView(rootView: View?) {
        ePlayer = rootView?.findViewById(R.id.ePlayer)
        tvSave = rootView?.findViewById(R.id.tvSave)
        pbLoading = rootView?.findViewById(R.id.pbLoading)
        rootView?.findViewById<Button>(R.id.recordvideo)?.setOnClickListener(View.OnClickListener {
            openCamera()
        })
        progressBar = rootView?.findViewById(R.id.progressBar)!!
        tvVideoProcessing = rootView.findViewById(R.id.tvVideoProcessing)
        tvInfo = rootView.findViewById(R.id.tvInfo)

        preferences = activity!!.getSharedPreferences("fetch_permission", Context.MODE_PRIVATE)

        rvVideoOptions = rootView.findViewById(R.id.rvVideoOptions)!!
        linearLayoutManager =
            LinearLayoutManager(activity!!.applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvVideoOptions.layoutManager = linearLayoutManager

        mContext = context

        //add video editing options
        //videoOptions.add(OptiConstant.FLIRT)
        videoOptions.add(Constant.TRIM)
        videoOptions.add(Constant.MUSIC)
        videoOptions.add(Constant.PLAYBACK)
        videoOptions.add(Constant.TEXT)
        videoOptions.add(Constant.OBJECT)
        videoOptions.add(Constant.MERGE)
        //videoOptions.add(OptiConstant.TRANSITION)

        optiVideoOptionsAdapter =
            OptiVideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        rvVideoOptions.adapter = optiVideoOptionsAdapter
        optiVideoOptionsAdapter.notifyDataSetChanged()

        checkStoragePermission(Constant.PERMISSION_STORAGE)
        checkStoragePermission(Constant.PERMISSION_CAMERA)

        //load FFmpeg
        try {
            FFmpeg.getInstance(activity).loadBinary(object : FFmpegLoadBinaryResponseHandler {
                override fun onFailure() {
                    Log.v("FFMpeg", "Failed to load FFMpeg library.")
                }

                override fun onSuccess() {
                    Log.v("FFMpeg", "FFMpeg Library loaded!")
                }

                override fun onStart() {
                    Log.v("FFMpeg", "FFMpeg Started")
                }

                override fun onFinish() {
                    Log.v("FFMpeg", "FFMpeg Stopped")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        ibGallery?.setOnClickListener {
//            openGallery()
//        }
//
//        ibCamera?.setOnClickListener {
//            openCamera()
//        }

        tvSave?.setOnClickListener {
            AlertDialog.Builder(context!!)
                .setTitle(Constant.APP_NAME)
                .setMessage(getString(R.string.save_video))
                .setPositiveButton(getString(R.string.Continue)) { dialog, which ->
                    if (masterVideoFile != null) {
                        val outputFile = createSaveVideoFile()
                        CommonMethods.copyFile(masterVideoFile, outputFile)
                        Toast.makeText(context, R.string.successfully_saved, Toast.LENGTH_SHORT).show()
                        Utils.refreshGallery(outputFile.absolutePath, context!!)
                        tvSave!!.visibility = View.GONE
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, which -> }
                .show()
        }


    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        //for playing video in landscape mode
        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v(tagName, "orientation: ORIENTATION_LANDSCAPE")
            orientationLand = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.v(tagName, "orientation: ORIENTATION_PORTRAIT")
            orientationLand = false
        }
        optiVideoOptionsAdapter =
            OptiVideoOptionsAdapter(videoOptions, activity!!.applicationContext, this, orientationLand)
        rvVideoOptions.adapter = optiVideoOptionsAdapter
        optiVideoOptionsAdapter.notifyDataSetChanged()
    }

    override fun onAudioFileProcessed(convertedAudioFile: File) {

    }

    override fun reInitPlayer() {
        initializePlayer()
    }

    override fun onDidNothing() {
        initializePlayer()
    }

    override fun onFileProcessed(file: File) {
        tvSave!!.visibility = View.VISIBLE
        masterVideoFile = file
        isLargeVideo = false

        val extension = CommonMethods.getFileExtension(masterVideoFile!!.absolutePath)

        //check video format before playing into exoplayer
        if(extension == Constant.AVI_FORMAT){
            convertAviToMp4() //avi format is not supported in exoplayer
        } else {
            playbackPosition = 0
            currentWindow = 0
            initializePlayer()
        }
    }

    override fun showLoading(isShow: Boolean) {
        if (isShow) {
            progressBar.visibility = View.VISIBLE
            tvVideoProcessing!!.visibility = View.VISIBLE
            setProgressValue()
        } else {
            progressBar.visibility = View.INVISIBLE
            tvVideoProcessing!!.visibility = View.INVISIBLE
        }
    }

    private fun setProgressValue() {
        var progressStatus = 1

        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus++
                handler.post {
                    progressBar.progress = progressStatus
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()
    }

    override fun getFile(): File? {
        return masterVideoFile
    }


    fun checkAllPermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if (blockedPermission != null && blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if (isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constant.RECORD_VIDEO)
            }
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoFile = Utils.createVideoFile(context!!)
            Log.v(tagName, "videoPath1: " + videoFile!!.absolutePath)
            videoUri = FileProvider.getUriForFile(
                context!!,
                "com.loki.tiktok.provider", videoFile!!
            )
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240) //4 minutes
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFile)
            startActivityForResult(cameraIntent, Constant.RECORD_VIDEO)
        }
    }

    private fun checkStoragePermission(permission: Array<String>) {
        val blockedPermission = checkHasPermission(activity, permission)
        if (blockedPermission != null && blockedPermission.size > 0) {
            val isBlocked = isPermissionBlocked(activity, blockedPermission)
            if (isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(permission, Constant.ADD_ITEMS_IN_STORAGE)
            }
        } else {
            itemStorageAction()
        }
    }

    private fun itemStorageAction() {
        val sessionManager = SessionManager()

        if (sessionManager.isFirstTime(activity!!)) {
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_1,
                "sticker_1",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_2,
                "sticker_2",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_3,
                "sticker_3",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_4,
                "sticker_4",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_5,
                "sticker_5",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_6,
                "sticker_6",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_7,
                "sticker_7",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_8,
                "sticker_8",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_9,
                "sticker_9",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_10,
                "sticker_10",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_11,
                "sticker_11",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_12,
                "sticker_12",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_13,
                "sticker_13",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_14,
                "sticker_14",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_15,
                "sticker_15",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_16,
                "sticker_16",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_17,
                "sticker_17",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_18,
                "sticker_18",
                context!!
            )
            Utils.copyFileToInternalStorage(
                R.drawable.sticker_19,
                "sticker_19",
                context!!
            )

            Utils.copyFontToInternalStorage(
                R.font.roboto_black,
                "roboto_black",
                context!!
            )

            sessionManager.setFirstTime(activity!!, false)
        }
    }

    private var isFirstTimePermission: Boolean
        get() = preferences.getBoolean("isFirstTimePermission", false)
        set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime).apply()

    private val isMarshmallow: Boolean
        get() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) or (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)

    private fun checkHasPermission(context: Activity?, permissions: Array<String>?): ArrayList<String> {
        permissionList = ArrayList()
        if (isMarshmallow && context != null && permissions != null) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }
        }
        return permissionList
    }

    private fun isPermissionBlocked(context: Activity?, permissions: ArrayList<String>?): Boolean {
        if (isMarshmallow && context != null && permissions != null && isFirstTimePermission) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        playdefault=false;
        if (resultCode==100){

            val outputFile = Utils.createVideoFile(context!!)
            Log.v(tagName, "outputFile: ${outputFile.absolutePath}")


            VideoEditor.with(context!!)
                .setType(Constant.VIDEO_AUDIO_MERGE)
                .setFile(masterVideoFile!!)
                .setAudioFile(masterAudioFile!!)
                .setOutputPath(outputFile.path)
                .setCallback(this)
                .main()
            ePlayer?.useController = true;
            return
        }
        else if (resultCode == Activity.RESULT_CANCELED) return




    }



    override fun onResume() {
        super.onResume()
        if(playdefault) {
            loaddefaultvideo()
            playdefault=false;
        }

    }


    private fun releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer?.currentPosition!!
            currentWindow = exoPlayer?.currentWindowIndex!!
            playWhenReady = exoPlayer?.playWhenReady
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    private fun initializePlayer() {
        try{
            tvInfo!!.visibility= View.GONE

            ePlayer?.useController = false
            exoPlayer = ExoPlayerFactory.newSimpleInstance(
                activity,
                DefaultRenderersFactory(activity),
                DefaultTrackSelector(), DefaultLoadControl()
            )

            ePlayer?.player = exoPlayer
            if(playdefault) {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
                ePlayer?.useController = false
            }
            else
                ePlayer?.useController = true
            exoPlayer?.playWhenReady = true

            exoPlayer?.addListener(playerListener)

            exoPlayer?.prepare(VideoUtils.buildMediaSource(Uri.fromFile(masterVideoFile), VideoFrom.LOCAL))

            exoPlayer?.seekTo(0)

            exoPlayer?.seekTo(currentWindow, playbackPosition)
        } catch (exception: Exception){
            Log.v(tagName, "exception: " + exception.localizedMessage)
        }
    }

    private val playerListener = object : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        }

        override fun onSeekProcessed() {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            Log.v(tagName, "onPlayerError: ${error.toString()}")
            Toast.makeText(mContext, "Video format is not supported", Toast.LENGTH_LONG).show()
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            pbLoading?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        override fun onPositionDiscontinuity(reason: Int) {
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

            if (playWhenReady && playbackState == Player.STATE_READY) {
                // Active playback.
            } else if (playWhenReady) {
                // Not playing because playback ended, the player is buffering, stopped or
                // failed. Check playbackState and player.getPlaybackError for details.
            } else {
                // Paused by app.
            }
        }
    }

    private fun convertAviToMp4() {

        AlertDialog.Builder(context!!)
            .setTitle(Constant.APP_NAME)
            .setMessage(getString(R.string.not_supported_video))
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                //output file is generated and send to video processing
                val outputFile = Utils.createVideoFile(context!!)
                Log.v(tagName, "outputFile: ${outputFile.absolutePath}")

                VideoEditor.with(context!!)
                    .setType(Constant.CONVERT_AVI_TO_MP4)
                    .setFile(masterVideoFile!!)
                    .setOutputPath(outputFile.path)
                    .setCallback(this)
                    .main()

                showLoading(true)
            }
            .setNegativeButton(R.string.no) { dialog, which ->
                releasePlayer()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun checkPermission(requestCode: Int, permission: String) {
        requestPermissions(arrayOf(permission,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO), requestCode)
    }

    override fun openGallery() {
        releasePlayer()
        checkPermission(Constant.VIDEO_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun openCamera() {
        val intent=Intent(activity,VideoRecorder::class.java)
        startActivityForResult(intent,100)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            Constant.VIDEO_GALLERY -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //call the gallery intent
                            Utils.refreshGalleryAlone(context!!)
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                            i.type = "video/*"
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                            startActivityForResult(i, Constant.VIDEO_GALLERY)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constant.AUDIO_GALLERY -> { //not used
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                activity as Activity,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //call the gallery intent
                            Utils.refreshGalleryAlone(context!!)
                            val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                            i.type = "video/*"
                            i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                            startActivityForResult(i, Constant.AUDIO_GALLERY)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constant.RECORD_VIDEO -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                context!!,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //call the camera intent
                            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            videoFile = Utils.createVideoFile(context!!)
                            Log.v(tagName, "videoPath1: " + videoFile!!.absolutePath)
                            videoUri = FileProvider.getUriForFile(
                                context!!,
                                "com.loki.tiktok.provider", videoFile!!
                            )
                            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240) //4 minutes
                            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFile)
                            startActivityForResult(cameraIntent, Constant.RECORD_VIDEO)
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }

            Constant.ADD_ITEMS_IN_STORAGE -> {
                for (permission in permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity as Activity, permission)) {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        break
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                context!!,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            itemStorageAction()
                        } else {
                            callPermissionSettings()
                        }
                    }
                }
                return
            }
        }
    }

    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context!!.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    private fun createSaveVideoFile(): File {
        val timeStamp: String = SimpleDateFormat(Constant.DATE_FORMAT, Locale.getDefault()).format(Date())
        val imageFileName: String = Constant.APP_NAME + timeStamp + "_"

        val path =
            Environment.getExternalStorageDirectory().toString() + File.separator + Constant.APP_NAME + File.separator + Constant.MY_VIDEOS + File.separator
        val folder = File(path)
        if (!folder.exists())
            folder.mkdirs()

        return File.createTempFile(imageFileName, Constant.VIDEO_FORMAT, folder)
    }

    private fun showBottomSheetDialogFragment(bottomSheetDialogFragment: BottomSheetDialogFragment) {
        val bundle = Bundle()
        bottomSheetDialogFragment.arguments = bundle
        bottomSheetDialogFragment.show(fragmentManager, bottomSheetDialogFragment.tag)
    }

    override fun videoOption(option: String) {
        //based on selected video editing option - helper, file is passed
        when (option) {
            Constant.FLIRT -> {
                masterVideoFile?.let { file ->
                    val filterFragment = FilterFragment()
                    filterFragment.setHelper(this@MasterProcessorFragment)
                    filterFragment.setFilePathFromSource(file)
                    showBottomSheetDialogFragment(filterFragment)
                }

                if (masterVideoFile == null) {
                    Utils.showGlideToast(
                        activity!!,
                        getString(R.string.error_filter)
                    )
                }
            }

            Constant.TRIM -> {

            }

            Constant.MUSIC -> {
                masterVideoFile?.let { file ->
                    releasePlayer()

                    val timeInMillis = Utils.getVideoDuration(context!!, file)
                    /*val duration = OptiCommonMethods.convertDurationInSec(timeInMillis)
                    Log.v(tagName, "videoDuration: $duration")*/

                    AddMusicFragment.newInstance().apply {
                        setHelper(this@MasterProcessorFragment)
                        setFilePathFromSource(file)
                        setDuration(timeInMillis)
                    }.show(fragmentManager, "OptiAddMusicFragment")
                }

                if (masterVideoFile == null) {
                    Utils.showGlideToast(
                        activity!!,
                        getString(R.string.error_music)
                    )
                }
            }

            Constant.PLAYBACK -> {

            }

            Constant.TEXT -> {

            }

            Constant.OBJECT -> {

            }

            Constant.MERGE -> {

            }

            Constant.TRANSITION -> {

            }
        }
    }

    override fun onProgress(progress: String) {
        Log.v(tagName, "onProgress()")
        showLoading(true)
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.v(tagName, "onSuccess()")
        showLoading(false)
        onFileProcessed(convertedFile)
    }

    override fun onFailure(error: Exception) {
        Log.v(tagName, "onFailure() ${error.localizedMessage}")
        Toast.makeText(mContext, "Video processing failed", Toast.LENGTH_LONG).show()
        showLoading(false)
    }

    override fun onNotAvailable(error: Exception) {
        Log.v(tagName, "onNotAvailable() ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.v(tagName, "onFinish()")
        showLoading(false)
    }
    private fun loaddefaultvideo(){
        val file = AddMusicFragment().createFileFromInputStream(
            context!!.assets.open("defaultvideo.mp4"),
            "defaultvideo.mp4"
            ,context!!)
        masterVideoFile=file
        val timeInMillis = Utils.getVideoDuration(context!!, masterVideoFile!!)
        Log.v(tagName, "timeInMillis: $timeInMillis")
        val duration = CommonMethods.convertDurationInMin(timeInMillis)
        Log.v(tagName, "videoDuration: $duration")

        //check if video is more than 4 minutes
        if (duration < Constant.VIDEO_LIMIT) {
            playbackPosition = 0
            currentWindow = 0
            ePlayer?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
            ePlayer?.useController = false;
            initializePlayer()

        } else {
            Toast.makeText(activity, getString(R.string.error_select_smaller_video), Toast.LENGTH_SHORT).show()

            val uri = Uri.fromFile(masterVideoFile)
            val intent = Intent(context, TrimmerActivity::class.java)
            intent.putExtra("VideoPath", masterVideoFile!!.absolutePath)
            intent.putExtra("VideoDuration", CommonMethods.getMediaDuration(context, uri))
            startActivityForResult(intent, Constant.MAIN_VIDEO_TRIM)
        }
    }
}