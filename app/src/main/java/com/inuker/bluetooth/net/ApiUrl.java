package com.inuker.bluetooth.net;

public class ApiUrl {

    public static final String BASE_URL_DEV = "http://test1.qhebusbar.net:90/";
    public static final String BASE_URL = "http://api.qhebusbar.com/";

    //远程充电桩开启
    //api/ChargingData/ChargAskStart
    // (string epilecode_in, string epilepoint_in, string controlType_in, string limitdata_in, string eusercardNo_in, string fixTime_in = "00000000")
    public static final String API_CHARGE_START = BASE_URL_DEV + "api/ChargingData/ChargAskStart";
    /// 远程充电桩关闭
    //api/ChargingData/EpileClose(string rid, string membercode)
    public static final String API_CHARGE_STOP = BASE_URL_DEV + "api/ChargingData/EpileClose";

}
