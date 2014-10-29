import net.java.games.input.*;
import java.io.*;

import java.util.Scanner;

/**
 * @author Frank Lasard
 * @author Adam Doyle
 * @version 2.3
 * @since 2012-01-01
 */
public class SystemController {

 	/**
 	 * The main method
 	 * <p>
 	 * The main method sets up the environment, validates each component, and executes the system
 	 * @param args Command line arguments
 	 */
 	public static void main(String[] args) { 
 		
 		//Debug variables
 		boolean debug = true;
 		int in = -1;

 		
 		//T0-DO: Move controller names and default port to preferences file
		String defaultPort = "/dev/ttyUSB0";	//The port that the Xbee transceiver can be found at
 		String[] controllerNames = { "PLAYSTATION(R)3 Controller (E0:AE:5E:BE:52:B3)"
 									,"PLAYSTATION(R)3 Controller (00:23:06:BE:D8:62)"
 									,"PLAYSTATION(R)3 Controller (E0:AE:5E:9B:E5:3B)"
 									,"PLAYSTATION(R)3 Controller (E0:AE:5E:2C:4C:51)"
 									,"PLAYSTATION(R)3 Controller (00:07:04:81:A2:69)"
 									,"PLAYSTATION(R)3 Controller (E0:AE:5E:69:43:DD)"
 									,"PLAYSTATION(R)3 Controller (E0:AE:5E:0E:98:39)"
 									,"PLAYSTATION(R)3 Controller (00:07:04:D2:AF:CB)"};	//Name from OS (sixad) driver output
 		int[] baseArray = {1,31,61,91,121,151,181,211};	//Base bytes for the 8 robots
 		boolean foundController = false;	//Controller found status
 		
 		
		
		//Setup PS2 Controller Environment
		RemoteController[] controllers = new RemoteController[controllerNames.length];	//Create an array to store remote controller threads
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();	//Get the current controller environment
		Controller[] cs = ce.getControllers();	//Get a list of all possible connected controllers
		
 		//Setup Xbee transciever
 		SimpleIO io = new SimpleIO(defaultPort);	//Create new Xbee serial connection at the default port
 		
 		
 		Scanner userIn = new Scanner(System.in);	//Open a Scanner for user input
 
		//Check each controller to verify that it is a DualShock3 and start a corresponding thread if so
		if(cs.length != 0){
			for (int i = 0; i < cs.length; i++){
				if(cs[i].getName().contains("PLAYSTATION(R)3")){
					System.out.println(cs[i].getName());
					for (int j = 0; j<controllerNames.length;j++){
						if (cs[i].getName().contains(controllerNames[j])){
							foundController = true;
							//Create a new controller object
							RemoteController t = new RemoteController (io, cs[i],baseArray[j]);
							//Store the controller thread reference
							controllers[j] = t;
							//Allocate a thread to each new controller
							new Thread(t).start();
						}
					}
				}
			}

			//If a valid controller(s) was found continue operations
			if(foundController){
				System.out.println("Playstation DualShock 3 Connected");
				
				//If valid Xbee serial port found continue operations
				if(io.portFound){

					//Place holder string
					String s;
					
					while(true){
						
						if(debug){
							try{
								if (!(io.input.available() == 0)){
									System.out.print("Ardunio Return ");
									in = io.input.read();
									System.out.println("is: " + in);
								}
							}
							catch(IOException e){
								System.out.println("Error Reading from Xbee Network");
								break;
							}
							catch(Exception e){
								System.out.println("Unknow Xbee Communication Error");
								break;
							}
						}
						else{
							System.out.println("Exit Program? (Y/N)");
							s = userIn.nextLine();
							if (s.equals("Y") || s.equals("y")){
								break;
							}
						}
					}
				}
				else{
					System.out.println("No Xbee Tranciever Found");
					System.out.println("Please Connect an Xbee Transciever");
				}
			}
			else{
				System.out.println("No Valid Controller Found");
				System.out.println("Please Connect PlayStation DualShock 3 Controller(s)");
			}
		}
	    else{
			System.out.println("No Controller Found");
			System.out.println("Please Connect a PlayStation DualShock 3 Controller");
		}
		
		//Close Scanner used for user input
		userIn.close();
		
		//Close Xbee Transceiver
 		System.out.println("Disconnecting Xbee Transiever");
 		io.disconnect();
 		
 		//Close Remote Controllers
 		System.out.println("Disconnecting Controllers");
 		for(int k = 0; k < controllers.length; k++){
 			if(controllers[k] != null){
 				controllers[k].setThreadStop(true);
 			}
 		}
 		
 		System.out.println("Exiting Program.");
	}
}