package cn.scut.dongxia.hazeremove;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;

import dehaze.HazeRemove;
import dehaze.VideoHazeRemove;

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int PREVIEW_WIDTH = 640;

    private static final int PREVIEW_HEIGHT = 480;

    //Widgets
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    private ImageButton mTakePhotoButton;

    private ImageButton mImageVideoChangeButton;

    private ImageButton mSettingsButton;

    private TextView mTestMode;

    private CameraBridgeViewBase mCameraView;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    public static CameraFragment newInstance(){
        return new CameraFragment();
    }

    private int mCameraId;

    private Camera mCamera;

    private HazeRemove hazeRemove;

    private boolean isHazeRemoveMode;

    private boolean isImageMode;

    private boolean isRecording;

    private SharedPreferences mSharedPreferences;

    private RenderScript renderScript;
    private ScriptIntrinsicYuvToRGB yuvToRGB;

    private Type.Builder yuvType,rgbaType;
    private Allocation in, out;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        renderScript = RenderScript.create(getContext());
//        yuvToRGB = ScriptIntrinsicYuvToRGB
//                .create(renderScript, Element.U8_4(renderScript));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }


    @Override
    public void onStart() {
        super.onStart();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        isHazeRemoveMode = mSharedPreferences.getBoolean("auto_haze_remove",false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
//        if (mTextureView.isAvailable()){
//            openCamera(PREVIEW_WIDTH,PREVIEW_HEIGHT);
//        }else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
//        hazeRemove = new VideoHazeRemove(15,0.1,0.95,10E-6, 20);
        mCameraView.setMaxFrameSize(640,480);
        mCameraView.enableView();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
//        closeCamera();
//        stopBackgroundThread();
//        hazeRemove = null;
        mCameraView.disableView();
        mCameraView.enableFpsMeter();
    }

    @Override
    public void onStop() {
        super.onStop();

        mSharedPreferences = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        renderScript.destroy();
    }

    private void initView(View root){
//        mTextureView = (AutoFitTextureView) root.findViewById(R.id.texture);
//        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        mTextureView.setAspectRatio(PREVIEW_WIDTH,PREVIEW_HEIGHT);

//        mTakePhotoButton = (ImageButton) root.findViewById(R.id.take_photo_button);
//        mTakePhotoButton.setOnClickListener(mOnClickListener);
//        mImageVideoChangeButton = (ImageButton) root.findViewById(R.id.image_video_change_button);
//        mImageVideoChangeButton.setOnClickListener(mOnClickListener);
//        mSettingsButton = (ImageButton) root.findViewById(R.id.settings_button);
//        mSettingsButton.setOnClickListener(mOnClickListener);
//
//        mTestMode = (TextView) root.findViewById(R.id.test_mode);
//        mTestMode.setOnClickListener(mOnClickListener);
        mCameraView = (JavaCameraView) root.findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(mCameraViewListener);
    }

    private SurfaceTexture mSurfaceTexture;
    TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");

            openCamera(PREVIEW_WIDTH,PREVIEW_HEIGHT);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
            closeCamera();
            openCamera(PREVIEW_WIDTH,PREVIEW_HEIGHT);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureUpdated: ");
        }
    };

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("CameraBackgroundThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    /**
    *  打开指定摄像头
     * @param previewWidth 预览图像的宽度
     * @param previewHeight 预览图像的高度
    * */
    private void openCamera(int previewWidth, int previewHeight){
        //判断是否有摄像头权限
        if (!checkCameraPermission()){
            requestCameraPermission();
            return;
        }

        int cameraNum = Camera.getNumberOfCameras();

        for (int cameraId = 0; cameraId < cameraNum; cameraId++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId,info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                mCameraId = cameraId;
                break;
            }
        }

        //打开摄像头
        mCamera = Camera.open(mCameraId);

        //调整摄像头预览输出方向
        adjustCameraOrientation();

        //设置摄像头预览输出大小
        setUpCameraPreviewSize(previewWidth,previewHeight);

        //设置预览回调
        mCamera.setPreviewCallback(mPreviewCallback);

        //设置预览输出
        mSurfaceTexture = new SurfaceTexture(1);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
//            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
        }catch (IOException e){
            e.printStackTrace();
        }

        //开始预览
        mCamera.startPreview();
    }

    private void closeCamera(){
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 调整摄像头方向
     */
    private void adjustCameraOrientation(){
        //获取当前屏幕方向
        int displayRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (displayRotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        //获取摄像头方向
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId,cameraInfo);
        //摄像头的方向
        int cameraOrientation = cameraInfo.orientation;

        //如果屏幕方向和摄像头方向不同，调整摄像头方向
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraOrientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraOrientation - degrees + 360) % 360;
        }
        //注意，旋转角度并不会影响获取的可预览大小数据
        mCamera.setDisplayOrientation(result);
    }

    /**
     * 调整摄像头方向
     * 当摄像头支持期待的预览大小：设置预览图像输出大小为期待的预览大小
     * 当摄像头不支持期待的预览大小但支持和期待的预览大小相同的长宽比
     * ：设置预览图像输出大小为与期待的预览大小最接近且长宽比相同的预览大小
     * 当摄像头不支持期待的预览大小且不支持和期待的预览大小相同的长宽比
     * ：
     */
    private void setUpCameraPreviewSize(int previewWidth, int previewHeight){
        //获取屏幕尺寸
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);


        //获取摄像头可选预览尺寸
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        //根据屏幕尺寸和摄像头可选预览尺寸设置预览大小
        int displayHeight = displaySize.y;
        int displayWidth = displaySize.x;
        Camera.Size previewSize = null;
        //case 1
        for (Camera.Size size : sizeList){
            if (size.width == previewWidth && size.height == previewHeight){
                previewSize = size;
                break;
            }
        }

        if (previewSize != null){
            parameters.setPreviewSize(previewSize.width,previewSize.height);
        }
        mCamera.setParameters(parameters);
    }

    /**
     * check the camera permission
     */
    private boolean checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestCameraPermission(){
        //权限获取是否应该通知用户
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(),FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(),FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }

    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getContext())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Activity activity = getActivity();
                            if (activity != null){
                                activity.finish();
                            }
                        }
                    })
                    .create();
        }
    }


    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            long start = System.currentTimeMillis();
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (yuvType == null){
                yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(data.length);
                in = Allocation.createTyped(renderScript,yuvType.create(),Allocation.USAGE_SCRIPT);

                rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                        .setX(size.width).setY(size.height);
                out = Allocation.createTyped(renderScript,rgbaType.create(),Allocation.USAGE_SCRIPT);
            }

            in.copyFrom(data);

            yuvToRGB.setInput(in);
            yuvToRGB.forEach(out);

            Bitmap bitmap = Bitmap.createBitmap(size.width,size.height, Bitmap.Config.ARGB_8888);
            out.copyTo(bitmap);

