package cn.scut.dongxia.hazeremove;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageWriter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraFragment extends Fragment{
    private static final String TAG = "CameraFragment";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    private ImageView mImageView;

    private CameraManager mCameraManager;

    private CameraDevice mCameraDevice;

    private String[] mCameraIdList;

    private String mCameraId;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private CaptureRequest mPreviewRequest;

    private ImageReader mImageReader;

    private CameraCaptureSession mCaptureSession;

    public static CameraFragment newInstance(){
        return new CameraFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
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

    private void initView(View view){
//        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
//        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        
//        mImageView = (ImageView) view.findViewById(R.id.image_view);
    }

    private void initData(){

        mImageReader = ImageReader.newInstance(400,600,
                ImageFormat.JPEG,2);

        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);

        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraIdList = mCameraManager.getCameraIdList();
            for (String cameraId : mCameraIdList){
                CameraCharacteristics characteristics
                        = mCameraManager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing == CameraCharacteristics.LENS_FACING_BACK){
                    mCameraId = cameraId;
                    return;
                }
            }
        }catch (CameraAccessException e){
            //TODO
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
//        if (mTextureView.isAvailable()){
//            openCamera(mCameraId);
//        }else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
        openCamera(mCameraId);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        closeCamera(mCameraId);
        stopBackgroundThread();
    }

    TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");
            openCamera(mCameraId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
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

    private void openCamera(String cameraId) {
        if (!checkCameraPermission()){
            requestCameraPermission();
            return;
        }
        try {
            mCameraId = cameraId;
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback,mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void closeCamera(String cameraId) {

        if (mCaptureSession != null){
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void setUpCameraOutputs(String cameraId, int width, int height) {
        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        } catch (CameraAccessException e){
            e.printStackTrace();
        }

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

    private void createCameraPreviewSession(){
        try {

//            SurfaceTexture texture = mTextureView.getSurfaceTexture();
//            assert texture != null;
//
//            texture.setDefaultBufferSize(mImageReader.getWidth(),mImageReader.getHeight());
//
//            Surface surface = new Surface(texture);

            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
//            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()),
                    mCameraCaptureSessionStateCallback,mBackgroundHandler);
        }catch (CameraAccessException e){

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

    CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            Log.d(TAG, "onOpened: ");

            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
            Log.d(TAG, "onDisconnected: ");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError: ");
            camera.close();
            mCameraDevice = null;
        }
    };

    CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback
            = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (null == mCameraDevice){
                return;
            }

            mCaptureSession = session;

            try{
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                mPreviewRequest = mPreviewRequestBuilder.build();
                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                        mCaptureCallback,mBackgroundHandler);
            }catch (CameraAccessException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed: ");
        }
    };

    ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable: ");
            final Image image = reader.acquireNextImage();

//            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//            byte[] data = new byte[buffer.remaining()];
//            buffer.get(data);
//
//            final Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            YuvImage yuvImage = new YuvImage(data,ImageFormat.NV21,400,600,null);
//            yuvImage.compressToJpeg(new Rect(0,0,400,600),50,out);
//            byte[] imageBytes = out.toByteArray();
//
//            final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);

//            mTextureView.setBackground(new BitmapDrawable(bitmap));

//             SurfaceTexture texture =  mTextureView.getSurfaceTexture();
//            Canvas canvas = mTextureView.lockCanvas();
//            if (canvas != null){
//                canvas.drawBitmap(bitmap,0,0,null);
//                mTextureView.unlockCanvasAndPost(canvas);
//            }

//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mImageView.setImageBitmap(bitmap);
//                }
//            });

            image.close();
        }
    };

    CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
//            Log.d(TAG, "onCaptureStarted: ");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
//            Log.d(TAG, "onCaptureProgressed: ");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            Log.d(TAG, "onCaptureCompleted: ");
        }
    };

}