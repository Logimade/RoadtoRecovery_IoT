package util;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import com.example.tudoem1.MainActivity;

public class TimeOut {


    private Handler handler;
    private Runnable r;
    private Activity activityA;


    public TimeOut(final Activity activity){
        handler = new Handler();
        activityA = activity;

        r = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(activityA, MainActivity.class);
                activityA.startActivity(intent);
            }
        };

        startHandler();
    }



    public void onUserInteraction() {
        activityA.onUserInteraction();
        stopHandler();
        startHandler();
    }


    public void stopHandler() {
        handler.removeCallbacks(r);
    }

    public void startHandler() {
        handler.postDelayed(r, Util.DISCONNECT_TIMEOUT);
    }


}
