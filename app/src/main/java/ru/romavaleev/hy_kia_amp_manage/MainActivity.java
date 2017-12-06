package ru.romavaleev.hy_kia_amp_manage;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView bassTV, midTv, trebTV, balanceTV, faderTV, volTV;
    private ImageView bassM, midM, trebM, balanceM, faderM, volM, bassP, midP, trebP, balanceP, faderP, volP;
    int bassVol, midVol, trebVol, balanceVol, faderVol, volVol;
    private SharedPreferences sp;
    private Handler sendHandler;
    public static Handler recHandler;
    private EditText recText;
    ImageView btnSettings;

    private Typeface typeFace;

    /*
   * Notifications from UsbService will be received here.
   */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB-COM готов", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "Права на использование USB-COM не получены", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "USB-COM не подключен", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB-COM отключен", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "Данный USB-COM не поддерживается", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(recHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/egb.ttf");
        getSupportActionBar().hide();

        bassTV = (TextView) findViewById(R.id.bassTV);
        midTv = (TextView) findViewById(R.id.midTV);
        trebTV = (TextView) findViewById(R.id.trebTV);
        balanceTV = (TextView) findViewById(R.id.balanceTV);
        faderTV = (TextView) findViewById(R.id.faderTV);
        volTV = (TextView) findViewById(R.id.volTV);

        recText = (EditText) findViewById(R.id.recText);
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isDebug", false)) {
            recText.setVisibility(View.GONE);
        }

        bassM = (ImageView) findViewById(R.id.bassMinus);
        trebM = (ImageView) findViewById(R.id.trebMinus);
        midM = (ImageView) findViewById(R.id.midMinus);
        balanceM = (ImageView) findViewById(R.id.balanceMinus);
        faderM = (ImageView) findViewById(R.id.fadeMinus);
        volM = (ImageView) findViewById(R.id.volMinus);
        bassP = (ImageView) findViewById(R.id.bassPlus);
        trebP = (ImageView) findViewById(R.id.trebPlus);
        midP = (ImageView) findViewById(R.id.midPlus);
        balanceP = (ImageView) findViewById(R.id.balancePlus);
        faderP = (ImageView) findViewById(R.id.fadePlus);
        volP = (ImageView) findViewById(R.id.volPlus);

        btnSettings = (ImageView) findViewById(R.id.btnSettings);


        ImageView.OnClickListener listenerV = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickProc(view);
            }
        };


        bassM.setOnClickListener(listenerV);
        midM.setOnClickListener(listenerV);
        trebM.setOnClickListener(listenerV);
        balanceM.setOnClickListener(listenerV);
        faderM.setOnClickListener(listenerV);
        volM.setOnClickListener(listenerV);
        bassP.setOnClickListener(listenerV);
        midP.setOnClickListener(listenerV);
        trebP.setOnClickListener(listenerV);
        balanceP.setOnClickListener(listenerV);
        faderP.setOnClickListener(listenerV);
        volP.setOnClickListener(listenerV);


        firstInit();

        //recText = (TextView) findViewById(R.id.textView);


        sendHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (usbService != null && !sp.getBoolean("oCanbus", false))
                    usbService.write((byte[]) msg.obj);

                if (sp.getBoolean("oCanbus", false)) {
                /*    mAm = (AudioManager) getSystemService(AUDIO_SERVICE);
                    String str = SystemProperties.get("hal9k.setParameters");
                    mParameter = str;
                    mAm.setParameters(this.mParameter);*/
                }
            }
        };
        chSett();

        recHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (sp.getBoolean("isDebug", false)) {
                    switch (msg.what) {
                        case UsbService.MESSAGE_FROM_SERIAL_PORT:
                            String data = (String) msg.obj;
                            recText.setText(data + " ");
                            break;
                    }
                } else {
                    recText.setText("");
                }
            }
        };
    }

    private void clickProc(View view) {
        boolean write = false;

        if (view.equals(bassM) && bassVol > 0) {
            bassVol--;
            labelTextFormatter(bassTV, bassVol);
            sp.edit().putInt("bas", bassVol).apply();
            write = true;
        }

        if (view.equals(midM) && midVol > 0) {
            midVol--;
            labelTextFormatter(midTv, midVol);
            sp.edit().putInt("mid", midVol).apply();
            write = true;
        }

        if (view.equals(trebM) && trebVol > 0) {
            trebVol--;
            labelTextFormatter(trebTV, trebVol);
            sp.edit().putInt("tre", trebVol).apply();
            write = true;
        }

        if (view.equals(balanceM) && balanceVol > 0) {
            balanceVol--;
            labelTextFormatter(balanceTV, balanceVol);
            sp.edit().putInt("bal", balanceVol).apply();
            write = true;
        }

        if (view.equals(faderM) && faderVol > 0) {
            faderVol--;
            labelTextFormatter(faderTV, faderVol);
            sp.edit().putInt("fad", faderVol).apply();
            write = true;
        }
        if (volVol < 4 && volVol > 0) {
            volVol = 4;
            write = true;
        }

        if (view.equals(volM) && volVol >= 4) {
            volVol = volVol - 4;
            labelTextFormatter(volTV, volVol);
            sp.edit().putInt("vol", volVol).apply();
            write = true;
        }


        if (view.equals(bassP) && bassVol < 20) {
            bassVol++;
            labelTextFormatter(bassTV, bassVol);
            sp.edit().putInt("bas", bassVol).apply();
            write = true;
        }

        if (view.equals(midP) && midVol < 20) {
            midVol++;
            labelTextFormatter(midTv, midVol);
            sp.edit().putInt("mid", midVol).apply();
            write = true;
        }

        if (view.equals(trebP) && trebVol < 20) {
            trebVol++;
            labelTextFormatter(trebTV, trebVol);
            sp.edit().putInt("tre", trebVol).apply();
            write = true;
        }

        if (view.equals(balanceP) && balanceVol < 20) {
            balanceVol++;
            labelTextFormatter(balanceTV, balanceVol);
            sp.edit().putInt("bal", balanceVol).apply();
            write = true;
        }

        if (view.equals(faderP) && faderVol < 20) {
            faderVol++;
            labelTextFormatter(faderTV, faderVol);
            sp.edit().putInt("fad", faderVol).apply();
            write = true;
        }

        if (view.equals(volP) && volVol < 140) {
            volVol = volVol + 4;
            labelTextFormatter(volTV, volVol);
            sp.edit().putInt("vol", volVol).apply();
            write = true;
        }

        if (write)
            chSett();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void firstInit() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        volVol = sp.getInt("vol", 10);
        balanceVol = sp.getInt("bal", 10);
        faderVol = sp.getInt("fad", 10);
        bassVol = sp.getInt("bas", 10);
        midVol = sp.getInt("mid", 10);
        trebVol = sp.getInt("tre", 10);


        labelTextFormatter(balanceTV, balanceVol);
        labelTextFormatter(faderTV, faderVol);
        labelTextFormatter(bassTV, bassVol);
        labelTextFormatter(midTv, midVol);
        labelTextFormatter(trebTV, trebVol);
        volTV.setText(String.valueOf(volVol));
        volTV.setTypeface(typeFace);

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });

    }

    private void showSettings() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settimgs);
        dialog.setCancelable(true);
        dialog.setTitle(null);

        CheckBox chDebug = (CheckBox) dialog.findViewById(R.id.chDebug);
        CheckBox chOpenCanbus = (CheckBox) dialog.findViewById(R.id.chCanBusAdapter);
        chDebug.setChecked(sp.getBoolean("isDebug", false));

        chDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sp.edit().putBoolean("isDebug", b).apply();
                if (b)
                    recText.setVisibility(View.VISIBLE);
                else
                    recText.setVisibility(View.GONE);
            }
        });
        chOpenCanbus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sp.edit().putBoolean("oCanbus", b).apply();
            }
        });


        LinearLayout dialogButton = (LinearLayout) dialog.findViewById(R.id.setBtnOk);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void labelTextFormatter(TextView et, int vol) {
        if (!et.equals(volTV)) {
            et.setText(String.valueOf(vol - 10));
        } else {
            et.setText(String.valueOf(vol / 4));
        }
        et.setTypeface(typeFace);
    }


    private void chSett() {

        byte[] data = new byte[9];
        data[0] = (byte) (volVol & 255);
        data[1] = (byte) (balanceVol & 255);
        data[2] = (byte) (faderVol & 255);
        data[3] = (byte) (bassVol & 255);
        data[4] = (byte) (midVol & 255);
        data[5] = (byte) (trebVol & 255);
        data[6] = (byte) (255);
        data[7] = (byte) (255);

        int checkSumm = 0;
        for (int i = 0; i < 8; i++) {
            checkSumm = data[i] + checkSumm;
        }
        data[8] = (byte) ((checkSumm & 255) ^ 255);

        Message msg = new Message();
        msg.obj = data;
        sendHandler.sendMessageDelayed(msg, 100);
    }


}
