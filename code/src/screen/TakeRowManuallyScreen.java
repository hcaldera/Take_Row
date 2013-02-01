package screen;

import takerow.Attendance;
import xml.StudentInfo;
import xml.XML_Creator;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.util.StringProvider;

public class TakeRowManuallyScreen extends MainScreen implements FieldChangeListener
{
	private static TakeRowManuallyScreen myInstance;
	private final ObjectChoiceField myStudentList;
	private final ButtonField myAttendedButton;
	private final ButtonField myMissedButton;
	private final ButtonField myPreviousButton;
	private final ButtonField myNextButton;
	private final MenuItem myTakeRowAllMI;
	private final MenuItem myTakeRowOnlyMissedMI;
	private final MenuItem myTakeRowNoDeviceMI;
	private final LabelField myStudentIDField;
	private final LabelField myStatusField;
	private final HorizontalFieldManager myAttendanceFieldMngr;
	private final HorizontalFieldManager myMoveFieldMngr;
	private Students[] myStudent;
	private StudentInfo myGroup;
	private byte myCurrentProcess;

	private class Students
	{
		private String studentName;
		private int index;
		private boolean pending;
		
		private Students()
		{
			this.studentName = "";
			this.pending = true;
			this.index = -1;
		}

		public String toString()
		{
			return this.studentName;
		}
	}

	private TakeRowManuallyScreen()
	{
		super( Screen.DEFAULT_MENU | Screen.DEFAULT_CLOSE );
		this.setTitle( TakeRowScreen.getInstance().getGroupSelected().getClassName() );

		this.myStudentList = new ObjectChoiceField( null, null );
		this.myAttendanceFieldMngr = new HorizontalFieldManager( Field.FIELD_HCENTER );
		this.myMoveFieldMngr = new HorizontalFieldManager( Field.FIELD_HCENTER );
		this.myAttendedButton = new ButtonField( "Attended", 0x18000L );
		this.myMissedButton = new ButtonField( "Missed", 0x18000L );
		this.myPreviousButton = new ButtonField( "Previous",0x18000L );
		this.myNextButton = new ButtonField( "Next", 0x18000L );
		this.myStudentIDField = new LabelField();
		this.myStatusField = new LabelField();

		this.myTakeRowAllMI = new MenuItem( new StringProvider("Take Row to all Students"), 0x330010, 0 );
		this.myTakeRowAllMI.setCommand( new Command( new CommandHandler()
		{
			//public void run()
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				if( Dialog.YES == getAnswer() )
				{
					initAllStudentsList();
				}
			}
		}));

		this.myTakeRowOnlyMissedMI = new MenuItem( new StringProvider("Take Row to missed students"), 0x330020, 1 );
		this.myTakeRowOnlyMissedMI.setCommand( new Command( new CommandHandler()
		{
			//public void run()
			public void execute( ReadOnlyCommandMetadata metadata, Object context )
			{
				if( Dialog.YES == getAnswer() )
				{
					initMissedStudentsList();
				}
			}
		}));

		this.myTakeRowNoDeviceMI = new MenuItem( new StringProvider("Take Row to students with no device"), 0x330030, 2 );
		this.myTakeRowNoDeviceMI.setCommand( new Command( new CommandHandler()
		{
			//public void run()
			public void execute( ReadOnlyCommandMetadata metadata, Object context)
			{
				if( Dialog.YES == getAnswer() )
				{
					initNoDeviceStudentsList();
				}
			}
		}));

		this.myStudentList.setChangeListener( this );
		this.myAttendedButton.setChangeListener( this );
		this.myMissedButton.setChangeListener( this );
		this.myPreviousButton.setChangeListener( this );
		this.myNextButton.setChangeListener( this );

		this.add( this.myStudentList );
		this.add( this.myAttendanceFieldMngr );
		this.myAttendanceFieldMngr.add( this.myAttendedButton );
		this.myAttendanceFieldMngr.add( this.myMissedButton );
		this.add( this.myMoveFieldMngr );
		this.myMoveFieldMngr.add( this.myPreviousButton );
		this.myMoveFieldMngr.add( this.myNextButton );
		this.add( this.myStudentIDField );
		this.add( this.myStatusField );
		this.addMenuItem( this.myTakeRowAllMI );
		this.addMenuItem( this.myTakeRowOnlyMissedMI );
		this.addMenuItem( this.myTakeRowNoDeviceMI );
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static TakeRowManuallyScreen getInstance()
	{
		if( null == TakeRowManuallyScreen.myInstance )
		{
			TakeRowManuallyScreen.myInstance = new TakeRowManuallyScreen();
		}
		return TakeRowManuallyScreen.myInstance;
	}

