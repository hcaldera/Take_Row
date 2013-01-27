package xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.component.Dialog;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import screen.TakeRowScreen;
import takerow.Attendance;

public class XML_Creator
{
	private static XML_Creator myInstance;
	private String strXML;
	private StudentInfo group;

	public XML_Creator()
	{
		this.group = TakeRowScreen.getInstance().getGroupSelected(); 
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static XML_Creator getInstance()
	{
		if( null == XML_Creator.myInstance )
		{
			XML_Creator.myInstance = new XML_Creator();
		}
		return XML_Creator.myInstance;
	}

	public void serializeStudentInfo()
	{
		Element rootElement = StudentInfo.myDocument.getDocumentElement(); // This node is the root node.
		rootElement.normalize();
		this.strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		this.updateNodes( rootElement );
		this.loadFile( StudentInfo.myXMLStudentInfo );
	}
	
	public void serializeAttendanceInfo()
	{
		this.group =  TakeRowScreen.getInstance().getGroupSelected();
		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd H_mm_ss"); //e.g. 2012-11-10 13_15_35
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd"); //e.g. 2012-11-10
		Calendar c = Calendar.getInstance(TimeZone.getDefault()/*.getTimeZone("GMT")*/);
		c.setTime(new Date(System.currentTimeMillis())); //now
		String formattedDate1 = dateFormat1.format(c, new StringBuffer(), null).toString();
		String formattedDate2 = dateFormat2.format(c, new StringBuffer(), null).toString();

		this.updateAttendanceStatus(formattedDate2);
		loadFile( StudentInfo.myXMLAttendanceInfo + 
				  TakeRowScreen.getInstance().getGroupSelected().getClassName() + " " +
				  TakeRowScreen.getInstance().getGroupSelected().getGroup() + " " + formattedDate1 + ".xml" );
		Attendance.myAttendanceStatus &= ~Attendance.XML_PENDING;
	}

	private void updateNodes(Node node)
	{
		NodeList childNodes;
		NodeList classNodes;
		NodeList allStudentNodes;
		NodeList studentNodes;
		int numClass;
		int numStudents;

		childNodes = node.getChildNodes();
		numClass = childNodes.getLength();
		this.strXML += "<" + node.getNodeName() + ">"; 								// <StudentsInfo>

		for( int i = 0; i < numClass; i++ )
		{
			classNodes = childNodes.item(i).getChildNodes();
			this.strXML +=	"<" + childNodes.item(i).getNodeName() + ">";			// <Class>

			// CLASS ID
			this.strXML += "<" + classNodes.item(0).getNodeName() + ">";
			this.strXML += classNodes.item(0).getFirstChild().getNodeValue();
			this.strXML += "</" + classNodes.item(0).getNodeName() + ">";

			// CLASS NAME
			this.strXML += "<" + classNodes.item(1).getNodeName() + ">";
			this.strXML += classNodes.item(1).getFirstChild().getNodeValue();
			this.strXML += "</" + classNodes.item(1).getNodeName() + ">";
			
			// GROUP
			this.strXML += "<" + classNodes.item(2).getNodeName() + ">";
			this.strXML += classNodes.item(2).getFirstChild().getNodeValue();
			this.strXML += "</" + classNodes.item(2).getNodeName() + ">";

			allStudentNodes = classNodes.item(3).getChildNodes();
			this.strXML += "<" + classNodes.item(3).getNodeName() + ">";			// <Students>
			numStudents = allStudentNodes.getLength();
			for( int j = 0; j < numStudents; j++ )
			{
				studentNodes = allStudentNodes.item(j).getChildNodes();
				this.strXML += "<" + allStudentNodes.item(j).getNodeName() + ">";	// <Student>

				// ID
				this.strXML += "<" + studentNodes.item(0).getNodeName() + ">";
				studentNodes.item(0).getFirstChild().setNodeValue( StudentInfo.getInstance(i).getStudentId(j) );
				this.strXML += studentNodes.item(0).getFirstChild().getNodeValue();
				this.strXML += "</" + studentNodes.item(0).getNodeName() + ">";

				// NAME
				this.strXML += "<" + studentNodes.item(1).getNodeName() + ">";
				studentNodes.item(1).getFirstChild().setNodeValue( StudentInfo.getInstance(i).getStudentName(j) );
				this.strXML += studentNodes.item(1).getFirstChild().getNodeValue();
				this.strXML += "</" + studentNodes.item(1).getNodeName() + ">";

				// DEVICE NAME
				this.strXML += "<" + studentNodes.item(2).getNodeName() + ">";
				studentNodes.item(2).getFirstChild().setNodeValue( StudentInfo.getInstance(i).getDeviceName(j) );
				this.strXML += studentNodes.item(2).getFirstChild().getNodeValue();
				this.strXML += "</" + studentNodes.item(2).getNodeName() + ">";
				
				this.strXML += "</" + allStudentNodes.item(j).getNodeName() + ">";	// </Student>
			}
			this.strXML += "</" + classNodes.item(3).getNodeName() + ">";			// </Students>
			this.strXML +=	"</" + childNodes.item(i).getNodeName() + ">";			// </Class>	
		}
		this.strXML += "</" + node.getNodeName() + ">";								// </StudentsInfo>
	}

	private void updateAttendanceStatus(String date)
	{

		int numStudent;
		this.strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		this.strXML += "<AttendanceList>"; 								// <root>

//		numClass = StudentInfo.getLength();
		this.strXML += "<Class>";										// <Class>

		//DATE
		this.strXML += "<date>";
		this.strXML += date;
		this.strXML += "</date>";
		
		//CLASS ID
		this.strXML += "<classid>";
		this.strXML += this.group.getClassID();
		this.strXML += "</classid>";

		// CLASS NAME
		this.strXML += "<classname>";
		this.strXML += this.group.getClassName();
		this.strXML += "</classname>";

		// GROUP NAME
		this.strXML += "<group>";
		this.strXML += this.group.getGroup();
		this.strXML += "</group>";

		numStudent = this.group.getAllStudentLength();
		this.strXML += "<Students>";									// Students
		for( int i = 0; i < numStudent; i++ )
		{
			this.strXML += "<student>";									// <Student>

			// ID
			this.strXML += "<id>";
			this.strXML += this.group.getStudentId(i);
			this.strXML += "</id>";

			// NAME
			this.strXML += "<name>";
			this.strXML += this.group.getStudentName(i);
			this.strXML += "</name>";

			// ATTENDANCE
			this.strXML += "<attendance>";
			this.strXML += this.group.getAttendanceStatusString(i);
			this.strXML += "</attendance>";

			
			this.strXML += "</student>";								// </Student>
		}
		this.strXML += "</Students>";									// Students
		this.strXML += "</Class>";										// </Class>

		this.strXML += "</AttendanceList>";								// </End root>

	}

	private void loadFile(String xmlName)
	{
		try
		{
			FileConnection fileConnection = (FileConnection)Connector.open( xmlName );
			if( fileConnection.exists() )
			{
				fileConnection.delete();
			}
			fileConnection.create();
			OutputStream outputStream = fileConnection.openOutputStream();
			outputStream.write( this.strXML.getBytes() );

			outputStream.close();
			fileConnection.close();
		}
		catch (IOException ioe)
		{
			Dialog.inform( ioe.getMessage() );
		}
		
		//TODO return true if successful.
	}
}

/** 
 * XML files are good medium for storing and exchanging the data over the network and they are widely
 * used in many technologies to store and exchange the data. A good example which I can provide here
 * are the web services, configuration files of many java based application servers and java based web
 * projects and also in the API's of famous social network sites like twitter.
 * 
 * It is easy to store the data as a XML file in application that you develop for the blackberry.
 * Though there are storage options like record store and persistent objects available in Blackberry
 * I prefer the XML files over them. Blackberry provides SAX parser for parsing and modifying XML but
 * I felt little difficult to use SAX api so I choose KXML as best alternative for SAX. I'm going to tell
 * how to create a XML file using KXML without loosing a sweat and parsing the already existing XML files.
 * 
 * 1. Creating a simple XML file using KXML2 api's
 * 
 * Consider a simple XML file as shown below.
 * 
 * <? xml version="1.0" encoding="UTF-8" ?>
 * <company> <employee id="1"> <fname>Vasudev</fname> <lname>Kamath</lname> <address>Karkala</address> </employee> ... </company>
 * 
 *  Use the below mentioned code for creating a xml file like above, the code itself is self explanatory.
 *  
 *  Document d = new Document();
 *  Element root = d.createElement("","company");
 *  Element employee = d.createElement("","employee");
 *  e.setAttribute("","id","1");
 *  
 *  Element fname = d.createElement("","fname");
 *  fname.addChild(Node.TEXT,"Vasudev");
 *  
 *  Element lname = d.createElement("","lname");
 *  lname.addChild(Node.TEXT,"Kamath");
 *  
 *  Element address = d.createElement(Node.TEXT,"address");
 *  address.addChild(Node.TEXT,"Karkala");
 *  
 *  employee.addChild(Node.ELEMENT,fname);
 *  employee.addChild(Node.ELEMENT,lname);
 *  employee.addChild(Node.ELEMENT,address);
 *  
 *  root.addChild(Node.ELEMENT,employee);
 *  d.addChild(Node.ELEMENT,root);
 *  
 *  Here first i'm creating a Document which is going to represent my XML file and then i'm creating the
 *  nodes required in my XML. The Node.ELEMENT represents and entire XML tag and its contents. For example
 *  is an XML Element and if you look at tag the content b/w opening and closing tag is simple text and
 *  is represented using Node.TEXT.
 *    
 *    Next step is to write this document to the file for which we need KXmlSerializer which is part of
 *    KXML2 apis. Below is the code to write the document to file
 *    
 *    String fileName = "file:///SDCard/Blackberry/company.xml"
 *    DataOutputStream os = null;
 *    FileConnection fc = null
 *    
 *    try
 *    {
 *    fc = (FileConnection)Connector.open(filename,Connector.READ_WRITE);
 *    if (! fc.exists())
 *    fc.create();
 *    
 *    os = fconn.openDataOutputStream();
 *    KXmlSerializer serializer = new KXmlSerializer();
 *    serializer.setOutput(os, "UTF-8");
 *    d.write(serializer);
 *    }
 *    catch (IOException e)
 *    {
 *    e.printStackTrace();
 *    }
 *    
 *    As you can see it is very simple to create XML files using KXML2 api's and possibility of missing
 *    few ending tags while writing XML file is very less since API itself takes care of creating XML files.
 *    
 *    Ok next step is parsing the XML file and it is also pretty simple.I'll tell how to read the XML file
 *    into a Document() object and parse it and functions required to get a particular node and its child
 *    node remaining logic you need to implement depending on your requirements. Below is the code for reading
 *    a file to Document and parsing it.
 *    
 *    Document d= new Document();
 *    FileConnection fc =  null;
 *    DataInputStream is = null;
 *    try
 *    {
 *       fc = (FileConnection) Connector.open(fileName, Connector.READ);
 *       is = fc.openDataInputStream();
 *       
 *       KXmlParser parser = new KXmlParser();
 *       parser.setInput(is, "UTF-8");
 *       d.parse(parser);
 *    }
 *    catch (IOException e)
 *    {
 *       e.printStackTrace();
 *    }
 *    catch (XmlPullParserException e)
 *    {
 *       e.printStackTrace();
 *    }
 *    
 *    
 *    This code gets the entire XML file to Document() object d and also parses it !!. Amazing rite
 *    you didn't even do a file read operation and yet have the whole content into Document() object.
 *    Well this is why I liked KXML more than SAX. If you are interested in seeing how it does you can
 *    get the source code of KXML2 api's and see. Now you have XML file in Document() object next how 
 *    to get the required node? It is very simple you have function called getElement(String 
 *    namespace,name) which gets particular node from its name in the XML file. There are many such 
 *    functions available in KXML2 apis for more information look at its java doc here.
 *    
 *    Last thing which I want to mention here is KXML library is meant not only for Blackberry but all
 *    those phones which supports J2ME. You need to have either KXML2 jar as library in your project or
 *    if you want to debug the KXML2 then you need to KXML2 source code and XML Pull parser source code.
 *    For more information on adding external library to your project please look at KB article in
 *    Blackberry website.
 *    
 *    Reference : Stack Overflow forums
 */