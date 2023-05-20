package com.vyw.tflite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vyw.tflite.databinding.ActivityFaceCalibratorBinding
import java.util.*
import kotlin.collections.ArrayList

class FaceCalibrator : Activity(), SurfaceHolder.Callback {
    private val REQUEST_CAMERA = 100
    private lateinit var  binding: ActivityFaceCalibratorBinding
    private var blazefacecnn = BlazeFaceNcnnCalibrate()

    private val facing = 0
    private var currentModel = 0
    private var currentCPUGPU = 0
    private var isCameraOpen : Boolean = false
    @Volatile private var isActiveCalibrate : Boolean = false

    private var myThread = ThreadPool()
    private var earData = ArrayList<String>()
    private var marData = ArrayList<String>()
    private val calibration = Runnable {
            if(!isActiveCalibrate){
                isActiveCalibrate = true
                earData = ArrayList()
                while (earData.size < 3) {
                    val data : DoubleArray = blazefacecnn.calibrateFeature()
                    if(data != null){
                        Log.v("FaceCalibratorData" , "Array value: ${data[0]} ${data[1]} ${data[2]}")
                        val ear = data[0]
                        if(ear != (0).toDouble() && ear < 1){
                            if(earData.isEmpty()){
                                earData.add(ear.toString())
                            }else{
                                run {
                                    val earFirst = earData.first().toDouble()
                                    val earLeast = (earFirst * 0.80)
                                    val earMost = earFirst + (earFirst - earLeast)
                                    if (ear > earLeast && ear < earMost) {
                                        earData.add(ear.toString())
                                    } else {
                                        earData = ArrayList()
                                    }
                                }
                            }
                        }

                        runOnUiThread{ binding.textView.text = earData.size.toString() }
                    }else{
                        Log.v("FaceCalibrator" , "Array value: No Data")
                    }

                    try{
                        Thread.sleep(1000)
                    }catch (e : java.lang.Exception){
                        e.printStackTrace()
                    }
                }
                val handler = Handler(Looper.getMainLooper())
                handler.post{
                    blazefacecnn.closeCamera()
                    val intent = Intent(this, Calibrate::class.java)
                    var bundle = Bundle()
                    bundle.putStringArrayList("ear", earData)
                    intent.putExtra("earBundle", bundle)
                    isActiveCalibrate = false
                    startActivity(intent)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceCalibratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blazefacecnn.initiateCamera()

        
        binding.textView.text = (0).toString()

        binding.cameraview.holder.setFormat(PixelFormat.RGBA_8888)
        binding.cameraview.holder.addCallback(this)

        reload()
    }

    private fun reload() {
        val retInit: Boolean = blazefacecnn.loadModel(assets, currentModel , currentCPUGPU)
        if (!retInit) {
            Log.e("MainActivity" , "blazefacecnn loadModel failed")
        }
    }

    // Surface override
    override fun surfaceCreated(holder: SurfaceHolder) {
        isCameraOpen = blazefacecnn.openCamera(facing)
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        blazefacecnn.setOutputWindow(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        blazefacecnn.closeCamera()
        Log.d("HatdogCallback" , "doghat destroyed")
    }

    //End of Surface override

    override fun onResume() {
        super.onResume()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    fun calibrate_button(view: View) {
        if(!isActiveCalibrate){
            myThread.execute(calibration)
        }else{
            Toast.makeText(this, "Calibration on Process", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}
