/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inuker.bluetooth.library;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.ClassicResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class ClassicBluetoothClient {
    // Debugging
    private static final String TAG = ClassicBluetoothClient.class.getName();

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";

    // Unique UUID for this application
//    private static final UUID MY_UUID_SECURE =
//            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID MY_UUID_INSECURE =
//            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // Member fields
    private final BluetoothAdapter mAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private ClassicResponse mConResponse;
    private ClassicResponse mReadResponse;
    private ClassicResponse mWriteResponse;
    private BleConnectStatusListener mBleConnectStatusListener;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantsClassic.CLASSIC_CON_SECCESS:
                    if (null != mConResponse) {
                        mConResponse.onResponse(ConstantsClassic.CLASSIC_CON_SECCESS, msg.obj);
                    }
                    break;
                case ConstantsClassic.CLASSIC_CON_FAILED:
                    if (null != mConResponse) {
                        mConResponse.onResponse(ConstantsClassic.CLASSIC_CON_FAILED, msg.obj);
                    }
                    break;

                case ConstantsClassic.MESSAGE_READ:
                    if (null != mReadResponse) {
                        byte[] buf = (byte[]) msg.obj;
                        byte[] bytes = Arrays.copyOf(buf, msg.arg1);
//                        String readMessage = new String(readBuf, 0, msg.arg1);
                        mReadResponse.onResponse(ConstantsClassic.MESSAGE_READ, bytes);
                    }
                    break;
                case ConstantsClassic.MESSAGE_WRITE:
                    if (null != mWriteResponse) {
                        mWriteResponse.onResponse(ConstantsClassic.MESSAGE_WRITE, msg.obj);
                    }
                    break;
                case ConstantsClassic.MESSAGE_STATE_CHANGE:
                    if (null != mBleConnectStatusListener) {
                        if (null == mRemoteDevice) {
                            return;
                        }
//                        public static final int STATUS_CONNECTED = 0x10;
//                        public static final int STATUS_DISCONNECTED = 0x20;
                        if (3 == msg.arg1) {
                            mBleConnectStatusListener.onConnectStatusChanged(mRemoteDevice.getAddress(), 0x10);
                        } else if (0 == msg.arg1) {
                            mBleConnectStatusListener.onConnectStatusChanged(mRemoteDevice.getAddress(), 0x20);
                        }

                    }
                    break;
            }

        }
    };
    private BluetoothDevice mRemoteDevice;

    public BluetoothDevice getRemoteDevice() {
        return mRemoteDevice;
    }

    public ClassicBluetoothClient() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    public void setConResponse(ClassicResponse response) {
        this.mConResponse = response;

    }

    public void setReadResponse(ClassicResponse response) {
        this.mReadResponse = response;

    }

    public void setWriteResponse(ClassicResponse response) {
        this.mWriteResponse = response;

    }

    public void setBleConnectStatusListener(BleConnectStatusListener bleConnectStatusListener) {
        this.mBleConnectStatusListener = bleConnectStatusListener;

    }


    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        BluetoothLog.i(TAG + "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(ConstantsClassic.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        BluetoothLog.i(TAG + "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_LISTEN);

    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect(String address) {
        mRemoteDevice = mAdapter.getRemoteDevice(address);
        BluetoothLog.i(TAG + "connect to: " + mRemoteDevice.getName());

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(mRemoteDevice);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(ConstantsClassic.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantsClassic.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        BluetoothLog.i(TAG + "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                if (null != mWriteResponse) {
                    mWriteResponse.onResponse(ConstantsClassic.CLASSIC_CON_FAILED, null);
                }
                return;
            }
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(ConstantsClassic.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantsClassic.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
//        ClassicBluetoothClient.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(ConstantsClassic.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantsClassic.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_NONE);

        // Start the service over to restart listening mode
//        ClassicBluetoothClient.this.start();
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            BluetoothLog.i(TAG + "BEGIN mConnectThread S");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                if (null != mHandler) {
                    mHandler.obtainMessage(ConstantsClassic.CLASSIC_CON_SECCESS, null)
                            .sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (null != mHandler) {
                    mHandler.obtainMessage(ConstantsClassic.CLASSIC_CON_FAILED, null)
                            .sendToTarget();
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (ClassicBluetoothClient.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            BluetoothLog.i(TAG + "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    int bytes1 = mmInStream.read(new byte[]{});
                    bytes = mmInStream.read(buffer);
                    Log.i(TAG, "read bytes = " + bytes);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(ConstantsClassic.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    // Start the service over to restart listening mode
//                    ClassicBluetoothClient.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(ConstantsClassic.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
