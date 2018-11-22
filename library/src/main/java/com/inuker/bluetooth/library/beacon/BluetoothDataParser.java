package com.inuker.bluetooth.library.beacon;

/**
 * Created by Administrator on 2018/3/16.
 * 蓝牙数据解析、编码器
 */

public interface BluetoothDataParser {
    /**
     * 将数据解析成对象
     *
     * @param data
     * @return
     */
    CommandResult parseFromBytes(byte[] data);

    /**
     * 字节加密
     */
    byte[] encodeToBytes(byte command, byte[] params, int serialNum);

    /**
     * 将命令编码成字节
     *
     * @param command
     * @param params
     * @return
     */
    byte[] toBytes(byte command, byte[] params, int SerialNum);

    /**
     * 获取下一个流水号
     *
     * @return
     */
    int nextSerialNum(int serialNum);
}