	public void onDisplay()
	{
		this.myAttendedButton.setEditable( false );
		this.myMissedButton.setEditable( false );
		this.myPreviousButton.setEditable( false );
		this.myNextButton.setEditable( false );
		this.myStudentList.setChoices( null );
		this.myStudentIDField.setText( null );
		this.myStatusField.setText( null );
		this.myCurrentProcess = 0x00;
	}

	/**
	 * @see MainScreen#close()
	 * @see MainScreen#onClose()
	 */
	public boolean onClose()
	{
		boolean EXIT_SUCCESS = true;
		int answer = Dialog.YES;
		if( (Attendance.myAttendanceStatus & Attendance.PROGRESS_ATTENDANCE) > 0 )
		{
			if( this.isProcessNOTCompleted() )
			{
				answer = Dialog.ask( Dialog.D_YES_NO, "The current process is not completed. Do you want to cancel?", Dialog.NO );
			}
			else
			{
				Attendance.myAttendanceStatus |= myCurrentProcess;
				Attendance.myAttendanceStatus |= Attendance.XML_PENDING;
				Attendance.myAttendanceStatus |= Attendance.REPORT_PENDING;
			}
		}
		
		if( Dialog.YES == answer )
		{
			if( (Attendance.myAttendanceStatus & Attendance.XML_PENDING) > 0 )
			{
				XML_Creator.getInstance().serializeAttendanceInfo();
			}
			Attendance.myAttendanceStatus &= ~Attendance.PROGRESS_ATTENDANCE;
			TakeRowScreen.getInstance().update();
			super.close();	
		}
		else
		{
			EXIT_SUCCESS = false;
		}
		return EXIT_SUCCESS;

	}

	public void fieldChanged( Field field, int context )
	{
		Field origField = field.getOriginal();
		Students tmpStudent = (Students)this.myStudentList.getChoice( this.myStudentList.getSelectedIndex() );

		if( origField == this.myStudentList )
		{
			this.myStudentIDField.setText( "Student ID: " + this.myGroup.getStudentId(tmpStudent.index) );
			this.myStatusField.setText( this.myGroup.getAttendanceStatusString(tmpStudent.index) );
		}
		else if( origField == this.myAttendedButton )
		{
			this.myGroup.setAttendandeStatus( tmpStudent.index, Attendance.ATTENDED );
			tmpStudent.pending = false;
			selectNextIndex();
		}
		else if( origField == this.myMissedButton )
		{
			this.myGroup.setAttendandeStatus( tmpStudent.index, Attendance.MISSED );
			tmpStudent.pending = false;
			selectNextIndex();
		}
		else if( origField == this.myPreviousButton )
		{
			if( this.myGroup.getAttendanceStatus(tmpStudent.index) != 0x7F )
			{
				tmpStudent.pending = false;
			}
			selectPreviousIndex();
		}
		else if( origField == this.myNextButton )
		{
			if( this.myGroup.getAttendanceStatus(tmpStudent.index) != 0x7F )
			{
				tmpStudent.pending = false;
			}
			selectNextIndex();
		}
	}

	/**
	 * It will check attendance of all students in the list.
	 */
	private void initAllStudentsList()
	{
		int length;
		this.myGroup = TakeRowScreen.getInstance().getGroupSelected();
		length = this.myGroup.getAllStudentLength();
		if( length > 0 )
		{
			Attendance.myAttendanceStatus |= Attendance.PROGRESS_ATTENDANCE;
			this.myCurrentProcess |= Attendance.BLUETOOTH_ATTENDANCE;
			this.myCurrentProcess |= Attendance.NO_DEVICE_ATTENDANCE;
			this.myCurrentProcess |= Attendance.MISSED_ATTENDANCE;
			this.myStudent = null;
			this.myStudent = new Students[length];
			for( int i = 0; i < length; i++ )
			{
				this.myStudent[i] = new Students();
				this.myStudent[i].studentName = this.myGroup.getStudentName( i );
				this.myStudent[i].index = i;
			}
			this.myStudentList.setChoices( this.myStudent );
			this.myStudentList.setEditable( true );
			this.myAttendedButton.setEditable( true );
			this.myMissedButton.setEditable( true );
			this.myPreviousButton.setEditable( true );
			this.myNextButton.setEditable( true );
		}
	}
	
