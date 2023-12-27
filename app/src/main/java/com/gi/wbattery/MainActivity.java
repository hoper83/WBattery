package com.gi.wbattery;


import static java.lang.String.*;

import androidx.appcompat.app.AppCompatActivity;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.BatteryManager;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    int current;
    Runnable updater;

    int level = 0;

    final Handler timerHandler = new Handler();
    double watt = 0;
    int volt;
    int status = -1;

    int health = 0;

    boolean isCharging = false;
    boolean usbCharge;
    boolean wCharge;
    boolean acCharge;

    int capacity;

    int temperature;

    private AdView mAdView;

    boolean first = true;

    IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private BroadcastReceiver mBatteryLevelReciver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {

            //Toast.makeText(getApplicationContext(),"aggiornamento", Toast.LENGTH_SHORT).show();


            BatteryManager batteryManager = (BatteryManager) getApplicationContext().getSystemService(BATTERY_SERVICE);

            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;


            if (first) {
                current = -1;
                watt = -1;
                first = false;
            } else {
                current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                watt = (int)(((double) current * (double) 5.5) / 1000);

                if ((isCharging & current < 0)||(!isCharging & current >= 0)) {
                    //Toast.makeText(getApplicationContext(),"Xiaomi", Toast.LENGTH_SHORT).show();
                    current = (int) ((-1) * current);
                    //watt = (int) ((-1) * watt);

                }

                if (current >= 50000 || current < -50000)
                {
                    current = current /1000;
                    //watt = watt /1000;
                }

                watt = (((double) current * 5.5) / 1000);

            }


            volt = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE , -1);
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL , -1);
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
            acCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_AC);
            wCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS);
            capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });



    }


    @Override
    protected void onPause() {
        unregisterReceiver(mBatteryLevelReciver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //onReceive();
        TextView textView = (TextView) findViewById(R.id.tv_ampere) ;
        TextView textView2 = (TextView) findViewById(R.id.tv_watt) ;
        TextView textView_pallino = (TextView) findViewById(R.id.tv_pallino) ;

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        textView_pallino.startAnimation(anim);

        textView.setText("----- mA");
        textView2.setText("----- Watt");



        current = -1;


        first = true;

        BatteryManager batteryManager = (BatteryManager) getApplicationContext().getSystemService(BATTERY_SERVICE);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        batteryLevel();


        updateTime();
    }

    void updateTime() {
        //timer=(TextView) findViewById(R.id.timerText);
        TextView textView = (TextView) findViewById(R.id.tv_ampere) ;
        TextView textView2 = (TextView) findViewById(R.id.tv_watt) ;
        TextView textView3 = (TextView) findViewById(R.id.tv_status) ;
        TextView textView4= (TextView) findViewById(R.id.tv_volt) ;
        TextView textView5= (TextView) findViewById(R.id.tv_livello) ;
        TextView textView6= (TextView) findViewById(R.id.tv_salute) ;
        TextView textView7= (TextView) findViewById(R.id.tv_tipocarica) ;
        //TextView textView8= (TextView) findViewById(R.id.tv_capacita) ;
        TextView textView9= (TextView) findViewById(R.id.tv_temperature) ;
        TextView textView10= (TextView) findViewById(R.id.tv_manufacturer);
        TextView textView11= (TextView) findViewById(R.id.tv_build);
        TextView textView12= (TextView) findViewById(R.id.tv_version);
        TextView textView13= (TextView) findViewById(R.id.tv_sdk);

        updater = new Runnable() {
            @Override
            public void run() {


                String man = android.os.Build.MANUFACTURER;
                String manup = man.substring(0,1).toUpperCase() + man.substring(1,man.length()).toLowerCase();


                if (current == -1) {
                    textView.setText("----- mA");
                    textView2.setText("----- Watt");
                }  else {

                        String watt2dec = format("%.2f", watt);
                        textView.setText("" + current + " mA");
                        textView2.setText(watt2dec + " Watt");

                }

                if (isCharging) {
                    textView3.setText(R.string.stato_in_carica);
                } else {textView3.setText(R.string.stato_in_scarica);};

                textView4.setText(volt+" mV");
                textView5.setText(level+"%");

                if (health == BatteryManager.BATTERY_HEALTH_GOOD) {
                    textView6.setText(R.string.health_good);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                    textView6.setText(R.string.health_overheat);
                } else if (health  == BatteryManager.BATTERY_HEALTH_COLD ){
                    textView6.setText(R.string.health_cold);
                } else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE)
                {
                    textView6.setText(R.string.over_voltage);
                } else if (health == BatteryManager.BATTERY_HEALTH_DEAD)
                {
                    textView6.setText(R.string.dead);
                } else if (health == BatteryManager.BATTERY_HEALTH_UNKNOWN)
                {
                    textView6.setText(R.string.unknow);
                } else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE )
                {
                    textView6.setText(R.string.unspecified_failure);
                } else {
                    textView6.setText("ND");
                }


                if (usbCharge) {
                    textView7.setText(R.string.USB_charging);
                } else if (acCharge) {
                    textView7.setText(R.string.AC_charging);
                } else if (wCharge ){
                    textView7.setText(R.string.wireless_charging);
                } else if  (!isCharging) {
                    textView7.setText(R.string.battery_charging);
                } else {
                    textView7.setText(R.string.unkonw_charging);
                }

          //      textView8.setText(""+capacity);
                textView9.setText(((""+temperature*0.1)+"0000").substring(0,4)+"°");


                textView10.setText(manup);

                String deviceName = android.os.Build.MODEL;

                textView11.setText(deviceName);

                StringBuilder builder = new StringBuilder();
                builder.append("android : ").append(Build.VERSION.RELEASE);

                String codename = "";

                Field[] fields = Build.VERSION_CODES.class.getFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    int fieldValue = -1;

                    try {
                        fieldValue = field.getInt(new Object());
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (fieldValue == Build.VERSION.SDK_INT) {
                        codename  =fieldName;
                        // builder.append("sdk=").append(fieldValue);
                    }
                }



                String version = Build.VERSION.RELEASE;
                int sdk = Build.VERSION.SDK_INT;
                //codename = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY;

                textView12.setText(version+" ("+codename+")");

                textView13.setText(""+sdk);




                timerHandler.postDelayed(updater,1100);
            }
        };
        timerHandler.post(updater);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(updater);
    }

    private void batteryLevel() {
        //IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReciver, batteryLevelFilter);

    }


}