package cn.scut.dongxia.hazeremove.dehaze;

import android.support.annotation.NonNull;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Timer;
import java.util.TimerTask;


public class DeHaze {

    private int r;
    private double omega;
    private double t0;
    private double eps;

    private Mat orig;

    private Mat recover;

    private Mat transmission;

    private AtmosphericLight[] mAirLight;
    private int mAirLightIdx = 0;

    private Timer mAirLightTimer;

    public DeHaze(int r, double t0, double omega, double eps, int width, int height){
        this.r = r;
        this.t0 = t0;
        this.omega = omega;
        this.eps = eps;

        orig = new Mat(height,width,CvType.CV_32FC4);

        mAirLight = new AtmosphericLight[2];
        mAirLight[0] = new AtmosphericLight();
        mAirLight[1] = new AtmosphericLight();

        transmission = new Mat(height,width,CvType.CV_32FC1);
        recover = new Mat(height,width,CvType.CV_8UC4);

        mAirLightTimer = new Timer();
        mAirLightTimer.schedule(mAlightTask,0,2*1000);

        n_createHazeRemoveModel(r,t0,omega,eps);
    }

    private void estimateAtmosphericLight(Mat origI,@NonNull AtmosphericLight atmosphericLight){
        float[] aLight = n_estimateAtmosphericLight(origI.getNativeObjAddr());

        atmosphericLight.data = aLight;
    }

    private Mat estimateTransmission(Mat origI, AtmosphericLight atmosphericLight, Mat transmission){
        n_estimateTramsmission(origI.getNativeObjAddr(),
                atmosphericLight.data,transmission.getNativeObjAddr());
        return transmission;
    }

    private Mat recover(Mat I, Mat transmission, AtmosphericLight atmosphericLight){
        n_recover(I.getNativeObjAddr(),transmission.getNativeObjAddr(),
                atmosphericLight.data, recover.getNativeObjAddr());
        return recover;
    }

    public void release(){
        n_deleteHazeRemoveModel();

        mAirLightTimer.cancel();
    }

    public Mat videoHazeRemove(Mat origI){

        origI.convertTo(orig,CvType.CV_32F);

        estimateTransmission(orig, mAirLight[mAirLightIdx], transmission);

        recover(orig,transmission, mAirLight[mAirLightIdx]);
        return recover;
    }


    private class AtmosphericLight{
        float[] data = new float[3];
    }

    private TimerTask mAlightTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (orig) {
                estimateAtmosphericLight(orig, mAirLight[1-mAirLightIdx]);
                mAirLightIdx = 1-mAirLightIdx;
            }
        }
    };

    private static native void n_createHazeRemoveModel(int r, double t0, double omega, double eps);

    private static native void n_deleteHazeRemoveModel();

    private static native float[] n_estimateAtmosphericLight(long origRgbaAddr);

    private static native void n_estimateTramsmission(long origRgbaAddr, float[] aLight, long transmissionAddr);

    private static native void n_recover(long origRgbaAddr, long transmissionAddr, float[] aLight, long recoverAddr);
}
