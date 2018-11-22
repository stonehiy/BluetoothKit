package com.inuker.bluetooth.library.beacon;


import com.inuker.bluetooth.library.utils.AESUtil;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * Created by Administrator on 2018/3/16.
 * 充电盒蓝牙数据解码器
 */

public class BluetoothDataParserImpl implements BluetoothDataParser {
    private static final String TAG = BluetoothDataParserImpl.class.getName();
    /**
     * 缺省秘钥
     */
    private static byte[] DEFAULT_KEY = "EU-GPS-BLE-(BSB)".getBytes(Charset.forName("utf-8"));

    /**
     * 缺省向量
     */
    private static byte[] DEFAULT_IV = "EU-GPS-BLE-(BSB)".getBytes(Charset.forName("utf-8"));

    /**
     * 加解密的秘钥
     */
    private byte[] key;


    public BluetoothDataParserImpl(byte[] key) {
        this.key = key == null ? DEFAULT_KEY : key;
    }

    //测试中的解密，实际项目中不需要
    public byte[] decrypt(byte[] buffer, byte[] key, byte[] iv, int offset, int dataLength) {
        return AESUtil.getInstance().decrypt(buffer, key, iv, offset, dataLength);
    }

    /**
     * 解析密文和数据
     *
     * @param buffer
     * @return
     */
    private CommandResult parseData(byte[] buffer) {
        CommandResult result = new CommandResult();

        //先通过AES解f密, 从下标1，开始解密, 长度为整包减去头尾2字节
//        byte[] buffer = decrypt(data, key, DEFAULT_IV, 0, data.length);

        if (buffer == null) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setTypeDesc("数据解密失败");
            BluetoothLog.i(TAG + "解密数据失败 data - " + buffer);
            return result;
        }

        //判断头尾
        if (buffer[0] != (byte) 0xFB) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setTypeDesc("数据格式错误，起始位和结束位校验失败");
            return result;
        }

        //计算校验和
        byte checksum = 0;
        for (int i = 1; i < buffer.length - 1; i++) {
            checksum ^= buffer[i];
        }

        //判断校验码是否相等
        if (buffer[buffer.length - 1] != checksum) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setTypeDesc("校验码错误");
            return result;
        }

        //设置原始数据
        result.setResultBytes(buffer);


        //ByteBuffer 包裹， 以便后续处理
        ByteBuffer bb = ByteBuffer.wrap(buffer);

        //偏移第2个字节，读command
        bb.position(2);

        //指令
        short command = (short) (bb.get() & 0xFF);

        switch (command) {
            //首次鉴权
            case 0x31:
                result.setType(CommandResult.CommandType.AUTH);
                result.setTypeDesc("首次鉴权");
                byte authResult = bb.get();
                if (authResult == (byte) 0x01) {
                    result.setResult(true);
                    result.setDesc("首次授权成功");
                    byte[] autoCode = new byte[8];
                    bb.get(autoCode);
                    //按位取反
                    for (int i = 0; i < autoCode.length; i++) {
                        autoCode[i] = (byte) (~(autoCode[i]) ^ 0xA5);
                    }
                    result.setSecondCode(autoCode);
                } else {
                    result.setResult(false);
                    result.setDesc("首次授权失败");
                }
                break;

            //二次授权
            case 0x32:
                result.setType(CommandResult.CommandType.SECOND_AUTH);
                result.setTypeDesc("二次鉴权");
                byte secondAuthResult = bb.get();
                if (secondAuthResult == (byte) 0x01) {
                    result.setResult(true);
                    result.setDesc("二次鉴权成功");
                } else {
                    result.setResult(false);
                    result.setDesc("二次鉴权失败");
                }
                break;

            //APP下发锁操作命令--0x10
            /*
            参数：
            0xA1—启动充电
            0xA2—停止充电
            0xA3—启用离线自动充电
            0xA4—禁止离线自动充电
            */
            case 0x10:
                result.setType(CommandResult.CommandType.ILLEGAL_DATA);
                result.setTypeDesc("状态查询");
                result.setType(CommandResult.CommandType.ILLEGAL_DATA);
                result.setResult(true);
                break;
            default:
                result.setType(CommandResult.CommandType.ILLEGAL_DATA);
                result.setDesc("没有该命令");
                result.setResult(false);
                break;
        }

        return result;
    }

    @Override
    public CommandResult parseFromBytes(byte[] data) {

        //调试中，无时间戳
        int timeLen = 4;
        byte[] encryptedData = Arrays.copyOfRange(data, 0, data.length - timeLen);

        CommandResult commandResult = parseData(encryptedData);
        //以下解析数据

        if (commandResult != null && commandResult.getType() != CommandResult.CommandType.ILLEGAL_DATA) {
            //+4 加上四个字节的时候戳
            byte[] resultBytes = new byte[commandResult.getResultBytes().length + 4];
            //解密后数据
            System.arraycopy(commandResult.getResultBytes(), 0, resultBytes, 0, commandResult.getResultBytes().length);

            //时间戳, 调试中，无时间戳
            System.arraycopy(data, data.length - 4, resultBytes, resultBytes.length - 4, 4);

            //重新设置数据, 避免外部调用方，不同设备不同业务逻辑处理
            commandResult.setResultBytes(resultBytes);
        }
        return commandResult;
    }


    //加密
    public byte[] encrypt(byte[] buffer, byte[] key, byte[] iv, int offset, int dataLength) {
        return AESUtil.getInstance().encrypt(buffer, key, iv, offset, dataLength);
    }


    @Override
    public byte[] toBytes(byte command, byte[] params, int serialNum){


        int paramLen = (params == null ? 0 : params.length);
        byte[] data = new byte[4 + paramLen];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        //StartFlag
        buffer.put((byte) 0xFB);

        byte sn = (byte) ((serialNum << 4 & 0xF0) | ((2 + paramLen) & 0x0F));

        //序列号和包长度
        buffer.put(sn);

        //指令编号
        buffer.put(command);

        //如果参数不为空，则填充参数
        if (params != null) {
            buffer.put(params);
        }

        //计算校验和
        byte checksum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            checksum ^= data[i];
        }
        buffer.put(checksum);

        return data;

    }

    @Override
    public byte[] encodeToBytes(byte command, byte[] params, int serialNum) {

        int paramLen = (params == null ? 0 : params.length);
        byte[] data = new byte[4 + paramLen];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        //StartFlag
        buffer.put((byte) 0xFB);

        byte sn = (byte) ((serialNum << 4 & 0xF0) | ((2 + paramLen) & 0x0F));

        //序列号和包长度
        buffer.put(sn);

        //指令编号
        buffer.put(command);

        //如果参数不为空，则填充参数
        if (params != null) {
            buffer.put(params);
        }

        //计算校验和
        byte checksum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            checksum ^= data[i];
        }
        buffer.put(checksum);


        //以下数据加密
        byte[] encryptedData = encrypt(data, key, DEFAULT_IV, 0, data.length);
        return encryptedData;
    }

    @Override
    public int nextSerialNum(int serialNum) {
        return serialNum + 1 & 0x0F;
    }

}
