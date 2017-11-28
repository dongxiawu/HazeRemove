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
 * Created by dongxia on 17-11-28.
 */

public abstract class HazeRemove {
    private static final String TAG = "HazeRemove";

    private Mat I;

    protected int r;
    protected double t0; //最终版本应该去掉
    protected double omega; //最终版本应该去掉
    protected double eps;

    protected Mat transmission;
    protected Scalar atmosphericLight;
    private Mat recover;

    public HazeRemove(int r, double t0, double omega, double eps){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;
    }

    protected Scalar estimateAtmosphericLight(Mat I){
        long start = System.currentTimeMillis();

        Scalar atmosphericLight;

        Mat minChannel = DarkChannel.calcMinChannel(I);

        double maxValue = 0;

        Mat aimRoi = new Mat();

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        MatOfDouble score = new MatOfDouble();

        for (int i = 0; i < minChannel.rows(); i+=r){
            for (int j = 0; j < minChannel.cols(); j+=r) {
                int w = (j+r < minChannel.cols()) ? r : minChannel.cols()-j;
                int h = (i+r < minChannel.rows()) ? r : minChannel.rows()-i;
//                roi = minChannel.adjustROI(i,i+h,j,j+w); //调整ROI很耗时
                Mat roi = new Mat(minChannel,new Rect(j,i,w,h));
                Core.meanStdDev(roi,mean,std);

                Core.subtract(mean,std,score);

                if (score.get(0,0)[0] > maxValue){
                    maxValue = score.get(0,0)[0];
                    aimRoi = new Mat(I,new Rect(j,i,w,h));
                }
            }
        }

        Core.meanStdDev(aimRoi,mean,std);

        atmosphericLight = new Scalar(mean.toArray());

        Log.d(TAG, atmosphericLight.toString());

        long stop = System.currentTimeMillis();

        Log.d(TAG, "估计大气光耗时: " + (stop - start) + " ms");

        return atmosphericLight;
    }

    protected Mat estimateTransmission(Mat I, Scalar atmosphericLight){
        Mat transmission;

        assert(I.channels() == 3);

        long start = System.currentTimeMillis();

        Mat normalized = I.clone();

        Core.divide(normalized, atmosphericLight, normalized);

        Mat darkChannel = DarkChannel.calcDarkChannel(normalized,r);

        //transmission = 1.0 - omega * darkChannel
        transmission = Mat.ones(darkChannel.size(), CvType.CV_32FC1);
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

    protected Mat recover(Scalar atmosphericLight, Mat transmission){
        long start = System.currentTimeMillis();

        atmosphericLight = estimateAtmosphericLight(I);
        transmission = estimateTransmission(I,atmosphericLight);

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

        long stop = System.currentTimeMillis();

        Log.d(TAG, "恢复图像耗时: " + (stop - start) + " ms");

        return recover;
    }


    public Mat process(Mat origI){
        long start = System.currentTimeMillis();

        int type = origI.type();

        Mat I;

        if (origI.depth() == CvType.CV_32F){
            I = origI.clone();
        }else {
            I = new Mat(origI.size(),CvType.CV_32FC3);
            origI.convertTo(I,CvType.CV_32F);
        }

        atmosphericLight = estimateAtmosphericLight(I);
        transmission = estimateTransmission(I,atmosphericLight);
        recover = recover(atmosphericLight,transmission);

        if (type != recover.type()){
            recover.convertTo(recover,type);
        }

        long stop = System.currentTimeMillis();

        Log.d(TAG, "处理图像耗时: " + (stop - start) + " ms");

        return recover;
    }
}
