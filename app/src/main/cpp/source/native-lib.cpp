#include <jni.h>
#include <opencv2/opencv.hpp>
#include "dehaze.h"
#include "cn_scut_dongxia_hazeremove_dehaze_DeHaze.h"

using namespace cv;
using namespace std;

DeHaze *deHaze;

JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1createHazeRemoveModel
        (JNIEnv *, jclass, jint r, jdouble t0, jdouble omega, jdouble eps, jint width, jint height){
    if (deHaze != NULL){
        delete deHaze;
        deHaze = NULL;
    }
    deHaze = new DeHaze(r, t0, omega, eps, width, height);
    deHaze->setFPS(15);
}

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: ([BIIJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame___3BIIJ
        (JNIEnv *env, jclass clazz, jbyteArray frame, jint width, jint height, jlong recoverAddr){

    jbyte  *frameYuv = env->GetByteArrayElements(frame, JNI_FALSE);
    Mat &recover = *(Mat *) recoverAddr;
    recover = deHaze->videoHazeRemove(frameYuv,0,width,height);
}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_createHazeRemoveModel
 * Signature: (IDDD)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1createHazeRemoveModel
        (JNIEnv *, jclass, jint r, jdouble t0, jdouble omega, jdouble eps){

    if (deHaze != NULL){
        delete deHaze;
    }
    deHaze = new DeHaze(r,t0,omega,eps);
    deHaze->setFPS(15);
}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_deleteHazeRemoveModel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1deleteHazeRemoveModel
        (JNIEnv *, jclass){
    if (deHaze != NULL){
        delete deHaze;
        deHaze = NULL;
    }
}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_videoHazeRemove
 * Signature: ([BIJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1videoHazeRemove
        (JNIEnv *env, jclass clazz, jbyteArray frame, jint format, jlong recoverAddr){
    jbyte  *frameYuv = env->GetByteArrayElements(frame, JNI_FALSE);
    Mat &recover = *(Mat *) recoverAddr;
    recover = deHaze->videoHazeRemove(frameYuv,format);
}