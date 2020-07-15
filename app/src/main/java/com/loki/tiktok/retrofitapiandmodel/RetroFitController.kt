/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.loki.tiktok.retrofitapiandmodel

import android.app.Activity
import android.os.Environment
import com.google.gson.GsonBuilder
import com.loki.tiktok.fragments.AddMusicFragment.Companion.masterAudioFile
import com.loki.tiktok.retrofitapiandmodel.Model.MusicResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*

class RetroFitController(activity: Activity) : Callback<MusicResponse> {
    private var activityMain:Activity = activity
    private var gerritAPI: ApiInterface?=null
    fun start() {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        gerritAPI = retrofit.create(ApiInterface::class.java)
        val call: Call<MusicResponse> = gerritAPI!!.loadChanges("status:open")
        call.enqueue(this)


    }



    private fun writeToDisk(body: ResponseBody): Boolean {
        return try {

            val audioFile =  File(
                Environment.getExternalStorageDirectory(),
                "lokiaudio_test4.mp3"
            )

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(audioFile)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()

                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
                masterAudioFile = audioFile
            }

        } catch (e: IOException) {
            false
        }
    }

    override fun onResponse(
        call: Call<MusicResponse>,
        response: Response<MusicResponse>
    ) {
        if (response.isSuccessful) {
            val changesList: MusicResponse = response.body()

            (activityMain as Imusicresponse).response(changesList)

            activityMain.runOnUiThread {
                val call: Call<ResponseBody> =
                    gerritAPI!!.downloadFile(fileUrl = changesList.musiclist[0].normalurl)

                call.enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(
                        call: Call<ResponseBody?>,
                        response: Response<ResponseBody?>
                    ) {

                        writeToDisk(response.body()!!)
                    }

                    override fun onFailure(
                        call: Call<ResponseBody?>,
                        t: Throwable
                    ) {

                    }
                })
            }

        } else {
            println(response.errorBody())
        }
    }

    @Override
    override fun onFailure(
        call: Call<MusicResponse>,
        t: Throwable
    ) {
        t.printStackTrace()
    }

    companion object {
        const val BASE_URL = "https://cchat.in/TikTokAPI/"
    }
    interface Imusicresponse{
        fun response(changelist:MusicResponse);
    }
}
