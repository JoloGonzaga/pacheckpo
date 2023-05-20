package com.vyw.tflite

data class Settings(
    var fps : Int = 0,
    var ringtone : String = "",
)

data class Calibration(
    var earAVG : Double = 0.0,
    var marAVG : Double = 0.0,
    var headPoseAVG : Double = 0.0,
)
