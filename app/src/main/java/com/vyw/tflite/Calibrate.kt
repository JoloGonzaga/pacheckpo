package com.vyw.tflite

import android.app.Activity
import android.content.Intent
import android.graphics.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.vyw.tflite.databinding.ActivityCalibrateBinding

class Calibrate: AppCompatActivity() {
    private var thread = ThreadPool()

    private lateinit var activityBundle : Bundle

    private var earData = ArrayList<Double>()
    private var earAverage : Double = 0.0
    private lateinit var binding: ActivityCalibrateBinding

    private val loadData = Runnable {
        try{
            Thread.sleep(5000)
        }catch (e : java.lang.Exception){
            e.printStackTrace()
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post{
            val intent = Intent(this, CameraStarter::class.java)
            var bundle = Bundle()
            bundle.putDouble("earAVG", earAverage)
            intent.putExtra("data", bundle)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalibrateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityBundle = intent.getBundleExtra("earBundle")!!
        val earStringArray = activityBundle?.getStringArrayList("ear")
        if (earStringArray != null) {
            for(x in earStringArray){
                earData.add(x.toDouble())
            }

            earAverage = earData.sum() / earData.size
        }
        Log.d("EarData", "Data : $earData \nAverage : $earAverage")

        thread.execute(loadData)
    }
}