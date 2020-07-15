/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.loki.tiktok.retrofitapiandmodel.Model

data class MusicResponse(val sucess:Int,val musiclist:ArrayList<Music>)
data class Music(val name:String,val slowurl:String,val tooslowurl:String,val normalurl:String,val fasturl:String,val actualLength:Int)