package com.atplatform.yuyenchia.smartlight;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;


public class ScanDatabase extends Activity {
    private Handler LoopHandler = new Handler();
    private AVObject Led_Data;
    private AVObject temp;
    public boolean Led_state = true;
    public int Duty_cycle = 0;
    private int renew_time = 0;
    private boolean Local_control = false;
    private int temp_count = 0;
    private boolean toaststate = false;
    private Handler uiHandler;


    public ScanDatabase(String DATACLASS, String ID, int Renew_time, Handler handler) {
        renew_time = Renew_time;
        uiHandler = handler;

        Led_Data = AVObject.createWithoutData(DATACLASS, ID);
        Led_Data.put("LED_STATE", true);
        Led_Data.put("DUTY_CYCLE", 0);
        Led_Data.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                // Log.d("saved", "success!");
            }
        });
        temp = AVObject.createWithoutData(DATACLASS, ID);
        LoopHandler.post(looper);
    }

    public void Turn_ON_Local() {
        Led_state = false;
        Led_Data.put("LED_STATE", false);
        Led_Data.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                //  Log.d("saved","local on");
            }
        });
    }

    public void Turn_OFF_Local() {
        Led_state = true;
        Led_Data.put("LED_STATE", true);
        Led_Data.saveInBackground();
    }

    public void Upload_duty_cycle(int duty_cycle) {

        Led_Data.put("DUTY_CYCLE", duty_cycle);
        Led_Data.saveInBackground();
    }

    private Runnable looper = new Runnable() {
        //    private   AVObject temp=AVObject.createWithoutData(dc,id);

        @Override
        public void run() {

            if (Local_control == true) {
                temp_count++;
                if (temp_count >= 5) {
                    temp_count = 0;
                    Local_control = false;
                }
            } else {
                temp.fetchInBackground(new GetCallback<AVObject>() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (avObject != null) {
                            Led_state = avObject.getBoolean("LED_STATE");
                            Duty_cycle = avObject.getInt("DUTY_CYCLE");
                            if (Led_state == true) {
                                Log.d("get", "true");
                            } else if (Led_state == false) {
                                Log.d("get", "false");
                            }
                            toaststate = false;
                        } else {
                            if (!toaststate) {
                                if (uiHandler!=null) {
                                    uiHandler.sendMessage(Message.obtain(uiHandler,0, "Network connect failed!"));
                                    toaststate = true;
                                }
                            }
                        }
                    }
                });
            }

            LoopHandler.postDelayed(looper, renew_time);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoopHandler.removeCallbacks(looper);
    }
}






