package sexy.park9eon.rx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(view -> {
            RxBluetooth.create(getApplicationContext())
                    .scan(new RxBluetooth.RxScanner() {

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
                            }
                        }
                    });
        });
    }
}
