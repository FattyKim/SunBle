package com.clj.sunble.callback;


public interface BleConnectNotifyImp {

    interface StringConstant {
        public static int CONNECT_SUCCESS = 1;
        public static int CONNECT_DIS = 2;
        public static int CONNECT_FAIL = 3;
    }

    void onConnectStatus(int status, String mac, String name);

    void onNotifyStatus(boolean b, String uuid);

    void onNotifyData(String data, String uuid);


}
