package com.mobrembski.edenredwidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;


public class ConfigureActivity extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public ConfigureActivity() {

    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();
        ConfigureDialog dialog = new ConfigureDialog(this);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        dialog.show();
    }

    private class ConfigureDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

        public ConfigureDialog(Context context) {
            super(context);
            this.setTitle(R.string.configure_title);
            setContentView(R.layout.activity_configure);
            findViewById(R.id.apply_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sp = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                    EditText et = (EditText) findViewById(R.id.edencard_number);
                    CheckBox notifyBox = (CheckBox) findViewById(R.id.notifyBox);
                    SeekBar bar = (SeekBar)findViewById(R.id.opacity_seekbar);
                    try {
                        long enteredNumber = Long.parseLong(et.getText().toString());
                        sp.edit().putLong(mAppWidgetId + "_cardid", enteredNumber).commit();
                        sp.edit().putBoolean(mAppWidgetId + "_notify", notifyBox.isChecked()).commit();
                        sp.edit().putInt(mAppWidgetId + "_opacity", bar.getProgress()).commit();
                    } catch (NumberFormatException ex) {
                        Toast.makeText(getApplicationContext(), R.string.card_input_invalid, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    dismiss();
                }
            });
            SeekBar bar = (SeekBar)findViewById(R.id.opacity_seekbar);
            bar.setOnSeekBarChangeListener(this);
            findViewById(R.id.exit_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        @SuppressLint("NewApi")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= 11) {
                View v = getWindow().getDecorView();
                float f = (float) progress / 100f;
                v.setAlpha(f);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
