
#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#define  LOG_TAG    "SkyDRMHoopsFacade"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

bool registerMobileSurfaceViewNatives(JNIEnv *env);
bool registerAndroidUserMobileSurfaceViewNatives(JNIEnv *env);
bool registerMobileAppNatives(JNIEnv *env);

JavaVM *g_javaVM;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv *env = NULL;
	if ((vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) || (env == NULL)) {
		LOGE("Error calling GetEnv");
		return -1;
	}

	g_javaVM = vm;

	if (!registerMobileSurfaceViewNatives(env))
		return -1;

	if (!registerAndroidUserMobileSurfaceViewNatives(env))
		return -1;

	if (!registerMobileAppNatives(env))
		return -1;

	return JNI_VERSION_1_6;
}

