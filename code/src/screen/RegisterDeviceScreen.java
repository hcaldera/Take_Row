package screen;

import function.BluetoothInfo;
import function.Pairing;

import xml.StudentInfo;
import xml.XML_Parser;
import xml.XML_Creator;

import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringProvider;

public class RegisterDeviceScreen extends MainScreen implements FieldChangeListener
{
	private static RegisterDeviceScreen myInstance;
	private boolean myChangesMade;
	private final ObjectChoiceField myDeviceList;
	private final ObjectChoiceField myStudentList;
	private final ButtonField myUpdateButton;
	private final LabelField myMsgField1;
	private final LabelField myMsgField2;
	private final ButtonField myDeleteRegisterButton;
	private final MenuItem mySaveChangesMI;

	public RegisterDeviceScreen()
	{
		super( Screen.DEFAULT_MENU | Screen.DEFAULT_CLOSE );

		this.setTitle( "Register Student Devices" );

		this.myDeviceList = new ObjectChoiceField( "Select device:", null );
		this.myStudentList = new ObjectChoiceField( "Select student:", null );
		this.myUpdateButton = new ButtonField( "Update Student Info", 0x18000 );
		this.myDeleteRegisterButton = new ButtonField( "Delete Student Device", 0x18000 );
		this.myMsgField1 = new LabelField();
		this.myMsgField2 = new LabelField();
		this.myChangesMade = false;
		
		this.mySaveChangesMI = new MenuItem( new StringProvider("Save Changes"), 0x330010, 0 );
		this.mySaveChangesMI.setCommand( new Command( new CommandHandler()
		{
			//public void run()
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				if( myChangesMade )
				{
					XML_Creator.getInstance().serializeStudentInfo();
					TakeRowScreen.getInstance().update();
					myChangesMade = false;
				}
			}
		}));

		this.myUpdateButton.setChangeListener( this );
		this.myDeviceList.setChangeListener( this );
		this.myStudentList.setChangeListener( this );
		this.myDeleteRegisterButton.setChangeListener( this );

		this.add( this.myStudentList );
		this.add( this.myDeviceList );
		this.add( this.myUpdateButton );
		this.add( this.myDeleteRegisterButton );
		this.add( this.myMsgField1 );
		this.add( this.myMsgField2 );
		addMenuItem( this.mySaveChangesMI );

		this.init();
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static RegisterDeviceScreen getInstance()
	{
		if( RegisterDeviceScreen.myInstance == null )
			 RegisterDeviceScreen.myInstance = new RegisterDeviceScreen();
		return  RegisterDeviceScreen.myInstance;
	}

	private void init()
	{
		this.myStudentList.setChoices( TakeRowScreen.getInstance().getGroupSelected().search() );
		this.myDeviceList.setChoices( Pairing.search() );
	}
	
	private void updateWork()
	{
		this.myStudentList.setChoices( null );
		this.myDeviceList.setChoices( null );
		init();
	}
	
	protected void onDisplay()
	{
		this.updateWork();
	}

	public void fieldChanged( Field field, int context )
	{
		Field origField = field.getOriginal();
		StudentInfo tmpStudentInfo;
		int tmpStudentIndex;
		BluetoothInfo tmpBluetoothInfo;
		tmpStudentInfo = TakeRowScreen.getInstance().getGroupSelected();
		tmpStudentIndex = this.myStudentList.getSelectedIndex();
		// tmpStudentInfo = ( StudentInfo )this.myStudentList.getChoice( this.myStudentList.getSelectedIndex() );
		boolean isDeviceRegistered;
		int tmpIndex;

		if( origField == this.myUpdateButton )
		{
			tmpIndex = this.myStudentList.getSelectedIndex();
			tmpBluetoothInfo = ( BluetoothInfo )this.myDeviceList.getChoice( this.myDeviceList.getSelectedIndex() );
			tmpStudentInfo.setValues( "devicename", tmpBluetoothInfo.toString(), tmpStudentIndex );
			this.updateWork();
			this.myStudentList.setSelectedIndex( tmpIndex );
			this.myStudentList.setFocus();
			this.myChangesMade = true;
		}
		else if( origField == this.myDeleteRegisterButton )
		{
			tmpIndex = this.myStudentList.getSelectedIndex();
			tmpStudentInfo.deleteDevice( tmpStudentIndex );
			this.updateWork();
			this.myStudentList.setSelectedIndex( tmpIndex );
			this.myStudentList.setFocus();
			this.myChangesMade = true;
		}
		else if( origField == this.myStudentList )
		{
			isDeviceRegistered = tmpStudentInfo.isDeviceRegistered( tmpStudentIndex );
			this.myDeleteRegisterButton.setEditable( isDeviceRegistered );
			this.myUpdateButton.setEditable( !isDeviceRegistered );
			this.myDeviceList.setEditable( !isDeviceRegistered );
			this.myMsgField1.setText( tmpStudentInfo.getStudentName( tmpStudentIndex ) );
			this.myMsgField2.setText( " " );
			if( isDeviceRegistered )
			{
				this.myMsgField2.setText( "** " + tmpStudentInfo.getDeviceName( tmpStudentIndex ) );
			}
		}
	}

	/**
	 * @see Screen#onClose()
	 * @see Screen#close()
	 */
	public boolean onClose()
	{
		int answer;
		boolean Exit_Success = true;
		if( this.myChangesMade )
		{
			answer = Dialog.ask(Dialog.D_SAVE, "Do you want to save the changes you made?");
			switch( answer )
			{
			case Dialog.SAVE:
				XML_Creator.getInstance().serializeStudentInfo();
				TakeRowScreen.getInstance().update();
			break;
			case Dialog.DISCARD:
				XML_Parser.getInstance().parseStudentInfo();
			break;
			case Dialog.CANCEL:
				Exit_Success = false;
			break;
			}
		}
		if( Exit_Success )
		{
			this.myChangesMade = false;
			super.close();
		}
		return Exit_Success;
	}
}