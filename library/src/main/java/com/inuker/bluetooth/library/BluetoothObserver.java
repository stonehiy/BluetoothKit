package com.inuker.bluetooth.library;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.receiver.BluetoothReceiver;
import com.inuker.bluetooth.library.receiver.listener.BleCharacterChangeListener;
import com.inuker.bluetooth.library.receiver.listener.BleConnectStatusChangeListener;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondStateChangeListener;
import com.inuker.bluetooth.library.receiver.listener.BluetoothStateChangeListener;
import com.inuker.bluetooth.library.utils.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by dingjikerbo on 17/4/25.
 */

public abstract class BluetoothObserver implements IBluetoothObserver {

//	private HashMap<String, HashMap<String, List<BleNotifyResponse>>> mNotifyResponses;
//	private HashMap<String, List<BleConnectStatusListener>> mConnectStatusListeners;
//	private List<BluetoothStateListener> mBluetoothStateListeners;
//	private List<BluetoothBondListener> mBluetoothBondListeners;
//
//	@Override
//	public void registerConnectStatusListener(String mac, BleConnectStatusListener listener) {
//
//	}
//
//	@Override
//	public void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener) {
//
//	}
//
//	@Override
//	public void registerBluetoothStateListener(BluetoothStateListener listener) {
//
//	}
//
//	@Override
//	public void unregisterBluetoothStateListener(BluetoothStateListener listener) {
//
//	}
//
//	@Override
//	public void registerBluetoothBondListener(BluetoothBondListener listener) {
//
//	}
//
//	@Override
//	public void unregisterBluetoothBondListener(BluetoothBondListener listener) {
//
//	}
//
//	private void registerBluetoothReceiver() {
//		BluetoothReceiver.getInstance().register(new BluetoothStateChangeListener() {
//			@Override
//			protected void onBluetoothStateChanged(int prevState, int curState) {
//				dispatchBluetoothStateChanged(curState);
//			}
//		});
//		BluetoothReceiver.getInstance().register(new BluetoothBondStateChangeListener() {
//			@Override
//			protected void onBondStateChanged(String mac, int bondState) {
//				dispatchBondStateChanged(mac, bondState);
//			}
//		});
//		BluetoothReceiver.getInstance().register(new BleConnectStatusChangeListener() {
//			@Override
//			protected void onConnectStatusChanged(String mac, int status) {
//				if (status == Constants.STATUS_DISCONNECTED) {
//					clearNotifyListener(mac);
//				}
//				dispatchConnectionStatus(mac, status);
//			}
//		});
//		BluetoothReceiver.getInstance().register(new BleCharacterChangeListener() {
//			@Override
//			public void onCharacterChanged(String mac, UUID service, UUID character, byte[] value) {
//				dispatchCharacterNotify(mac, service, character, value);
//			}
//		});
//	}
//
//	private void dispatchCharacterNotify(String mac, UUID service, UUID character, byte[] value) {
//		HashMap<String, List<BleNotifyResponse>> notifyMap = mNotifyResponses.get(mac);
//		if (notifyMap != null) {
//			String key = generateCharacterKey(service, character);
//			List<BleNotifyResponse> responses = notifyMap.get(key);
//			if (responses != null) {
//				for (final BleNotifyResponse response : responses) {
//					response.onNotify(service, character, value);
//				}
//			}
//		}
//	}
//
//	private void dispatchConnectionStatus(final String mac, final int status) {
//		List<BleConnectStatusListener> listeners = mConnectStatusListeners.get(mac);
//		if (!ListUtils.isEmpty(listeners)) {
//			for (final BleConnectStatusListener listener : listeners) {
//				listener.invokeSync(mac, status);
//			}
//		}
//	}
//
//	private void dispatchBluetoothStateChanged(final int currentState) {
//		if (currentState == Constants.STATE_OFF || currentState == Constants.STATE_ON) {
//			for (final BluetoothStateListener listener : mBluetoothStateListeners) {
//				listener.invokeSync(currentState == Constants.STATE_ON);
//			}
//		}
//	}
//
//	private void dispatchBondStateChanged(final String mac, final int bondState) {
//		for (final BluetoothBondListener listener : mBluetoothBondListeners) {
//			listener.invokeSync(mac, bondState);
//		}
//	}
}
