package com.vyw.tflite

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vyw.tflite.databinding.ActivityMainBinding
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
    }

    fun btnClick(view: View) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        }else{
            val intent = Intent(this, FaceCalibrator::class.java)
            startActivity(intent)
        }
    }

    fun about_btnClick(view: View) {
        val intent = Intent(this, About::class.java)
        startActivity(intent)
    }

    fun settings_btnClick(view: View) {
        val intent = Intent(this, settings_activity::class.java)
        startActivity(intent)
    }

    fun exit_click(view: View) {
        val mBuilder = AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes", null)
            .setNegativeButton("No", null)

        val mAlertDialog = mBuilder.create()
        mAlertDialog.show()

        val mYesButton = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        mYesButton.setOnClickListener {
            finish()
        }
        val mNoButton = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        mNoButton.setOnClickListener {
            mAlertDialog.cancel()
        }

    }


}
