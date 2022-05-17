package com.devpark.customcameraapi2.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.devpark.customcameraapi2.R

class SampleCameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_camera)

        savedInstanceState ?: supportFragmentManager.beginTransaction().replace(R.id.container, Camera2BasicFragment().newInstance()).commit()

    }//end of onCreate


}