package com.devpark.customcameraapi2

import android.Manifest
import android.hardware.camera2.TotalCaptureResult

import android.hardware.camera2.CaptureRequest

import android.hardware.camera2.CameraCaptureSession

import android.hardware.camera2.CaptureResult

import android.hardware.camera2.CameraAccessException

import android.hardware.camera2.CameraDevice

import android.hardware.camera2.CameraManager

import android.graphics.SurfaceTexture

import android.hardware.camera2.CameraCharacteristics

import android.hardware.camera2.params.StreamConfigurationMap

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import java.util.*

/**
 * https://metalkin.tistory.com/92 참고
 */
class Camera2APIs(impl: Camera2Interface?) {

    private val TAG: String = "Camera2APIs_TAG"

    interface Camera2Interface {
        fun onCameraDeviceOpened(cameraDevice: CameraDevice?, cameraSize: Size?)
    }

    private var mInterface: Camera2Interface? = impl
    private var mCameraSize: Size? = null

    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    /**
     * CameraManager 사용가능한 카메라를 나열하고, CameraDevice를 취득하기 위한 Camera2 API의 첫번째 클래스.
     *  카메라 시스템 서비스 매니저 리턴.
     */
    fun cameraManager1(activity: Activity): CameraManager? {
        return activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }//end of cameraManager2


    /**
     * CameraManager 에 의에 나열된 Camera 하드웨어, 사용가능 세팅등에 대한 정보 취득.
     * 사용가능한 카메라 리스트를 가져와 후면 카메라(LENS_FACING_BACK) 사용하여 해당 cameraId 리턴.
     * StreamConfiguratonMap은 CaptureSession을 생성할때 surfaces를 설정하기 위한 출력 포맷 및 사이즈등의 정보를 가지는 클래스.
     * 사용가능한 출력 사이즈중 가장 큰 사이즈 선택.
     */
    fun cameraCharacteristics2(cameraManager: CameraManager): String? {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes: Array<Size> = map!!.getOutputSizes(SurfaceTexture::class.java)
                    mCameraSize = sizes[0]
                    for (size in sizes) {
                        if (size.width > this.mCameraSize!!.width) {
                            mCameraSize = size
                        }
                    }
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }//end of cameraCharacteristics2


    /**
     * onOpened()에서 취득한 CameraDevice로 CaptureSession, CaptureRequest가 이뤄지는데
     * Camera2 APIs 처리과정을 MainActivity에서 일원화하여 표현하기 위해 인터페이스로 처리.
     */
    private val mCameraDeviceStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            mInterface!!.onCameraDeviceOpened(camera, mCameraSize)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }

    /**
     * 실질적인 해당 카메라를 나타내는 클래스.
     * CameraManager에 의해 비동기 콜백으로 취득.
     * 비동기 콜백 CameraDevice.StateCallback onOpened()로 취득.
     * null파라미터는 MainThread를 이용하고, 작성한 Thread Handler를 넘겨주면 해당 Thread로 콜백이 떨어진다. 비교적 딜레이가 큰(~500ms) 작업이라 Thread 권장.
     */
    fun cameraDevice3(cameraManager: CameraManager, cameraId: String?, context: Context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            cameraManager.openCamera(cameraId!!, mCameraDeviceStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val mCaptureSessionCallback: CameraCaptureSession.StateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            try {
                mCaptureSession = cameraCaptureSession
                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder!!.build(), mCaptureCallback, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
    }

    /**
     * CameraCaptureSession
     * CameraDevice에 의해 이미지 캡쳐를 위한 세션 연결.
     * 해당 세션이 연결될 surface를 전달.
     */
    fun captureSession4(cameraDevice: CameraDevice, surface: Surface?) {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(surface), mCaptureSessionCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * CaptureRequest
     * CameraDevice에 의해 Builder패턴으로 생성하며, 단일 이미지 캡쳐를 위한 하드웨어 설정(센서, 렌즈, 플래쉬) 및 출력 버퍼등의 정보(immutable).
     * 해당 리퀘스트가 연결될 세션의 surface를 타겟으로 지정.
     */
    fun captureRequest5(cameraDevice: CameraDevice, surface: Surface?) {
        try {
            mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            if (surface != null) {
                mPreviewRequestBuilder!!.addTarget(surface)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * CaptureResult
     * CaptureRequest가 수행되고 비동기 CameraCaptureSession.CaptureCallback으로 취득.
     * 해당 세션의 리퀘스트 정보 뿐만 아니라 캡쳐 이미지의 Metadata정보도 포함.
     */
    private val mCaptureCallback: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
        }
    }

    fun closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (null != mCameraDevice) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
    }
}