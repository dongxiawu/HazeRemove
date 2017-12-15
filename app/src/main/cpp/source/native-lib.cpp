#include <jni.h>
#include <opencv2/opencv.hpp>
#include "dehaze.h"


using namespace cv;
using namespace std;

extern  "C" {
/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame
        (JNIEnv *, jobject, jlong);

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame__JJ
        (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeCreateHazeRemoveModel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeCreateHazeRemoveModel
        (JNIEnv *, jobject);

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativedeleteHazeRemoveModel
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeDeleteHazeRemoveModel
        (JNIEnv *, jobject);

}

DeHaze *deHaze;


JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame
        (JNIEnv *env, jobject, jlong addrRgba){
    Mat &mRgb = *(Mat *) addrRgba;

    DeHaze deHaze(7,0.1,0.95,10E-6);
    mRgb = deHaze.imageHazeRemove(mRgb);
//    mRgb = (deHaze).videoHazeRemove(mRgb);
//    mRgb = (*deHaze).videoHazeRemove(mRgb);
}

JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeCreateHazeRemoveModel
        (JNIEnv *, jobject){
    if (deHaze != NULL){
        delete deHaze;
    }
    deHaze = new DeHaze(7,0.1,0.95,10E-6);
    deHaze->setFPS(15);
}


JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeDeleteHazeRemoveModel
        (JNIEnv *, jobject){
    if (deHaze != NULL){
        delete deHaze;
    }
}

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame__JJ
        (JNIEnv *, jobject, jlong origAddr, jlong resultAddr){
//    Mat &orig = *(Mat *) origAddr;
//    Mat &result = *(Mat *) resultAddr;
//
//    DeHaze deHaze(7,0.1,0.95,10E-6);
//    result = (deHaze).videoHazeRemove(orig);
////    result = (*deHaze).videoHazeRemove(orig);
}