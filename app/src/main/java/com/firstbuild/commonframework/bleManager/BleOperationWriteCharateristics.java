package com.firstbuild.commonframework.blemanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Hollis on 1/22/16.
 */
public class BleOperationWriteCharateristics extends BleOperation {
    private String TAG = BleOperationWriteCharateristics.class.getSimpleName();
    private String characteristicsUuid;
    private byte[] values;

    public BleOperationWriteCharateristics(BluetoothDevice device, String characteristicsUuid, byte[] values) {
        super(device);
        this.characteristicsUuid = characteristicsUuid;
        this.values = values.clone();
    }

    @Override
    public boolean hasCallback() {
        return true;
    }

    @Override
    public void execute(BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "" + characteristicsUuid);

        List<BluetoothGattService> bleGattServices = bluetoothGatt.getServices();

        // Iterate services and characteristic
        for (BluetoothGattService service : bleGattServices) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {

                if (characteristic.getUuid().toString().equalsIgnoreCase(characteristicsUuid)) {
                    Log.d(TAG, "Found Characteristic for writing: " + characteristic.getUuid().toString());

                    printGattValue(values);
                    characteristic.setValue(values);

                    boolean result = bluetoothGatt.writeCharacteristic(characteristic);
                    Log.d(TAG, "result of writeCharacteristic is " + result);

                    break;
                } else {
                    // Do nothing
                }
            }
        }


    }

    private void printGattValue(byte[] values) {
        StringBuilder hexValue = new StringBuilder();

        // Print value in hexa decimal format
        System.out.print("Value: ");
        for (byte value : values) {
            hexValue.append(String.format("0x%02x ", value));
        }

        System.out.println(hexValue);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if(!(o instanceof BleOperationWriteCharateristics)) {
            return false;
        }

        BleOperationWriteCharateristics other = (BleOperationWriteCharateristics)o;

        return this.characteristicsUuid.equalsIgnoreCase(other.characteristicsUuid) &&
                Arrays.equals(this.values, other.values) && this.getDevice().equals(other.getDevice());
    }

    @Override
    public int hashCode() {
        return characteristicsUuid.hashCode();
    }
}
