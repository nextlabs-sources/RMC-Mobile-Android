#include "com_skydrm_sdk_nxl_NxlFileHandler.h"
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include <utils.h>

#include "nxlexception.hpp"

#ifdef ANDROID_ENV_FIPS_MODE

#include "openssl/crypto.h"
#include "openssl/err.h"

#endif

#ifndef LOG_TAG
#define LOG_TAG "NXL_BRIDGE_NXLUTILS"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#endif

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
#ifdef ANDROID_ENV_FIPS_MODE
    // Init FIPS mode.
    if (FIPS_mode() != 0) {
        LOGD("Already in FIPS mode\n");
    } else {
        int ret = FIPS_mode_set(1);
        if (ret == 0) {
            LOGE("FIPS_mode_set() returns 0\n");
            unsigned long err;
            while ((err = ERR_get_error()) != 0) {
                LOGE("\tmsg = %lu, %s\n", err, ERR_error_string(err, NULL));
            }
        }
    }
#endif

    return JNI_VERSION_1_6;
}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    isMatchNxlFmt
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_isMatchNxlFmt
        (JNIEnv *env, jclass obj, jstring pathObj, jboolean isFastObj) {
    bool rt = false;
    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == pathObj)
        return (jboolean) false;
    // prepare params
    const char *path = env->GetStringUTFChars(pathObj, NULL);
    if (NULL == path)
        return (jboolean) false;
    // do task
    try {

        if (JNI_TRUE == isFastObj) {
            rt = nxl::util::simplecheck(path);
        } else {
            rt = nxl::util::checkNXL(path);
        }
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }
    // clean up
    env->ReleaseStringUTFChars(pathObj, path);

    return (jboolean) rt;

}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    convertToNxlFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_convertToNxlFile
        (JNIEnv *env, jclass obj, jstring ownerIdOjb, jstring srcOjb, jstring dstObj,
         jbyteArray keyObj, jboolean overwrite) {
    bool rt = false;

    //sanity check
    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == ownerIdOjb)
        return (jboolean) false;
    if (NULL == srcOjb)
        return (jboolean) false;
    if (NULL == dstObj)
        return (jboolean) false;
    if (NULL == keyObj)
        return (jboolean) false;

    //prepare params
    const char *ownerId = env->GetStringUTFChars(ownerIdOjb, NULL);
    if (NULL == ownerId)
        return (jboolean) false;
    const char *src = env->GetStringUTFChars(srcOjb, NULL);
    if (NULL == src)
        return (jboolean) false;
    const char *dst = env->GetStringUTFChars(dstObj, NULL);
    if (NULL == dst)
        return (jboolean) false;
    jbyte *pKey = env->GetByteArrayElements(keyObj, NULL);
    if (NULL == pKey)
        return (jboolean) false;
    // do task
    try {
        nxl::util::convert(ownerId, src, dst, (NXL_CRYPTO_TOKEN *) pKey, NULL, overwrite);
        rt = true;
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }
    // clear up
    env->ReleaseStringUTFChars(ownerIdOjb, ownerId);
    env->ReleaseStringUTFChars(srcOjb, src);
    env->ReleaseStringUTFChars(dstObj, dst);
    env->ReleaseByteArrayElements(keyObj, pKey, 0);

    return (jboolean) rt;
}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    decryptToNormalFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;[BZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_decryptToNormalFile
        (JNIEnv *env, jclass obj, jstring srcOjb, jstring dstObj, jbyteArray keyObj,
         jboolean overwrite) {
    bool rt = false;

    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == srcOjb)
        return (jboolean) false;
    if (NULL == dstObj)
        return (jboolean) false;
    if (NULL == keyObj)
        return (jboolean) false;
    // prepare params
    const char *src = env->GetStringUTFChars(srcOjb, NULL);
    if (NULL == src)
        return (jboolean) false;
    const char *dst = env->GetStringUTFChars(dstObj, NULL);
    if (NULL == dst)
        return (jboolean) false;
    jbyte *pKey = env->GetByteArrayElements(keyObj, NULL);
    if (NULL == pKey)
        return (jboolean) false;
    // do task
    try {
        // osm:wait for verion2
        nxl::util::decrypt(src, dst, (NXL_CRYPTO_TOKEN *) pKey, overwrite);
        rt = true;
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }

    // clean up
    env->ReleaseStringUTFChars(srcOjb, src);
    env->ReleaseStringUTFChars(dstObj, dst);
    env->ReleaseByteArrayElements(keyObj, pKey, 0);

    return (jboolean) rt;

}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    extractInfoFromNxlFile
 * Signature: (Ljava/lang/String;[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_extractInfoFromNxlFile
        (JNIEnv *env, jclass obj, jstring pathObj, jbyteArray ownerIdObj, jbyteArray DUIDObj) {
    bool rt = false;

    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == pathObj)
        return (jboolean) false;
    if (NULL == ownerIdObj)
        return (jboolean) false;
    if (NULL == DUIDObj)
        return (jboolean) false;

    // prepare params
    const char *path = env->GetStringUTFChars(pathObj, NULL);
    if (NULL == path)
        return (jboolean) false;

    jbyte *pOwnerId = env->GetByteArrayElements(ownerIdObj, NULL);
    if (NULL == pOwnerId)
        return (jboolean) false;

    jbyte *pDUID = env->GetByteArrayElements(DUIDObj, NULL);
    if (NULL == pDUID)
        return (jboolean) false;


    // do task
    try {
        // for DUID
        NXL_CRYPTO_TOKEN token = {0};
        nxl::util::read_token_info_from_nxl(path, &token);
        memcpy(pDUID, token.UDID, 32);

        // for owner id
        int len_buf = 256;
        char buf[256] = {0};
        nxl::util::read_ownerid_from_nxl(path, buf, &len_buf);
        memcpy(pOwnerId, buf, len_buf);

        rt = true;

    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }

    // clean up
    env->ReleaseStringUTFChars(pathObj, path);
    env->ReleaseByteArrayElements(ownerIdObj, pOwnerId, 0);
    env->ReleaseByteArrayElements(DUIDObj, pDUID, 0);


    return (jboolean) rt;

}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    extractFingerPrint
 * Signature: (Ljava/lang/String;[BI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_extractFingerPrint
        (JNIEnv *env, jclass obj, jstring pathObj, jbyteArray fingerprintObj, jint version) {

    bool rt = false;
    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == pathObj)
        return (jboolean) false;
    if (NULL == fingerprintObj)
        return (jboolean) false;
    // prepare params
    const char *path = env->GetStringUTFChars(pathObj, NULL);
    if (NULL == path)
        return (jboolean) false;

    jbyte *pfingerprint = env->GetByteArrayElements(fingerprintObj, NULL);
    if (NULL == pfingerprint)
        return (jboolean) false;

    // do task
    try {
        if (version == 1) {
            /*for version 1; total bytes is 804
                rootAgreementKey    [256]
                icaAgreementKey     [256]
                ownerid             [256];
                duid                [32]
                ml                  4;*/

            NXL_CRYPTO_TOKEN token = {0};
            nxl::util::read_token_info_from_nxl(path, &token);
            // rootAgreementKey
            memcpy(pfingerprint, token.PublicKey, 256);
            // rootICAAgreementKey
            memcpy(pfingerprint + 256, token.PublicKeyWithiCA, 256);
            // ownerid
            int len_buf = 256;
            nxl::util::read_ownerid_from_nxl(path, (char *) (pfingerprint + (256 + 256)), &len_buf);
            // duid
            memcpy(pfingerprint + (256 + 256 + 256), token.UDID, 32);
            // ml
            memcpy(pfingerprint + (256 + 256 + 256 + 32), &token.ml, 4);
            rt = true;
        } else if (version == 2) {
            /*for version 2; total bytes is 836
                rootAgreementKey    [256]
                icaAgreementKey     [256]
                ownerid             [256];
                duid                [32]
                ml                  4;
                otp                [32];*/

            NXL_CRYPTO_TOKEN token = {0};
            nxl::util::read_token_info_from_nxl(path, &token);
            // rootAgreementKey
            memcpy(pfingerprint, token.PublicKey, 256);
            // rootICAAgreementKey
            memcpy(pfingerprint + 256, token.PublicKeyWithiCA, 256);
            // ownerid
            int len_buf = 256;
            nxl::util::read_ownerid_from_nxl(path, (char *) (pfingerprint + (256 + 256)), &len_buf);
            // duid
            memcpy(pfingerprint + (256 + 256 + 256), token.UDID, 32);
            // ml
            memcpy(pfingerprint + (256 + 256 + 256 + 32), &token.ml, 4);
            // otp
            memcpy(pfingerprint + (256 + 256 + 256 + 32 + 4), &token.otp, 32);

            rt = true;
        } else {
            rt = false;
        }
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }

    // clean up
    env->ReleaseStringUTFChars(pathObj, path);
    env->ReleaseByteArrayElements(fingerprintObj, pfingerprint, 0);

    return (jboolean) rt;

}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    calcHmacSha256
 * Signature: ([B[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_calcHmacSha256
        (JNIEnv *env, jclass obj, jbyteArray keyObj, jbyteArray msgObj, jbyteArray outhashObj) {
    bool rt = false;

    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == keyObj)
        return (jboolean) false;
    if (NULL == msgObj)
        return (jboolean) false;
    if (NULL == outhashObj)
        return (jboolean) false;
    // prepare parmas
    jbyte *key = env->GetByteArrayElements(keyObj, NULL);
    if (NULL == key)
        return (jboolean) false;
    jsize key_len = env->GetArrayLength(keyObj);

    jbyte *msg = env->GetByteArrayElements(msgObj, NULL);
    if (NULL == msg)
        return (jboolean) false;
    jsize msg_len = env->GetArrayLength(msgObj);

    jbyte *outhash = env->GetByteArrayElements(outhashObj, NULL);
    if (NULL == outhash)
        return (jboolean) false;

    char *pMsg = new char[msg_len + 1];
    memset(pMsg, 0, msg_len + 1);
    memcpy(pMsg, msg, msg_len);

    char *pKey = new char[key_len + 1];
    memset(pKey, 0, key_len + 1);
    memcpy(pKey, key, key_len);

    int hashlen = 64;
    char *pHash = new char[hashlen + 1];
    memset(pHash, 0, hashlen + 1);


    // do task
    try {
        if (key_len != 64) {
            throw NXEXCEPTION("key must is a hexString ,and length is 64");
        }

        nxl::util::hmac_sha256((const char *) pMsg, msg_len, (const char *) pKey, (char *) pHash,
                               &hashlen);
        //{
        //	LOGE("hmac_sha256_ key: %s", pKey);
        //	LOGE("hmac_sha256_ msg: %s", pMsg);
        //	LOGE("hmac_sha256_ hsh: %s", pHash);
        //}
        // copy to out
        memcpy(outhash, pHash, 64);
        rt = true;
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }
    // release
    if (pMsg != NULL) {
        delete[]pMsg;
    }
    if (pKey != NULL) {
        delete[] pKey;
    }
    if (pHash != NULL) {
        delete[] pHash;
    }

    env->ReleaseByteArrayElements(keyObj, key, 0);
    env->ReleaseByteArrayElements(msgObj, msg, 0);
    env->ReleaseByteArrayElements(outhashObj, outhash, 0);

    return (jboolean) rt;
}

/*
 * Class:     com_skydrm_sdk_nxl_NxlFileHandler
 * Method:    sectionInfo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Z[B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_sectionInfo
        (JNIEnv *env, jclass obj, jstring pathObj, jstring sectionObj, jboolean bset,
         jbyteArray keyObj, jbyteArray bufObj) {

    bool rt = false;

    // sanity check
    if (NULL == env)
        return (jboolean) false;
    if (NULL == pathObj)
        return (jboolean) false;
    if (NULL == sectionObj)
        return (jboolean) false;
    if (NULL == keyObj)
        return (jboolean) false;
    if (NULL == bufObj)
        return (jboolean) false;

    // prepare parmas
    const char *path = env->GetStringUTFChars(pathObj, NULL);
    if (NULL == path)
        return (jboolean) false;
    const char *sec_name = env->GetStringUTFChars(sectionObj, NULL);
    if (NULL == sec_name)
        return (jboolean) false;
    jbyte *pKey = env->GetByteArrayElements(keyObj, NULL);
    if (NULL == pKey)
        return (jboolean) false;
    jbyte *pBuf = env->GetByteArrayElements(bufObj, NULL);
    if (NULL == pBuf)
        return (jboolean) false;
    jsize buf_len = env->GetArrayLength(bufObj);
    // do task

    try {
        int flag = 0;
        if (bset) {
            nxl::util::write_section_in_nxl(path,
                                            sec_name, (const char *) pBuf,
                                            strlen((const char *) pBuf), flag,
                                            (const NXL_CRYPTO_TOKEN *) pKey);
        } else {
            nxl::util::read_section_in_nxl(path, sec_name,
                                           (char *) pBuf, (int *) &buf_len, &flag,
                                           (const NXL_CRYPTO_TOKEN *) pKey);
        }
        rt = true;
    } catch (nxl::exception &ex) {
        LOGE("Exception: %s", ex.details().c_str());
        rt = false;
    } catch (std::exception &ex) {
        LOGE("Exception: %s", ex.what());
        rt = false;
    }

    // release
    env->ReleaseStringUTFChars(pathObj, path);
    env->ReleaseStringUTFChars(sectionObj, sec_name);
    env->ReleaseByteArrayElements(keyObj, pKey, 0);
    env->ReleaseByteArrayElements(bufObj, pBuf, 0);
    return (jboolean) rt;
}

JNIEXPORT jboolean JNICALL Java_com_skydrm_sdk_nxl_NxlFileHandler_readSection
        (JNIEnv *env, jclass obj, jstring localNxlPathObj, jstring sectionNameObj,
         jbyteArray bufferObj) {

    bool rt = false;
    //Sanity check first.
    //Local nxl path.
    const char *localPath = env->GetStringUTFChars(localNxlPathObj, nullptr);
    if (localPath == nullptr) {
        return (jboolean) false;
    }
    const char *sectionName = env->GetStringUTFChars(sectionNameObj, nullptr);
    //Decide which section u want to read(adhoc or central policy).
    if (sectionName == nullptr) {
        return (jboolean) false;
    }
    jbyte *buffer = env->GetByteArrayElements(bufferObj, nullptr);
    //Buffer to load prepared section.
    if (buffer == nullptr) {
        return (jboolean) false;
    }
    jsize bufferLength = env->GetArrayLength(bufferObj);

    try {
        int flag = 0;
        nxl::util::read_section_in_nxl(localPath, sectionName, (char *) buffer, &bufferLength,
                                       &flag);
        rt = true;
    } catch (nxl::exception &e) {
        LOGE("Exception: %s", e.details().c_str());
        rt = false;
    }

    //Release resource.
    env->ReleaseStringUTFChars(localNxlPathObj, localPath);
    env->ReleaseStringUTFChars(sectionNameObj, sectionName);
    env->ReleaseByteArrayElements(bufferObj, buffer, 0);

    return (jboolean) rt;
}

#ifdef __cplusplus
}
#endif