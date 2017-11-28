package dehaze;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-23.
 */

public class DarkChannel {

    public static Mat calcMinChannel(Mat src){

        assert (!src.empty());

        Vector<Mat> channels = new Vector<>();

        Core.split(src,channels);

        Mat minChannel = channels.get(0);

        for (Mat channel : channels){
            Core.min(channel,minChannel,minChannel);
        }
        return minChannel;
    }

    public static Mat calcDarkChannel(Mat src, int r){
        Mat minChannel = calcMinChannel(src);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT, new Size(2*r+1,2*r+1));

        Mat darkChannel = new Mat();
        Imgproc.erode(minChannel,darkChannel,kernel);

        return darkChannel;
    }
}
