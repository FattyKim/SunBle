package com.clj.sunble;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.clj.sunble.callback.BleGattCallback;
import com.clj.sunble.callback.BleNotifyCallback;
import com.clj.sunble.callback.BleConnectNotifyImp;
import com.clj.sunble.data.BleDevice;
import com.clj.sunble.exception.BleException;
import com.clj.sunble.utils.HexUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sungege on 2018/11/19.
 */

public class BlueToothUtils {

    private final String TAG = "BlueToothUtils";

    private BleConnectNotifyImp mBleNotifyImp;


    public static BlueToothUtils getInstance() {
        return BlueToothUtils.BlueToothUtilsHolder.sBlueToothUtils;
    }

    private static class BlueToothUtilsHolder {
        private static final Handler mHandler = new Handler();
        private static final BlueToothUtils sBlueToothUtils = new BlueToothUtils();
    }

    public void applicationInit(Application app) {
        BleManager.getInstance().init(app);
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 3000)
                .setOperateTimeout(5000);
    }


    public void connect(BleConnectNotifyImp bleNotifyImp, final String... args) {
        if (bleNotifyImp == null)
            return;
        mBleNotifyImp = bleNotifyImp;
        //这里要判断MAC值存在不
        String s = "";
        if (!TextUtils.isEmpty(s)) {
            BleManager.getInstance().connect(s, new BleGattCallback() {
                @Override
                public void onStartConnect() {
                    Log.d(TAG, "start connect");
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.d(TAG, "connectFail NAME:" + bleDevice.getName() + "  MAC:" + bleDevice.getMac());
                    if (mBleNotifyImp != null)
                        mBleNotifyImp.onConnectStatus(
                                BleConnectNotifyImp.StringConstant.CONNECT_FAIL,
                                bleDevice.getMac(),
                                bleDevice.getName()
                        );
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.d(TAG, "connectSuccess NAME:" + bleDevice.getName() + "  MAC:" + bleDevice.getMac());
                    if (mBleNotifyImp != null)
                        mBleNotifyImp.onConnectStatus(
                                BleConnectNotifyImp.StringConstant.CONNECT_SUCCESS,
                                bleDevice.getMac(),
                                bleDevice.getName()
                        );
                    initNotify(bleDevice, Arrays.asList(args));
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.d(TAG, "connectDis NAME:" + bleDevice.getName() + "  MAC:" + bleDevice.getMac());
                    if (mBleNotifyImp != null)
                        mBleNotifyImp.onConnectStatus(
                                BleConnectNotifyImp.StringConstant.CONNECT_DIS,
                                bleDevice.getMac(),
                                bleDevice.getName()
                        );
                }
            });
        } else {
            //走扫描
        }
    }


    public void initNotify(final BleDevice b, final List<String> list) {

        BleManager.getInstance().notify(b, list.get(0), list.get(1), new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                Log.d(TAG, "onNotifySuccess master:" + list.get(0) + "  slave:" + list.get(1));
                if (mBleNotifyImp != null)
                    mBleNotifyImp.onNotifyStatus(true, list.get(1));
                list.remove(0);
                list.remove(0);
                if (list.size() > 0)
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initNotify(b, list);
                        }
                    }, 400);

            }

            @Override
            public void onNotifyFailure(BleException exception) {
                Log.d(TAG, "onNotifyFailure master:" + list.get(0) + "  slave:" + list.get(1));
                if (mBleNotifyImp != null)
                    mBleNotifyImp.onNotifyStatus(false, list.get(1));
            }

            @Override
            public void onCharacteristicChanged(byte[] data) {
                Log.d(TAG, "onCharacteristicChanged master:" + list.get(0) + "  slave:" + list.get(1));
                Log.d(TAG, list.get(0) + "--" + list.get(1) + " data:" + HexUtil.formatHexString(data));
                if (mBleNotifyImp != null)
                    mBleNotifyImp.onNotifyData(list.get(1), list.get(1));
            }
        });
    }


    public void missAllConnect() {
        BleManager.getInstance().disconnectAllDevice();
    }


}
