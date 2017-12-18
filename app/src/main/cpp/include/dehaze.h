#ifndef _DEHAZE_DEHAZE_H
#define _DEHAZE_DEHAZE_H

#include <opencv2/opencv.hpp>
#include <queue>

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

    void setFPS(int fps);

    cv::Vec3f estimateAtmosphericLight(const cv::Mat& I);
    cv::Mat estimateTransmission(const cv::Mat& I, cv::Vec3f atmosphericLight);
    cv::Mat recover(const cv::Mat& I, const cv::Mat& transmission,
                    cv::Vec3f atmosphericLight);
private:
//    cv::Vec3f estimateAtmosphericLight();
//    cv::Mat estimateTransmission();
//    cv::Vec3f estimateAtmosphericLightVideo();
//    cv::Mat estimateTransmissionVideo();
//    cv::Mat recover();


    cv::Vec3f estimateAtmosphericLightVideo(const cv::Mat& I);
    cv::Mat estimateTransmissionVideo(const cv::Mat& I, cv::Vec3f atmosphericLight);

private:
    //common
    int r;
    double t0;
    double omega;
    double eps;


    unsigned char look_up_table[256];

    cv::Mat lookUpTable;

//    cv::Mat I;
//    cv::Mat I_YUV;
//    cv::Vec3f atmosphericLight = cv::Vec3f(0,0,0);
//    cv::Mat rough_transmission;
//    cv::Mat transmission;

    //video
    cv::Vec3f atmosphericLightSum;
    std::queue<cv::Vec3f> atmosphericLightQueue;
//    cv::Mat preI;
//    cv::Mat pre_transmission;
//    cv::Mat pre_rough_transmission;

    int fps;

};

#endif //DEHAZE_DEHAZE_H
