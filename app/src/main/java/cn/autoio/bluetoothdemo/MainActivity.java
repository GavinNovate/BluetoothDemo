package cn.autoio.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import java.util.Set;

import cn.autoio.bluetoothdemo.blue.Bluetooth;
import cn.autoio.bluetoothdemo.grant.PermissionsManager;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 主界面 负责打开蓝牙，搜索设备，连接设备
 * 资料：http://www.jianshu.com/p/fc46c154eb77
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Toolbar toolbar;
    private RelativeLayout switchLayout;
    private Switch switchView;
    private LinearLayout contentLayout;

    private Bluetooth bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 请求全部权限
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, null);

        bluetooth = Bluetooth.get();

        initView();
        Log.d(TAG, "onCreate: ");
    }

    private void initView() {
        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // View
        switchLayout = (RelativeLayout) findViewById(R.id.switchLayout);
        switchView = (Switch) findViewById(R.id.switchView);
        contentLayout = (LinearLayout) findViewById(R.id.contentLayout);

        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_main);

        switchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchView.isChecked()) {
                    bluetooth.turnOff();
                } else {
                    bluetooth.turnOn();
                }
            }
        });

        toolbar.getMenu().getItem(0).setEnabled(bluetooth.getState() == BluetoothAdapter.STATE_ON);
        switchView.setChecked(bluetooth.getState() == BluetoothAdapter.STATE_ON);
        contentLayout.setVisibility(bluetooth.getState() == BluetoothAdapter.STATE_ON ? View.VISIBLE : View.GONE);
        if (bluetooth.getState() == BluetoothAdapter.STATE_ON) {
            blueOpened();
        }

        bluetooth
                .observeState()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer state) throws Exception {
                        toolbar.getMenu().getItem(0).setEnabled(state == BluetoothAdapter.STATE_ON);
                        switchView.setChecked(state == BluetoothAdapter.STATE_ON);
                        contentLayout.setVisibility(state == BluetoothAdapter.STATE_ON ? View.VISIBLE : View.GONE);
                        if (state == BluetoothAdapter.STATE_ON) {
                            blueOpened();
                        }
                    }
                });

        toolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toolbar.getMenu().getItem(0).setEnabled(false);
                bluetooth.startDiscovery()
                        .subscribe(new Observer<BluetoothDevice>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(BluetoothDevice device) {
                                Log.d(TAG, "onNext: " + device.getName() + " -- " + device.getAddress());
//                                if (device.getAddress().equals("38:BC:1A:33:3F:00")) {
                                if (device.getAddress().equals("00:08:2A:F0:14:B9")) {

                                    final String TAG = "bond";
                                    bluetooth.bond(device).subscribe(new Observer<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            Log.d(TAG, "onSubscribe: ");
                                        }

                                        @Override
                                        public void onNext(Integer value) {
                                            switch (value) {
                                                case BluetoothDevice.BOND_BONDED:
                                                    Log.d(TAG, "onNext: BOND_BONDED");
                                                    break;
                                                case BluetoothDevice.BOND_BONDING:
                                                    Log.d(TAG, "onNext: BOND_BONDING");
                                                    break;
                                                case BluetoothDevice.BOND_NONE:
                                                    Log.d(TAG, "onNext: BOND_NONE");
                                                    break;
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            Log.d(TAG, "onComplete: ");
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: ");
                                toolbar.getMenu().getItem(0).setEnabled(true);
                            }
                        });
                return true;
            }
        });
    }

    private void blueOpened() {
        Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                Log.d(TAG, "blueOpened: " + device.getName() + " -- " + device.getAddress());
            }
        }
//        bluetooth.listen();
    }
}
