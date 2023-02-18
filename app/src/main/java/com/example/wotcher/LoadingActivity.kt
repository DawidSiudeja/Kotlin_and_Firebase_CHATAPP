package com.example.wotcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.wotcher.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        var loadingBinding: ActivityLoadingBinding = ActivityLoadingBinding.inflate(layoutInflater)
        var view = loadingBinding.root
        setContentView(view)


        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object  : Runnable {
            override fun run() {
                val intent = Intent(this@LoadingActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 5000)

    }
}