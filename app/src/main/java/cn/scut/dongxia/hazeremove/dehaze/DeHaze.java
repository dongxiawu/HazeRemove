package cn.scut.dongxia.hazeremove.dehaze;

import org.opencv.core.CvType;
import org.opencv.core.Mat;


public class DeHaze {

    private int r;
    double t0;
    private double omega;
    private double eps;

    private int width,height;

    private Mat recover;

    public DeHaze(int r, double t0, double omega, double eps, int width, int height){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;

        this.width = width;
        this.height = height;

        recover = new Mat(height,width,CvType.CV_8UC4);

        n_createHazeRemoveModel(r, t0, omega, eps, width, height);
    }

    public void release(){
        n_deleteHazeRemoveModel();
        recover.release();
    }

    public Mat videoHazeRemove(Mat origI){

        return recover;
    }

    public Mat videoHazeRemove(byte[] frame, int format){
        n_videoHazeRemove(frame,format,recover.getNativeObjAddr());
        return recover;
    }

    private static native void n_createHazeRemoveModel(int r, double t0, double omega,
                                                       double eps, int width, int height);

    private static native void n_deleteHazeRemoveModel();

    private static native void n_videoHazeRemove(byte[] frame, int format, long recoverAddr);
}
