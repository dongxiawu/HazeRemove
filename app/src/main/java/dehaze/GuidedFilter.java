package dehaze;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

/**
 * Created by dongxia on 17-11-23.
 */
//考虑可以用什么设计模式重构，策略模式？
public abstract class GuidedFilter {

    protected Mat I;
    protected int r;
    protected double eps;

    private int Idepth;

    public GuidedFilter(Mat origI, int r, double eps){

        assert (origI.channels() == 1 || origI.channels() ==3);

        if (origI.depth() == CvType.CV_32F){
            this.I = origI.clone();
        }else {
            this.I = converTo(origI, CvType.CV_32F);
        }

        this.r = r;
        this.eps = eps;

        Idepth = origI.depth();
    }

    public Mat filter(Mat p){
        return filter(p,-1);
    }


    public Mat filter(Mat p, int depth){
        Mat p2 = converTo(p,Idepth);

        Mat result;
        if (p.channels() == 1){
            result = filterSingleChannel(p2);
        } else {
            result = new Mat();
            Vector<Mat> pc = new Vector<>();
            Core.split(p2,pc);

            for (Mat c : pc){
                c = filterSingleChannel(c);
            }

            Core.merge(pc,result);
        }

        return converTo(result, depth == -1 ? p.depth() : depth);
    }

    Mat boxfilter(Mat I, int r){
        Mat result = new Mat(I.size(),I.type());
        Imgproc.blur(I,result,new Size(2*r+1,2*r+1));
        return result;
    }

    private Mat converTo(Mat mat, int depth){
        if (mat.depth() == depth){
            return mat.clone();
        }
        Mat result = new Mat();
        mat.convertTo(result,depth);

        return result;
    }

    protected abstract Mat filterSingleChannel(Mat p);
}
