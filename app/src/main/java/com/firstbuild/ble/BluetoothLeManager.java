package com.firstbuild.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ryanlee on 3/14/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeManager {
    private final static String TAG = BluetoothLeManager.class.getSimpleName();

    private Context context = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothGatt bluetoothGatt = null;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private List<BluetoothGattService> bluetoothServices;
    private HashMap<String, BluetoothListener> callbacks = null;
    private static BluetoothLeManager object = null;

    public static BluetoothLeManager getSharedObject(){
        if(object == null){
            object = new BluetoothLeManager();
        }

        return object;
    }

    public BluetoothLeManager(){
        Log.d(TAG, "IN BluetoothLeManager");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean checkBluetoothOnOff(){
        Log.d(TAG, "IN checkBluetoothOnOff");
        boolean isTurnOn = false;
        if(bluetoothAdapter.isEnabled()){
            isTurnOn = true;
        }

        return isTurnOn;
    }

    public void startScan(Context context){
        Log.d(TAG, "IN startScan");
        this.context = context;

        // Register intents for broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(broadcastReceiver, intentFilter);

        bluetoothAdapter.startDiscovery();
    }

    public void stopScan(Context context){
        Log.d(TAG, "IN stopScan");
        this.context = context;
        bluetoothAdapter.cancelDiscovery();
    }

    public void pairing(Context context, BluetoothDevice device) {
        Log.d(TAG, "In pairing - device: " + device);

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unpairing(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(Context context, BluetoothDevice device){
        Log.d(TAG, "IN connectToDevice - Device: " + device);
        bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback);
    }

    public ArrayList<BluetoothDevice> getBluetoothDeviceList(){
        return bluetoothDevices;
    }

    public List<BluetoothGattService> getBluetootheServices(){
        return bluetoothServices;
    }

    public byte[] readData(String targetUuid){
        Log.d(TAG, "IN readData");

        if(targetUuid == null){
            Log.d(TAG, "No characteristics has been read");
            return null;
        }

        if(bluetoothServices == null){
            Log.d(TAG, "bluetoothServices is null");
            return null;
        }

        byte[] value = null;

        for(BluetoothGattService service : bluetoothServices){

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for(BluetoothGattCharacteristic characteristic : characteristics){
                String Uuid = characteristic.getUuid().toString();

                if(Uuid.equals(targetUuid)) {
                    value = characteristic.getValue();
                    Log.d(TAG, "uuid: " + characteristic.getUuid().toString() + ", value: " + value);
                    break;
                }
            }
        }

        return value;
    }

    public boolean writeData(byte[] value, String targetUuid) {
        if (value == null || targetUuid == null) {
            Log.d(TAG, "Error no values or wrong uuid");
            return false;
        }

        for (BluetoothGattService service : bluetoothServices) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                String Uuid = characteristic.getUuid().toString();

                if(Uuid.equals(targetUuid)){
                    Log.d(TAG, "Target Uuid: " + characteristic.getUuid().toString());
                    characteristic.setValue(value);
                    bluetoothGatt.writeCharacteristic(characteristic);
                    break;
                }
            }
        }

        return true;
    }

    public void close(){
        context.unregisterReceiver(broadcastReceiver);
    }

    private ArrayList<BluetoothDevice> addDevice(BluetoothDevice device){

        // Create array list to contain devices scanned.
        if(bluetoothDevices == null){
            bluetoothDevices = new ArrayList<BluetoothDevice>();
        }

        if(bluetoothDevices.contains(device) == false) {
            bluetoothDevices.add(device);
        }

        return bluetoothDevices;
    }


    public void addListener(BluetoothListener listener){
        if(callbacks == null){
            callbacks = new HashMap<String, BluetoothListener>();
        }
        Log.d(TAG, "listener: " + listener);

        callbacks.put(listener.toString(), listener);
    }

    public void notifyUpdates(String listener, Object... args){
        Log.d(TAG, "In notifyUpdates");

        for(Map.Entry<String, BluetoothListener> entry : callbacks.entrySet()){
            BluetoothListener callback = entry.getValue();

            if(listener.equals("onScan")) {
                callback.onScan((String) args[0]);
            }
            else if(listener.equals("onPairing")){
                callback.onPairing((Context) args[0], (Intent) args[1]);
            }
            else if(listener.equals("onConnectionStateChange")){
                callback.onConnectionStateChange((BluetoothGatt) args[0], (int) args[1], (int) args[2]);
            }
            else if(listener.equals("onServicesDiscovered")){
                callback.onServicesDiscovered((BluetoothGatt) args[0], (int) args[1]);
            }
            else if(listener.equals("onCharacteristicWrite")){
                callback.onCharacteristicWrite((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1], (int) args[2]);
            }
            else if(listener.equals("onCharacteristicRead")){
                callback.onCharacteristicRead((BluetoothGatt) args[0], (BluetoothGattCharacteristic) args[1], (int) args[2]);
            }
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "In BroadcastReceiver - Discovery finished");
                notifyUpdates("onScan", new Object[]{action});
            }
            // Update device list whenever new devices found
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDevice(device);
                notifyUpdates("onScan", new Object[]{action});
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                notifyUpdates("onPairing", new Object[]{context, intent});
            }
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead:  " + characteristic.getUuid().toString());
            notifyUpdates("onCharacteristicRead", gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // This will get called anytime you perform a read or write characteristic operation
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt g, final BluetoothGattCharacteristic c, int status) {
            Log.d(TAG, "onCharacteristicWrite:  " + c.getUuid().toString());
            notifyUpdates("onCharacteristicWrite", new Object[]{g, c, status});
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            Log.d(TAG, "connection status: " + status + ", newState: " + newState);
            if(BluetoothProfile.STATE_CONNECTED == newState){
                Log.d(TAG, "Connected to " + gatt.getDevice().getAddress() + " and Request Service");
                notifyUpdates("onConnectionStateChange", new Object[]{gatt, status, newState});
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            Log.d(TAG, "In onServicesDiscovered");

            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            bluetoothServices = gatt.getServices();
            notifyUpdates("onServicesDiscovered", new Object[]{gatt, status});
        }
    };
}


