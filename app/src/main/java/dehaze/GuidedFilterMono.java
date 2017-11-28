package dehaze;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by dongxia on 17-11-24.
 */

public class GuidedFilterMono extends GuidedFilter {

    private Mat mean_I;
    private Mat var_I;

    public GuidedFilterMono(Mat origI, int r, double eps){
        super(origI, r, eps);

        mean_I = boxfilter(I,r);
        Mat mean_II = boxfilter(this.I.mul(I),r);
        var_I = new Mat();
        Core.subtract(mean_II,mean_I.mul(mean_I),var_I);
    }

    @Override
    protected Mat filterSingleChannel(Mat p) {
        Mat mean_p = boxfilter(p, r);
        Mat mean_Ip = boxfilter(I.mul(p), r);
        Mat cov_Ip = new Mat();
        Core.subtract(mean_Ip, mean_I.mul(mean_p), cov_Ip);

        Mat a = new Mat();
        Core.add(var_I, new Scalar(eps),var_I);
        Core.divide(cov_Ip, var_I, a);

        Mat b = new Mat();
        Core.subtract(mean_p, a.mul(mean_I), b);

        Mat mean_a = boxfilter(a, r);
        Mat mean_b = boxfilter(b, r);

        Mat result = new Mat();
        Core.add(mean_a.mul(I), mean_b, result);

        return result;
    }
}
