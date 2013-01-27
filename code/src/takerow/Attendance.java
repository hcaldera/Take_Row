package takerow;

import net.rim.device.api.ui.component.Dialog;
import screen.TakeRowScreen;
import xml.StudentInfo;
import function.ConnectionHandler;

public class Attendance
{
	private static Attendance myInstance;
	private StudentInfo group = TakeRowScreen.getInstance().getGroupSelected();
	public static ConnectionHandler myTakeRow = null;

	public final static byte MISSED = 0;					// bit # 8|7|6|5|4|3|2|1
	public final static byte ATTENDED = 1;
	public final static byte BLUETOOTH_ATTENDANCE = 0x01;	// bit 1 0|0|0|0|0|0|0|1
	public final static byte NO_DEVICE_ATTENDANCE = 0x02;	// bit 2 0|0|0|0|0|0|1|0
	public final static byte MISSED_ATTENDANCE = 0x04;		// bit 3 0|0|0|0|0|1|0|0
	public final static byte PROGRESS_ATTENDANCE = 0x08;	// bit 4 0|0|0|0|1|0|0|0 
	public final static byte XML_PENDING = 0x10;			// bit 5 0|0|0|1|0|0|0|0
	public final static byte REPORT_PENDING = 0x20;			// bit 6 0|0|1|0|0|0|0|0
	public static byte myAttendanceStatus = 0x00;

	/*
	 * attendanceStatus bits
	 * ----------------------------------------------------------------------
	 * | bits		| 	Value					|      0		|     1		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 1			| Attendance by bluetooth	| not taken		| taken		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 2			| Attendance no devices		| not taken		| taken		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 3			| Attendance missed			| not taken		| taken		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 4			| Attendance in progress	| false			| true		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 5			| XML Pending to create		| false			| true		|
	 * |------------|---------------------------|---------------|-----------|
	 * | 6			| Report Pending			| false			| true		|
	 * ----------------------------------------------------------------------
	 */

	private Attendance()
	{
		
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static Attendance getInstance()
	{
		if( null == Attendance.myInstance )
		{
			Attendance.myInstance = new Attendance();
		}
		return Attendance.myInstance;
	}

	public void TakeRow()
	{
		if( (Attendance.PROGRESS_ATTENDANCE & Attendance.myAttendanceStatus) == 0 )
		{
			this.group = TakeRowScreen.getInstance().getGroupSelected();
			Attendance.myAttendanceStatus |= Attendance.PROGRESS_ATTENDANCE;
			Attendance.myTakeRow = null;
			Attendance.myTakeRow =  ConnectionHandler.getInstance();
			TakeRowScreen.getInstance().update();
		}
	}

	public void showAttendanceReport()
	{
		String message;
		int attended = 0;
		int missed = 0;
		int pending = 0;
		for( int i = 0; i < this.group.getAllStudentLength(); i++ )
		{
			switch( this.group.getAttendanceStatus(i) )
			{
				case Attendance.ATTENDED:
					attended++;
					break;
				case Attendance.MISSED:
					missed++;
					break;
				default:
					pending++;
					break;
			}
		}
		message = String.valueOf(attended);
		message = ( 1 == attended ? message + " student attended.\n" : message + " students attended.\n" );
		message = message + String.valueOf(missed);
		message = ( 1 == missed ? message + " student missed.\n" : message + " students missed.\n" );
		message = message + String.valueOf( pending );
		message = ( 1 == pending ? message + " student pending" : message + " students pending" );
		Dialog.inform( message );
	}
}