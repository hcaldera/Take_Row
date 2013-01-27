package function;

import net.rim.device.api.bluetooth.BluetoothSerialPort;
import net.rim.device.api.bluetooth.BluetoothSerialPortListener;

import java.io.IOException;
import java.util.Vector;

import screen.TakeRowScreen;

public class Pairing implements BluetoothSerialPortListener
{
	private static Pairing myInstance;
	private static BluetoothInfo myDeviceInfo = null;
	private boolean myPairFailed;
	private BluetoothSerialPort myPort;
	private BluetoothInfo myPairedDeviceInfo;

	private Pairing()
	{
		this.myPort = null;
		this.myPairedDeviceInfo = null;
		this.myPairFailed = false;
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static Pairing getInstance()
	{
		if( Pairing.myInstance == null )
		{
			Pairing.myInstance = new Pairing();
		}
		return Pairing.myInstance;
	}

	public boolean isPaired()
	{
		return ( this.myPairedDeviceInfo != null );
	}

	public boolean isPairFailed()
	{
		return this.myPairFailed;
	}

	/**
	 * Attempts to pair this Tracker with a locator by creating a Bluetooth connection between them.
	 * @param port the Bluetooth port to connect to.
	 * @return true if pairing was successful; false otherwise.
	 * @throws IOException 
	 */
	public boolean pair( BluetoothInfo port )
	{
		Pairing.myDeviceInfo = port;
		this.myPairedDeviceInfo = null;
		this.myPairFailed = false;
		try
		{
			this.myPort = new BluetoothSerialPort(port.getInfo(), BluetoothSerialPort.BAUD_115200,
				BluetoothSerialPort.DATA_FORMAT_PARITY_NONE | BluetoothSerialPort.DATA_FORMAT_STOP_BITS_1 | BluetoothSerialPort.DATA_FORMAT_DATA_BITS_8,
				BluetoothSerialPort.FLOW_CONTROL_NONE, 1024, 1024, Pairing.getInstance() );
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public void setPairedDevice(BluetoothInfo pairedDeviceInfo)
	{
		myPairedDeviceInfo = pairedDeviceInfo;
	}

	public void unpair()
	{
		if( this.isPaired() ) 
		{
			this.myPairedDeviceInfo = null;
			this.myPort.disconnect();
			this.myPort.close();
			this.myPort = null;
		}
	}

	/**
	 * Clears the bluetooth serial port buffer.
	 * @see BluetoothSerialPort#close()
	 */
	public void clearBuffer()
	{
		if( null != this.myPort )
		{
			this.myPort.close();
		}
	}

	/**
	 * Searches for Bluetooth serial ports opened and not registered. 
	 * @return the set of Bluetooth serial ports found.
	 */
	public static BluetoothInfo[] search()
	{
		//final BluetoothSerialPortInfo[] portInfo = BluetoothSerialPort.getSerialPortInfo();
		final Vector actualInfo = new Vector();

		// Search each port
		for( int i = 0; i < BluetoothInfo.myLength; i++ )
		{
			String deviceName = BluetoothInfo.getInstance(i).toString();
			if( TakeRowScreen.getInstance().getGroupSelected().isDeviceRegistered(deviceName) == false )
			{
				actualInfo.addElement( new BluetoothInfo(BluetoothInfo.getInstance(i).getInfo(), deviceName) );
			}
		}
		
		BluetoothInfo[] resultingInfo = new BluetoothInfo[actualInfo.size()];
		actualInfo.copyInto( resultingInfo );
		return resultingInfo;
	}

	/* BluetoothSerialPortInfo methods */
	public void dataReceived(int length)
	{
		
	}

	public void dataSent()
	{
		
	}

	public void deviceConnected(boolean success)
	{
		if(success)
		{
			this.setPairedDevice( Pairing.myDeviceInfo );
			this.myPairFailed = false;
		}
		else
		{
			this.setPairedDevice( null );
			this.myPairFailed = true;
		}
	}

	public void deviceDisconnected()
	{
		this.setPairedDevice( null );
	}

	public void dtrStateChange(boolean high)
	{
		
	}
}