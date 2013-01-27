package function;

import net.rim.device.api.bluetooth.BluetoothSerialPort;
import net.rim.device.api.bluetooth.BluetoothSerialPortInfo;

/**
 * Wrapper class for BluetoothSerialPortInfo. Returns an appropriate name for the device to be
 * displayed in the GUI.
 */
public class BluetoothInfo 
{
	private static BluetoothInfo[] myInstance;
	public static int myLength;
	private final BluetoothSerialPortInfo myInfo;
	private final String myName;

	/**
	 * Creates a new BluetoothInfo with a serial port info and a name.
	 * @param info The Bluetooth serial port info.
	 * @param name The name to be displayed in the GUI.
	 */
	public BluetoothInfo( BluetoothSerialPortInfo info, String name ) 
	{
		this.myInfo = info;
		this.myName = name;
	}

	public static BluetoothInfo getInstance( int index )
	{
		BluetoothInfo bluetoothInstance = null;
		if( BluetoothInfo.myInstance != null )
		{
			bluetoothInstance = BluetoothInfo.myInstance[index];
		}
		return bluetoothInstance;
	}

	/**
	 * Creates new BluetoothInfo objects.
	 */
	public static void initializesInstances() 
	{
		BluetoothInfo.myInstance = null;
		final BluetoothSerialPortInfo[] portInfo = BluetoothSerialPort.getSerialPortInfo();
		BluetoothInfo.myLength = portInfo.length;
		if( 0 < BluetoothInfo.myLength )
		{
			BluetoothInfo.myInstance = new BluetoothInfo[BluetoothInfo.myLength];
			// Search each port
			for( int i = 0; i < portInfo.length; i++ )
			{
				String deviceName = portInfo[i].getDeviceName();
				BluetoothInfo.myInstance[i] = new BluetoothInfo(portInfo[i], deviceName);
			}
		}
	}

	/**
	 * Returns the name of this info to be displayed in the GUI.
	 * @return the name of this info.
	 */
	public String toString() 
	{
		return this.myName;
	}

	/**
	 * Returns the serial port info.
	 * @return the serial port info.
	 */
	public BluetoothSerialPortInfo getInfo()
	{
		return this.myInfo;
	}

	public static BluetoothInfo getBluetoothInfo( String devicename ) 
	{
		BluetoothInfo bluetoothInstance = null;
		boolean bluetoothFound = false;
		if( !devicename.equalsIgnoreCase("noDeviceFound") )
		{
			for( int i = 0; (i < BluetoothInfo.myLength) && (!bluetoothFound); i++ )
			{
				if( BluetoothInfo.myInstance[i].myName.equalsIgnoreCase(devicename) )
				{
					bluetoothFound = true;
					bluetoothInstance = BluetoothInfo.myInstance[i];
				}
			}
		}
		return bluetoothInstance;
	}
}