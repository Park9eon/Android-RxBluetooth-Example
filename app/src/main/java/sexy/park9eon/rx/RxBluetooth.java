package sexy.park9eon.rx;

import android.bluetooth.*;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

import java.util.UUID;

public final class RxBluetooth<T> extends Observable<T> {

    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static abstract class BluetoothSubscriber<T> extends Subscriber<T> {

        private RxBluetooth<T> rxBluetooth;

        private RxBluetooth<T> subscribe(RxBluetooth<T> rxBluetooth) {
            this.rxBluetooth = rxBluetooth;
            this.rxBluetooth.subscribe(this);
            return this.rxBluetooth;
        }

        public RxBluetooth<T> getRxBluetooth() {
            return this.rxBluetooth;
        }
    }

    public static class Result {

        private final BluetoothDevice device;
        private final int rssi;

        public Result(BluetoothDevice device, int rssi) {
            this.device = device;
            this.rssi = rssi;
        }

        public BluetoothDevice getDevice() {
            return device;
        }

        public int getRssi() {
            return rssi;
        }

    }

    private static final class RxScanner implements Observable.OnSubscribe<Result>, Action0 {

        private Object scanner;
        private RxBluetooth<Result> rxBluetooth;

        public RxScanner(Context context) {
            this.rxBluetooth = new RxBluetooth<>(context, this);
            this.rxBluetooth.doOnSubscribe(this);
        }

        @Override
        public void call(Subscriber<? super Result> subscriber) {
            if (rxBluetooth.isEnable()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.scanner = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            subscriber.onNext(new Result(result.getDevice(), result.getRssi()));
                        }
                    };
                } else {
                    this.scanner = (BluetoothAdapter.LeScanCallback) (bluetoothDevice, i, bytes) -> subscriber.onNext(new Result(bluetoothDevice, i));
                }
                this.rxBluetooth.startScan(this.scanner);
            } else {
                subscriber.onError(new Exception("Device not fount Exception"));
            }
        }

        // 종료시 발동
        @Override
        public void call() {
            // 종료처리
            this.rxBluetooth.stopScan();
        }
    }

    public static class RxConnector implements Observable.OnSubscribe<BluetoothGattCharacteristic>, Action0 {

        private RxBluetooth<android.bluetooth.BluetoothGattCharacteristic> rxBluetooth;
        private BluetoothGattCallback callback;
        private BluetoothDevice device;

        public RxConnector(Context context, BluetoothDevice device) {
            this.rxBluetooth = new RxBluetooth<>(context, this);
            this.device = device;
        }

        @Override
        public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
            callback = new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        rxBluetooth.bluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (rxBluetooth.bluetoothGatt != null) {
                            rxBluetooth.bluetoothGatt.close();
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        subscriber.onNext(characteristic);
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    subscriber.onNext(characteristic);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    subscriber.onNext(characteristic);
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        rxBluetooth.enableNotification();
                    }
                }
            };
            this.rxBluetooth.connect(this.device, callback);
        }

        @Override
        public void call() {
            // TODO
            this.rxBluetooth.disconnect();
        }
    }

    private final BluetoothManager manager;
    private final Context context;
    private Object scanner = null;
    private BluetoothGatt bluetoothGatt = null;

    private RxBluetooth(Context context, OnSubscribe<T> subscriber) {
        super(subscriber);
        this.context = context;
        this.manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static RxBluetooth<Result> scan(Context context, BluetoothSubscriber<Result> subscriber) {
        return subscriber.subscribe(new RxScanner(context).rxBluetooth);
    }

    protected boolean isEnable() {
        return this.manager != null && this.manager.getAdapter().isEnabled();
    }

    protected RxBluetooth<BluetoothGattCharacteristic> connect(BluetoothDevice device, BluetoothSubscriber<BluetoothGattCharacteristic> subscriber) {
        this.stopScan();
        return subscriber.subscribe(new RxConnector(context, device).rxBluetooth);
    }

    protected void connect(BluetoothDevice device, BluetoothGattCallback callback) {
        this.bluetoothGatt = device.connectGatt(this.context, false, callback);
        this.bluetoothGatt.connect();
    }

    protected void disconnect() {
        this.bluetoothGatt.disconnect();
    }

    private void startScan(Object o) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.manager.getAdapter().getBluetoothLeScanner().startScan((ScanCallback) o);
        } else {
            this.manager.getAdapter().startLeScan((BluetoothAdapter.LeScanCallback) o);
        }
        this.scanner = o;
    }

    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.manager.getAdapter().getBluetoothLeScanner().stopScan((ScanCallback) this.scanner);
        } else {
            this.manager.getAdapter().stopLeScan((BluetoothAdapter.LeScanCallback) this.scanner);
        }
    }

    public void sendMessage(String message) {
        this.sendMessage(message.getBytes());
    }

    public void sendMessage(byte[]message) {
        BluetoothGattService gattService = this.bluetoothGatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(RX_CHAR_UUID);
        characteristic.setValue(message);
        this.bluetoothGatt.writeCharacteristic(characteristic);
    }

    public void enableNotification() {
        BluetoothGattService service = this.bluetoothGatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(TX_CHAR_UUID);
        this.bluetoothGatt.setCharacteristicNotification(txCharacteristic, true);
        BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.bluetoothGatt.writeDescriptor(descriptor);
    }

}
