
// Auto-generated file

#include "UserMobileSurface.h"

#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "jpaths.h"

#define  JNI_LOG_TAG    "SkyDRMHoopsFacadeUserMobileSurfaceView"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,JNI_LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,JNI_LOG_TAG,__VA_ARGS__)

#include "JNIHelpers.h"

static jboolean loadFileS(JNIEnv *env, jclass cobj, jlong ptr, jstring fileName) {
    if (ptr == NULL) {
        return JNI_FALSE;
    }
    try {

        JNIHelpers::String cfileName(env, fileName);
        jboolean ret = ((UserMobileSurface *) ptr)->loadFile(cfileName.str());
        return ret;
    } catch (std::exception &e) {
        LOGE("Exception: %s", e.what());
    }
    return JNI_FALSE;
}


static void setOperatorOrbitV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->setOperatorOrbit();

}


static void setOperatorZoomAreaV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->setOperatorZoomArea();

}


static void setOperatorFlyV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->setOperatorFly();

}


static void setOperatorSelectPointV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->setOperatorSelectPoint();

}


static void setOperatorSelectAreaV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->setOperatorSelectArea();

}


static void onModeSimpleShadowZ(JNIEnv *env, jclass cobj, jlong ptr, jboolean enable) {

    ((UserMobileSurface *) ptr)->onModeSimpleShadow(enable);

}


static void onModeSmoothV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onModeSmooth();

}


static void onModeHiddenLineV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onModeHiddenLine();

}


static void onModeFrameRateV(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onModeFrameRate();

}


static void onUserCode1V(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onUserCode1();

}


static void onUserCode2V(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onUserCode2();

}


static void onUserCode3V(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onUserCode3();

}


static void onUserCode4V(JNIEnv *env, jclass cobj, jlong ptr) {

    ((UserMobileSurface *) ptr)->onUserCode4();

}


bool registerAndroidUserMobileSurfaceViewNatives(JNIEnv *env) {
    jclass k = env->FindClass(JPATH_ANDROID_USER_MOBILE_SURFACE_VIEW);
    if (k == NULL) {
        LOGE("Error loading class %s", JPATH_ANDROID_USER_MOBILE_SURFACE_VIEW);
        return false;
    }

    JNINativeMethod methods[] = {
            {"loadFileS",               "(JLjava/lang/String;)Z", (void *) loadFileS},
            {"setOperatorOrbitV",       "(J)V",                   (void *) setOperatorOrbitV},
            {"setOperatorZoomAreaV",    "(J)V",                   (void *) setOperatorZoomAreaV},
            {"setOperatorFlyV",         "(J)V",                   (void *) setOperatorFlyV},
            {"setOperatorSelectPointV", "(J)V",                   (void *) setOperatorSelectPointV},
            {"setOperatorSelectAreaV",  "(J)V",                   (void *) setOperatorSelectAreaV},
            {"onModeSimpleShadowZ",     "(JZ)V",                  (void *) onModeSimpleShadowZ},
            {"onModeSmoothV",           "(J)V",                   (void *) onModeSmoothV},
            {"onModeHiddenLineV",       "(J)V",                   (void *) onModeHiddenLineV},
            {"onModeFrameRateV",        "(J)V",                   (void *) onModeFrameRateV},
            {"onUserCode1V",            "(J)V",                   (void *) onUserCode1V},
            {"onUserCode2V",            "(J)V",                   (void *) onUserCode2V},
            {"onUserCode3V",            "(J)V",                   (void *) onUserCode3V},
            {"onUserCode4V",            "(J)V",                   (void *) onUserCode4V},
    };
    const size_t count = sizeof(methods) / sizeof(methods[0]);

    if (env->RegisterNatives(k, methods, count) != JNI_OK) {
        LOGE("Error calling RegisterNatives");
        return false;
    }

    return true;
}

