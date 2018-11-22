package com.inuker.bluetooth.library.beacon;

import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.nio.ByteBuffer;

public class Test {

    public static void main(String[] str) {
        String s = "640B3101080706050403020133";
        byte[] bytes = ByteUtils.stringToBytes(s);
        CommandResult commandResult = parseData(bytes);

    }

    /**
     * 解析数据
     *
     * @param buffer
     * @return
     */
    public static CommandResult parseData(byte[] buffer) {
        CommandResult result = new CommandResult();

        //先通过AES解f密, 从下标1，开始解密, 长度为整包减去头尾2字节
//        byte[] buffer = decrypt(data, key, DEFAULT_IV, 0, data.length);

        if (buffer == null) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setTypeDesc("数据解密失败");
            System.out.println("解密数据失败 data - " + buffer);
            return result;
        }

        //判断头尾
        if (buffer[0] != (byte) 0x64) {
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
}
