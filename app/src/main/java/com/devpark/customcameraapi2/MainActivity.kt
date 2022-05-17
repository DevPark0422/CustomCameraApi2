package com.devpark.customcameraapi2

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    @JvmField
    val REQUEST_CAMERA_PERMISSION = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun onMoveGoogle(view: View) {

    }//end of onMoveGoogle

    fun onMoveCamera(view: View) {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setTitle("카메라 접근 퍼미션")
            dialog.setMessage(R.string.camera_permission_message)
            dialog.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener() { _, _ ->
                requestCameraPermission()
            })
            dialog.setNegativeButton(android.R.string.cancel, null)
            dialog.show()
            return
        } else {
            goCamera()
        }
    }//end of onMoveCamera

    fun onMoveGallery(view: View) {


    }//end of onMoveGallery


    private fun requestCameraPermission() {
        /**
         * shouldShowRequestPermissionRationale
         * 사용자가 권한 요청을 명시적으로 거부한 경우 true를 반환한다.
         * 사용자가 권한 요청을 처음 보거나, 다시 묻지 않음 선택한 경우, 권한을 허용한 경우 false를 반환한다.
         */
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            dialog.setTitle("앱 설정 이동")
            dialog.setMessage("앱 설정에서 카메라 접근 권한 허용이 필요합니다.")
            dialog.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener() { _, _ ->
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${applicationContext.packageName}"))
                startActivity(i)
            })
            dialog.setNegativeButton(android.R.string.cancel, null)
            dialog.show()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goCamera()
            } else {
                Toast.makeText(this, "카메라 접근을 하기 위해 권한 허용이 필요하합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun goCamera() {
        startActivity(Intent(this, CameraActivity::class.java))
    }

}//end of class
