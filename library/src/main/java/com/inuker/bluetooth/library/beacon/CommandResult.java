package com.inuker.bluetooth.library.beacon;


public class CommandResult {

    /**
     * 枚举类，
     */
    public static enum CommandType {
        /**
         * 非法数据
         */
        ILLEGAL_DATA(0),

        /**
         * 首次授权
         */
        AUTH(1),

        /**
         * 第二次授权
         */
        SECOND_AUTH(2),

        /**
         * 启动充电
         */
        CHARGE_START_RES(3),

        /**
         * 停止充电
         */
        CHARGE_STOP_RES(4),

        /**
         * 启用离线自动充电
         */
        OFF_CHARGE_START(6),

        /**
         * 禁止离线自动充电
         */
        UNOFF_CHARGE_START(6);

        public final int code;

        private CommandType(int code) {
            this.code = code;
        }

    }


    private CommandType type;//0.非法数据 1.首次鉴权，2.二次鉴权，3.启动充电，4.停止充电,舍弃,5.启用离线自动充电,6.禁止离线自动充电
    private String typeDesc;
    private boolean result;
    private String desc;


    /**
     * 二次授权码
     */
    private byte[] secondCode;

    /**
     * 收到的蓝牙的全部数据，包括头尾
     */
    private byte[] resultBytes;

    /**
     * @return type
     */
    public CommandType getType() {
        return type;
    }

    /**
     * @param type 要设置的 type
     */
    public void setType(CommandType type) {
        this.type = type;
    }

    /**
     * @return typeDesc
     */
    public String getTypeDesc() {
        return typeDesc;
    }

    /**
     * @param typeDesc 要设置的 typeDesc
     */
    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    /**
     * @return result
     */
    public boolean isResult() {
        return result;
    }

    /**
     * @param result 要设置的 result
     */
    public void setResult(boolean result) {
        this.result = result;
    }

    /**
     * @return Desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param Desc 要设置的 Desc
     */
    public void setDesc(String Desc) {
        this.desc = Desc;
    }

    /**
     * @return secondCode
     */
    public byte[] getSecondCode() {
        return secondCode;
    }

    /**
     * @param secondCode 要设置的 secondCode
     */
    public void setSecondCode(byte[] secondCode) {
        this.secondCode = secondCode;
    }


    public byte[] getResultBytes() {
        return resultBytes;
    }

    public void setResultBytes(byte[] resultBytes) {
        this.resultBytes = resultBytes;
    }
}