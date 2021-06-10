package com.qhebusbar.base;

import com.inuker.bluetooth.library.beacon.BluetoothDataParserImpl;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.utils.ByteUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_ble() {
        String str = "640C11A1010755000000005555EF";
        BluetoothDataParserImpl bluetoothDataParser = new BluetoothDataParserImpl(null);
        CommandResult cr =  bluetoothDataParser.parseFromBytes(ByteUtils.hexToByteArray(str));
        System.out.println("cr = "+ cr);
    }
}