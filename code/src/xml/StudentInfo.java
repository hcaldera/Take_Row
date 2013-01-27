package xml;

import org.w3c.dom.Document;
import takerow.Attendance;
import function.BluetoothInfo;

public class StudentInfo
{
	private class Information
	{
		private int myID;
		private String myName;
		private String myDeviceName;
		private boolean myDeviceRegistered;
		private byte myAttendanceStatus;
		private BluetoothInfo myBluetoothInfo;

		private Information()
		{
			this.myID = 0;
			this.myName = "";
			this.myDeviceName = "";
			this.myDeviceRegistered = false;
			this.myAttendanceStatus = 0x7F;
		}

		/**
		 * Returns the student Id.
		 * @return String of the student Id.
		 * @see Object#toString()
		 */
		public String toString()
		{
			String tempId = String.valueOf( this.myID );
			if( this.myDeviceRegistered )
			{
				tempId = "**" + tempId;
			}
			return tempId;		
		}
	};

	private static StudentInfo[] myInstance;
	private Information[] myStudentInfo;
	private String myClassID;
	private String myClassName;
	private String myGroup;
	private boolean myDevicesRegistered;
	private boolean myDevicesNotRegistered;
	public static Document myDocument = null;

	public static final String myXMLStudentInfo = "file:///store/home/user/documents/StudentsInfo/StudentsInfo.xml";
	public static final String myXMLAttendanceInfo = "file:///store/home/user/documents/StudentsInfo/";

	public StudentInfo()
	{
		this.myStudentInfo = null;
		this.myClassID = "";
		this.myClassName = "";
		this.myGroup = "";
		this.myDevicesRegistered = false;
		this.myDevicesNotRegistered = false;
	}

	public static void initializeInstances( int length )
	{
		StudentInfo.myInstance = null;
		StudentInfo.myInstance = new StudentInfo[length];
		for( int i = 0; i < length; i++ )
		{
			StudentInfo.myInstance[i] = new StudentInfo();
		}
	}

	public void initializaStudentInfo( int length )
	{
		this.myStudentInfo = null;
		this.myStudentInfo = new Information[length];
		for( int i = 0; i < length; i++ )
		{
			this.myStudentInfo[i] = new Information();
		}
	}

	public static StudentInfo[] getInstance()
	{
		return StudentInfo.myInstance;
	}
	
	public static StudentInfo getInstance( int index )
	{
		StudentInfo studentInstance = null;
		if( null != StudentInfo.myInstance )
		{
			studentInstance = StudentInfo.myInstance[index];
		}
		return studentInstance;
	}

	/**
	 * Checks if a device is already registered so it will not
	 * get registered to another student.
	 * @param devicename The name of the device to be checked.
	 * @return TRUE if the device is registered to any student. False otherwise.
	 */
	public boolean isDeviceRegistered( String devicename )
	{
		boolean deviceFound = false;
		String tempDeviceName;

		for( int i = 0; (i < this.myStudentInfo.length) && (!deviceFound); i++ )
		{
			tempDeviceName = this.myStudentInfo[i].myDeviceName;
			if( (this.myStudentInfo[i].myDeviceRegistered) && (tempDeviceName.equalsIgnoreCase(devicename)) )
			{
				deviceFound = true;
			}
		}
		return deviceFound;
	}

	public boolean isDeviceRegistered( int index )
	{
		return this.myStudentInfo[index].myDeviceRegistered;
	}

	public boolean areDeviceRegistered()
	{
		return this.myDevicesRegistered;
	}

	/**
	 * Deletes registered device from student info.
	 * @param studentInfo The student instance.
	 */
	public void deleteDevice( int index )
	{
		this.myStudentInfo[index].myDeviceRegistered = false;
		this.myStudentInfo[index].myDeviceName = "";
		this.resetMyDevicesFound();
	}

