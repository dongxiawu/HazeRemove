package cn.scut.dongxia.hazeremove;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("dehaze-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState){
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, CameraFragment.newInstance())
                    .commit();
        }
    }

}
