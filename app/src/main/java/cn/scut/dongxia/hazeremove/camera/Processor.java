package cn.scut.dongxia.hazeremove.camera;

import org.opencv.core.Mat;

/**
 * Created by dongxia on 17-12-16.
 */

public interface Processor {
    void onFrameInput(Mat mat);

    void onProcess();

    Mat onFrameOutput();
}
