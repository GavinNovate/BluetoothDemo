package cn.autoio.bluetoothdemo.blue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 蓝牙广播接收器
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private Bluetooth bluetooth = Bluetooth.get();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (bluetooth != null && intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    bluetooth.onStateChanged(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0));
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    bluetooth.onDiscoveryStarted();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    bluetooth.onDiscoveryFinished();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    bluetooth.onDeviceFound((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    bluetooth.onBondStateChanged((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    break;
            }
        }
    }
}
