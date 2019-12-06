package com.atplatform.yuyenchia.smartlight;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class https {
    public static String AdoptPost(String URL, String Data) {
        String Result = null;
        try {
            // 1.定义请求url
            java.net.URL url = new URL( URL );
            // 2.建立一个https的连接
            //HttpURLConnection是http连接
            //HttpsURLConnection是https连接
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //设置该http连接的cookid，没有可以不设置。
            //conn.setRequestProperty("cookie", MainSP.getString( "CookieId","" ));
            // 3.设置请求的方式等
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML,like Gecko) Chrome/45.0.2454.101 Safari/537.36" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            //conn.setRequestProperty( "Content-Length", Data.length() + "" );
            conn.setConnectTimeout( 5000 );//设置连接超时时间
            conn.setReadTimeout( 5000 ); //设置读取的超时时间
            // 4.一定要记得设置 把数据以流的方式写给服务器
            conn.setDoOutput( true ); // 设置要向服务器写数据
            conn.getOutputStream().write( Data.getBytes() );
            int code = conn.getResponseCode(); // 服务器的响应码 200 OK //404 页面找不到
            // // 503服务器内部错误
            if (code == 200) {
                int contentLength = conn.getContentLength();
                InputStream is = conn.getInputStream();
                // 把is的内容转换为字符串
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read( buffer )) != -1) {
                    bos.write( buffer, 0, len );
                }
                //将返回的数据转换成为string数据
                Result = new String( bos.toByteArray());
                is.close();
            } else {
                Log.d( "请求失败","19\n"+"code:"+String.valueOf( code ) );

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    public static void set(String json){
        String res = AdoptPost("127.0.0.1:8090/api/box/set", json);

    }


}
