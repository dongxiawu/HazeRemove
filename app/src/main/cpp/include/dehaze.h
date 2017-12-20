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

class DeHaze
{
public:
    DeHaze(int r, double t0, double omega, double eps);//构造函数不能是虚函数
    ~DeHaze() = default;

    cv::Mat imageHazeRemove(const cv::Mat& I);

    cv::Mat videoHazeRemove(const cv::Mat& I);

    cv::Mat videoHazeRemove(jbyte* data, int format, int width, int height);

    void setFPS(int fps);

//    cv::Vec3f estimateAtmosphericLight(const cv::Mat& I);
//    cv::Mat estimateTransmission(const cv::Mat& I, cv::Vec3f atmosphericLight);
//    cv::Mat recover(const cv::Mat& I, const cv::Mat& transmission,
//                    cv::Vec3f atmosphericLight);
private:
    cv::Vec3f estimateAtmosphericLight();

    cv::Mat estimateTransmission();

    cv::Vec3f estimateAtmosphericLightVideo();

    cv::Mat estimateTransmissionVideo();

    cv::Mat recover();

    void initGammaLookUpTable(double gamma);

    void preProcessOrigFrame(jbyte* const data, int format, int width, int height);

//    cv::Vec3f estimateAtmosphericLightVideo(const cv::Mat& I);
//
//    cv::Mat estimateTransmissionVideo(const cv::Mat& I, cv::Vec3f atmosphericLight);

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
//    cv::Mat I;
//    cv::Mat I_YUV;
    cv::Vec3f atmosphericLight = cv::Vec3f(0,0,0);
//    cv::Mat rough_transmission;
    cv::Mat transmission;

    //video
    cv::Vec3f atmosphericLightSum;
    std::queue<cv::Vec3f> atmosphericLightQueue;

    int fps;

};

#endif //DEHAZE_DEHAZE_H
