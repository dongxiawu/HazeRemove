package dehaze;

import android.support.annotation.NonNull;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by dongxia on 17-11-28.
 */

public class VideoHazeRemove extends ImageHazeRemove {
    private static final String TAG = "VideoHazeRemove";

    private int fps;

    private int curFrame = 0;

    public VideoHazeRemove(int r, double t0, double omega, double eps, int fps){
        super(r, t0, omega, eps);

        this.fps = fps;
    }

    @Override
    Scalar estimateAtmosphericLight() {
        if (curFrame%(fps*2) != 1){
            return atmosphericLight;
        }
        return super.estimateAtmosphericLight();
    }

    @Override
    Mat estimateTransmission() {
        if (curFrame%(fps*2) != 1){
            return transmission;
        }

        return super.estimateTransmission();
    }

    @Override
    public Mat process(@NonNull Mat origI) {
        curFrame++;
        return super.process(origI);
    }
}
