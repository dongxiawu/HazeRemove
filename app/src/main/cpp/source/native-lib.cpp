#include <jni.h>
#include <opencv2/opencv.hpp>
#include <fastguidedfilter.h>
#include "dehaze.h"
//#include "darkchannel.h"
#include "cn_scut_dongxia_hazeremove_dehaze_DeHaze.h"

using namespace cv;
using namespace std;

extern  "C" {
/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: (J)V
 */
//JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame
//        (JNIEnv *, jobject, jlong);

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

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: ([BIIJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame___3BIIJ
        (JNIEnv *, jclass, jbyteArray, jint, jint, jlong);

}

DeHaze *deHaze;

//
//JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame
//        (JNIEnv *env, jobject, jlong addrRgba){
//    Mat &mRgb = *(Mat *) addrRgba;
//
//    mRgb = (*deHaze).videoHazeRemove(mRgb);
//}

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


//JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1estimateAtmosphericLight
//        (JNIEnv *env, jclass, jlong addrRgba, jfloatArray array){
//    Mat &mRgba = *(Mat *) addrRgba;
//
//    jfloat *airLight;
//
//    airLight = env->GetFloatArrayElements(array,NULL);
//    int len = env->GetArrayLength(array);
//
//    if  (airLight == NULL || len != 3){
//        return;
//    }
//
//    Vec3f a = DeHaze(7,0.1,0.95,10E-6).estimateAtmosphericLight(mRgba);
//
//    airLight[0] = a[0];
//    airLight[1] = a[1];
//    airLight[2] = a[2];
//
//    env->ReleaseFloatArrayElements(array,airLight,0);
//
//}
//
//JNIEXPORT jfloatArray JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1estimateAtmosphericLight__J
//        (JNIEnv *env, jclass, jlong addrRgba){
//
//    Mat &mRgba = *(Mat *) addrRgba;
//    Vec3f a = DeHaze(7,0.1,0.95,10E-6).estimateAtmosphericLight(mRgba);
//
//    jfloatArray atmosphericLight = env->NewFloatArray(3);
//    jfloat arr[3];
//    arr[0] = a[0];
//    arr[1] = a[1];
//    arr[2] = a[2];
//    env->SetFloatArrayRegion(atmosphericLight,0,3,arr);
//    env->ReleaseFloatArrayElements(atmosphericLight,arr,0);
//    return atmosphericLight;
//}
//
//JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1estimateTramsmission
//        (JNIEnv *env, jclass clazz, jlong origRgbaAddr, jlong transmissionAddr, jfloatArray light, jint r, jdouble eps){
////    Mat &mRgba = *(Mat *) origRgbaAddr;
////    DeHaze(7,0.1,0.95,10E-6)
////    Vec3f a = DeHaze(7,0.1,0.95,10E-6).estimateAtmosphericLight(mRgba);
////
////    jfloatArray atmosphericLight = env->NewFloatArray(3);
////    jfloat arr[3];
////    arr[0] = a[0];
////    arr[1] = a[1];
////    arr[2] = a[2];
////    env->SetFloatArrayRegion(atmosphericLight,0,3,arr);
////    env->ReleaseFloatArrayElements(atmosphericLight,arr,0);
////    return atmosphericLight;
//}


/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_createHazeRemoveModel
 * Signature: (IDDD)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1createHazeRemoveModel
        (JNIEnv *env, jclass clazz, jint r, jdouble t0, jdouble omega, jdouble eps){
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
        (JNIEnv *env, jclass clazz){
    if (deHaze != NULL){
        delete deHaze;
    }
}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_estimateAtmosphericLight
 * Signature: (J)[F
 */
JNIEXPORT jfloatArray JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1estimateAtmosphericLight
        (JNIEnv *env, jclass, jlong origRgbaAddr){
//    Mat &mRgba = *(Mat *) origRgbaAddr;
//    Vec3f a = deHaze->estimateAtmosphericLight(mRgba);
//
//    jfloatArray atmosphericLight = env->NewFloatArray(3);
//    jfloat *arr = env->GetFloatArrayElements(atmosphericLight,JNI_FALSE);
//    arr[0] = a[0];
//    arr[1] = a[1];
//    arr[2] = a[2];
////    env->SetFloatArrayRegion(atmosphericLight,0,3,arr);
//    env->ReleaseFloatArrayElements(atmosphericLight,arr,JNI_COMMIT);
//    return atmosphericLight;
}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_estimateTramsmission
 * Signature: (J[FJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1estimateTramsmission
        (JNIEnv *env, jclass, jlong origRgbaAddr, jfloatArray aLight, jlong transmissionAddr){

//    Mat &mRgba = *(Mat *) origRgbaAddr;
//    Mat &transmission = *(Mat *) transmissionAddr;
//    jfloat *arr = env->GetFloatArrayElements(aLight,JNI_FALSE);
//    Vec3f atmosphericLight;
//    atmosphericLight[0] = arr[0];
//    atmosphericLight[1] = arr[1];
//    atmosphericLight[2] = arr[2];
//
//    transmission = deHaze->estimateTransmission(mRgba,atmosphericLight);

}

/*
 * Class:     cn_scut_dongxia_hazeremove_dehaze_DeHaze
 * Method:    n_recover
 * Signature: (JJ[FJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_dehaze_DeHaze_n_1recover
        (JNIEnv *env, jclass clazz, jlong origRgbaAddr, jlong transmissionAddr,
         jfloatArray aLight, jlong recoverAddr){

//    Mat &mRgba = *(Mat *) origRgbaAddr;
//    Mat &transmission = *(Mat *) transmissionAddr;
//    Mat &recover = *(Mat *) recoverAddr;
//    jfloat *arr = env->GetFloatArrayElements(aLight,JNI_FALSE);
//    Vec3f atmosphericLight;
//    atmosphericLight[0] = arr[0];
//    atmosphericLight[1] = arr[1];
//    atmosphericLight[2] = arr[2];
//
//    recover = deHaze->recover(mRgba,transmission,atmosphericLight);
}

/*
 * Class:     cn_scut_dongxia_hazeremove_CameraFragment
 * Method:    nativeProcessFrame
 * Signature: ([BIIJ)V
 */
JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame___3BIIJ
        (JNIEnv *env, jclass clazz, jbyteArray frame, jint width, jint height, jlong recoverAddr){

    jbyte  *frameYuv = env->GetByteArrayElements(frame, JNI_FALSE);

//    Mat yChannel(height,width,CV_8UC1,frameYuv);
//
//    Mat yuvChannel(height + (height/2),width,CV_8UC1,frameYuv);
//
//    Mat rgba;
//    cvtColor(yuvChannel,rgba,COLOR_YUV2RGBA_NV21,4);

    Mat &recover = *(Mat *) recoverAddr;

    recover = deHaze->videoHazeRemove(frameYuv,0,width,height);
//    recover = deHaze->videoHazeRemove(rgba);
}