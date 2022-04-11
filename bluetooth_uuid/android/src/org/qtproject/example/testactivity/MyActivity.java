/****************************************************************************
**
** Copyright (C) 2016 The Qt Company Ltd.
** Contact: https://www.qt.io/licensing/
**
** This file is part of the QtAndroidExtras module of the Qt Toolkit.
**
** $QT_BEGIN_LICENSE:BSD$
** Commercial License Usage
** Licensees holding valid commercial Qt licenses may use this file in
** accordance with the commercial license agreement provided with the
** Software or, alternatively, in accordance with the terms contained in
** a written agreement between you and The Qt Company. For licensing terms
** and conditions see https://www.qt.io/terms-conditions. For further
** information use the contact form at https://www.qt.io/contact-us.
**
** BSD License Usage
** Alternatively, you may use this file under the terms of the BSD license
** as follows:
**
** "Redistribution and use in source and binary forms, with or without
** modification, are permitted provided that the following conditions are
** met:
**   * Redistributions of source code must retain the above copyright
**     notice, this list of conditions and the following disclaimer.
**   * Redistributions in binary form must reproduce the above copyright
**     notice, this list of conditions and the following disclaimer in
**     the documentation and/or other materials provided with the
**     distribution.
**   * Neither the name of The Qt Company Ltd nor the names of its
**     contributors may be used to endorse or promote products derived
**     from this software without specific prior written permission.
**
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
** "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
** LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
** A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
** OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
** SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
** LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
** DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
** THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
** (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
** OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
**
** $QT_END_LICENSE$
**
****************************************************************************/

package org.qtproject.example.testactivity;

import java.util.Set;
import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.content.IntentFilter;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;

import org.qtproject.example.testactivity.NativeFunctions;

public class MyActivity extends org.qtproject.qt5.android.bindings.QtActivity
{
    private static final String TAG = MyActivity.class.getSimpleName();
    private static NativeFunctions mNativeFunctions;
    public final String IAP2_UUID       = "00000000-DECA-FADE-DECA-DEAFDECACAFE";
    public final String CARPLAY_UUID    = "2D8D2466-E14D-451C-88BC-7301ABEA291A";
    private static boolean mDiscovering = false;
    private static boolean mFetchingFoundDeviceUUIDs = false;
    private static ArrayList<BluetoothDevice> mScanFoundDeviceList = new ArrayList<BluetoothDevice>();
    private static ArrayList<String> mScanDeviceAddressList = new ArrayList<String>();
    private static ArrayList<String> mPairedDeviceAddressList = new ArrayList<String>();
    private ArrayList<DeviceUUIDRecord> mScanDeviceUUIDRecordList;
    private ArrayList<DeviceUUIDRecord> mPairedDeviceUUIDRecordList;

    public class DeviceUUIDRecord
    {
        public String deviceName;
        public String deviceAddress;
        public ArrayList<ParcelUuid> uuidList;

        public DeviceUUIDRecord() {
            deviceName = "";
            deviceAddress = "";
            uuidList = new ArrayList<ParcelUuid>();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG, "onCreate Called");

        registerIntentFilter();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy Called");

        unregisterReceiver(receiver);

        super.onDestroy();
    }

    private void registerIntentFilter()
    {
        IntentFilter    filter;

        Log.d(TAG, "registerIntentFilter()");
        filter  = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver  = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (context == null) {
                Log.e(TAG, "BluetoothScanReceiver context null");
                return;
            }
            Log.i(TAG, "Receiver.onReceive() : " + intent);
            final String action = intent.getAction();
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action == null ? "" : action) {
                case BluetoothDevice.ACTION_FOUND:
                    if (mDiscovering == true) {
                        collectFoundDevices(device);
                    }
//                    updateSpecialDevice(device);
//                    onBluetoothDeviceActionConnected(device, null);
                    break;
                case BluetoothDevice.ACTION_UUID:
                    Parcelable[] uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    ArrayList<ParcelUuid> uuidExtraList = new ArrayList<ParcelUuid>();
                    ParcelUuid[] uuidExtra = null;
                    if (uuids != null) {
                        Log.d(TAG, "EXTRA_UUID length is " + uuids.length);
                        for (Parcelable parcelable : uuids) {
                            ParcelUuid parcelUuid = (ParcelUuid) parcelable;
                            uuidExtraList.add(parcelUuid);
//                            Log.d(TAG, "ExtraUuidAdd->UUID: " + parcelUuid);
                        }

                        if (uuidExtraList.isEmpty() == false){
                            uuidExtra = new ParcelUuid[uuids.length];
                            uuidExtra = uuidExtraList.toArray(uuidExtra);
                        }
                    }
                    onBluetoothDeviceActionConnected(device, uuidExtra);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (mDiscovering == true) {
                        mDiscovering = false;
                        Log.i(TAG, "Device UUIDs discovery finished.");
                        if (mFetchingFoundDeviceUUIDs == false){
                            mFetchingFoundDeviceUUIDs = true;
                            Log.i(TAG, "Fetching found devices UUIDs start.");
                            Log.i(TAG, "mScanFoundDeviceList size[" + mScanFoundDeviceList.size() + "]");
                        }
                        else {
                            Log.e(TAG, "mFetchingFoundDeviceUUIDs is true, status error!");
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
//                    updateSpecialDevice(device);
//                    onBluetoothDeviceActionConnected(device, null);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    break;
                default:
                    break;
            }
        }
    };

