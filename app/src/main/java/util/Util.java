package util;

import android.app.Activity;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    public static final int TIME_SPLASH = 5 * 1000;
    public static final int TIME_RELOAD = 15 * 1000;
    public static final long DISCONNECT_TIMEOUT = 1000000; // 10 min = 10 * 60 * 1000 ms

    public static int nrMedicamentos = -1;

    public static void fullscreen(Activity act) {
        act.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }


    public static String currentDataTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd,  HH:mm");
        Date date = new Date();

        return dateFormat.format(date);

    }
}
