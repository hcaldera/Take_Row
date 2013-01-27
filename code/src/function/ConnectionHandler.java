package function;

import function.Pairing;
import screen.TakeRowScreen;
import takerow.Attendance;
import xml.StudentInfo;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.ui.UiApplication;

public class ConnectionHandler
{
	private static ConnectionHandler myInstance;
	private StudentInfo group;
	private Timer myTimerInstance;
	private int myCounter = 0;
	private int mainCounter = 0;
	private int myTimer = 0;

	private static final byte NOT_PAIRING = 0;
	private final static byte PAIR_INIT = 1;
	private final static byte PAIR_IN_PROGRESS = 2;
	private final static byte PAIR_ACCEPTED = 3;
	private final static byte PAIR_FAILED = 4;
	private final static byte CLOSE_INIT = 5;
	private final static byte CLOSE_IN_PROGRESS = 6;
	private static byte myFlagStaus = NOT_PAIRING;

	private ConnectionHandler()
	{
		this.myCounter = 0;
		this.mainCounter = 0;
		this.myTimer = 0;
		this.group = TakeRowScreen.getInstance().getGroupSelected();
		this.myTimerInstance = new Timer();
		this.myTimerInstance.scheduleAtFixedRate(new RemindTask(), 0, 1000);
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static ConnectionHandler getInstance()
	{
		if( null == ConnectionHandler.myInstance )
		{
			ConnectionHandler.myInstance = new ConnectionHandler();
		}
		return ConnectionHandler.myInstance;
	}

	class RemindTask extends TimerTask
	{
		public void run()
		{
		UiApplication.getUiApplication().invokeLater( new Runnable()
		{
		public void run()
		{
			TakeRowScreen.getInstance().writeMessage( group.getStudentName(myCounter) );
			switch( ConnectionHandler.myFlagStaus )
			{
			case ConnectionHandler.NOT_PAIRING:
				if( 	(group.isDeviceRegistered(myCounter) ) &&
						(Attendance.ATTENDED !=  group.getAttendanceStatus(myCounter) ) )
				{
					BluetoothInfo bluetoothinfo = group.getBluetoothInfo(myCounter);
					ConnectionHandler.myFlagStaus = ConnectionHandler.PAIR_INIT;
					Pairing.getInstance().pair( bluetoothinfo );					
				}
				else
					myCounter++;
				break;

			case ConnectionHandler.PAIR_INIT:
				myTimer = 20;
				ConnectionHandler.myFlagStaus = ConnectionHandler.PAIR_IN_PROGRESS;
				break;

			case ConnectionHandler.PAIR_IN_PROGRESS:
				if( myTimer > 0 )
				{
					if( Pairing.getInstance().isPaired() )
					{
						ConnectionHandler.myFlagStaus = ConnectionHandler.PAIR_ACCEPTED;
					}
					else
					{
						if( Pairing.getInstance().isPairFailed() )
						{
							Pairing.getInstance().clearBuffer();
							ConnectionHandler.myFlagStaus = ConnectionHandler.PAIR_FAILED;
						}
						myTimer--;
					}
				}
				else
				{
					// FIXME some phones never return anything when they are connected
					// but, so far,  all phones return false when they can not be
					// reached by bluetooth. (Only if the buffer is not full)
					Pairing.getInstance().setPairedDevice(group.getBluetoothInfo(myCounter));
					ConnectionHandler.myFlagStaus = ConnectionHandler.PAIR_ACCEPTED;
				}
				break;

			case ConnectionHandler.PAIR_ACCEPTED:
				group.setAttendandeStatus( myCounter, Attendance.ATTENDED );
				ConnectionHandler.myFlagStaus = ConnectionHandler.CLOSE_INIT;
				Pairing.getInstance().unpair();
				break;

			case ConnectionHandler.PAIR_FAILED:
				group.setAttendandeStatus( myCounter, Attendance.MISSED );
				ConnectionHandler.myFlagStaus = ConnectionHandler.CLOSE_INIT;
				break;

			case ConnectionHandler.CLOSE_INIT:
				ConnectionHandler.myFlagStaus = ConnectionHandler.CLOSE_IN_PROGRESS;
				break;

			case ConnectionHandler.CLOSE_IN_PROGRESS:
				if( !Pairing.getInstance().isPaired() )
				{
					ConnectionHandler.myFlagStaus = ConnectionHandler.NOT_PAIRING;
					myCounter++;
				}
				break;
			}
			if( group.getAllStudentLength() == myCounter )
			{
				myCounter = 0;
				// Take row three times to make sure, all cell phones are detected.
				mainCounter++;
				if( 3 == mainCounter )
				{
					myTimerInstance.cancel(); // Terminate the timer thread
					Attendance.myAttendanceStatus |= Attendance.BLUETOOTH_ATTENDANCE;
					Attendance.myAttendanceStatus |= Attendance.XML_PENDING;
					Attendance.myAttendanceStatus |= Attendance.REPORT_PENDING;
					Attendance.myAttendanceStatus &= ~Attendance.PROGRESS_ATTENDANCE;

					mainCounter = 0;
					TakeRowScreen.getInstance().update();
					ConnectionHandler.myInstance = null;
				}
			}
		}
		});
		}
	}
}