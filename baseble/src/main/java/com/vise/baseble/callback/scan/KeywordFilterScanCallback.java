package com.vise.baseble.callback.scan;

import android.text.TextUtils;

import com.vise.baseble.model.BluetoothLeDevice;

public class KeywordFilterScanCallback extends BleScanCallback{
    private String mKeyword;
    public KeywordFilterScanCallback(String keyword, IScanCallback scanCallback) {
        super(scanCallback);
        mKeyword = keyword;
    }

    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        BluetoothLeDevice tempDevice = null;
        String tempName = bluetoothLeDevice.getName();
        if (!TextUtils.isEmpty(tempName) && tempName.contains(mKeyword)) {
            tempDevice = bluetoothLeDevice;
        }

        return tempDevice;
    }
}
