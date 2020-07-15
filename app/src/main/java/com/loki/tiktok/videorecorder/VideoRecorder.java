
package com.loki.tiktok.videorecorder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.loki.tiktok.R;
import com.loki.tiktok.fragments.MasterProcessorFragment;
import com.loki.tiktok.fragments.PlaybackSpeedDialogFragment;
import com.loki.tiktok.interfaces.FFMpegCallback;
import com.loki.tiktok.retrofitapiandmodel.Model.MusicResponse;
import com.loki.tiktok.retrofitapiandmodel.RetroFitController;
import com.skyfishjy.library.RippleBackground;
import com.spotify.protocol.types.Track;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static com.loki.tiktok.spotifyplayer.SpotifyPlayer.mSpotifyAppRemote;

@SuppressWarnings("deprecation")
public class VideoRecorder extends Activity implements SurfaceHolder.Callback, RetroFitController.Imusicresponse, FFMpegCallback {
    private final String VIDEO_PATH_NAME = "/mnt/sdcard/VGA_30fps_512vbrate" + SystemClock.currentThreadTimeMillis() + ".mp4";

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private View mToggleButton;
    private RippleBackground rippleBackground;
    private boolean mInitSuccesful;
    private MusicResponse musicResponse;
    private ProgressBar progressBar;
    private ProgressBar progressBarConversion;
    private File audioFile;
    private String playback;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.video_recorder);

        // we shall take the video in landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mToggleButton = (ImageButton) findViewById(R.id.toggleRecordingButton1);


        init_listener();
        RetroFitController controller = new RetroFitController(this);
        controller.start();
        progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        progressBar.setVisibility(View.VISIBLE);
        progressBarConversion = (ProgressBar) findViewById(R.id.progressBar);
        progressBarConversion.setVisibility(View.GONE);

    }

    private void setMicMuted(boolean state){
        AudioManager myAudioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);

        // get the working mode and keep it
        int workingAudioMode = myAudioManager.getMode();

        myAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // change mic state only if needed
        if (myAudioManager.isMicrophoneMute() != state) {
            myAudioManager.setMicrophoneMute(state);
        }

        // set back the original working mode
        myAudioManager.setMode(workingAudioMode);
    }
    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if (mCamera == null) {
            mCamera = openFrontFacingCameraGingerbread();
            mCamera.setDisplayOrientation(90); // use for set the orientation of the preview
            mCamera.unlock();
        }

        audioFile = new File(Environment.getExternalStorageDirectory(),
                "audio_test4.mp4");
        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();


        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        ((AudioManager)this.getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM,true);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        CamcorderProfile profile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH);

        mMediaRecorder.setProfile(profile);
        mMediaRecorder.setCaptureRate(30f);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setOrientationHint(270);
        mMediaRecorder.setOutputFile(audioFile.getAbsolutePath());
        mMediaRecorder.setPreviewDisplay(surface);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (!mInitSuccesful) {
                initRecorder(mHolder.getSurface());
                mMediaRecorder.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
        progressBarConversion.setVisibility(View.VISIBLE);
        (new PlaybackSpeedDialogFragment()).
                processVideowithcontext(playback, "1.0", this, audioFile, this);

    }

    private MediaPlayer m;

    private MediaSource extractMediaSourceFromUri(Uri uri) {
        String userAgent = Util.getUserAgent(this, "Exo");
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, userAgent))
                .setExtractorsFactory(new DefaultExtractorsFactory()).createMediaSource(uri);
    }

    public void loadfromsoundpool() {

        MediaSource mediaSource = this.extractMediaSourceFromUri(Uri.parse("asset:///deviprasadgharpehai.mp3"));
        SimpleExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(
                getBaseContext(), new DefaultRenderersFactory(getBaseContext())
                , new DefaultTrackSelector(),
                new DefaultLoadControl()
        );

        // AudioAttributes here from exoplayer package !!!
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        // In 2.9.X you don't need to manually handle audio focus :D
        exoPlayer.setAudioAttributes(attr, true);
        exoPlayer.prepare(mediaSource);
        exoPlayer.getPlayWhenReady();

    }

    private void loadmusic() {
        try {

            m.setOnCompletionListener(mediaPlayer -> {
                if (m.isPlaying()) {
                    m.stop();
                    m.release();
                    m = new MediaPlayer();
                }
                shutdown();
            });
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSpotify() {
        mSpotifyAppRemote.getPlayerApi().resume();
        mSpotifyAppRemote.getPlayerApi().play("spotify:artist:4YRxDV8wJFPHPTeXepOstw");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }


    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e("loki", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }


    @Override
    public void response(@NotNull MusicResponse changelist) {
        musicResponse = changelist;
        progressBar.setVisibility(View.GONE);

    }

    private void mediaPrepare(String musicurl) {
        m = new MediaPlayer();
        try {
            m.setAudioStreamType(AudioManager.STREAM_MUSIC);
            m.setDataSource(getApplicationContext(), Uri.parse(musicurl));
            progressBar.setVisibility(View.VISIBLE);
            m.setOnPreparedListener(mediaPlayer -> {
                progressBar.setVisibility(View.GONE);
                progressBarConversion.setMax((int) (m.getDuration() / 1000.0f));
            });

            m.prepareAsync();
            m.setVolume(.5f, .5f);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                m.setPlaybackParams(new PlaybackParams().setSpeed(0.5f));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final Handler handler = new Handler();
    private int progressStatus = 1;

    @Override
    public void onProgress(@NotNull String progress) {
        if (progress.contains("time=")) {
            String time = progress.split("time=")[1];
            time = time.split(" ")[0];
            progressStatus = Integer.parseInt((time.split(":")[2]).split("\\.")[0]);
            new Thread(() -> {
                while (progressStatus < 100) {

                    handler.post(() -> progressBarConversion.setProgress(progressStatus));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onSuccess(@NotNull File convertedFile, @NotNull String type) {
        MasterProcessorFragment.Companion.setMasterVideoFile(convertedFile);
        setResult(100);
        if (m.isPlaying()) {
            m.stop();
            m.release();
            m = new MediaPlayer();
        }
        finish();
    }

    @Override
    public void onFailure(@NotNull Exception error) {

    }

    @Override
    public void onNotAvailable(@NotNull Exception error) {

    }

    @Override
    public void onFinish() {

    }

    private void init_listener() {
        rippleBackground = (RippleBackground) findViewById(R.id.toggleRecordingButton);

        mToggleButton.setOnTouchListener((View.OnTouchListener) (view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                resetMediaplayer();
                ((TextView)findViewById(R.id.countdown)).setVisibility(View.VISIBLE);
                new Thread((Runnable) () -> {
                    for (int i = 0; i < 3; i++) {
                        try {
                            Thread.sleep(500);
                            int finalI = i;
                            runOnUiThread(() -> ((TextView) findViewById(R.id.countdown)).setText((finalI + 1) + ""));

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread((Runnable) () -> {
                        ((TextView) findViewById(R.id.countdown)).setVisibility(View.GONE);
                        mMediaRecorder.start();
                        rippleBackground.startRippleAnimation();
                        try {
                            loadmusic();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }).start();


                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                if (m.isPlaying()) {
                    m.stop();
                    m.release();
                    m = new MediaPlayer();
                    shutdown();
                }

                return true;
            }

            return false;
        });
        findViewById(R.id.fast1).setOnClickListener(view -> {
            playback = "1.5";
            mediaPrepare(musicResponse.getMusiclist().get(0).getFasturl());
        });
        findViewById(R.id.normal).setOnClickListener(view -> {
            playback = "1.0";
            mediaPrepare(musicResponse.getMusiclist().get(0).getNormalurl());

        });
        findViewById(R.id.slow1).setOnClickListener(view -> {
            playback = "0.75";
            mediaPrepare(musicResponse.getMusiclist().get(0).getSlowurl());
        });
        findViewById(R.id.slow2).setOnClickListener(view -> {
            playback = "0.5";
            mediaPrepare(musicResponse.getMusiclist().get(0).getTooslowurl());
        });
    }

    private void resetMediaplayer() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.release();
            mCamera = null;
            initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
