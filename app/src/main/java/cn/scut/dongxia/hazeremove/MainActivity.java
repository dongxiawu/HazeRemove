package cn.scut.dongxia.hazeremove;

import android.content.Intent;
import android.content.res.Configuration;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if (null == savedInstanceState){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout,CameraFragment.newInstance())
                    .commit();
        }
    }


    private void initView(){

    }
}
