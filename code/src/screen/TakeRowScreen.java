package screen;

import function.BluetoothInfo;
import takerow.Attendance;
import xml.*;

import net.rim.device.api.bluetooth.BluetoothSerialPort;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringProvider;

public class TakeRowScreen extends MainScreen implements FieldChangeListener
{
	private static TakeRowScreen myInstance;
	private final ObjectChoiceField myGroupList;
	private final ButtonField myTakeRowButton;
	private final ButtonField myRegisterDevicesButton;
	private final LabelField myMsgField;
	private final ButtonField myTakeRowManuallyButton;
	private MenuItem myCreateMI;
	private LabelField myInfoField;
	private StudentInfo myGroupSelected;

	private TakeRowScreen()
	{
		super( Screen.DEFAULT_MENU | Screen.DEFAULT_CLOSE );

		this.myCreateMI = new MenuItem( new StringProvider("Create XML file"), 0x330010, 0 );
		this.myCreateMI.setCommand(new Command(new CommandHandler()
		{
			/*public void run()*/
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				int answer = Dialog.NO;
				if( (Attendance.myAttendanceStatus & Attendance.PROGRESS_ATTENDANCE) > 0 )
				{
					Dialog.alert( "Wait until Take Row process is over" );
				}
				else
				{
					answer = Dialog.YES;
					if( 0 == (Attendance.myAttendanceStatus & Attendance.XML_PENDING) )
					{
						answer = Dialog.ask(Dialog.D_YES_NO, "There is no pending XML file, do you watn to proceed?", Dialog.NO);
					}
				}

				if( Dialog.YES == answer )
				{
					XML_Creator.getInstance().serializeAttendanceInfo();
					updateWork();
				}
			}
		}));

		this.setTitle( "Take Row 0.9.9" );
		this.myMsgField = new LabelField();
		this.myGroupList = new ObjectChoiceField( "Select Group", null );
		this.myTakeRowButton = new ButtonField( "Take Row", 0x200018000L );
		this.myTakeRowManuallyButton = new ButtonField( "Take Row Mannually", 0x200018000L );
		this.myRegisterDevicesButton = new ButtonField( "Update Student List", 0x18000L);
		this.myInfoField = new LabelField();

		this.myGroupList.setChangeListener( this );
		this.myTakeRowButton.setChangeListener( this );
		this.myRegisterDevicesButton.setChangeListener( this );
		this.myTakeRowManuallyButton.setChangeListener( this );

		this.add( this.myMsgField );
		this.add( this.myGroupList );
		this.add( this.myTakeRowButton );
		this.add( this.myTakeRowManuallyButton );
		this.add( new SeparatorField() );
		this.add( new SeparatorField() );
		this.add( this.myRegisterDevicesButton );
		this.add( this.myInfoField );
		this.addMenuItem( this.myCreateMI );

		if( BluetoothSerialPort.isSupported() )
		{
			BluetoothInfo.initializesInstances();
		}

		XML_Parser.getInstance().parseStudentInfo();
		this.myGroupList.setChoices( StudentInfo.getInstance() );

	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static TakeRowScreen getInstance() 
	{
		if(TakeRowScreen.myInstance == null)
		{
			TakeRowScreen.myInstance = new TakeRowScreen();
		}
		return TakeRowScreen.myInstance;
	}

	private void updateWork()
	{
		final boolean canTakeRow = ( this.myGroupSelected.areDeviceRegistered() ) && ( BluetoothSerialPort.isSupported() ); 

		this.myTakeRowButton.setEditable( canTakeRow );
		this.myMsgField.setText( this.myGroupSelected.getClassName() + " " + this.myGroupSelected.getGroup() );
		if( canTakeRow )
		{
			this.myTakeRowButton.setFocus();
		}
		else 
		{
			this.myRegisterDevicesButton.setFocus();
		}

		if( (Attendance.myAttendanceStatus & Attendance.PROGRESS_ATTENDANCE) > 0 )
		{
			this.myGroupList.setEditable( false );
			this.myTakeRowButton.setEditable( false );
			this.myTakeRowManuallyButton.setEditable( false );
			this.myRegisterDevicesButton.setEditable( false );
		}
		else
		{
			this.myGroupList.setEditable( 0 == (Attendance.myAttendanceStatus & Attendance.XML_PENDING) );
			this.myRegisterDevicesButton.setEditable( 0 == (Attendance.myAttendanceStatus & Attendance.XML_PENDING) );
			this.myTakeRowManuallyButton.setEditable( true );
			this.myRegisterDevicesButton.setEditable( true );
			this.myInfoField.setText(" ");
			if( (Attendance.myAttendanceStatus & Attendance.REPORT_PENDING) > 0  )
			{
				Attendance.myAttendanceStatus &= ~Attendance.REPORT_PENDING;
				Attendance.getInstance().showAttendanceReport();
			}
		}
	}
	
	public void writeMessage(String message)
	{
		this.myInfoField.setText( message );
	}

	public void update() 
	{
		UiApplication.getUiApplication().invokeAndWait
		( new Runnable() 
		{
			public void run()
			{
				updateWork();
				updateDisplay();
			}
		} );
	}

	public StudentInfo getGroupSelected()
	{
		return this.myGroupSelected;
	}
	public void onDisplay()
	{
		this.updateWork();
	}

	public boolean onClose()
	{
		int answer;
		boolean EXIT_SUCCESS = true;
		if( (Attendance.PROGRESS_ATTENDANCE & Attendance.myAttendanceStatus) > 0 )
		{
			answer = Dialog.ask(Dialog.D_YES_NO, "You are taking row, are you sure you want to cancel and exit?", Dialog.NO);
			if( Dialog.NO == answer )
			{
				EXIT_SUCCESS = false;
			}
		}
		
		if( EXIT_SUCCESS )
		{
			if( (Attendance.myAttendanceStatus & Attendance.XML_PENDING) > 0 )
			{
				XML_Creator.getInstance().serializeAttendanceInfo();
			}
			super.close();	
		}
		return EXIT_SUCCESS;
	}

	public void fieldChanged( Field field, int context )
	{
		final Field origField = field.getOriginal();

		if( (origField == this.myGroupList) && (null != this.myGroupList.getChoice(this.myGroupList.getSelectedIndex())) )
		{
			this.myGroupSelected = (StudentInfo)this.myGroupList.getChoice( this.myGroupList.getSelectedIndex() );
			this.updateWork();
		}
		else if( origField == this.myTakeRowButton )
		{
			Attendance.getInstance().TakeRow();
		}
		else if( origField == this.myRegisterDevicesButton )
		{
			if( BluetoothSerialPort.isSupported() )
			{
				UiApplication.getUiApplication().pushScreen( RegisterDeviceScreen.getInstance() );
			}
			else
			{
				this.myInfoField.setText( "Bluetooth is not supported on this device" );
			}
		}
		else if ( origField == this.myTakeRowManuallyButton )
		{
			UiApplication.getUiApplication().pushScreen( TakeRowManuallyScreen.getInstance() );
		}			
	}
}