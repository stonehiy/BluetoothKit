package com.inuker.bluetooth.library.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-CBC加解密算法
 *
 * @author Admin
 */
public class AESUtil {

    private volatile static AESUtil instance;


    public static AESUtil getInstance() {
        if (instance == null) {
            synchronized (AESUtil.class) {
                if (instance == null) {
                    instance = new AESUtil();
                }
            }
        }
        return instance;
    }

    //加密
    public byte[] encrypt(byte[] buffer, byte[] key, byte[] iv) {
        return encrypt(buffer, key, iv, 0, buffer.length);
    }

    //加密
    public byte[] encrypt(byte[] buffer, byte[] key, byte[] iv, int offset, int len) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, spec, ivParameterSpec);
            return cipher.doFinal(buffer, offset, len);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    //解密
    public byte[] decrypt(byte[] buffer, byte[] key, byte[] iv) {
        return decrypt(buffer, key, iv, 0, buffer.length);
    }


    //解密
    public byte[] decrypt(byte[] buffer, byte[] key, byte[] iv, int offset, int len) {
        try {
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, spec, ivParameterSpec);
            return cipher.doFinal(buffer, offset, len);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
