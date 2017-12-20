//
// Created by dongxia on 17-11-8.
//
#include <guidedfilter.h>
#include "dehaze.h"
#include "fastguidedfilter.h"

using namespace cv;
using namespace std;

static cv::Mat calcMinChannel(const vector<Mat>& channels);

static cv::Mat calcDarkChannel(const vector<Mat>& channels, int r);

static cv::Mat calcDarkChannel(const Mat& minChannel, int r);


void DeHaze::setFPS(int fps){
    this->fps = fps;
}

DeHaze::DeHaze(int r, double t0, double omega, double eps) : r(r), t0(t0), omega(omega), eps(eps)
{
    initGammaLookUpTable(0.7);

}

cv::Mat DeHaze::imageHazeRemove(const cv::Mat& origI)
{
//    CV_Assert(I.channels() == 3);
    Mat I;
    if (origI.depth() != CV_32F){
        origI.convertTo(I, CV_32F);
    }

    double start = clock();

    estimateAtmosphericLight();
    double stop = clock();
    LOGD("估计大气光耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    estimateTransmission();
    stop = clock();
    LOGD("计算透射率耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    Mat recoverImg = recover();
    stop = clock();
    LOGD("恢复图像耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);
    return recoverImg;
}

cv::Mat DeHaze::videoHazeRemove(const cv::Mat& origI){

    if (origI.depth() != CV_32F){
        origI.convertTo(origRgba, CV_32F);
    } else{
        origRgba = origI.clone();
    }
    split(origRgba,origRgbaChannels);

//    vector<Mat> yuvChannel;

//    Mat yuv;
//    cvtColor(origRgba,yuv,COLOR_RGBA2YUV_IYUV);
//    split(yuv,yuvChannel);
    origY = origRgbaChannels[0];

    minChannel = calcMinChannel(origRgbaChannels);

    darkChannel = calcDarkChannel(minChannel,r);

    estimateAtmosphericLightVideo();
    estimateTransmissionVideo();
    return recover();
}

cv::Mat DeHaze::videoHazeRemove(jbyte* data, int format, int width, int height){
    preProcessOrigFrame(data, format, width, height);

    estimateAtmosphericLightVideo();
    estimateTransmissionVideo();
    return recover();
}

//3-5ms
cv::Vec3f DeHaze::estimateAtmosphericLight(){

    double start = clock();

    double maxValue = 0;
    Mat aimRoi;

//    #pragma omp parallel for
    for (int i = 0; i < minChannel.rows; i+=r) {
        for (int j = 0; j < minChannel.cols; j+=r) {
            int w = (j+r < minChannel.cols) ? r : minChannel.cols-j;
            int h = (i+r < minChannel.rows) ? r : minChannel.rows-i;
            Mat roi(minChannel,Rect(j,i,w,h));
            cv::Scalar mean, std, score;
            meanStdDev(roi,mean,std);
            score = mean -std;
            if (score.val[0] > maxValue){
                maxValue = score.val[0];
                aimRoi = Mat(origRgba,Rect(j,i,w,h));
            }
        }
    }

    cv::Scalar mean,std;
    meanStdDev(aimRoi,mean,std);

    atmosphericLight[0] = static_cast<float> (mean.val[0]);
    atmosphericLight[1] = static_cast<float> (mean.val[1]);
    atmosphericLight[2] = static_cast<float> (mean.val[2]);

    double stop = clock();
    LOGD("大气光: [ %.2f, %.2f, %.2f ]", atmosphericLight[0], atmosphericLight[1], atmosphericLight[2]);
    LOGD("估计大气光耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    return atmosphericLight;
}

cv::Mat DeHaze::estimateTransmission(){

    double start = clock();
    double stop;

    Mat yNormalized = origY/(atmosphericLight[0]*0.299 +
            atmosphericLight[1]*0.587 + atmosphericLight[2]*0.114);

    start = clock();
    Mat darkChannel = calcDarkChannel(yNormalized,r);
    transmission = 1.0 - omega * darkChannel;
    stop = clock();
    LOGD("粗略透射率计算耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    //透射率修正
    float k = 0.3;
    transmission = min(max(k/abs(1-darkChannel),1).mul(transmission),1);
    stop = clock();
    LOGD("透射率修正耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    //10ms左右
    start = clock();
    transmission = fastGuidedFilter(yNormalized, transmission, 8*r, 4, eps);
    stop = clock();
    LOGD("快速导向滤波耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    return transmission;
}

cv::Vec3f DeHaze::estimateAtmosphericLightVideo(){
    double start = clock();

    atmosphericLight = estimateAtmosphericLight();

    while (atmosphericLightQueue.size() < fps*2){
        atmosphericLightQueue.push(atmosphericLight);
        atmosphericLightSum += atmosphericLight;
    }

    atmosphericLight = atmosphericLightSum/(fps*2);

    atmosphericLightSum -= atmosphericLightQueue.front();
    atmosphericLightQueue.pop();

    double stop = clock();
    LOGD("估计大气光耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    return atmosphericLight;
}

cv::Mat DeHaze::estimateTransmissionVideo(){

    Mat transmission = estimateTransmission();

    return transmission;

}

cv::Mat DeHaze::recover(){

    double start;
    double stop;

    start = clock();
    vector<Mat> channels;
    split(origRgba,channels);
    stop = clock();
    LOGD("分离原始图像耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    Mat t = transmission;

    channels[0] = (channels[0]-atmosphericLight[0])/t + atmosphericLight[0];
    channels[1] = (channels[1]-atmosphericLight[1])/t + atmosphericLight[1];
    channels[2] = (channels[2]-atmosphericLight[2])/t + atmosphericLight[2];
    Mat recover;
    merge(channels,recover);

    stop = clock();
    LOGD("粗略恢复图像耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    recover.convertTo(recover,CV_8U);
    stop = clock();
    LOGD("转化成8位图像耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    //pow(recover,0.7,recover);//3ms gamma矫正
    LUT(recover,mGammaLookUpTable,recover);
    stop = clock();
    LOGD("gamma矫正耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    return recover;
}

void DeHaze::preProcessOrigFrame(jbyte* const data, int format, int width, int height){
    double start = clock();
    double stop;

    origY = Mat(height, width, CV_8UC1, data);
    origY.convertTo(origY,CV_32F);

    Mat yuvChannel(height + (height/2), width, CV_8UC1, data);

    cvtColor(yuvChannel, origRgba, COLOR_YUV2RGBA_NV21, 4);
    stop = clock();
    LOGD("yuv 转换为 rgba 耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    if (origRgba.depth() != CV_32F){
        origRgba.convertTo(origRgba, CV_32F);
    }
    stop = clock();
    LOGD("转换原图片深度耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    split(origRgba,origRgbaChannels);
    stop = clock();
    LOGD("分离原始图片通道耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);
    //origY = origRgbaChannels[0];

    start = clock();
    minChannel = calcMinChannel(origRgbaChannels);
    stop = clock();
    LOGD("计算最小值通道道耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);

    start = clock();
    darkChannel = calcDarkChannel(minChannel, r);
    stop = clock();
    LOGD("计算暗通道道耗时：%.2f ms", (stop-start)/CLOCKS_PER_SEC*1000);
}

void DeHaze::initGammaLookUpTable(double gamma){
    mGammaLookUpTable = Mat(1, 256, CV_8U);
    uchar* p = mGammaLookUpTable.ptr();
    for( int i = 0; i < 256; ++i){
        //计算并截断
        p[i] = saturate_cast<uchar>(pow((i/255.0), gamma) * 255.0);
    }
}

//local method
//静态代表只有本文件内可以用
static cv::Mat calcMinChannel(const vector<Mat>& channels) {
    CV_Assert(channels.size() > 0);

    Mat minChannel = channels[0];
    for(size_t i = 1;i<channels.size();i++){
        minChannel = min(minChannel,channels[i]);
    }

    return minChannel;
}

static cv::Mat calcDarkChannel(const vector<Mat>& channels, int r) {
    Mat minChannel = calcMinChannel(channels);

    return calcDarkChannel(minChannel, r);
}

static cv::Mat calcDarkChannel(const Mat& minChannel, int r) {

    CV_Assert(minChannel.channels() == 1);

    Mat kernel = getStructuringElement(MORPH_RECT,Size(2*r+1,2*r+1));

    Mat darkChannel;
    erode(minChannel,darkChannel,kernel);

    return darkChannel;
}