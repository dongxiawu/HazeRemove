package dehaze;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by dongxia on 17-11-28.
 */
public class ImageHazeRemove extends HazeRemove {
    private static final String TAG = "ImageHazeRemove";

    public ImageHazeRemove(int r, double t0, double omega, double eps){
        super(r, t0, omega, eps);
    }

    @Override
    Scalar estimateAtmosphericLight() {
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

        return atmosphericLight;
    }

    @Override
    Mat estimateTransmission() {
        Mat transmission;

        assert(I.channels() == 3);

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

        return transmission;
    }
}
