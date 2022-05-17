package com.devpark.customcameraapi2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import android.view.View

import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import android.widget.Toast

import android.hardware.Camera
import android.hardware.camera2.*

import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.Semaphore
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat


/**
 * https://metalkin.tistory.com/92 참고
 */
class CameraActivity : AppCompatActivity(), Camera2APIs.Camera2Interface, TextureView.SurfaceTextureListener {

    private val TAG: String = "CameraActivity"

    private var mTextureView: TextureView? = null

    private var mCamera: Camera2APIs? = null
    private val mRatio34: Int = 34
    private val mRatio11: Int = 11
    private var mRatio: Int = mRatio34

    lateinit var mIvRatio: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        mTextureView = findViewById(R.id.view_texture)
        mTextureView?.surfaceTextureListener = this

        mIvRatio = findViewById(R.id.ic_ratio_btn)

        mCamera = Camera2APIs(this)


    }//end of onCreate()

    override fun onResume() {
        super.onResume()
        if (mTextureView!!.isAvailable) {
            openCamera()
        } else {
            mTextureView!!.surfaceTextureListener = this
        }
    }


    /**
     * Camera2APIS
     */
    private fun openCamera() {
        if (mCamera == null) mCamera = Camera2APIs(this)

        val cameraManager = mCamera!!.cameraManager1(this)
        val cameraId = mCamera!!.cameraCharacteristics2(cameraManager!!)
        mCamera!!.cameraDevice3(cameraManager, cameraId, this)
    }


    fun onItemClick(view: View) {

        when (view.id) {
            R.id.layout_close -> finish()
            R.id.ic_ratio_btn -> {
                mRatio = if (mRatio == mRatio34) {
                    setRatio11()
                    mIvRatio.setImageResource(R.drawable.ic_camera_ratio02) //3:4
                    mRatio11
                } else {
                    setRatio34()
                    mIvRatio.setImageResource(R.drawable.ic_camera_ratio01) //1:1
                    mRatio34
                }
            }

        }//end of when


    }//end of onItemClick

    override fun onCameraDeviceOpened(cameraDevice: CameraDevice?, cameraSize: Size?) {

        val texture = mTextureView!!.surfaceTexture
        texture!!.setDefaultBufferSize(cameraSize!!.width, cameraSize!!.height) // w:h = 3:4


        Log.i("CameraAPI2", "width : ${cameraSize!!.width}, height : ${cameraSize!!.height}")
        val surface = Surface(texture)
         mCamera!!.captureSession4(cameraDevice!!, surface)
        mCamera!!.captureRequest5(cameraDevice, surface)
    }


    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, width: Int, height: Int) {
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

    }


    var constraintLayout: ConstraintLayout? = null
    var constraintSet: ConstraintSet? = null

    /**
     * 1:1 비율 화면 만들기
     */
    private fun setRatio11() {
        constraintLayout = findViewById(R.id.layout_camera)
        constraintSet = ConstraintSet()
        constraintSet!!.clone(constraintLayout)

        constraintSet!!.connect(R.id.view_texture, ConstraintSet.START, R.id.layout_camera, ConstraintSet.START)
        constraintSet!!.connect(R.id.view_texture, ConstraintSet.END, R.id.layout_camera, ConstraintSet.END)
        constraintSet!!.connect(R.id.view_texture, ConstraintSet.TOP, R.id.layout_camera, ConstraintSet.TOP)
        constraintSet!!.connect(R.id.view_texture, ConstraintSet.BOTTOM, R.id.layout_camera, ConstraintSet.BOTTOM)

        constraintSet!!.applyTo(constraintLayout)

        val layoutParams = mTextureView!!.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.dimensionRatio = "H, 1:1"
        mTextureView!!.layoutParams = layoutParams

        if (mTextureView!!.isAvailable) {
            openCamera()
        } else {
            mTextureView!!.surfaceTextureListener = this
        }

    }//end of setRation11

    private fun setRatio34() {
        constraintLayout = findViewById(R.id.layout_camera)
        constraintSet = ConstraintSet()
        constraintSet!!.clone(constraintLayout)

        constraintSet!!.connect(R.id.view_texture, ConstraintSet.START, R.id.layout_camera, ConstraintSet.START)
        constraintSet!!.connect(R.id.view_texture, ConstraintSet.END, R.id.layout_camera, ConstraintSet.END)
        constraintSet!!.connect(R.id.view_texture, ConstraintSet.TOP, R.id.layout_camera, ConstraintSet.TOP)
        constraintSet!!.clear(R.id.view_texture, ConstraintSet.BOTTOM)
        //constraintSet!!.connect(R.id.view_texture, ConstraintSet.BOTTOM, R.id.layout_camera, ConstraintSet.BOTTOM)

        constraintSet!!.applyTo(constraintLayout)

        val layoutParams = mTextureView!!.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.dimensionRatio = "H, 3:4"
        mTextureView!!.layoutParams = layoutParams

        if (mTextureView!!.isAvailable) {
            openCamera()
        } else {
            mTextureView!!.surfaceTextureListener = this
        }

    }//end of setRatio34()


}
