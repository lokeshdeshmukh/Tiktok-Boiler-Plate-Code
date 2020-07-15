

package com.loki.tiktok

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        initListener()
    }

    private fun initListener() {
        val handler = Handler()
        handler.postDelayed({ callActivityIntent() }, 3000)
    }

    private fun callActivityIntent() {
        val intentFlag = Intent(this@SplashScreen, MainActivity::class.java)
        intentFlag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentFlag)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}