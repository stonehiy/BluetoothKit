package com.inuker.bluetooth.library.utils;

import java.lang.reflect.Method;

/**
 * Created by dingjikerbo on 17/5/6.
 */

public class ReflectUtils {

    public static Method getMethod(Class<?> clazz, Method method) {
        Method remote = null;
        try {
            remote = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            remote.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return remote;
    }
}
