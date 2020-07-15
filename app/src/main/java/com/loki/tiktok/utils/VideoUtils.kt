

package com.loki.tiktok.utils

import android.net.Uri
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import java.util.concurrent.TimeUnit

object VideoUtils {

    fun secToTime(totalSeconds: Long): String {
        return String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalSeconds),
            TimeUnit.MILLISECONDS.toMinutes(totalSeconds) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalSeconds)), // The change is in this line
            TimeUnit.MILLISECONDS.toSeconds(totalSeconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalSeconds))
        )
    }

    fun buildMediaSource(uri: Uri, fromWho: String, userAgent: String = ""): MediaSource? {

        when (fromWho) {

            VideoFrom.LOCAL -> {

                val dataSpec = DataSpec(uri)

                val fileDataSource = FileDataSource()
                try {
                    fileDataSource.open(dataSpec)
                } catch (e: FileDataSource.FileDataSourceException) {
                    e.printStackTrace()
                }
                val factory = DataSource.Factory { fileDataSource }

                return ExtractorMediaSource.Factory(factory).createMediaSource(fileDataSource.uri)
            }

            VideoFrom.REMOTE -> {
                return ExtractorMediaSource.Factory(
                    DefaultHttpDataSourceFactory(userAgent)
                ).createMediaSource(uri)
            }

            else -> {
                return null
            }

        }
    }
}


class VideoFrom {
    companion object {
        const val LOCAL = "LOCAL"
        const val REMOTE = "REMOTE"
    }
}