package cn.scut.dongxia.hazeremove.dehaze;

import org.opencv.core.Mat;

public class DeHaze {

    private int r;
    private double omega;
    private double t0;
    private double eps;

    public DeHaze(int r, double t0, double omega, double eps){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;
    }

    public void release(){

    }

    public Mat imageHazeRemove(Mat I){
        return null;
    }

    public Mat videoHazeRemove(Mat I){
        return null;
    }

    private float[] estimateAtmosphericLight(Mat I){
        return null;
    }
    private Mat estimateTransmission(Mat I, float[] atmosphericLight){
        return null;
    }
    private float[] estimateAtmosphericLightVideo(Mat I){
        return null;
    }
    private Mat estimateTransmissionVideo(Mat I, float[] atmosphericLight){
        return null;
    }
    private Mat recover(Mat I, Mat transmission, float[] atmosphericLight){
        return null;
    }


    public interface DeHazeModeChangeListener{
        void onEnterDeHazeMode();

        void onExitDeHazeMode();
    }

}
