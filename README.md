# Android-RxBluetooth-Example
> Android Nordic bluetooth reactive connect Example

## What is?
안드로이드 노르딕 디바이스 블루투스연결을 RxJava를 이용하여 함수형으로 사용할 수 있게 하였습니다.

## Gradle Setting

1. **project build.gradle**

  ```gradle
  dependencies {
    classpath 'com.android.tools.build:gradle:1.5.0'
    classpath 'me.tatarka:gradle-retrolambda:3.2.5' 
  }
  ```
  
2. **app build.gradle**

  ```gradle
  apply plugin: 'me.tatarka.retrolambda'
  ....
  android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
  ....
  }
  dependencies {
    compile 'io.reactivex:rxandroid:1.1.0'
  }
  ```
  
## 설명
```java
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
          } else {
              if (result.getDevice().getName().equals("@{some name}")) {
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
```
## 버그
1. onCharacteristicChanged not called
  **해결책**
  > Service Discovered부분에 위 코드를 추가한다.

  ```java
  public void enableNotification() {
        BluetoothGattService service = this.bluetoothGatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic txCharacteristic = service.getCharacteristic(TX_CHAR_UUID);
        this.bluetoothGatt.setCharacteristicNotification(txCharacteristic, true);
        BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.bluetoothGatt.writeDescriptor(descriptor);
  }
  ```
  

## TODO
1. 에러처리
2. Subscriber value 세분화
3. 메모리관리