	/**
	 * It will check attendance of any student whose status is missed or pending.
	 */
	private void initMissedStudentsList()
	{
		int length;
		int instanceLength;
		int missed = 0;
		this.myGroup = TakeRowScreen.getInstance().getGroupSelected();
		length = this.myGroup.getAllStudentLength();
		instanceLength = this.myGroup.getMissedLength();
		instanceLength += this.myGroup.getNoDeviceLength();
		if( (0 < length) && (0 < instanceLength) )
		{
			Attendance.myAttendanceStatus |= Attendance.PROGRESS_ATTENDANCE;
			this.myCurrentProcess |= Attendance.MISSED_ATTENDANCE;
			this.myStudent = null;
			this.myStudent = new Students[this.myGroup.getMissedLength()];
			for( int i = 0; i < length; i++ )
			{
				if( (Attendance.MISSED == this.myGroup.getAttendanceStatus(i)) || (!this.myGroup.isDeviceRegistered(i)) )
				{
					this.myStudent[missed] = new Students();
					this.myStudent[missed].studentName = this.myGroup.getStudentName( i );
					this.myStudent[missed].index = i;
					missed++;
				}
			}
			this.myStudentList.setChoices( this.myStudent );
			this.myStudentList.setEditable( true );
			this.myAttendedButton.setEditable( true );
			this.myMissedButton.setEditable( true );
			this.myPreviousButton.setEditable( true );
			this.myNextButton.setEditable( true );
		}
	}

	/**
	 * It will check attendance of students that have not registered a device yet.
	 */
	private void initNoDeviceStudentsList()
	{
		int length;
		int instanceLength;
		int noDevice = 0;
		this.myGroup = TakeRowScreen.getInstance().getGroupSelected();
		length = this.myGroup.getAllStudentLength();
		instanceLength = this.myGroup.getNoDeviceLength();
		if( (0 < length) && (0 < instanceLength) )
		{
			Attendance.myAttendanceStatus |= Attendance.PROGRESS_ATTENDANCE;
			this.myCurrentProcess |= Attendance.MISSED_ATTENDANCE;
			this.myStudent = null;
			this.myStudent = new Students[instanceLength];
			for( int i = 0; i < length; i++ )
			{
				if( !this.myGroup.isDeviceRegistered(i) )
				{
					this.myStudent[noDevice] = new Students();
					this.myStudent[noDevice].studentName = this.myGroup.getStudentName( i );
					this.myStudent[noDevice].index = i;
					noDevice++;
				}
			}
			this.myStudentList.setChoices( this.myStudent );
			this.myStudentList.setEditable( true );
			this.myAttendedButton.setEditable( true );
			this.myMissedButton.setEditable( true );
			this.myPreviousButton.setEditable( true );
			this.myNextButton.setEditable( true );
		}
	}

	/**
	 * It will check if the current process is not completed yet.
	 * @return True is the current process is NOT completed, false otherwise.
	 */
	private boolean isProcessNOTCompleted()
	{
		boolean notCompleted = false;
		int length = this.myStudent.length;
		for( int i = 0; (i < length) && (!notCompleted) ; i++  )
		{
			notCompleted = this.myStudent[i].pending;
		}
		return notCompleted;
	}

	/**
	 * If an attempt to close the window or to start another process while
	 * a current process is still running, but not finished, the user will
	 * be asked to proceed or cancel.
	 * @return The answer of the user, or YES (0x04) if no process is in progress.
	 */
	private int getAnswer()
	{
		int answer = Dialog.YES;
		if( (Attendance.myAttendanceStatus & Attendance.PROGRESS_ATTENDANCE) > 0 )
		{
			if( this.isProcessNOTCompleted() )
			{
				answer = Dialog.ask( Dialog.D_YES_NO, "The current process is not completed. Do you want to cancel?", Dialog.NO );
			}
			else
			{
				Attendance.myAttendanceStatus |= myCurrentProcess;
				Attendance.myAttendanceStatus &= ~Attendance.PROGRESS_ATTENDANCE;
				Attendance.myAttendanceStatus |= Attendance.XML_PENDING;
				Attendance.myAttendanceStatus |= Attendance.REPORT_PENDING;
			}
		}
		return answer;
	}

	private void selectNextIndex()
	{
		int currentIndex = this.myStudentList.getSelectedIndex();
		Students tmpStudent;
		if( (currentIndex + 1) < this.myStudentList.getSize() )
		{
			this.myStudentList.setSelectedIndex( currentIndex + 1 );
		}
		else
		{
			// At the last index, the Label field need to be updated so 
			// it does not show the attendance status erroneously.
			tmpStudent = (Students)this.myStudentList.getChoice( this.myStudentList.getSelectedIndex() );
			this.myStatusField.setText( this.myGroup.getAttendanceStatusString(tmpStudent.index) );			
		}
	}
	
	private void selectPreviousIndex()
	{
		int currentIndex = this.myStudentList.getSelectedIndex();
		if( 0 < currentIndex )
		{
			this.myStudentList.setSelectedIndex( currentIndex - 1 );
		}
	}
}