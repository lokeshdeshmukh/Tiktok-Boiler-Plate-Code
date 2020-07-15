/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.loki.tiktok.retrofitapiandmodel

import com.loki.tiktok.retrofitapiandmodel.Model.MusicResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*


interface ApiInterface {
    @Headers("Cache-control: no-cache")
    @GET("getallmusiclist.php")
    fun loadChanges(@Query("q") status: String): Call<MusicResponse>


    @GET
    fun downloadFile(@Url fileUrl:String): Call<ResponseBody>
}