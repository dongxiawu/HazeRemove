package dehaze;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-24.
 */

public class GuidedFilterColor extends GuidedFilter{

    private Vector<Mat> Ichannels;

    private Mat mean_I_r, mean_I_g, mean_I_b;
    private Mat invrr, invrg, invrb, invgg, invgb, invbb;

    public GuidedFilterColor(Mat origI, int r, double eps){
        super(origI, r, eps);

        Ichannels = new Vector<>();
        Core.split(I, Ichannels);

        mean_I_r = boxfilter(Ichannels.get(0),r);
        mean_I_g = boxfilter(Ichannels.get(1),r);
        mean_I_b = boxfilter(Ichannels.get(2),r);

        // variance of I in each local patch: the matrix Sigma in Eqn (14).
        // Note the variance in each local patch is a 3x3 symmetric matrix:
        //           rr, rg, rb
        //   Sigma = rg, gg, gb
        //           rb, gb, bb
        Mat var_I_rr = boxfilter(Ichannels.get(0).mul(Ichannels.get(0)), r);
        Core.subtract(var_I_rr, mean_I_r.mul(mean_I_r),var_I_rr);
        Core.add(var_I_rr,new Scalar(eps),var_I_rr);

        Mat var_I_rg = boxfilter(Ichannels.get(0).mul(Ichannels.get(1)), r);
        Core.subtract(var_I_rg, mean_I_r.mul(mean_I_g),var_I_rg);

        Mat var_I_rb = boxfilter(Ichannels.get(0).mul(Ichannels.get(2)), r);
        Core.subtract(var_I_rb, mean_I_r.mul(mean_I_b),var_I_rb);

        Mat var_I_gg = boxfilter(Ichannels.get(1).mul(Ichannels.get(1)), r);
        Core.subtract(var_I_gg, mean_I_g.mul(mean_I_g),var_I_gg);
        Core.add(var_I_gg,new Scalar(eps),var_I_gg);

        Mat var_I_gb = boxfilter(Ichannels.get(1).mul(Ichannels.get(2)), r);
        Core.subtract(var_I_gb, mean_I_b.mul(mean_I_b),var_I_gb);

        Mat var_I_bb = boxfilter(Ichannels.get(2).mul(Ichannels.get(2)), r);
        Core.subtract(var_I_bb, mean_I_b.mul(mean_I_b),var_I_bb);
        Core.add(var_I_bb,new Scalar(eps),var_I_bb);

        // Inverse of Sigma + eps * I
        invrr = new Mat();
        Core.subtract(var_I_gg.mul(var_I_bb), var_I_gb.mul(var_I_gb),invrr);
        invrg = new Mat();
        Core.subtract(var_I_gb.mul(var_I_rb), var_I_rg.mul(var_I_bb),invrg);
        invrb = new Mat();
        Core.subtract(var_I_rg.mul(var_I_gb), var_I_gg.mul(var_I_rb),invrb);
        invgg = new Mat();
        Core.subtract(var_I_rr.mul(var_I_bb), var_I_rb.mul(var_I_rb),invgg);
        invgb = new Mat();
        Core.subtract(var_I_rb.mul(var_I_rg), var_I_rr.mul(var_I_gb),invgb);
        invbb = new Mat();
        Core.subtract(var_I_rr.mul(var_I_gg), var_I_rg.mul(var_I_rg),invbb);

        Mat covDet = new Mat();
        Core.add(invrr.mul(var_I_rr), invrg.mul(var_I_rg), covDet);
        Core.add(covDet, invrb.mul(var_I_rb), covDet);

        Core.divide(invrr,covDet,invrr);
        Core.divide(invrg,covDet,invrg);
        Core.divide(invrb,covDet,invrb);
        Core.divide(invgg,covDet,invgg);
        Core.divide(invgb,covDet,invgb);
        Core.divide(invbb,covDet,invbb);
    }

    @Override
    protected Mat filterSingleChannel(Mat p) {
        Mat mean_p = boxfilter(p, r);

        Mat mean_Ip_r = boxfilter(Ichannels.get(0).mul(p), r);
        Mat mean_Ip_g = boxfilter(Ichannels.get(1).mul(p), r);
        Mat mean_Ip_b = boxfilter(Ichannels.get(2).mul(p), r);

        // covariance of (I, p) in each local patch.
        Mat cov_Ip_r = new Mat();
        Core.subtract(mean_Ip_r, mean_I_r.mul(mean_p), cov_Ip_r);
        Mat cov_Ip_g = new Mat();
        Core.subtract(mean_Ip_g, mean_I_g.mul(mean_p),cov_Ip_g);
        Mat cov_Ip_b = new Mat();
        Core.subtract(mean_Ip_b, mean_I_b.mul(mean_p),cov_Ip_b);

        Mat a_r = new Mat();
        Core.add(invrr.mul(cov_Ip_r), invrg.mul(cov_Ip_g), a_r);
        Core.add(a_r,invrb.mul(cov_Ip_b),a_r);
        Mat a_g = new Mat();
        Core.add(invrg.mul(cov_Ip_r), invgg.mul(cov_Ip_g), a_g);
        Core.add(a_g,invrb.mul(cov_Ip_b),a_g);
        Mat a_b = new Mat();
        Core.add(invrb.mul(cov_Ip_r), invgb.mul(cov_Ip_g), a_b);
        Core.add(a_b,invbb.mul(cov_Ip_b),a_b);

        Mat b = new Mat();// Eqn. (15) in the paper;
        Core.subtract(mean_p, a_r.mul(mean_I_r),b);
        Core.subtract(b, a_b.mul(mean_I_b),b);

        // Eqn. (16) in the paper;
        Mat result = new Mat();
        Core.add(boxfilter(a_r, r).mul(Ichannels.get(0)),
                boxfilter(a_g, r).mul(Ichannels.get(1)), result);
        Core.add(result, boxfilter(a_b, r).mul(Ichannels.get(2)), result);
        Core.add(result, boxfilter(b, r), result);

        return result;
    }
}
