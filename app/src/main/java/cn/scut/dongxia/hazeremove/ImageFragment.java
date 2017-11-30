package cn.scut.dongxia.hazeremove;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

import dehaze.DeHaze;
import dehaze.ImageHazeRemove;


public class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";
    
    
    private static final int SELECT_PHOTO = 1;

    private Button mSelect;

    private Button mProcess;

//    private ImageView mImageView;

    private TextureView mTextureView;

    private TextView mTimeText;

    private Bitmap mBitmap;

    public static ImageFragment newInstance() {
        return new ImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mTextureView.isAvailable()){
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mTextureView.setSurfaceTextureListener(null);
    }

    private void initView(View root){
        mSelect = (Button) root.findViewById(R.id.select_button);
        mSelect.setOnClickListener(mOnClickListener);
        mProcess = (Button) root.findViewById(R.id.process_button);
        mProcess.setOnClickListener(mOnClickListener);

        mTimeText = (TextView) root.findViewById(R.id.time_text);
        mTextureView = (TextureView) root.findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.getFrameAtTime();
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.select_button:
                    openAlbum();
                    break;
                case R.id.process_button:
                    if (mBitmap != null){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final long start = System.currentTimeMillis();
                                Mat dest = new Mat();
                                Utils.bitmapToMat(mBitmap,dest);
                                dest =new ImageHazeRemove(15, 0.01, 0.95, 10E-6).process(dest);
//                                dest = new DeHaze(15,0.1,0.95,10E-6)
//                                        .imageHazeRemove(dest);
                                Utils.matToBitmap(dest,mBitmap);
                                final long stop = System.currentTimeMillis();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Canvas canvas = mTextureView.lockCanvas();
                                        canvas.drawBitmap(mBitmap,0,0,null);
                                        mTextureView.unlockCanvasAndPost(canvas);
//                                        mImageView.setImageBitmap(mBitmap);
                                        mTimeText.setText((stop - start) + " ms");
                                    }
                                });
                            }
                        }).start();
                    }
                    break;
                default:break;
            }
        }
    };

    //打开相册
    private void openAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SELECT_PHOTO:
                if (data != null){
                    ContentResolver resolver = getActivity().getContentResolver();
                    Uri uri = data.getData();
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(resolver,uri);
                        if (mTextureView.isAvailable()){
                            Canvas canvas = mTextureView.lockCanvas();
                            canvas.drawBitmap(mBitmap,0,0,null);
                            mTextureView.unlockCanvasAndPost(canvas);
                        }

//                        mImageView.setImageBitmap(mBitmap);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                break;
                default:break;
        }
    }

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: ");
            if (mBitmap != null){
                Canvas canvas = mTextureView.lockCanvas();
                canvas.drawBitmap(mBitmap,0,0,null);
                mTextureView.unlockCanvasAndPost(canvas);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

}
