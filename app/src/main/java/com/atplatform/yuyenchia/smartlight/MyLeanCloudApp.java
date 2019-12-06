package com.atplatform.yuyenchia.smartlight;

import android.app.Application;
import android.graphics.Typeface;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;

import java.lang.reflect.Field;


public class MyLeanCloudApp extends Application {
    public static Typeface typefaceStHeiTi;
    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this,"3x1teTj3EBqyr6s0UzG0nroP-gzGzoHsz","evT4PsqqdJzJvjxNXKeYXFYw");
        // 放在 SDK 初始化语句 AVOSCloud.initialize() 后面，只需要调用一次即可
        AVOSCloud.setDebugLogEnabled(true);//app发布后关闭


        typefaceStHeiTi = Typeface.createFromAsset(getAssets(), "fonts/msyh.ttf");
        try {
            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null, typefaceStHeiTi);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}

