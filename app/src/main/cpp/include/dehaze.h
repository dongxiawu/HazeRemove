#ifndef _DEHAZE_DEHAZE_H
#define _DEHAZE_DEHAZE_H

#include <opencv2/opencv.hpp>
#include <queue>
#include <jni.h>
#include <android/log.h>

#define LOG_TAG "JNI_DEHAZE"

#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG, __VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG, __VA_ARGS__)

#define YUV_NV21 0x11
#define YUV_YV12 0x32315659

class DeHaze
{
public:
    DeHaze(int r, double t0, double omega, double eps);//构造函数不能是虚函数
    DeHaze(int r, double t0, double omega, double eps, int width, int height);//构造函数不能是虚函数
    ~DeHaze() = default;

    cv::Mat imageHazeRemove(const cv::Mat& I);

    cv::Mat videoHazeRemove(const cv::Mat& I);

    cv::Mat videoHazeRemove(jbyte* data, int format, int width, int height);

    cv::Mat videoHazeRemove(jbyte* data, int format);

    void setFPS(int fps);

private:
    cv::Vec3i estimateAtmosphericLight();

    cv::Mat estimateTransmission();

    cv::Vec3i estimateAtmosphericLightVideo();

    cv::Mat estimateTransmissionVideo();

    cv::Mat recover();

    void initGammaLookUpTable(double gamma);

    void preProcessOrigFrame(jbyte* const data, int format, int width, int height);

private:
    //common
    int r;
    double t0;
    double omega;
    double eps;

    cv::Mat mGammaLookUpTable;

    cv::Mat origRgba;
    cv::Mat origY;
    std::vector<cv::Mat> origRgbaChannels;
    cv::Mat minChannel;
    cv::Mat darkChannel;
    cv::Vec3i atmosphericLight;
    cv::Mat transmission;
    cv::Mat recoverMat;

    //video
    cv::Vec3i atmosphericLightSum;
    std::queue<cv::Vec3i> atmosphericLightQueue;

    int fps;
    int width;
    int height;

};

#endif //DEHAZE_DEHAZE_H
