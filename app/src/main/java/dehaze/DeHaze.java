package dehaze;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-23.
 */
public class DeHaze {
    private static final String TAG = "DeHaze";

    private Mat I;

    private int r;
    private double t0; //最终版本应该去掉
    private double omega; //最终版本应该去掉
    private double eps;

    private Mat transmission;
    private Scalar atmosphericLight;

    private Mat recover;

    public DeHaze(int r, double t0, double omega, double eps){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;
    }

    public Mat imageHazeRemove(Mat I){
        assert (I.channels() == 3);

        if (I.depth() != CvType.CV_32F){
            this.I = new Mat(I.size(),CvType.CV_32FC3);
            I.convertTo(this.I,CvType.CV_32F,1.0/255.0);
        }else {
            this.I = I.clone();
        }

        estimateAtmosphericLight();
        estimateTransmission();

        return recover();
    }

    private Scalar estimateAtmosphericLight(){

        long start = System.currentTimeMillis();

        Mat minChannel = DarkChannel.calcMinChannel(I);

        double maxValue = 0;

        Mat aimRoi = new Mat();

        for (int i = 0; i < minChannel.rows(); i+=r){
            for (int j = 0; j < minChannel.cols(); j+=r) {
                int w = (j+r < minChannel.cols()) ? r : minChannel.cols()-j;
                int h = (i+r < minChannel.rows()) ? r : minChannel.rows()-i;
                Mat roi = new Mat(minChannel,new Rect(j,i,w,h));
                MatOfDouble mean = new MatOfDouble();
                MatOfDouble std = new MatOfDouble();
                MatOfDouble score = new MatOfDouble();

                Core.meanStdDev(roi,mean,std);

                Core.subtract(mean,std,score);

                if (score.get(0,0)[0] > maxValue){
                    maxValue = score.get(0,0)[0];
                    aimRoi = new Mat(I,new Rect(j,i,w,h));
                }
            }
        }

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(aimRoi,mean,std);

        atmosphericLight = new Scalar(mean.toArray());

        Log.d(TAG, atmosphericLight.toString());

        long stop = System.currentTimeMillis();

        Log.d(TAG, "估计大气光耗时: " + (stop - start) + " ms");

        return atmosphericLight;
    }

    Mat estimateTransmission(){
        assert(I.channels() == 3);

        long start = System.currentTimeMillis();

        Mat normalized = I.clone();

        Core.divide(normalized, atmosphericLight, normalized);

        Mat darkChannel = DarkChannel.calcDarkChannel(normalized,r);

        //transmission = 1.0 - omega * darkChannel
        transmission = Mat.ones(darkChannel.size(),CvType.CV_32FC1);
        Core.subtract(transmission,darkChannel.mul(
                new Mat(darkChannel.size(),CvType.CV_32FC1,new Scalar(omega))),transmission);

//        transmission = min(max(k/abs(1-darkChannel),1).mul(transmission),1);
        double k = 0.3;
        Mat temp = new Mat();
        Core.absdiff(darkChannel,Mat.ones(darkChannel.size(),darkChannel.type()),temp);
        Core.divide(k,temp,temp);
        Core.max(temp, new Scalar(1), temp);
        temp = temp.mul(transmission);
        Core.min(temp,new Scalar(1),transmission);

//        GuidedFilter guidedFilter = new GuidedFilterColor(I,8*r,eps);
//        transmission = guidedFilter.filter(transmission);

        Mat gray = new Mat(I.size(),CvType.CV_32FC1);
        Imgproc.cvtColor(I,gray,Imgproc.COLOR_RGB2GRAY);
        FastGuidedFilter filter = new FastGuidedFilterMono(gray,8*r,4, eps);
        transmission = filter.filter(transmission);

        long stop = System.currentTimeMillis();

        Log.d(TAG, "估计透射率耗时: " + (stop - start) + " ms");

        return transmission;
    }

    private Mat recover(){
        long start = System.currentTimeMillis();

        assert (I.type() == CvType.CV_32FC3);

        Vector<Mat> channels = new Vector<>();

        Core.split(I,channels);

        for (Mat channel : channels){
            Core.subtract(channel, atmosphericLight,channel);
            Core.divide(channel,transmission,channel);
            Core.add(channel, atmosphericLight, channel);
        }

        recover = new Mat();
        Core.merge(channels,recover);

        recover.convertTo(recover,CvType.CV_8UC3,255);

        long stop = System.currentTimeMillis();

        Log.d(TAG, "恢复图像耗时: " + (stop - start) + " ms");

        return recover;
    }
}
