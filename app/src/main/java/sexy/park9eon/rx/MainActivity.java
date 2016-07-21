package sexy.park9eon.rx;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        scan();
    }

    public void scan() {
        findViewById(R.id.button).setOnClickListener(view -> {
            RxBluetooth.scan(getApplicationContext(), new RxBluetooth.BluetoothSubscriber<RxBluetooth.Result>() {

                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(RxBluetooth.Result result) {
                    if (result.getDevice().getName() == null) {
                        Log.d("TEST", result.getDevice().getAddress() + " : null");
                    } else {
                        Log.d("TEST", result.getDevice().getAddress() + " : " + result.getDevice().getName());
                        if (result.getDevice().getName().equals("iDL")) {
                            getRxBluetooth().stopScan();
                            getRxBluetooth().connect(result.getDevice(), new RxBluetooth.BluetoothSubscriber<BluetoothGattCharacteristic>() {
                                @Override
                                public void onStart() {
                                    super.onStart();
                                    Button button = (Button) findViewById(R.id.button);
                                    button.setText(result.getDevice().getAddress());
                                    button.setOnClickListener(view1 -> {
                                        try {
                                            this.getRxBluetooth().sendMessage(editText.getText().toString());
                                        } catch (Exception e){
                                            onError(e);
                                        }
                                    });
                                    button.setOnLongClickListener(view1 -> {
                                        this.getRxBluetooth().disconnect();
                                        button.setText("re scan");
                                        scan();
                                        return true;
                                    });
                                }

                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                                    Log.d("Request", Arrays.toString(bluetoothGattCharacteristic.getValue()));
                                }
                            });
                        }
                    }
                }
            });
        });

    }
}
