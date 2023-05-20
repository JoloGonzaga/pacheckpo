// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>
#include <time.h>

#include "Face_detection/face.h"

#include "Face_detection/ndkcamera.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON

using namespace std::chrono;

std::vector<Object> faceobjects;

static int draw_unsupported(cv::Mat& rgb)
{
    const char text[] = "unsupported";

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);

    int y = (rgb.rows - label_size.height) / 2;
    int x = (rgb.cols - label_size.width) / 2;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                    cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 0, 0));

    return 0;
}

static int draw_fps(cv::Mat& rgb)
{
    // resolve moving average
    float avg_fps = 0.f;
    {
        static double t0 = 0.f;
        static float fps_history[10] = {0.f};

        double t1 = ncnn::get_current_time();
        if (t0 == 0.f)
        {
            t0 = t1;
            return 0;
        }

        float fps = 1000.f / (t1 - t0);
        t0 = t1;

        for (int i = 9; i >= 1; i--)
        {
            fps_history[i] = fps_history[i - 1];
        }
        fps_history[0] = fps;

        if (fps_history[9] == 0.f)
        {
            return 0;
        }

        for (int i = 0; i < 10; i++)
        {
            avg_fps += fps_history[i];
        }
        avg_fps /= 10.f;
    }

    char text[32];
    sprintf(text, "FPS=%.2f", avg_fps);

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

    int y = 0;
    int x = rgb.cols - label_size.width;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                    cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));

    return 0;
}

float threshold = 0.15f;
time_t timeSecStart = 0;
static int draw_alert(cv::Mat& rgb){
    if(faceobjects.size() == 1){
        float avgEAR = (faceobjects[0].earright + faceobjects[0].earleft) / 2;

        if(avgEAR < threshold){
            if(timeSecStart == NULL){
                time(&timeSecStart);
            }
            time_t timeSecNow;
            time(&timeSecNow);
            int sec = (int)timeSecNow - (int)timeSecStart;
            //__android_log_print(ANDROID_LOG_DEBUG, "alertTime", "time %i", sec);
            if(sec > 2){
//                __android_log_print(ANDROID_LOG_DEBUG, "alertTimeNotif", "ALERT");
                faceobjects[0].isAlert = true;
                const char text[] = "ALERT";

                int baseLine = 0;
                cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);

                int y = (rgb.rows - label_size.height) / 2;
                int x = (rgb.cols - label_size.width) / 2;

                cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                              cv::Scalar(255, 0, 0), -1);

                cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                            cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(255, 255, 255));
            }
        }else{
            time(&timeSecStart);
            faceobjects[0].isAlert = false;
        }

    }
    return 0;
}

static Face* g_blazeface = 0;
static ncnn::Mutex lock;


//Class for DETECTOR
class MyNdkCamera : public NdkCameraWindow
{
public:
    void on_image_render(cv::Mat& rgb) const override;
};

bool isdraw = true;
void MyNdkCamera::on_image_render(cv::Mat& rgb) const
{
    {
        ncnn::MutexLockGuard g(lock);

        if (g_blazeface)
        {
            high_resolution_clock::time_point t1 = high_resolution_clock::now();
            g_blazeface->detect(rgb, faceobjects);
            high_resolution_clock::time_point t2 = high_resolution_clock::now();

            if(faceobjects.size() > 0 && isdraw){
//                duration<double, std::milli> time_span = t2-t1;
//                __android_log_print(ANDROID_LOG_DEBUG, "TimeFace","MiliSegundo: %f", time_span.count());
                g_blazeface->draw(rgb, faceobjects, true);
            }

            draw_alert(rgb);
        }
        else
        {
            draw_unsupported(rgb);
        }
    }
    draw_fps(rgb);
}

//Class for DETECTOR
class CameraCalibration : public NdkCameraWindow
{
public:
    virtual void on_image_render(cv::Mat& rgb) const;
};

void CameraCalibration::on_image_render(cv::Mat& rgb) const
{
    {
        ncnn::MutexLockGuard g(lock);

        if (g_blazeface)
        {
            high_resolution_clock::time_point t1 = high_resolution_clock::now();
            g_blazeface->detect(rgb, faceobjects);
            high_resolution_clock::time_point t2 = high_resolution_clock::now();

            if(faceobjects.size() > 0 && isdraw){
//                duration<double, std::milli> time_span = t2-t1;
//                __android_log_print(ANDROID_LOG_DEBUG, "TimeFace","MiliSegundo: %f", time_span.count());
                g_blazeface->draw(rgb, faceobjects, true);
            }

            draw_alert(rgb);
        }
        else
        {
            draw_unsupported(rgb);
        }
    }
    draw_fps(rgb);
}

static MyNdkCamera* g_camera = 0;

extern "C" {
    JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");

//        g_camera = new MyNdkCamera;

        return JNI_VERSION_1_4;
    }

    JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

        {
            ncnn::MutexLockGuard g(lock);

            delete g_blazeface;
            g_blazeface = 0;
        }

        delete g_camera;
        g_camera = 0;
    }
}

