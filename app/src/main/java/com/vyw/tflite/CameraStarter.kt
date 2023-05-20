package com.vyw.tflite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vyw.tflite.databinding.ActivityCameraStarterBinding

class CameraStarter : Activity(), SurfaceHolder.Callback{
    private var calibration = Calibration()
    private val REQUEST_CAMERA = 100

    private lateinit var binding : ActivityCameraStarterBinding
    private var blazefacecnn = BlazeFaceNcnn()
    private val facing = 0
    private var currentModel = 0
    private var currentCPUGPU = 0
    @Volatile private var isCameraOpen : Boolean = false
    private var draw = true

    private val threadPool = ThreadPool()
    private val alert = Runnable {
        while (isCameraOpen) {
            blazefacecnn.alertTrigger()
            Log.v("ncnnSensor", blazefacecnn.sensor.toString())
            Log.v("ncnnSensor", calibration.earAVG.toString())
            try{
                Thread.sleep(1000)
            }catch (e : java.lang.Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if(calibration.ear.isEmpty()){
//            val intent = Intent(this, Calibrate::class.java)
//            startActivity(intent)
//        }
        val calibrateBundle = intent.getBundleExtra("data")
        calibration.copy(earAVG = calibrateBundle!!.getDouble("earAVG"))

        binding = ActivityCameraStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blazefacecnn.initiateCamera()

        binding.cameraview!!.holder.setFormat(PixelFormat.RGBA_8888)
        binding.cameraview!!.holder.addCallback(this)

//        binding.isdraw.setOnCheckedChangeListener { _, isChecked ->
//            draw = isChecked
//            reload()
//        }
        reload()
    }

    private fun reload() {
        val retInit: Boolean = blazefacecnn.loadModel(assets, currentModel , currentCPUGPU, draw)
        if (!retInit) {
            Log.e("MainActivity" , "blazefacecnn loadModel failed")
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isCameraOpen = blazefacecnn.openCamera(facing)
        threadPool.execute(alert)
    }
    override fun surfaceChanged(holder: SurfaceHolder , format: Int , width: Int , height: Int) {
        blazefacecnn.setOutputWindow(holder.surface)
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isCameraOpen = blazefacecnn.closeCamera()
        Log.d("HatdogCallback" , "doghat destroyed")
    }

    //Overrides Activity() function
    override fun onResume() {
        super.onResume()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        isCameraOpen = blazefacecnn.closeCamera()
    }

    fun back_click(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}