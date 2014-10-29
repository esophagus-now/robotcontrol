import java.io.*;
import gnu.io.*;

/**
 * An interface to a synchronized serial port
 * 
 * @author Frank Lasard
 * @author Adam Doyle
 * @version 2.1
 * @since 2013-10-01
 */
public class SimpleIO {
	static CommPortIdentifier portId = null;
	static String message = "Hello, world";
	private byte transmitByte;
	private SerialPort serialPort;
	protected InputStream input;
	private OutputStream output;	
	protected boolean portFound = false;
	
	/**
	 * Constructor for a serial port
	 * @param port The name of the port to connect (e.g. COM1, /dev/ttyUSB0)
	 */
	public SimpleIO(String port){
		connect(port);
	}
	
	//Write a byte to the Arduino
	/**
	 * A synchronized method for multiple devices to write to a serial port
	 * @param data An 8 bit value to be written to the serial port
	 */
	public synchronized void writeByte(Byte data){
		this.transmitByte = data;

	    try {
	    	output.write(transmitByte);
	    	Thread.sleep(15);
			//outputStream.write(messageString.getBytes());
	    } 
	    catch (IOException e) {}
	    catch (Exception e) {}
	}
	
	
	/**
	 * When called this method will attempt to open and configure a serial port at the provided mount point
	 * @param defaultPort The default OS mount point to connect to
	 */
	public synchronized void connect(String defaultPort){
		
		//Tell gnu.io to look at default mount points
		System.setProperty("gnu.io.rxtx.SerialPorts", defaultPort);
				
		try{
			//Get Default Port Id
			portId = CommPortIdentifier.getPortIdentifier(defaultPort);
		}
		catch(NoSuchPortException nSPE){
			System.out.println("No Port Identifier for default port " + defaultPort + " found.");
		}

		if(portId != null){
		    //Verify that possible port is actually a serial port
		    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
			    
			    try {
			    	//Try to open port
			    	serialPort = (SerialPort) portId.open("SimpleIO", 2000);
			    	System.out.println("Opened Default Serial Port.");
			    	 
			    	//Try to configure port
			    	serialPort.setSerialPortParams(57600, 
						       SerialPort.DATABITS_8, 
						       SerialPort.STOPBITS_1, 
						       SerialPort.PARITY_NONE);
			    	System.out.println("Serial Port Configured.");
			    	
			    	//Try to open output stream
			    	output = serialPort.getOutputStream();
			    	System.out.println("Output Stream Opened");
			    	
			    	//Try to open input stream
			    	input = serialPort.getInputStream();
			    	System.out.println("Input Stream Opened.");
			    	
			    	System.out.println("Serial Port " + defaultPort +" Ready");
				    portFound = true;
			    } 
			    //If the port is in use, exit
			    catch (PortInUseException pIUE) {
			    	System.out.println("Default Port in use.");
			    } 
			    catch (UnsupportedCommOperationException uSCOE) {
			    	System.out.println("Serial Port Configuration Failed.");
			    }
			    catch (IOException iOE) {
			    	System.out.println("Input/Output Stream Could Not Be Opened.");
			    }
			    catch (Exception e){
			    	System.out.println("Unknow error while opening default comm port");
			    } 
			}
		}

		//Port was not found
		if (!portFound) {
		    System.out.println("Default Port " + defaultPort + " not found.");
		} 
	}

	/**
	 * This method will properly disconnect a serial port
	 */
	public synchronized void disconnect(){
		if (serialPort != null){
			try{
				output.close();
				input.close();
			}
			catch(Exception e){
				System.out.println("Unable to close Input/Output Streams properly");
			}
			
			serialPort.close();
		}	
	}
}