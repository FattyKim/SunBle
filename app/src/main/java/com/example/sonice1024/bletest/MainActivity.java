package com.example.sonice1024.bletest;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.sonice1024.bletest.databinding.ActivityMainBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;


import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding bind;
    private BleDevice mBleDevice;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 3000)
                .setOperateTimeout(5000);

    }

    public void showPermission(final String address) {
        if (Build.VERSION.SDK_INT >= 23) {
            RxPermissions rxPermissions = new RxPermissions(this);
            // 添加所需权限
            rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                Toast.makeText(MainActivity.this, "同意权限 ", Toast.LENGTH_SHORT).show();
                                connect(address);
                            } else {
                                Toast.makeText(MainActivity.this, "拒绝权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            connect(address);
        }
    }


    public void connectBle(View v) {
        showPermission("CC:78:AB:A2:55:AD");
    }


    private void connect(String s) {

        BleManager.getInstance().connect(s, new BleGattCallback() {
            @Override
            public void onStartConnect() {
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                bind.setStatus("连接失败啦~!");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mBleDevice = bleDevice;
                initNotify(bleDevice);
                bind.setStatus("连接成功啦~!");
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bind.setStatus("连接中断啦~!");
            }
        });
    }


    private void initNotify(final BleDevice bleDevice) {

        BleManager.getInstance().notify(
                bleDevice,
                "0000180D-0000-1000-8000-00805f9b34fb",
                "00002A37-0000-1000-8000-00805f9b34fb",
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        bind.setStatusHeart("打开心率通知操作成功");
                        notifyPower(bleDevice);
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        bind.setStatusHeart("打开心率通知操作失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        count++;
                        bind.setHeartCount(count + "");
                        bind.setHeartValue(HexStrToBytesUtils.bytesToHexString(data));
                    }
                });


    }

    private void notifyPower(final BleDevice bleDevice) {

        BleManager.getInstance().notify(
                bleDevice,
                "0000180F-0000-1000-8000-00805f9b34fb",
                "00002A19-0000-1000-8000-00805f9b34fb",
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        bind.setStatusPower("打开电量通知操作成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        bind.setStatusPower("打开心率通知操作失败");
                        Log.d("MainActivity", exception.getDescription());
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        bind.setPowerValue(HexStrToBytesUtils.bytesToHexString(data));
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        count = 0;
        BleManager.getInstance().disconnect(mBleDevice);
    }
}
