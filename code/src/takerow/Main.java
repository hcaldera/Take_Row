package takerow;

import screen.TakeRowScreen;

import net.rim.device.api.ui.UiApplication;

public final class Main extends UiApplication
{
	public static void main( String[] args ) //throws Exception
	{
		new Main().enterEventDispatcher();
	}

	public Main()
	{
		super();
		
		this.pushScreen( TakeRowScreen.getInstance() );
	}
}