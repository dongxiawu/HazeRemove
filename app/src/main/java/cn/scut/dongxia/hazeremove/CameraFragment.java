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
import android.hardware.Camera;
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
import android.media.MediaRecorder;
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
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    public static CameraFragment newInstance(){
        return new CameraFragment();
    }

    private int cameraId;

    private Camera camera;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mImageView = (ImageView) view.findViewById(R.id.image_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        int cameraNum = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId< cameraNum; cameraId++){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(cameraId,cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                this.cameraId = cameraId;
                break;
            }
        }

        camera = Camera.open(cameraId);
//        Camera.Parameters parameters = camera.getParameters();
//        parameters.setPreviewSize(400,600);
//        camera.setParameters(parameters);

//        SurfaceView surfaceView = new SurfaceView(getContext());
//        try {
//            camera.setPreviewDisplay(surfaceView.getHolder());
//        }catch (IOException e){
//            e.printStackTrace();
//        }
        
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.d(TAG, "onPreviewFrame: ");
                Camera.Size size = camera.getParameters().getPreviewSize();

                YuvImage yuvImage = new YuvImage(data,ImageFormat.NV21,size.width,size.height,null);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0,0,yuvImage.getWidth(),yuvImage.getHeight()),100,outputStream);
                byte[] rawImage = outputStream.toByteArray();

                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                options.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage,0,rawImage.length,options);
                Log.d(TAG, "onPreviewFrame: " + "width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());

                mImageView.setImageBitmap(bitmap);
            }
        });
//        camera.startPreview();

        if (mTextureView.isAvailable()){

        }else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        stopBackgroundThread();
    }

    TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");

            try {
                camera.setPreviewTexture(surface);
//                camera.setPreviewDisplay(surfaceView.getHolder());
            }catch (IOException e){
                e.printStackTrace();
            }
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(400,600);
            parameters.setPreviewFpsRange(20,30);
            parameters.setPictureSize(400,600);

            camera.startPreview();
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

}