package sexy.park9eon.rx;

import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

public final class RxBluetooth extends Observable<RxBluetooth.Result> {

    public static class Result {

        private final RxBluetooth rx;
        private final BluetoothDevice device;
        private final int rssi;

        public Result(BluetoothDevice device, int rssi, RxBluetooth rx) {
            this.device = device;
            this.rssi = rssi;
            this.rx = rx;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public int getRssi() {
            return rssi;
        }

    }

    private static final class BluetoothSubscriber implements Observable.OnSubscribe<Result>, Action0 {

        private Object scanner;
        private RxBluetooth rxBluetooth;

        public BluetoothSubscriber(Context context) {
            this.rxBluetooth = new RxBluetooth(context, this);
            this.rxBluetooth.doOnSubscribe(this);
        }

        @Override
        public void call(Subscriber<? super Result> subscriber) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.scanner = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        subscriber.onNext(new Result(result.getDevice(), result.getRssi(), rxBluetooth));
                    }
                };
            } else {
                this.scanner = (BluetoothAdapter.LeScanCallback) (bluetoothDevice, i, bytes) -> subscriber.onNext(new Result(bluetoothDevice, i, rxBluetooth));
            }
            this.rxBluetooth.startScan(this.scanner);
        }

        // 종료시 발동
        @Override
        public void call() {
            // 종료처리
            this.rxBluetooth.stopScan();
        }
    }

    public static abstract class RxScanner extends Subscriber<Result> {

        private RxBluetooth rxBluetooth;

        public void subscribe(RxBluetooth rxBluetooth) {
            this.rxBluetooth = rxBluetooth;
            this.rxBluetooth.subscribe(this);
        }

        public void stopScan() {
            unsubscribe(); // BluetoothSubscriber 호출!
        }
    }

    private final BluetoothManager manager;
    private final Context context;
    private Object scanner = null;

    private RxBluetooth(Context context, BluetoothSubscriber subscriber) {
        super(subscriber);
        this.context = context;
        this.manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static RxBluetooth create(Context context) {
        return new BluetoothSubscriber(context).rxBluetooth;
    }

    public void scan(RxScanner scanner) {
        scanner.subscribe(this);
    }

    private void startScan(Object o) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.manager.getAdapter().getBluetoothLeScanner().startScan((ScanCallback) o);
        } else {
            this.manager.getAdapter().startLeScan((BluetoothAdapter.LeScanCallback) o);
        }
        this.scanner = o;
    }

    private void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.manager.getAdapter().getBluetoothLeScanner().stopScan((ScanCallback) this.scanner);
        } else {
            this.manager.getAdapter().stopLeScan((BluetoothAdapter.LeScanCallback) this.scanner);
        }
    }

}
