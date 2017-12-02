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
}

JNIEXPORT void JNICALL Java_cn_scut_dongxia_hazeremove_CameraFragment_nativeProcessFrame
        (JNIEnv *env, jobject, jlong addrRgba){
    Mat &mRgb = *(Mat *) addrRgba;

    DeHaze deHaze(7,0.1,0.95,10E-6);
    mRgb = deHaze.imageHazeRemove(mRgb);
    LOGD("ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
}