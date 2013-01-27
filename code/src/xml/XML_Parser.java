package xml;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML_Parser
{
	private static XML_Parser myInstance;

	public XML_Parser()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			FileConnection fileConnection = (FileConnection)Connector.open( StudentInfo.myXMLStudentInfo );
			InputStream inputStream = fileConnection.openInputStream();
			StudentInfo.myDocument = builder.parse( inputStream );
			fileConnection.close();
			inputStream.close();
		}
		catch ( Exception e )
		{
			StudentInfo.myDocument = null;
			Dialog.alert( e.toString() );
		}
	}

	/**
	 * Returns the singleton instance of this class.
	 * @return the singleton instance of this class.
	 */
	public static XML_Parser getInstance() 
	{
		if( XML_Parser.myInstance == null )
		{
			XML_Parser.myInstance = new XML_Parser();
		}
		return XML_Parser.myInstance;
	}

	/**
	 * Reads student information from XML file and initializes StudentInfo class.
	 */
	public void parseStudentInfo()
	{
		if( null == StudentInfo.myDocument)
		{
			Dialog.alert( "Invalid XML file" );
		}
		else
		{
			Element rootElement = StudentInfo.myDocument.getDocumentElement(); // This node is the root node.
			rootElement.normalize();
			NodeList childNodes = rootElement.getChildNodes();
			int numElements = childNodes.getLength();
			StudentInfo.initializeInstances( numElements );
			this.readNodes( rootElement );

		}
	}

	private void readNodes( Node node )
	{
		NodeList childNodes;
		NodeList classNodes;
		NodeList allStudentNodes;
		NodeList studentNodes;
		String varValue;
		String varName;
		int numClass;
		int numStudents;

		childNodes = node.getChildNodes();
		numClass = childNodes.getLength();

		for( int i = 0; i < numClass; i++ )
		{
			classNodes = childNodes.item(i).getChildNodes();

			// CLASS ID
			varName = classNodes.item(0).getNodeName();
			varValue = classNodes.item(0).getFirstChild().getNodeValue();
			StudentInfo.getInstance(i).setValues( varName, varValue, 0 );

			// CLASS NAME
			varName = classNodes.item(1).getNodeName();
			varValue = classNodes.item(1).getFirstChild().getNodeValue();
			StudentInfo.getInstance(i).setValues( varName, varValue, 0 );

			// GROUP
			varName = classNodes.item(2).getNodeName();
			varValue = classNodes.item(2).getFirstChild().getNodeValue();
			StudentInfo.getInstance(i).setValues( varName, varValue, 0 );

			allStudentNodes = classNodes.item(3).getChildNodes();
			numStudents = allStudentNodes.getLength();
			StudentInfo.getInstance(i).initializaStudentInfo(numStudents);
			for( int j = 0; j < numStudents; j++ )
			{
				studentNodes = allStudentNodes.item(j).getChildNodes();

				// ID
				varName = studentNodes.item(0).getNodeName();
				varValue = studentNodes.item(0).getFirstChild().getNodeValue();
				StudentInfo.getInstance(i).setValues(varName, varValue, j);

				// NAME
				varName = studentNodes.item(1).getNodeName();
				varValue = studentNodes.item(1).getFirstChild().getNodeValue();
				StudentInfo.getInstance(i).setValues(varName, varValue, j);

				// DEVICE NAME
				varName = studentNodes.item(2).getNodeName();
				varValue = studentNodes.item(2).getFirstChild().getNodeValue();
				StudentInfo.getInstance(i).setValues(varName, varValue, j);
			}			
		}
	}
}

/*
		if( Node.ELEMENT_NODE == node.getNodeType() )
		{
			String strNode;
			int group = XML_Parser.indexGroup;
			int student = XML_Parser.indexStudent;
			NodeList childNodes = node.getChildNodes();
			int numChildren = childNodes.getLength();
			Node firstChild = childNodes.item( 0 );

			// If the node has only one child and that child is a Text node, then
			// that child is one of the variables to be used by StudentInfo class.
			if ( numChildren == 1 && firstChild.getNodeType() == Node.TEXT_NODE ) 
			{
				strNode = ( node.getNodeName() );
				if ( StudentInfo.getInstance(group).setValues(strNode, firstChild.getNodeValue()) )
				{
					XML_Parser.indexStudent++;
				}
			}
			else 
			{
				// Recursively visit all this node's children.
				for ( int i = 0; i < numChildren; ++i ) 
				{
					// TODO eliminate recursive method
					readNodes( childNodes.item(i) );
				}
			}
		}
		else
		{
			// TODO It should never get here. Decide what to do.
		}
	}
}
*/