package dehaze;

import android.support.annotation.NonNull;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-23.
 * 计算最小值通道和暗通道的工具类
 */

public class DarkChannel {

    /**
     * 计算最小值通道
     * @param src 待处理图像,不能为空
     *
     * @return 最小值图像
     */
    public static Mat calcMinChannel(@NonNull Mat src){

        Vector<Mat> channels = new Vector<>();

        Core.split(src,channels);

        Mat minChannel = channels.get(0);

        for (Mat channel : channels){
            Core.min(channel,minChannel,minChannel);
        }
        return minChannel;
    }

    /**
     * 计算暗通道
     * @param src 待处理图像,不能为空
     * @param r 计算半径
     *
     * @return 暗通道图像
     */
    public static Mat calcDarkChannel(@NonNull Mat src, int r){
        Mat minChannel = calcMinChannel(src);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_RECT, new Size(2*r+1,2*r+1));

        Mat darkChannel = new Mat();
        Imgproc.erode(minChannel,darkChannel,kernel);

        return darkChannel;
    }
}