	/**
	 * Determines in which member variable it will be saved the read value.
	 * @param varName The name of the variable.
	 * @param varValue The value to be saved.
	 * @return TRUE if all members are already saved.
	 */
	public boolean setValues( String varName, String varValue, int index )
	{
		boolean instanceFinished = false;
		if( varName.equalsIgnoreCase("id") )
		{
			this.myStudentInfo[index].myID = Integer.parseInt( varValue );
		}
		else if( varName.equalsIgnoreCase("name") )
		{
			this.myStudentInfo[index].myName = varValue;
		}
		else if( varName.equalsIgnoreCase("devicename") )
		{
			// In our XML file format, devicename is the last node.
			// Return true to be aware that we instanced all member variables.
			instanceFinished = true;

			this.myStudentInfo[index].myBluetoothInfo = BluetoothInfo.getBluetoothInfo(varValue);
			if( null != this.myStudentInfo[index].myBluetoothInfo )
			{
				this.myStudentInfo[index].myDeviceRegistered = true;
				this.myStudentInfo[index].myDeviceName = varValue;
				this.myDevicesRegistered = true;
			}
			else
			{
				this.myStudentInfo[index].myDeviceRegistered = false;
				this.myStudentInfo[index].myDeviceName = "";
				this.myDevicesNotRegistered = true;
			}
		}
		else if( varName.equalsIgnoreCase("classid") )
		{
			this.myClassID = varValue;
		}
		else if ( varName.equalsIgnoreCase("classNAME") )
		{
			this.myClassName = varValue;
		}
		else if( varName.equalsIgnoreCase( "group" ) )
		{
			this.myGroup = varValue;
		}
		return instanceFinished;
	}

	/**
	 * Searches for students that has no paired device yet. 
	 * @return ID list of students with no registered device.
	 */
	public Information[] search()
	{
		return this.myStudentInfo;
	}

	/**
	 * Resets myDevicesFound value when a device is deleted from the Student Info list.
	 */
	private void resetMyDevicesFound()
	{
		this.myDevicesRegistered = false;
		this.myDevicesNotRegistered = false;
		int i = 0;
		while( (!this.myDevicesRegistered) && (i < this.myStudentInfo.length) )
		{
			this.myDevicesRegistered = this.myStudentInfo[i++].myDeviceRegistered;
		}
		i = 0;
		while( (!this.myDevicesNotRegistered) && (i < this.myStudentInfo.length) )
		{
			this.myDevicesNotRegistered = ( false == this.myStudentInfo[i++].myDeviceRegistered );//StudentInfo.myInstance[i++].myDeviceRegistered);
		}
	}

	public String getStudentId( int index )
	{
		return String.valueOf(this.myStudentInfo[index].myID);
	}

	public String getStudentName( int index )
	{
		return this.myStudentInfo[index].myName;
	}

	public String getDeviceName( int index )
	{
		String deviceName = "noDeviceFound";
		if(this.myStudentInfo[index].myDeviceRegistered)
		{
			deviceName = this.myStudentInfo[index].myDeviceName;
		}
		return deviceName;
	}

	public int getAllStudentLength()
	{
		return this.myStudentInfo.length;
	}
	
	public int getMissedLength()
	{
		int missed = 0;
		for( int i = 0; i < this.myStudentInfo.length; i++ )
		{
			if( Attendance.MISSED == this.myStudentInfo[i].myAttendanceStatus )
			{
				missed++;
			}
		}
		return missed;
	}
	
	public int getNoDeviceLength()
	{
		int noDevice = 0;
		for( int i = 0; i < this.myStudentInfo.length; i++ )
		{
			if( !this.myStudentInfo[i].myDeviceRegistered )
			{
				noDevice++;
			}
		}
		return noDevice;
	}

	public BluetoothInfo getBluetoothInfo( int index )
	{
		return this.myStudentInfo[index].myBluetoothInfo;
	}

	public String getAttendanceStatusString( int index )
	{
		String attendanceStatus;
		switch( this.myStudentInfo[index].myAttendanceStatus )
		{
		case Attendance.ATTENDED:
			attendanceStatus = "Attended";
			break;
		case Attendance.MISSED:
			attendanceStatus = "Missed";
			break;
		default:
			attendanceStatus = "Pending";
		}

		return attendanceStatus;
	}
	
	public byte getAttendanceStatus( int index )
	{
		return this.myStudentInfo[index].myAttendanceStatus;
	}

	/**
	 * Saves the attendance status of the student.
	 * @param index The index of the student.
	 * @param attendance The status to be saved. Can be Attended or Missed.
	 */
	public void setAttendandeStatus( int index, byte attendance )
	{
		this.myStudentInfo[index].myAttendanceStatus = attendance; 
	}

	public String getClassID()
	{
		return this.myClassID;
	}

	public String getClassName()
	{
		return this.myClassName;
	}

	public String getGroup()
	{
		return this.myGroup;
	}

	public String toString()
	{
		return this.myClassID + ", Gropu " + this.myGroup;
	}
}