extern "C" {
    // public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_loadModel(JNIEnv* env, jobject thiz, jobject assetManager, jint modelid, jint cpugpu, jboolean draw)
    {
        isdraw = draw;
        if (modelid < 0 || modelid > 6 || cpugpu < 0 || cpugpu > 1)
        {
            return JNI_FALSE;
        }

        AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);
        const char* modeltypes[] =
        {
            "blazeface",
            "blazeface",
            "blazeface"
        };
        const int target_sizes[] =
        {
            192,
            320,
            640
        };
        const char* modeltype = modeltypes[(int)modelid];
        int target_size = target_sizes[(int)modelid];
        bool use_gpu = (int)cpugpu == 1;

        char modelFinal[256];
        sprintf(modelFinal, "Models/%s", modeltype);
        __android_log_print(ANDROID_LOG_DEBUG, "ncnnModel", "loadModel %s", modelFinal);
        // reload
        {
            ncnn::MutexLockGuard g(lock);

            if (use_gpu && ncnn::get_gpu_count() == 0)
            {
                // no gpu
                delete g_blazeface;
                g_blazeface = 0;
            }
            else
            {
                if (!g_blazeface)
                    g_blazeface = new Face;
                g_blazeface->load(mgr, modelFinal,target_size, use_gpu);
            }
        }

        return JNI_TRUE;
    }

    // public native boolean openCamera(int facing);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_openCamera(JNIEnv* env, jobject thiz, jint facing)
    {
        if (facing < 0 || facing > 1)
            return JNI_FALSE;

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);

        g_camera->open((int)facing);

        return JNI_TRUE;
    }

    // public native boolean closeCamera();
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_closeCamera(JNIEnv* env, jobject thiz)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");

        g_camera->close();

        return JNI_FALSE;
    }

    // public native boolean setOutputWindow(Surface surface);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_setOutputWindow(JNIEnv* env, jobject thiz, jobject surface)
    {
        ANativeWindow* win = ANativeWindow_fromSurface(env, surface);

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);

        g_camera->set_window(win);

        return JNI_TRUE;
    }
    JNIEXPORT jstring JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_alertTrigger(JNIEnv *env, jobject thiz) {
        return env->NewStringUTF((faceobjects.size() > 0)?std::to_string(faceobjects[0].isAlert).c_str():"NF");
    }

    JNIEXPORT void JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnn_initiateCamera(JNIEnv *env, jobject thiz) {
        g_camera = new MyNdkCamera;
    }
}

extern "C" {
// public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_loadModel(JNIEnv* env, jobject thiz, jobject assetManager, jint modelid, jint cpugpu)
    {
        if (modelid < 0 || modelid > 6 || cpugpu < 0 || cpugpu > 1)
        {
            return JNI_FALSE;
        }

        AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);
        const char* modeltypes[] =
                {
                        "blazeface",
                        "blazeface",
                        "blazeface"
                };
        const int target_sizes[] =
                {
                        192,
                        320,
                        640
                };
        const char* modeltype = modeltypes[(int)modelid];
        int target_size = target_sizes[(int)modelid];
        bool use_gpu = (int)cpugpu == 1;

        char modelFinal[256];
        sprintf(modelFinal, "Models/%s", modeltype);
        __android_log_print(ANDROID_LOG_DEBUG, "ncnnModel", "loadModel %s", modelFinal);
        // reload
        {
            ncnn::MutexLockGuard g(lock);

            if (use_gpu && ncnn::get_gpu_count() == 0)
            {
                // no gpu
                delete g_blazeface;
                g_blazeface = 0;
            }
            else
            {
                if (!g_blazeface)
                    g_blazeface = new Face;
                g_blazeface->load(mgr, modelFinal,target_size, use_gpu);
            }
        }

        return JNI_TRUE;
    }

    // public native boolean openCamera(int facing);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_openCamera(JNIEnv* env, jobject thiz, jint facing)
    {
        if (facing < 0 || facing > 1)
            return JNI_FALSE;

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);

        g_camera->open((int)facing);

        return JNI_TRUE;
    }

    // public native boolean closeCamera();
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_closeCamera(JNIEnv* env, jobject thiz)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");

        g_camera->close();

        return JNI_FALSE;
    }

    // public native boolean setOutputWindow(Surface surface);
    JNIEXPORT jboolean JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_setOutputWindow(JNIEnv* env, jobject thiz, jobject surface)
    {
        ANativeWindow* win = ANativeWindow_fromSurface(env, surface);

        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);

        g_camera->set_window(win);

        return JNI_TRUE;
    }
    JNIEXPORT jstring JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_alertTrigger(JNIEnv *env, jobject thiz) {
        return env->NewStringUTF((faceobjects.size() > 0)?std::to_string(faceobjects[0].isAlert).c_str():"NF");
    }

    JNIEXPORT jdoubleArray JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_calibrateFeature(JNIEnv *env, jobject thiz) {
        jdoubleArray result;
        if(!faceobjects.empty()){
            result = env->NewDoubleArray(3);
            if(result == NULL){
                return NULL;
            }

            jdouble arrayData[3];
            double ear = (faceobjects[0].earleft + faceobjects[0].earright)/2;
            double mar = faceobjects[0].mar;

            arrayData[0] = ear;
            arrayData[1] = mar;
            arrayData[2] = 0.0;

            env->SetDoubleArrayRegion(result, 0, 3, arrayData);
            return result;
        }else{
            return result;
        }
    }

    JNIEXPORT void JNICALL
    Java_com_vyw_tflite_BlazeFaceNcnnCalibrate_initiateCamera(JNIEnv *env, jobject thiz) {
        g_camera = new MyNdkCamera;
    }
}