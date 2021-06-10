package com.inuker.bluetooth.library.beacon;


import com.inuker.bluetooth.library.utils.AESUtil;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


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
     * 解析数据
     *
     * @param buffer
     * @return
     */
    private CommandResult parseData(byte[] buffer) {
        CommandResult result = new CommandResult();
        result.setResultBytes(buffer);

        //先通过AES解f密, 从下标1，开始解密, 长度为整包减去头尾2字节
//        byte[] buffer = decrypt(data, key, DEFAULT_IV, 0, data.length);

        if (buffer == null) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setDesc("数据为空");
            BluetoothLog.i(TAG + "数据为空");
            return result;
        }

        //判断头尾
        if (buffer[0] != (byte) 0x64) {
            result.setType(CommandResult.CommandType.ILLEGAL_DATA);
            result.setDesc("数据格式错误");
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
            result.setDesc("校验码错误");
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
                byte resultType = bb.get();
                byte resultCode = bb.get();
                byte errorCode = bb.get();
                if (resultType == (byte) 0xA1) {
                    result.setType(CommandResult.CommandType.CHARGE_START_RES);
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("启动充电成功");
                        result.setTypeDesc("启动充电");
                    } else {
                        /*
                         0001（失败）系统正在充电
                         0002（失败）系统故障
                         0003（失败）请确认枪连接
                         0004（失败）离线自动充电已开启
                         */
                        result.setResult(false);
                        if (0x01 == errorCode) {
                            result.setDesc("启动充电失败,充电盒正在充电中");
                        } else if (0x02 == errorCode) {
                            result.setDesc("启动充电失败,充电盒故障");
                        } else if (0x03 == errorCode) {
                            result.setDesc("启动充电失败,请确认枪是否连接");
                        } else if (0x04 == errorCode) {
                            result.setDesc("启动充电失败,离线自动充电已开启");
                        } else {
                            result.setDesc("启动充电失败");
                        }
                        result.setTypeDesc("启动充电");
                    }
                } else if (resultType == (byte) 0xA2) {
                    result.setType(CommandResult.CommandType.CHARGE_STOP_RES);
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("停止充电成功");
                        result.setTypeDesc("停止充电");
                    } else {
                        /*
                          0001（失败） 离线自动充电已开启
                          0002（失败） 系统处于空闲状态
                         */
                        result.setResult(false);
                        if (0x01 == errorCode) {
                            result.setDesc("停止充电失败,离线自动充电已开启");
                        } else if (0x02 == errorCode) {
                            result.setDesc("停止充电失败,充电盒处于空闲状态");
                        } else {
                            result.setDesc("停止充电失败");
                        }

                        result.setTypeDesc("停止充电");
                    }
                } else if (resultType == (byte) 0xA3) {
                    result.setType(CommandResult.CommandType.OFF_CHARGE_START);
                    result.setTypeDesc("启用离线自动充电");
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("启用离线自动充电成功");
                    } else {
                        result.setResult(false);
                        result.setDesc("启用离线自动充电失败，充电盒正在充电中");
                    }

                } else if (resultType == (byte) 0xA4) {
                    result.setType(CommandResult.CommandType.UNOFF_CHARGE_START);
                    result.setTypeDesc("设置电桩号");
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("禁止离线自动充电成功");
                    } else {
                        result.setResult(false);
                        result.setDesc("禁止离线自动充电失败");
                    }

                } else if (resultType == (byte) 0xA5) {
                    result.setType(CommandResult.CommandType.SET_PILE_NO);
                    result.setTypeDesc("设置电桩号");
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("设置电桩号成功");
                    } else {
                        result.setResult(false);
                        result.setDesc("设置电桩号失败");
                    }
                } else if (resultType == (byte) 0xA6) {
                    result.setType(CommandResult.CommandType.SET_PILE_POWER);
                    result.setTypeDesc("设置功率");
                    if (0x01 == resultCode) {
                        result.setResult(true);
                        result.setDesc("设置功率成功");
                    } else {
                        result.setResult(false);
                        result.setDesc("设置功率失败");
                    }
                }
                break;
            //APP下发获取设备命令--0x11
            /*
            参数：
            0xA1—桩编号
            0xA2—桩供应商信息
            0xA3—额定功率
            0xA4—硬件版本
            0xA5-软件版本
            0xA6-bootloader软件版本
            */
            case 0x11:
                byte ry = bb.get();
                byte rc = bb.get();
                if (ry == (byte) 0xA1) {
                    result.setType(CommandResult.CommandType.GET_PILE_NO);
                    result.setTypeDesc("获取桩编号");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        result.setDesc("桩编号：" + ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取桩编号失败");
                    }
                } else if (ry == (byte) 0xA2) {
                    result.setType(CommandResult.CommandType.GET_PILE_COMPANY);
                    result.setTypeDesc("获取桩供应商信息");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        result.setDesc("桩供应商信息：" +  ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取桩供应商信息失败");
                    }
                } else if (ry == (byte) 0xA3) {
                    result.setType(CommandResult.CommandType.GET_PILE_POWER);
                    result.setTypeDesc("获取额定功率");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        result.setDesc("额定功率：" +  ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取额定功率失败");
                    }
                } else if (ry == (byte) 0xA4) {
                    result.setType(CommandResult.CommandType.GET_PILE_HARDWARE_VERSION);
                    result.setTypeDesc("获取硬件版本");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        result.setDesc("硬件版本：" +  ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取硬件版本失败");
                    }
                } else if (ry == (byte) 0xA5) {
                    result.setType(CommandResult.CommandType.GET_PILE_SOFTWARE_VERSION);
                    result.setTypeDesc("获取软件版本");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        bf.flip();
                        result.setDesc("软件版本：" +  ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取软件版本失败");
                    }
                } else if (ry == (byte) 0xA6) {
                    result.setType(CommandResult.CommandType.GET_PILE_BOOTLOADER_VERSION);
                    result.setTypeDesc("获取bootloader软件版本");
                    if (rc == 0x01) {
                        result.setResult(true);
                        byte[] body = new byte[buffer.length - 6];
                        ByteBuffer bf = bb.get(body, 0, body.length);
                        result.setDesc("bootloader软件版本：" +  ByteUtils.byteToHexString(body));
                    } else {
                        result.setResult(false);
                        result.setDesc("获取bootloader软件版本失败");
                    }
                }
                break;
            default:
                result.setType(CommandResult.CommandType.ILLEGAL_DATA);
                result.setDesc("没有该命令");
                result.setResult(false);
                break;
        }

        return result;
    }

    private String byteBuffer2String(ByteBuffer buffer) {
        {
            Charset charset = null;
            CharsetDecoder decoder = null;
            CharBuffer charBuffer = null;
            try {
                charset = Charset.forName("UTF-8");
                decoder = charset.newDecoder();
                // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
                charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
                return charBuffer.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }
        }
    }

    @Override
    public CommandResult parseFromBytes(byte[] data) {

        //调试中，无时间戳
//        int timeLen = 4;
//        byte[] encryptedData = Arrays.copyOfRange(data, 0, data.length - timeLen);

        CommandResult commandResult = parseData(data);
        BluetoothLog.i("BluetoothDataParserImpl commandResult desc = " + commandResult.getDesc());
        //以下解析数据
        /*
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
        */
        return commandResult;
    }


    //加密
    public byte[] encrypt(byte[] buffer, byte[] key, byte[] iv, int offset, int dataLength) {
        return AESUtil.getInstance().encrypt(buffer, key, iv, offset, dataLength);
    }


    @Override
    public byte[] toBytes(byte command, byte[] params, int serialNum) {


        int paramLen = (params == null ? 0 : params.length);
        byte[] data = new byte[4 + paramLen];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        //StartFlag
        buffer.put((byte) 0x64);

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
        buffer.put((byte) 0x64);

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
