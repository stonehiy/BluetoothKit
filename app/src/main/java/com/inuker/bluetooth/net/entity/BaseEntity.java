package com.inuker.bluetooth.net.entity;

import java.io.Serializable;
import java.util.List;

public class BaseEntity implements Serializable {
    public int code;
    public String message;
    public Object data;
    public Object list;

    public boolean ok() {
        return code == 3000;
    }
}