//            long start = System.currentTimeMillis();
//            YuvImage yuvImage = new YuvImage(data,ImageFormat.NV21,size.width,size.height,null);
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            yuvImage.compressToJpeg(new Rect(0,0,yuvImage.getWidth(),yuvImage.getHeight()),100,outputStream);
//            byte[] rawImage = outputStream.toByteArray();
//
//            Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage,0,rawImage.length);

            Log.d(TAG, "onPreviewFrame: " + "width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());
            long stop = System.currentTimeMillis();
            Log.d(TAG, "将数据流转换成 bitmap 耗时：" + (stop - start) + " ms");

            //去雾处理
            //TODO
            start = System.currentTimeMillis();
            Mat dest = new Mat();
            Utils.bitmapToMat(bitmap,dest);
            dest = hazeRemove.process(dest);

            bitmap = Bitmap.createBitmap(dest.width(),dest.height(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dest,bitmap);


            int height = mTextureView.getHeight();
            int width = mTextureView.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(width*1.0f/bitmap.getWidth(),height*1.0f/bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);

            stop = System.currentTimeMillis();
            Log.d(TAG, "处理图像并转换成 bitmap 并放大 耗时：" + (stop - start) + " ms");

            Canvas canvas = mTextureView.lockCanvas();
                canvas.drawBitmap(bitmap,0,0,null);
            mTextureView.unlockCanvasAndPost(canvas);

        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(TAG, "onConfigurationChanged: ");
    }


    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.take_photo_button:
                    if (isImageMode){

                    }else {
                        if (isRecording){

                        }else {

                        }
                    }
                    break;
                case R.id.image_video_change_button:
                    if (isImageMode){
                        mImageVideoChangeButton
                                .setImageResource(R.drawable.ic_camera_alt_white_36dp);
                    }else {
                        mImageVideoChangeButton
                                .setImageResource(R.drawable.ic_videocam_white_36dp);
                    }
                    isImageMode = !isImageMode;
                    break;
                case R.id.settings_button:
                    getFragmentManager().beginTransaction().addToBackStack(CameraFragment.class.getName())
                            .replace(R.id.frame_layout,SettingsFragment.newInstance()).commit();
                    break;
                case R.id.test_mode:
                    getFragmentManager().beginTransaction().addToBackStack(CameraFragment.class.getName())
                            .replace(R.id.frame_layout,ImageFragment.newInstance()).commit();
                    break;
                default:break;
            }
        }
    };


    private Mat mRgba;
    CameraBridgeViewBase.CvCameraViewListener2 mCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
            mRgba = new Mat(height, width, CvType.CV_8UC4);
            nativeCreateHazeRemoveModel();
        }

        @Override
        public void onCameraViewStopped() {
            mRgba.release();
            nativeDeleteHazeRemoveModel();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            long start = System.currentTimeMillis();
            mRgba = inputFrame.rgba();
            nativeProcessFrame(mRgba.getNativeObjAddr());
            long stop = System.currentTimeMillis();
            Log.d(TAG, "耗时： " + (stop - start) + " ms");
            return mRgba;
        }
    };

    public native void nativeProcessFrame(long matAddrRgba);

    private native void nativeCreateHazeRemoveModel();

    private native void nativeDeleteHazeRemoveModel();
}