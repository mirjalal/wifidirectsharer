package com.talmir.mickinet.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.talmir.mickinet.R;
import com.talmir.mickinet.helpers.background.CrashReport;

import java.io.File;
import java.text.DecimalFormat;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText messageBody = findViewById(R.id.problem);

        findViewById(R.id.send_problem).setOnClickListener(v -> {
            if (messageBody.getText().toString().length() > 0) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"mirjalal.talishinski@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Help/Feedback/Question about MickiNet");
                i.putExtra(
                    Intent.EXTRA_TEXT,
                    messageBody.getText().toString() + (((CheckBox) findViewById(R.id.system_logs)).isChecked() ? getInfoAboutDevice() : "")
                );
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.send_mail)));
                } catch (android.content.ActivityNotFoundException ignored) {
                    Toast.makeText(getApplicationContext(), R.string.no_email_client, Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(getApplicationContext(), R.string.describe_problem, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author  mirjalal
     * @since   22.7.17
     * @return  a string that contains device information
     */
    @NonNull
    private String getInfoAboutDevice() {
        StringBuilder log = new StringBuilder("\n\n\n--System and Hardware Info--");
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            long totalBlocks = stat.getBlockCount();

            log.append("\nVersion code: ").append(packageInfo.versionCode)
               .append("\nVersion name: ").append(packageInfo.versionName)
               .append("\nModel: ").append(Build.MODEL)
               .append("\nBoard: ").append(Build.BOARD)
               .append("\nBootloader: ").append(Build.BOOTLOADER)
               .append("\nBrand: ").append(Build.BRAND)
               .append("\nDevice: ").append(Build.DEVICE)
               .append("\nTotal RAM: ").append(memInfo.totalMem).append(" (").append(formatSize(memInfo.totalMem)).append(')')
               .append("\nAvailable RAM: ").append(memInfo.availMem).append(" (").append(formatSize(memInfo.availMem)).append(')')
               .append("\nFree space built-in: ").append(totalBlocks).append(" (").append(formatSize(totalBlocks * blockSize)).append(')')
               .append("\nAvailable space built-in: ").append(availableBlocks).append(" (").append(formatSize(availableBlocks * blockSize)).append(')')
               .append("\nHardware: ").append(Build.HARDWARE)
               .append("\nManufacturer: ").append(Build.MANUFACTURER)
               .append("\nProduct: ").append(Build.PRODUCT)
               .append("\nTags: ").append(Build.TAGS)
               .append("\nDisplay: ").append(Build.DISPLAY)
               .append("\nDisplay width: ").append(displaymetrics.widthPixels)
               .append("\nDisplay height: ").append(displaymetrics.heightPixels);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int abisLength = Build.SUPPORTED_ABIS.length;
                for (int i = 0; i < abisLength; i++)
                    log.append("\nSupported ABI ").append(i + 1).append(": ").append(Build.SUPPORTED_ABIS[i]);
            }

            Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            boolean isCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            log.append("\n--Battery Info--");
            log.append("\nCharging status: ").append(isCharging ? "Charging" : "Not charging");
            if (isCharging) {
                log.append("\nCharging type: ");
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
                    log.append("USB");
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
                    log.append("AC");
                if (chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS)
                    log.append("Wireless");
            }
            log.append("\nBattery level: ").append((int)((level / (float)scale) * 100));
        } catch (/*PackageManager.NameNotFound*/Exception e) {
            CrashReport.report(getApplicationContext(), ContactActivity.class.getName());
        }
        return log.toString();
    }

    /**
     * Converts file/memory size/length from long to huuuumaan readable string.
     *
     * @param size size in bytes
     * @return human readable file size
     */
    @NonNull
    private String formatSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
