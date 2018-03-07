package mobi.inthepocket.android.beacons.ibeaconscanner.utils;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Pair;

import java.util.Arrays;
import java.util.UUID;

import mobi.inthepocket.android.beacons.ibeaconscanner.Beacon;

/**
 * Utility class with beacon management functions.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class BeaconUtils
{
    private BeaconUtils() {}

    /**
     * Identifies a beacon.
     *
     * @param scanResult {@link ScanResult} which might be a beacon.
     * @return a Pair representing whether the scan result is a beacon,
     * and where the beacon can be found in the scan record.
     */
    public static Pair<Boolean, Integer> isBeaconPattern(final ScanResult scanResult)
    {
        final byte[] scanRecord = scanResult.getScanRecord().getBytes();

        int startByte = 2;
        while (startByte <= 5)
        {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // identifies an iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15)
            {
                // first element identifies correct data length
                return new Pair<>(true, startByte);
            }
            startByte++;
        }

        return new Pair<>(false, startByte);
    }

    /**
     * Create a {@link Beacon} from a {@link android.bluetooth.le.ScanRecord}.
     * This function assumes the scanrecord contains a beacon.
     * That can be verified with {@link #isBeaconPattern(ScanResult)}.
     *
     * @param scanRecord The byte array representation of a {@link android.bluetooth.le.ScanRecord} containing the beacon.
     * @param startByte  Where the beacon identification starts.
     * @return {@link Beacon} with the UUID, major and minor found in the scanRecord parameter.
     */
    public static Beacon createBeaconFromScanRecord(final byte[] scanRecord, int startByte)
    {
        // get the UUID from the hex result
        final byte[] uuidBytes = new byte[16];
        System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
        final UUID uuid = ConversionUtils.bytesToUuid(uuidBytes);

        // get the major from hex result
        final int major = ConversionUtils.byteArrayToInteger(Arrays.copyOfRange(scanRecord, startByte + 20, startByte + 22));

        // get the minor from hex result
        final int minor = ConversionUtils.byteArrayToInteger(Arrays.copyOfRange(scanRecord, startByte + 22, startByte + 24));

        return Beacon.newBuilder()
                .setUUID(uuid)
                .setMajor(major)
                .setMinor(minor)
                .build();
    }
}