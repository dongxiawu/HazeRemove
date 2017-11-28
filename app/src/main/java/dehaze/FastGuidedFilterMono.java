package dehaze;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by dongxia on 17-11-23.
 */

public class FastGuidedFilterMono extends FastGuidedFilter{


    private Mat mean_I;
    private Mat var_I;

    public FastGuidedFilterMono(Mat origI, int r, int scaleSize, double eps){
        super(origI, r, scaleSize, eps);

        mean_I = boxfilter(I_temp,r_temp);
        Mat mean_II = boxfilter(I_temp.mul(I_temp),r_temp);
        var_I = new Mat();
        Core.subtract(mean_II,mean_I.mul(mean_I),var_I);
    }

    @Override
    protected Mat filterSingleChannel(Mat p) {

        Mat p_temp = new Mat();

        Imgproc.resize(p,p_temp,new Size(p.cols()/scaleSize, p.rows()/scaleSize));

        Mat mean_p = boxfilter(p_temp, r_temp);
        Mat mean_Ip = boxfilter(I_temp.mul(p_temp), r_temp);
        Mat cov_Ip = new Mat();
        Core.subtract(mean_Ip, mean_I.mul(mean_p), cov_Ip);

        Mat a = new Mat();
        Core.add(var_I, new Scalar(eps),var_I);
        Core.divide(cov_Ip, var_I, a);

        Mat b = new Mat();
        Core.subtract(mean_p, a.mul(mean_I), b);

        Mat mean_a = boxfilter(a, r_temp);
        Mat mean_b = boxfilter(b, r_temp);

        Imgproc.resize(mean_a, mean_a, new Size(p.cols(), p.rows()));
        Imgproc.resize(mean_b, mean_b, new Size(p.cols(), p.rows()));

        Mat result = new Mat();
        Core.add(mean_a.mul(I), mean_b, result);

        return result;
    }

}
