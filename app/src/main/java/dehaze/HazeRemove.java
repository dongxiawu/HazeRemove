package dehaze;

import android.support.annotation.NonNull;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-28.
 *
 * 去雾抽象类
 * 具体实现类 {@link ImageHazeRemove} or {@link VideoHazeRemove}
 */
public abstract class HazeRemove {
    private static final String TAG = "HazeRemove";

    protected Mat I;

    protected int r; //filter radius
    protected double t0; //最终版本应该去掉
    protected double omega; //最终版本应该去掉
    protected double eps; //regularization parameter

    protected Mat transmission;
    protected Scalar atmosphericLight;
    private Mat recover;

    public HazeRemove(int r, double t0, double omega, double eps){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;
    }

    /**
     * 估计大气光
     * 必须在 {# estimateTransmission} 之前调用
     *
     * @return 大气光强度
     */
    abstract Scalar estimateAtmosphericLight();


    /**
     * 估计透射率
     * 必须在 {# estimateAtmosphericLight} 之后调用
     *
     * @return 透射率图像
     */
    abstract Mat estimateTransmission();

    /**
     * 恢复图像
     * 必须在 {# estimateAtmosphericLight} 和 {# estimateTransmission} 之后调用
     *
     * @return 透射率图像
     */
    protected Mat recover(){

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

        return recover;
    }


    /**
     * 处理带雾图片
     * @param origI 待处理图像,不能为空
     *
     * @return 处理后图像
     */
    public Mat process(@NonNull Mat origI){
        long start = System.currentTimeMillis();

        int type = origI.type();

        /* 将带雾图片转换成 32位 */
        if (origI.depth() == CvType.CV_32F){
            this.I = origI.clone();
        }else {
            this.I = new Mat(origI.size(),CvType.CV_32FC3);
            origI.convertTo(I,CvType.CV_32F);
        }

        atmosphericLight = estimateAtmosphericLight();
        transmission = estimateTransmission();
        recover = recover();

        if (type != recover.type()){
            recover.convertTo(recover,type);
        }

        long stop = System.currentTimeMillis();

        Log.d(TAG, "处理图像耗时: " + (stop - start) + " ms");

        return recover;
    }
}
