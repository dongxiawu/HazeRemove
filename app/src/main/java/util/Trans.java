package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.RenderScript;

/**
 * Created by dongxia on 17-11-29.
 * 优化：改成枚举
 */

public class Trans {
    private static volatile Trans mInstance;

    private Context mContext;

    private RenderScript mRenderScript;

    private Trans(Context context){
        mContext = context.getApplicationContext();
        mRenderScript = RenderScript.create(mContext);


    }

    public Trans getInstance(Context context){
        if (mInstance == null){
            synchronized (Trans.class){
                if (mInstance == null){
                    mInstance = new Trans(context);
                }
            }
        }
        return mInstance;
    }



    public void destory(){
        mInstance = null;
        mContext = null;
    }
}