    private void onBluetoothDeviceActionConnected(BluetoothDevice device, ParcelUuid[] uuidExtra)
    {
        BluetoothClass              bluetoothClass;
        String                      name;
        String                      address;
        String                      device_tag;

        Log.i(TAG, "onBluetoothDeviceActionConnected()");

        if (device == null) {
            Log.e(TAG, "device is null");
            return;
        }

        name    = device.getName();
        address = device.getAddress();

        if (address == null) {
            Log.e(TAG, "bluetooth device address is null");
            return;
        }

        if (name == null) {
            Log.e(TAG, "bluetooth name is null");
            device_tag = address;
        }
        else {
            device_tag = name;
        }

        bluetoothClass = device.getBluetoothClass();

        if (bluetoothClass == null)
        {
            Log.e(TAG, "bluetoothClass is null");
        }
        else {
            Log.i(TAG, "Device[" + device_tag + "] bluetoothClass is " + bluetoothClass.toString());
        }

        boolean isSupportiAP2 = false;
        boolean isSupportCarPlay = false;
        ParcelUuid[] uuids = device.getUuids();

        if (uuidExtra != null){
            uuids = uuidExtra;
            Log.i(TAG, "uuidExtra is not null, use UUID Extra");
        }

        if (null != uuids) {
            Log.i(TAG, "Device[" + device_tag + "] uuids.length is " + uuids.length);
            if (uuids.length > 0) {
                for (ParcelUuid uuid : uuids) {
                    Log.i(TAG, "Device UUID:" + uuid.toString());
                    if (IAP2_UUID.equalsIgnoreCase(uuid.toString())) {
                        isSupportiAP2 = true;
                    }
                    else if (CARPLAY_UUID.equalsIgnoreCase(uuid.toString())) {
                        isSupportCarPlay = true;
                    }
                }
            }
        }
        else {
            Log.e(TAG, "Device[" + device_tag + "] UUIDs is null");
        }

        if (isSupportiAP2 == true || isSupportCarPlay == true) {
            Log.i(TAG, "Device[" + device_tag + "] support iap2 -> " + "iAP2:" + isSupportiAP2 +", CarPlay:"+isSupportCarPlay);
        }
        else {
            Log.i(TAG, "Device[" + device_tag + "] does not support iap2");
        }
    }

    private static void collectFoundDevices(BluetoothDevice device)
    {
        Log.i(TAG, "collectFoundDevices()");

        if (device == null){
            Log.e(TAG, "collectFoundDevices() -> device is null");
            return;
        }

        if (mScanFoundDeviceList.contains(device) == false) {
            mScanFoundDeviceList.add(device);
            Log.i(TAG, "mScanFoundDeviceList size[" + mScanFoundDeviceList.size() + "]");
        }
    }

    private static void updateSpecialDevice(BluetoothDevice device)
    {
        Log.i(TAG, "updateSpecialDevice()");
        String name;
        String address;
        String device_tag;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.e(TAG, "updateSpecialDevice() adapter is null");
            return;
        }

        if (adapter.isEnabled() == false) {
            Log.e(TAG, "updateSpecialDevice() BluetoothAdapter is not ready for use");
            return;
        }

        if(device == null)
        {
            Log.e(TAG, "updateSpecialDevice() device is null");
            return;
        }

        name    = device.getName();
        address = device.getAddress();

        if(address == null)
        {
            Log.e(TAG, "updateSpecialDevice() bluetooth device address is null");
            return;
        }

        if (name == null) {
            Log.e(TAG, "updateSpecialDevice() bluetooth name is null");
            device_tag = address;
        }
        else {
            device_tag = name;
        }

        Log.i(TAG, "Device[" + device_tag + "]" + " fetchUuidsWithSdp()");
        device.fetchUuidsWithSdp();
    }

    public static void getPairedDeviceUUIDs()
    {
        mNativeFunctions.callFromJava("Hello from JAVA!");
        Log.d(TAG, "getPairedDeviceUUIDs()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter == null)
        {
            Log.e(TAG, "getPairedDeviceUUIDs() adapter is null");
            return;
        }

        if (adapter.isEnabled() == false){
            Log.e(TAG, "getPairedDeviceUUIDs() BluetoothAdapter is not ready for use");
            return;
        }
        if (adapter.isDiscovering() == true) {
            Log.e(TAG, "getPairedDeviceUUIDs() BluetoothAdapter is Discovering, cancelDiscovery first");
            adapter.cancelDiscovery();
        }

        Set<BluetoothDevice>    pairedDevices;
        pairedDevices   = adapter.getBondedDevices();

        if (pairedDevices.size() < 1) {
            Log.i(TAG, "getPairedDeviceUUIDs() There is no paired device!");
            return;
        }

        for(BluetoothDevice device : pairedDevices)
            device.fetchUuidsWithSdp();
    }

    public static void startBluetoothDiscovery()
    {
        Log.d(TAG, "startBluetoothDiscovery()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter == null)
        {
            Log.e(TAG, "startBluetoothDiscovery() adapter is null");
            return;
        }

        if (adapter.isEnabled() == false){
            Log.e(TAG, "startBluetoothDiscovery() BluetoothAdapter is not ready for use");
            return;
        }
        if (adapter.isDiscovering() == true) {
            Log.e(TAG, "startBluetoothDiscovery() BluetoothAdapter is Discovering, cancelDiscovery first.");
            adapter.cancelDiscovery();
        }
        mScanFoundDeviceList.clear();
        mScanDeviceAddressList.clear();
        mDiscovering = true;
        mFetchingFoundDeviceUUIDs = false;
        Log.i(TAG, "Device UUIDs discovery start.");
        adapter.startDiscovery();
    }
}
