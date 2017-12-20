package cn.scut.dongxia.hazeremove.camera;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import cn.scut.dongxia.hazeremove.BuildConfig;
import cn.scut.dongxia.hazeremove.R;

public abstract class AbsCameraBridgeView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AbsCameraBridgeView";
    private static final int MAX_UNSPECIFIED = -1;

    @IntDef({STOPPED, STARTED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraState {}
    private static final int STOPPED = 0;
    private static final int STARTED = 1;

    private @CameraState int mState = STOPPED;

    private Bitmap mCacheBitmap;
    private CameraViewListener mListener;
    private boolean mSurfaceExist;
    private final Object mSyncObject = new Object();

    protected int mFrameWidth;
    protected int mFrameHeight;

    protected int mMaxHeight;
    protected int mMaxWidth;

    protected float mScale = 0;

    protected @CameraId int mCameraId = CAMERA_ID_ANY;

    protected boolean mEnabled;

    protected FpsMeter mFpsMeter = null;

    @IntDef({CAMERA_ID_ANY,CAMERA_ID_BACK,CAMERA_ID_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraId {}
    public static final int CAMERA_ID_ANY   = -1;
    public static final int CAMERA_ID_BACK  = 99;
    public static final int CAMERA_ID_FRONT = 98;

    public AbsCameraBridgeView(Context context) {
        this(context, null);
    }

    public AbsCameraBridgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsCameraBridgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsCameraBridgeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        int count = attrs.getAttributeCount();
        Log.d(TAG, "Attr count: " + count);

        TypedArray styledAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.AbsCameraBridgeView);
        mCameraId = styledAttrs.getInt(R.styleable.CameraBridgeViewBase_camera_id, CAMERA_ID_ANY);
        styledAttrs.recycle();

        mMaxWidth = MAX_UNSPECIFIED;
        mMaxHeight = MAX_UNSPECIFIED;
        getHolder().addCallback(this);
    }

    /**
     * Sets the camera id
     * @param cameraId new camera id
     */
    public void setCameraId(@CameraId int cameraId) {
        this.mCameraId = cameraId;
    }

    public interface CameraViewListener {

        public void onCameraViewStarted(int width, int height);

        public void onCameraViewStopped();

        //不能有耗时操作，否则会导致丢帧和处理中的帧被替代
        public Mat onCameraFrame(CameraViewFrame inputFrame);
    };

    public interface CameraViewFrame {

        /**
         * This method returns RGBA Mat with frame
         */
        public Mat rgba();

        /**
         * This method returns single channel gray scale Mat with frame
         */
        public Mat gray();

        public void putOrigData(byte[] frameData);

        public byte[] getOrigData();

        public int getWidth();

        public int getHeight();

        public int getOrigDataFormat();
    };


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "call surfaceChanged event");
        synchronized(mSyncObject) {
            if (!mSurfaceExist) {
                mSurfaceExist = true;
                checkCurrentState();
            } else {
                /** Surface changed. We need to stop camera and restart with new parameters */
                /* Pretend that old surface has been destroyed */
                mSurfaceExist = false;
                checkCurrentState();
                /* Now use new surface. Say we have it now */
                mSurfaceExist = true;
                checkCurrentState();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        /* Do nothing. Wait until surfaceChanged delivered */
//        synchronized(mSyncObject) {
//            mSurfaceExist = true;
//            checkCurrentState();
//        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized(mSyncObject) {
            mSurfaceExist = false;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can enable the camera connection.
     * The actual onCameraViewStarted callback will be delivered only after both this method is called and surface is available
     */
    public void enableView() {
        synchronized(mSyncObject) {
            mEnabled = true;
            checkCurrentState();
        }
    }

    /**
     * This method is provided for clients, so they can disable camera connection and stop
     * the delivery of frames even though the surface view itself is not destroyed and still stays on the scren
     */
    public void disableView() {
        synchronized(mSyncObject) {
            mEnabled = false;
            checkCurrentState();
        }
    }

    /**
     * This method enables label with fps value on the screen
     */
    public void enableFpsMeter() {
        if (mFpsMeter == null) {
            mFpsMeter = new FpsMeter();
            mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
        }
    }

    public void disableFpsMessage() {
        mFpsMeter = null;
    }

    public void setCameraViewListener(CameraViewListener listener) {
        mListener = listener;
    }

    /**
     * This method sets the maximum size that camera frame is allowed to be. When selecting
     * size - the biggest size which less or equal the size set will be selected.
     * As an example - we set setMaxFrameSize(200,200) and we have 176x152 and 320x240 sizes. The
     * preview frame will be selected with 176x152 size.
     * This method is useful when need to restrict the size of preview frame for some reason (for example for video recording)
     * @param maxWidth - the maximum width allowed for camera frame.
     * @param maxHeight - the maximum height allowed for camera frame
     */
    public void setMaxFrameSize(int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }

    public Size getFrameSize(){
        return new Size(mFrameWidth,mFrameHeight);
    }

    /**
     * Called when mSyncObject lock is held
     */
    private void checkCurrentState() {
        Log.d(TAG, "call checkCurrentState");
        int targetState;

        if (mEnabled && mSurfaceExist && getVisibility() == VISIBLE) {
            targetState = STARTED;
        } else {
            targetState = STOPPED;
        }

        if (targetState != mState) {
            /* The state change detected. Need to exit the current state and enter target state */
            processExitState(mState);
            mState = targetState;
            processEnterState(mState);
        }
    }

    private void processEnterState(int state) {
        Log.d(TAG, "call processEnterState: " + state);
        switch(state) {
            case STARTED:
                onEnterStartedState();
                if (mListener != null) {
                    mListener.onCameraViewStarted(mFrameWidth, mFrameHeight);
                }
                break;
            case STOPPED:
                onEnterStoppedState();
                if (mListener != null) {
                    mListener.onCameraViewStopped();
                }
                break;
        };
    }

    private void processExitState(int state) {
        Log.d(TAG, "call processExitState: " + state);
        switch(state) {
            case STARTED:
                onExitStartedState();
                break;
            case STOPPED:
                onExitStoppedState();
                break;
        };
    }

    private void onEnterStoppedState() {
        /* nothing to do */
    }

    private void onExitStoppedState() {
        /* nothing to do */
    }

    // NOTE: The order of bitmap constructor and camera connection is important for android 4.1.x
    // Bitmap must be constructed before surface
    private void onEnterStartedState() {
        Log.d(TAG, "call onEnterStartedState");
        /* Connect camera */
        if (!connectCamera(getWidth(), getHeight())) {
            AlertDialog ad = new AlertDialog.Builder(getContext()).create();
            ad.setCancelable(false); // This blocks the 'BACK' button
            ad.setMessage("It seems that you device does not support camera (or it is locked). Application will be closed.");
            ad.setButton(DialogInterface.BUTTON_NEUTRAL,  "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ((Activity) getContext()).finish();
                }
            });
            ad.show();

        }
    }

    private void onExitStartedState() {
        disconnectCamera();
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
        }
    }

    /**
     * This method shall be called by the subclasses when they have valid
     * object and want it to be delivered to external client (via callback) and
     * then displayed on the screen.
     * @param frame - the current frame to be delivered
     */
    protected void deliverAndDrawFrame(CameraViewFrame frame) {
        Mat modified;

        if (mListener != null) {
            modified = mListener.onCameraFrame(frame);
        } else {
            modified = frame.rgba();
        }

        boolean bmpValid = true;
        if (modified != null) {
            try {
                Utils.matToBitmap(modified, mCacheBitmap);
            } catch(Exception e) {
                Log.e(TAG, "Mat type: " + modified);
                Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "mStretch value: " + mScale);

                if (mScale != 0) {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2),
                                    (int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2 + mScale*mCacheBitmap.getWidth()),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2 + mScale*mCacheBitmap.getHeight())), null);
                } else {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((canvas.getWidth() - mCacheBitmap.getWidth()) / 2,
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2,
                                    (canvas.getWidth() - mCacheBitmap.getWidth()) / 2 + mCacheBitmap.getWidth(),
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2 + mCacheBitmap.getHeight()), null);
                }

                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 30);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * This method is invoked shall perform concrete operation to initialize the camera.
     * CONTRACT: as a result of this method variables mFrameWidth and mFrameHeight MUST be
     * initialized with the size of the Camera frames that will be delivered to external processor.
     * @param width - the width of this SurfaceView
     * @param height - the height of this SurfaceView
     */
    protected abstract boolean connectCamera(int width, int height);

    /**
     * Disconnects and release the particular camera object being connected to this surface view.
     * Called when syncObject lock is held
     */
    protected abstract void disconnectCamera();

    // NOTE: On Android 4.1.x the function must be called before SurfaceTexture constructor!
    protected void AllocateCache()
    {
        mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
    }

    /**
     * This helper method can be called by subclasses to select camera preview size.
     * It goes over the list of the supported preview sizes and selects the maximum one which
     * fits both values set via setMaxFrameSize() and surface frame allocated for this view
     * @param supportedSizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return optimal frame size
     */
    protected Size calculateCameraFrameSize(List<Camera.Size> supportedSizes, int surfaceWidth, int surfaceHeight) {
        int calcWidth = 0;
        int calcHeight = 0;

        int maxAllowedWidth = (mMaxWidth != MAX_UNSPECIFIED && mMaxWidth < surfaceWidth)? mMaxWidth : surfaceWidth;
        int maxAllowedHeight = (mMaxHeight != MAX_UNSPECIFIED && mMaxHeight < surfaceHeight)? mMaxHeight : surfaceHeight;

        for (Camera.Size size : supportedSizes) {
            int width = size.width;
            int height = size.height;

            if (width <= maxAllowedWidth && height <= maxAllowedHeight) {
                if (width >= calcWidth && height >= calcHeight) {
                    calcWidth = (int) width;
                    calcHeight = (int) height;
                }
            }
        }

        return new Size(calcWidth, calcHeight);
    }

}
