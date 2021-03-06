package cn.scut.dongxia.hazeremove;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.scut.dongxia.hazeremove.camera.AbsCameraBridgeView;
import cn.scut.dongxia.hazeremove.camera.CameraPreview;
import cn.scut.dongxia.hazeremove.dehaze.DeHaze;


public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    private long lastFrameTime = 0;

    //Widgets
    private CameraPreview mCameraView;
//    private JavaCameraView mCameraView;

    private DeHaze deHaze;

    private Mat recover;

    private ExecutorService executors;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkCameraPermission()){
            requestCameraPermission();
        }

//        executors = Executors.newCachedThreadPool();
        executors = Executors.newFixedThreadPool(6);
//        executors = Executors.newSingleThreadExecutor();

        mCameraView.enableView();
        mCameraView.enableFpsMeter();
        mCameraView.setCameraViewListener(new AbsCameraBridgeView.CameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                nativeCreateHazeRemoveModel();
//                deHaze = new DeHaze(7,0.1,0.95,10E-6,width,height);
                recover = new Mat(height,width, CvType.CV_8UC4);
            }

            @Override
            public void onCameraViewStopped() {
                nativeDeleteHazeRemoveModel();
//                deHaze.release();
            }

            @Override
            public Mat onCameraFrame(AbsCameraBridgeView.CameraViewFrame inputFrame) {
//                Mat rgba = inputFrame.rgba();
//                nativeProcessFrame(rgba.getNativeObjAddr());
//                Mat recover = deHaze.videoHazeRemove(rgba);
                nativeProcessFrame(inputFrame.getOrigData(),inputFrame.getWidth(),
                        inputFrame.getHeight(),recover.getNativeObjAddr());
                return recover;
//                return rgba;
            }
        });
//        mCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
//            @Override
//            public void onCameraViewStarted(int width, int height) {
//                nativeCreateHazeRemoveModel();
//                deHaze = new DeHaze(7,0.1,0.95,10E-6,width,height);
//            }
//
//            @Override
//            public void onCameraViewStopped() {
//                nativeDeleteHazeRemoveModel();
//                deHaze.release();
//            }
//
//            @Override
//            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
////                Mat rgba = inputFrame.rgba().clone();
//                Mat rgba = inputFrame.rgba();
//                Mat recover = deHaze.videoHazeRemove(rgba);
//
//                return recover;
//            }
//        });

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        mCameraView.disableView();
        executors.shutdown();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView(View root){
        mCameraView = (CameraPreview) root.findViewById(R.id.camera_view);
        mCameraView.setMaxFrameSize(1920/2,1080/2);
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

//    public native void nativeProcessFrame(long matAddrRgba);
//
//    public native void nativeProcessFrame(long origAddr, long resultAddr);

    private native void nativeCreateHazeRemoveModel();

    private native void nativeDeleteHazeRemoveModel();

    private native static void nativeProcessFrame(byte[] frame, int width, int height, long recoverAddr);

}