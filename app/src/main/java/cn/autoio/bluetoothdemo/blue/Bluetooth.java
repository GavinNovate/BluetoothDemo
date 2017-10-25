package cn.autoio.bluetoothdemo.blue;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by gavin on 17-10-25.
 */

/**
 * 1.获取蓝牙，不支持返回NUll #
 * 2.获取蓝牙状态 观察蓝牙状态 打开蓝牙 关闭蓝牙 #
 * 3.获取已配对列表 #
 * 4.扫描附近蓝牙设备 取消扫描附近设备 #
 * 5.发起配对请求 监听配对结果
 * 7.发起蓝牙连接
 * 8.监听接收到的蓝牙连接
 * 9.发送数据
 * 10.观察接收到的数据
 */
public class Bluetooth {

    private static final String TAG = "Bluetooth";

    private BluetoothAdapter bluetoothAdapter;

    // 蓝牙状态监听器
    private Subject<State> blueStateSubject;
    // 蓝牙设备发现监听器
    private Subject<BluetoothDevice> blueDeviceSubject;

    private HashMap<BluetoothDevice, Subject<Integer>> bondStateMap;

    private Bluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueStateSubject = PublishSubject.create();
        bondStateMap = new HashMap<>();
    }

    /**
     * 获取蓝牙
     *
     * @return 蓝牙, 或者为空表示该设备不支持蓝牙
     */
    @Nullable
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public static Bluetooth get() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return null;
        }
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取蓝牙状态
     *
     * @return 蓝牙状态
     */
    public State getState() {
        return convertState(bluetoothAdapter.getState());
    }

    /**
     * 观察蓝牙状态
     *
     * @return 蓝牙状态观察对象
     */
    public Observable<State> observeState() {
        return blueStateSubject;
    }

    /**
     * 打开蓝牙
     *
     * @return 是否操作成功（操作成功不代表蓝牙打开成功，蓝牙状态需要另外监听）
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public boolean turnOn() {
        return bluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     *
     * @return 是否操作成功（操作成功不代表蓝牙关闭成功，蓝牙状态需要另外监听）
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public boolean turnOff() {
        return bluetoothAdapter.disable();
    }

    /**
     * 获取已配对的蓝牙列表
     *
     * @return 已配对的蓝牙列表, 或者为空表示出错
     */
    @Nullable
    public Set<BluetoothDevice> getBondedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    /**
     * 开始扫描附近的设备
     *
     * @return 是否操作成功
     */
    public Observable<BluetoothDevice> startDiscovery() {
        blueDeviceSubject = PublishSubject.create();
        return blueDeviceSubject.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                if (!bluetoothAdapter.startDiscovery() && !disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
        });
    }

    /**
     * 取消扫描附近设备
     *
     * @return 是否操作成功
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    public boolean cancelDiscovery() {
        return bluetoothAdapter.cancelDiscovery();
    }

    /**
     * 发起配对请求
     *
     * @param bluetoothDevice 蓝牙设备
     * @return 绑定状态监听器
     */
    public Observable<Integer> bond(final BluetoothDevice bluetoothDevice) {
        Subject<Integer> subject = bondStateMap.get(bluetoothDevice);
        if (subject == null) {
            subject = PublishSubject.create();
            bondStateMap.put(bluetoothDevice, subject);
        }
        return subject
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            bluetoothDevice.createBond();
                        }
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        bondStateMap.remove(bluetoothDevice);
                    }
                });
    }

    public void connect(final BluetoothDevice serviceDevice) {
        cancelDiscovery();
        serviceDevice.createBond();
        Observable.just(0)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        try {
                            BluetoothSocket socket = serviceDevice.createRfcommSocketToServiceRecord(UUID.fromString("05c66aab-54c4-4a28-a5fc-3fdb98865824"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

//    public void listen() {
//        Observable.just(0)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(new Consumer<Integer>() {
//                    @Override
//                    public void accept(Integer integer) throws Exception {
//                        try {
//                            Log.d(TAG, "accept: 1");
//                            BluetoothServerSocket serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BlueTest", UUID.fromString("05c66aab-54c4-4a28-a5fc-3fdb98865824"));
//                            Log.d(TAG, "accept: 2");
//                            BluetoothSocket socket = serverSocket.accept();
//                            Log.d(TAG, "accept: 3" + socket);
//                            serverSocket.close();
//                            Log.d(TAG, "accept: 4");
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }

    /**
     * 蓝牙状态改变
     *
     * @param state 状态
     */
    void onStateChanged(int state) {
        if (blueStateSubject.hasObservers()) {
            blueStateSubject.onNext(convertState(state));
        }
    }


    /**
     * 蓝牙扫描开始
     */
    void onDiscoveryStarted() {

    }

    /**
     * 蓝牙扫描结束
     */
    void onDiscoveryFinished() {
        if (blueDeviceSubject != null && blueDeviceSubject.hasObservers() && !blueDeviceSubject.hasComplete()) {
            blueDeviceSubject.onComplete();
        }
    }

    /**
     * 扫描到设备
     *
     * @param bluetoothDevice 蓝牙设备
     */
    void onDeviceFound(BluetoothDevice bluetoothDevice) {
        if (blueDeviceSubject != null && blueDeviceSubject.hasObservers() && !blueDeviceSubject.hasComplete()) {
            blueDeviceSubject.onNext(bluetoothDevice);
        }
    }

    /**
     * 蓝牙绑定状态改变
     *
     * @param bluetoothDevice 蓝牙设备
     */
    void onBondStateChanged(BluetoothDevice bluetoothDevice) {
        Subject<Integer> subject = bondStateMap.get(bluetoothDevice);
        if (subject != null && subject.hasObservers()) {
            subject.onNext(bluetoothDevice.getBondState());
            if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDING) {
                subject.onComplete();
            }
        }
    }

    /**
     * 转换数字类型的状态为枚举类型的状态
     *
     * @param state 数字类型的状态
     * @return 枚举类型的状态
     */
    private State convertState(int state) {
        switch (state) {
            // 打开中
            case BluetoothAdapter.STATE_TURNING_ON:
                return State.TURNING_ON;
            // 打开
            case BluetoothAdapter.STATE_ON:
                return State.ON;
            // 关闭中
            case BluetoothAdapter.STATE_TURNING_OFF:
                return State.TURNING_OFF;
            // 关闭
            case BluetoothAdapter.STATE_OFF:
                return State.OFF;
            default:
                return State.NONE;
        }
    }

    /**
     * 蓝牙状态
     */
    public enum State {
        NONE,
        TURNING_ON,
        ON,
        TURNING_OFF,
        OFF
    }

    /**
     * 单例持有类
     */
    private static class SingletonHolder {
        private static final Bluetooth INSTANCE = new Bluetooth();
    }
}
