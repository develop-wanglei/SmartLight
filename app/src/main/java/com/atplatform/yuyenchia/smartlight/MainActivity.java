package com.atplatform.yuyenchia.smartlight;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;


import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String Bus_2_LED1 = "BCM5";//LED灯端口定义
    private static final String Bus_2_LED2 = "BCM6";//LED灯端口定义
    private static final String Bus_3_LED1 = "BCM12";//LED灯端口定义
    private static final String Bus_3_LED2 = "BCM26";//LED灯端口定义
    private static final String Bus_3_LED3 = "BCM7";//LED灯端口定义
    private static final String PWM_NAME = "PWM0";//pwm1-13 PWM0-18
    private long TimeTicket = 0;

    private int Waterfall_light_init = 50;
    private int Waterfall_light = 3*Waterfall_light_init;

    private Gpio Bus2_LEDGpio_1;//SMART_LED
    private Gpio Bus2_LEDGpio_2;//SMART_LED
    private Gpio Bus3_LEDGpio_1;//SMART_LED
    private Gpio Bus3_LEDGpio_2;//SMART_LED
    private Gpio Bus3_LEDGpio_3;//SMART_LED
    private Pwm mPwm;

    private Handler LoopHandler = new Handler();

    private int duty_cycle = 0;
    private int duty_cycle_temp = 0;
    private int duty_cycle_pre = 0;
    private int duty_cycle_now = 0;

    private Switch Led1_switch;
    private Switch Led2_switch;
    private Switch Led3_switch;
    SeekBar Led_seekbar;
    TextView brightness;

    public Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
            } catch (Exception e) {

            }
        }
    };

    private ScanDatabase SMART_LED_SCAN = new ScanDatabase(
            "LEDS", "5de75866dd3c13007f5e129a", 300, uiHandler);
    private ScanDatabase SMART_LED_SCAN_2 = new ScanDatabase(
            "LEDS", "5de7686f21b47e007fb43928", 2500, uiHandler);
    private ScanDatabase SMART_LED_SCAN_3 = new ScanDatabase(
            "LEDS", "5de7686921b47e007fb43925", 2500, uiHandler);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity created.");
        PeripheralManager Manager = PeripheralManager.getInstance();
        setContentView(R.layout.smartlightlayout);

        brightness = findViewById(R.id.textView);
        Led1_switch = findViewById(R.id.switch1);
        Led2_switch = findViewById(R.id.switch2);
        Led3_switch = findViewById(R.id.switch3);
        Led_seekbar = findViewById(R.id.seekBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        try {
            Bus2_LEDGpio_1 = Manager.openGpio(Bus_2_LED1);
            Bus2_LEDGpio_1.setActiveType(Gpio.ACTIVE_LOW);
            Bus2_LEDGpio_1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            Bus2_LEDGpio_2 = Manager.openGpio(Bus_2_LED2);
            Bus2_LEDGpio_2.setActiveType(Gpio.ACTIVE_LOW);
            Bus2_LEDGpio_2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            Bus3_LEDGpio_1 = Manager.openGpio(Bus_3_LED1);
            Bus3_LEDGpio_1.setActiveType(Gpio.ACTIVE_LOW);
            Bus3_LEDGpio_1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            Bus3_LEDGpio_2 = Manager.openGpio(Bus_3_LED2);
            Bus3_LEDGpio_2.setActiveType(Gpio.ACTIVE_LOW);
            Bus3_LEDGpio_2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            Bus3_LEDGpio_3 = Manager.openGpio(Bus_3_LED3);
            Bus3_LEDGpio_3.setActiveType(Gpio.ACTIVE_LOW);
            Bus3_LEDGpio_3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            //loop init
            LoopHandler.post(looper);

        } catch (IOException e) {
            Log.e(TAG, "Unable to on GPIO", e);
        }
        try {
            //PWM init
            mPwm = Manager.openPwm(PWM_NAME);
            initializePwm(mPwm);
        } catch (IOException e) {
            Log.w(TAG, "Unable to on PWM", e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Led1_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    SMART_LED_SCAN.Turn_ON_Local();
                else
                    SMART_LED_SCAN.Turn_OFF_Local();
            }
        });
        Led2_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    SMART_LED_SCAN_2.Turn_ON_Local();
                else
                    SMART_LED_SCAN_2.Turn_OFF_Local();
            }
        });
        Led3_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    SMART_LED_SCAN_3.Turn_ON_Local();
                else
                    SMART_LED_SCAN_3.Turn_OFF_Local();
            }
        });
        Led_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                duty_cycle_temp = progress;
                //SMART_LED_SCAN.Upload_duty_cycle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private Runnable looper = new Runnable() {//主任务循环（1ms
        @Override
        public void run() {
            TimeTicket++;
            brightness.setText(String.valueOf(duty_cycle));

            if (TimeTicket > 9999) {
                TimeTicket = 0;
            }

            if (TimeTicket % 10 == 0) {
                if (Led1_switch.isChecked()) {
                    try {
                        duty_cycle = Led_seekbar.getProgress();
                        mPwm.setEnabled(false);
                        mPwm.setPwmFrequencyHz(100);
                        mPwm.setPwmDutyCycle(duty_cycle);
                        mPwm.setEnabled(true);
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to set duty cycle", e);
                    }
                } else if(!SMART_LED_SCAN.Led_state) {
                    if (duty_cycle != SMART_LED_SCAN.Duty_cycle) {
                        duty_cycle = SMART_LED_SCAN.Duty_cycle;
                        try {
                            mPwm.setEnabled(false);
                            mPwm.setPwmFrequencyHz(100);
                            mPwm.setPwmDutyCycle(duty_cycle);
                            mPwm.setEnabled(true);
                        } catch (IOException e) {
                            Log.w(TAG, "Unable to set duty cycle", e);
                        }
                    }
                } else {
                    try {
                        mPwm.setEnabled(false);
                        mPwm.setPwmFrequencyHz(100);
                        mPwm.setPwmDutyCycle(0);
                        mPwm.setEnabled(true);
                        mPwm.setEnabled(false);
                        duty_cycle = 0;
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to set duty cycle", e);
                    }
                }
            }
            if (TimeTicket % 100 == 0) {
                duty_cycle_now = duty_cycle_temp;
                if (duty_cycle_pre != duty_cycle_now) {
                    SMART_LED_SCAN.Led_state = true;
                    SMART_LED_SCAN.Duty_cycle = duty_cycle_temp;
                    SMART_LED_SCAN.Upload_duty_cycle(duty_cycle_temp);
                }
                duty_cycle_pre = duty_cycle_now;
            }

            setgpioValue(Bus2_LEDGpio_1, SMART_LED_SCAN_2.Led_state);
            setgpioValue(Bus2_LEDGpio_2, SMART_LED_SCAN_2.Led_state);

            if(Led3_switch.isChecked() || !SMART_LED_SCAN_3.Led_state) {
                if (Waterfall_light > 2*Waterfall_light_init) {
                    setgpioValue(Bus3_LEDGpio_1, false);
                    setgpioValue(Bus3_LEDGpio_2, true);
                    setgpioValue(Bus3_LEDGpio_3, true);
                    Waterfall_light--;
                } else if (Waterfall_light > Waterfall_light_init) {
                    setgpioValue(Bus3_LEDGpio_1, true);
                    setgpioValue(Bus3_LEDGpio_2, false);
                    setgpioValue(Bus3_LEDGpio_3, true);
                    Waterfall_light--;
                } else if (Waterfall_light > 0) {
                    setgpioValue(Bus3_LEDGpio_1, true);
                    setgpioValue(Bus3_LEDGpio_2, true);
                    setgpioValue(Bus3_LEDGpio_3, false);
                } else {
                    Waterfall_light = 3*Waterfall_light_init;
                }
                Waterfall_light--;
            } else {
                setgpioValue(Bus3_LEDGpio_1, true);
                setgpioValue(Bus3_LEDGpio_2, true);
                setgpioValue(Bus3_LEDGpio_3, true);
            }

            LoopHandler.postDelayed(looper, 1);

        }

    };


    private void setgpioValue(Gpio GPIOS, boolean value) {
        try {
            GPIOS.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(50);
        pwm.setPwmDutyCycle(1);
        pwm.setEnabled(true);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoopHandler.removeCallbacks(looper);



        if (mPwm != null) {
            try {
                mPwm.close();
                mPwm = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            }
        }
        if (Bus2_LEDGpio_1 != null) {
            try {
                Bus2_LEDGpio_1.close();
                Bus2_LEDGpio_1 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close LED", e);
            }
        }
        if (Bus2_LEDGpio_2 != null) {
            try {
                Bus2_LEDGpio_2.close();
                Bus2_LEDGpio_2 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close LED", e);
            }
        }
        if (Bus3_LEDGpio_1 != null) {
            try {
                Bus3_LEDGpio_1.close();
                Bus3_LEDGpio_1 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close LED", e);
            }
        }
        if (Bus3_LEDGpio_2 != null) {
            try {
                Bus3_LEDGpio_2.close();
                Bus3_LEDGpio_2 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close LED", e);
            }
        }
        if (Bus3_LEDGpio_3 != null) {
            try {
                Bus3_LEDGpio_3.close();
                Bus3_LEDGpio_3 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close LED", e);
            }
        }
        SMART_LED_SCAN.onDestroy();
        SMART_LED_SCAN_2.onDestroy();
        SMART_LED_SCAN_3.onDestroy();
    }
}






