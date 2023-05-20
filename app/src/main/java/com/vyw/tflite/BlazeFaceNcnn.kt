package com.vyw.tflite

import android.content.res.AssetManager
import android.util.Log
import android.view.Surface


class BlazeFaceNcnn {
    var sensor : Boolean = false

    external fun loadModel(mgr: AssetManager? , modelid: Int , cpugpu: Int, draw : Boolean): Boolean
    external fun openCamera(facing: Int): Boolean
    external fun closeCamera(): Boolean
    external fun setOutputWindow(surface: Surface?): Boolean
    external fun alertTrigger() : String
    external fun initiateCamera()

    fun alertSensor(alert : String) : String{
        Log.v("ncnnThread", "Hello $alert")
        sensor = alert == "1"

        return ""
    }

    companion object{
        init {
            System.loadLibrary("native-lib")
        }
    }
}