package dehaze;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by dongxia on 17-11-28.
 */

public class VideoHazeRemove extends HazeRemove {
    private static final String TAG = "VideoHazeRemove";

    private int fps;

    private int curFrame = 0;

    public VideoHazeRemove(int r, double t0, double omega, double eps, int fps){
        super(r, t0, omega, eps);

        this.fps = fps;
    }

    @Override
    protected Scalar estimateAtmosphericLight(Mat I) {
        if (curFrame % (2*fps) == 0){
            return super.estimateAtmosphericLight(I);
        }
        return atmosphericLight;
    }

    @Override
    public Mat process(Mat origI) {
        curFrame++;
        return super.process(origI);
    }
}
