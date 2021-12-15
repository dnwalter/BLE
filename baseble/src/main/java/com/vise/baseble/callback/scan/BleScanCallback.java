package com.vise.baseble.callback.scan;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;

import com.vise.baseble.ViseBle;
import com.vise.baseble.common.BleConfig;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 扫描设备回调
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 17/8/1 22:58.
 */
public class BleScanCallback extends ScanCallback implements IScanFilter {
    protected Handler handler = new Handler(Looper.myLooper());
    protected boolean isScan = true;//是否开始扫描
    protected boolean isScanning = false;//是否正在扫描
    protected BluetoothLeDeviceStore bluetoothLeDeviceStore;//用来存储扫描到的设备
    protected IScanCallback scanCallback;//扫描结果回调
    protected List<ScanFilter> mScanFilters = new ArrayList<>();

    public BleScanCallback(IScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        if (scanCallback == null) {
            throw new NullPointerException("this scanCallback is null!");
        }
        bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    }

    public void setScanFilters(List<ScanFilter> filters) {
        mScanFilters = filters;
    }

    // 根据蓝牙mac地址添加过滤
    public void setAddressFilter(String address) {
        ScanFilter addressFilter = new ScanFilter.Builder()
                .setDeviceAddress(address)
                .build();
        mScanFilters.clear();
        mScanFilters.add(addressFilter);
    }

    public BleScanCallback setScan(boolean scan) {
        isScan = scan;
        return this;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scan() {
        if (isScan) {
            if (isScanning) {
                return;
            }
            bluetoothLeDeviceStore.clear();
            if (BleConfig.getInstance().getScanTimeout() > 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;

                        if (ViseBle.getInstance().getScanner() != null) {
                            ViseBle.getInstance().getScanner().stopScan(BleScanCallback.this);
                        }

                        if (bluetoothLeDeviceStore.getDeviceMap() != null
                                && bluetoothLeDeviceStore.getDeviceMap().size() > 0) {
                            scanCallback.onScanFinish(bluetoothLeDeviceStore);
                        } else {
                            scanCallback.onScanTimeout();
                        }
                    }
                }, BleConfig.getInstance().getScanTimeout());
            }else if (BleConfig.getInstance().getScanRepeatInterval() > 0){
                //如果超时时间设置为一直扫描（即 <= 0）,则判断是否设置重复扫描间隔
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isScanning = false;

                        if (ViseBle.getInstance().getScanner() != null) {
                            ViseBle.getInstance().getScanner().stopScan(BleScanCallback.this);
                        }

                        if (bluetoothLeDeviceStore.getDeviceMap() != null
                                && bluetoothLeDeviceStore.getDeviceMap().size() > 0) {
                            scanCallback.onScanFinish(bluetoothLeDeviceStore);
                        } else {
                            scanCallback.onScanTimeout();
                        }
                        isScanning = true;
                        if (ViseBle.getInstance().getScanner() != null) {
                            if (mScanFilters.size() > 0) {
                                ViseBle.getInstance().getScanner().startScan(mScanFilters, new ScanSettings.Builder().build(), BleScanCallback.this);
                            }else {
                                ViseBle.getInstance().getScanner().startScan(BleScanCallback.this);
                            }
                        }
                        handler.postDelayed(this,BleConfig.getInstance().getScanRepeatInterval());
                    }
                }, BleConfig.getInstance().getScanRepeatInterval());
            }
            isScanning = true;
            if (ViseBle.getInstance().getScanner() != null) {
                if (mScanFilters.size() > 0) {
                    ViseBle.getInstance().getScanner().startScan(mScanFilters, new ScanSettings.Builder().build(), BleScanCallback.this);
                }else {
                    ViseBle.getInstance().getScanner().startScan(BleScanCallback.this);
                }
            }
        } else {
            isScanning = false;
            if (ViseBle.getInstance().getScanner() != null) {
                ViseBle.getInstance().getScanner().stopScan(BleScanCallback.this);
            }
        }
    }

    public BleScanCallback removeHandlerMsg() {
        handler.removeCallbacksAndMessages(null);
        bluetoothLeDeviceStore.clear();
        return this;
    }

//    @Override
//    public void onBatchScanResults(List<ScanResult> results) {
//        for (ScanResult result : results) {
//            BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
//            BluetoothLeDevice filterDevice = onFilter(bluetoothLeDevice);
//            if (filterDevice != null) {
//                bluetoothLeDeviceStore.addDevice(filterDevice);
//                scanCallback.onDeviceFound(filterDevice);
//            }
//        }
//    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothLeDevice bluetoothLeDevice = new BluetoothLeDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
        BluetoothLeDevice filterDevice = onFilter(bluetoothLeDevice);
        if (filterDevice != null) {
            bluetoothLeDeviceStore.addDevice(filterDevice);
            scanCallback.onDeviceFound(filterDevice);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        return bluetoothLeDevice;
    }

}
