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
}
