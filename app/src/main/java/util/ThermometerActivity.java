/*package util;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Formatter;

import com.example.agorasim.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import visiomed.fr.bleframework.command.CommandFactory;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.common.BLEContext;
import visiomed.fr.bleframework.data.SerialNumber;
import visiomed.fr.bleframework.data.thermometer.ThermometerData;
import visiomed.fr.bleframework.device.Thermometer;
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent;
import visiomed.fr.bleframework.event.common.BLEEvent;
import visiomed.fr.bleframework.event.thermometer.ThermometerCommandStateEvent;
import visiomed.fr.bleframework.event.thermometer.ThermometerEvent;

import com.squareup.otto.Subscribe;


public class ThermometerActivity extends AppCompatActivity {


    @BindView(R.id.thermometer_device_text_view)
    TextView thermometer_device_text_view;

    @BindView(R.id.thermometer_state_text_view)
    TextView thermometer_state_text_view;

    @BindView(R.id.thermometer_command_text_view)
    TextView thermometer_command_text_view;

    @BindView(R.id.thermometer_result_text_view)
    TextView thermometer_result_text_view;

    @BindView(R.id.thermometer_command_list_view)
    ListView thermometer_command_list_view;


    private String mac;
    private BLECenter bleCenter;
    private Thermometer thermometer;

    private ThermometerActivity self;

    private Menu menu;
    private TextView thermometer_activity_info_text_view;
    private TextView thermometer_activity_value_text_view;
    private ProgressBar progressBar;

    private boolean standby = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);
        ButterKnife.bind(this);

        setTitle("Thermometer");

        bleCenter = BLEContext.getBLECenter(getApplicationContext());
        mac = getIntent().getStringExtra("MAC");
        thermometer = (Thermometer) bleCenter.getDevice(mac);

    }

    @Override
    protected void onResume() {
        super.onResume();
        bleCenter.getBus().register(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        thermometer_device_text_view.setText(thermometer.getBleDevice().getName());

        updateThermometerState("");
        updateCmdState("");
        updateResult(null);

        thermometer.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleCenter.getBus().unregister(this);

        thermometer.disconnect();
    }

    /************************************************************************************************************************
     UI operation
     *************************************************************************************************************************/




    /*private void updateCmdState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermometer_command_text_view.setText(Html.fromHtml("Cmd:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=#ADEBAD>" + state + "</font>"));
            }
        });
    }

    private void updateThermometerState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermometer_state_text_view.setText(Html.fromHtml("State:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font color=#ADEBAD>" + state + "</font>"));
            }
        });
    }

    private void updateResult(final ThermometerData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data == null) {
                    thermometer_result_text_view.setText("-- °C");
                } else {
                    thermometer_result_text_view.setText(data.toString());
                }
            }
        });
    }



    @Subscribe
    @SuppressWarnings("unused")
    public void onThermometerEvent(final ThermometerEvent event) {
        if (event.getMac().equalsIgnoreCase(mac)) {
            updateResult(event.getThermometerData());
        }
    }

}
*/
