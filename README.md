# Android-RxBluetooth-Example
> Android Nordic bluetooth reactive connect Example

## What is?
안드로이드 노르딕 디바이스 블루투스연결을 RxJava를 이용하여 함수형으로 사용할 수 있게 하였습니다.

## 초기설정

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
  }
