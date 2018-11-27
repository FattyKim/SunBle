package com.example.sonice1024.bletest;

import android.bluetooth.BluetoothGatt;
import android.databinding.DataBindingUtil;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.clj.sunble.BleManager;
import com.clj.sunble.callback.BleConnectNotifyImp;
import com.clj.sunble.callback.BleNotifyCallback;
import com.clj.sunble.callback.BleScanCallback;
import com.clj.sunble.callback.BleWriteCallback;
import com.clj.sunble.data.BleDevice;
import com.clj.sunble.exception.BleException;
import com.clj.sunble.scan.BleScanRuleConfig;
import com.example.sonice1024.bletest.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        BleManager.getInstance().disconnectAllDevice();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        BleManager.getInstance().disconnectAllDevice();
        super.onBackPressed();
    }

    private void connect(String value) {
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().startConnect(value, new BleConnectNotifyImp() {
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bind.setStatus("连接成功啦~~" + bleDevice.getMac());
                initNotify(bleDevice);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                bind.setStatus("连接失败啦~~" + bleDevice.getMac());
            }

            @Override
            public void onConnectDis(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                bind.setStatus("连接中断啦~~" + device.getMac());
            }
        });
    }

    public void connectBle(View v) {
        String value = BleManager.getInstance().getSpMac();
        if (!TextUtils.isEmpty(value))
            connect(value);

    }

    public void scan(View v) {


        if (!BleManager.getInstance().isBlueEnable())
            return;
        BleManager.getInstance().disconnectAllDevice();
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(5000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {


                //创建布局管理
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                bind.recyclerView.setLayoutManager(layoutManager);

                //创建适配器
                MyAdapter adapter = new MyAdapter(R.layout.item, scanResultList);
                adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        List<BleDevice> list = adapter.getData();
                        connect(list.get(position).getMac());
                    }
                });
                //给RecyclerView设置适配器
                bind.recyclerView.setAdapter(adapter);

            }
        });
    }

    private void initNotify(final BleDevice bleDevice) {

        BleManager.getInstance().notify(
                bleDevice,
                "0000ffe0-0000-1000-8000-00805f9b34fb",
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        bind.setStatusData("打开通知操作成功");
                        initWrite(bleDevice);
                    }


                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        bind.setStatusData("打开通知操作失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        bind.setDataValue(HexStrToBytesUtils.bytesToHexString(data));
                    }
                });
    }

    private void initWrite(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                "0000ffe0-0000-1000-8000-00805f9b34fb",
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                "UIH".getBytes(),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                    }
                });
    }

    public class MyAdapter extends BaseQuickAdapter<BleDevice, BaseViewHolder> {

        public MyAdapter(@LayoutRes int layoutResId, @Nullable List<BleDevice> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, BleDevice item) {
            //可链式调用赋值
            helper.setText(R.id.name, item.getName())
                    .setText(R.id.address, item.getMac());
            //获取当前条目position
            //int position = helper.getLayoutPosition();
        }
    }


}
