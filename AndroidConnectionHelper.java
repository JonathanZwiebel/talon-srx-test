package org.usfirst.frc.team8.robot;

import org.json.simple.parser.ParseException;
import org.spectrum3847.RIOdroid.RIOdroid;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Supplies wrapper methods for using adb to control the Android
 *
 * <h1><b>Fields</b></h1>
 * 	<ul>
 * 		<li>Instance and State variables:
 * 			<ul>
 * 				<li>{@link AndroidConnectionHelper#s_instance}: Private static instance of this class (Singleton)</li>
 * 				<li>{@link AndroidConnectionHelper#s_connectionState}: Current state of connection (private)</li>
 * 				<li>{@link AndroidConnectionHelper#s_streamState}: Current state of streaming</li>
 * 				<li><b>See:</b>{@link ConnectionState}</li>
 * 			</ul>
 * 		</li>
 * 		<li>Utility variables:
 * 			<ul>
 * 				<li>{@link AndroidConnectionHelper#s_secondsAlive}: Private count of seconds the program has run for</li>
 * 				<li>{@link AndroidConnectionHelper#s_stateAliveTime}: Private count of seconds the state has run for</li>
 * 				<li>{@link AndroidConnectionHelper#s_adbServerCreated}: Private boolean representing existence an adb server</li>
 * 				<li>{@link AndroidConnectionHelper#s_visionRunning}: Private boolean representing whether vision program is currently running</li>
 * 				<li>{@link AndroidConnectionHelper#s_running}: Private boolean representing whether the thread is running</li>
 * 				<li>{@link AndroidConnectionHelper#mTesting}: Private boolean representing whether program is testing on a pc with
 * 																adb installed and included in the path	</li>
 * 			</ul>
 * 		</li>
 * 	</ul>
 *
 * <h1><b>Accessors and Mutators</b></h1>
 * 	<ul>
 * 		<li>{@link AndroidConnectionHelper#getInstance()}</li>
 * 		<li>{@link AndroidConnectionHelper#SetState(ConnectionState)}</li>
 * 		<li>{@link AndroidConnectionHelper#SetStreamState(StreamState)}</li>
 * 	</ul>
 *
 * <h1><b>External Access Functions</b>
 * 	<br><BLOCKQUOTE>For using as a wrapper for RIOdroid</BLOCKQUOTE></h1>
 * 	<ul>
 * 		<li>{@link AndroidConnectionHelper#start(StreamState)}</li>
 * 		<li>{@link AndroidConnectionHelper#StartVisionApp()}</li>
 * 	</ul>
 *
 * 	<h1><b>Internal Functions</b>
 * 	 <br><BLOCKQUOTE>Paired with external access functions. These compute the actual function for the external access</BLOCKQUOTE></h1>
 * 	 <ul>
 * 	     <li>{@link AndroidConnectionHelper#InitializeServer()}</li>
 * 	     <li>{@link AndroidConnectionHelper#VisionInit()}</li>
 * 	     <li>{@link AndroidConnectionHelper#StreamVision()}</li>
 * 	 </ul>
 *
 * @see ConnectionState
 * @see StreamState
 * @author Alvin
 *
 */

public class AndroidConnectionHelper implements Runnable{

	public static class Constants{
		// Android app information
		public static String kPackageName = "com.frc8.team8vision";
		public static String kActivityName = "MainActivity";
		public static int kAndroidConnectionUpdateRate = 100;	// Update rate in milliseconds
		public static int kAndroidServerSocketUpdateRate = 50;
		public static int kAndroidServerSocketPort = 8008;
	}
	
	/**
	 * State of connection between the roboRIO and nexus
	 *
	 * <ul>
	 *     <li>{@link ConnectionState#PREINIT}</li>
	 *     <li>{@link ConnectionState#STARTING_SERVER}</li>
	 *     <li>{@link ConnectionState#IDLE}</li>
	 *     <li>{@link ConnectionState#START_VISION_APP}</li>
	 * </ul>
	 */
	public enum ConnectionState{
		PREINIT, STARTING_SERVER, IDLE, START_VISION_APP, STREAMING;
	}

	/**
	 * State of streaming data from Nexus
	 *
	 * <ul>
	 *     <li>{@link StreamState#IDLE}</li>
	 *     <li>{@link StreamState#JSON}</li>
	 *     <li>{@link StreamState#BROADCAST}</li>
	 * </ul>
	 */
	public enum StreamState{
		IDLE, JSON, BROADCAST
	}

	// Instance and state variables
	private static AndroidConnectionHelper s_instance;
	private static ConnectionState s_connectionState = ConnectionState.PREINIT;
	private static StreamState s_streamState = StreamState.IDLE;

	// Utility variables
	private static double s_secondsAlive = 0;
	private static double s_stateAliveTime = 0;
	private static byte[] s_imageData = null;
	private static boolean s_adbServerCreated = false;
	private static boolean s_visionRunning = false;
	private static boolean s_running = false;
	private boolean mTesting = false;
	private static NetworkTable s_visionTable;


	/**
	 * Creates an AndroidConnectionHelper instance
	 * Cannot be called outside as a Singleton
	 */
	private AndroidConnectionHelper(){}

	/**
	 * @return The instance of the ACH
	 */
	public static AndroidConnectionHelper getInstance(){
		if(s_instance == null){
			s_instance = new AndroidConnectionHelper();
		}
		return s_instance;
	}

	/**
	 * Sets the state of connection
	 * @param state State to switch to
	 */
	private void SetState(ConnectionState state){
		s_connectionState = state;
	}

	/**
	 * Sets the state of streaming between the Nexus
	 * @param state State to switch to
	 */
	private void SetStreamState(StreamState state){
		if(s_streamState.equals(state)){
			System.out.println("Warning: in AndroidConnectionHelper.SetStreamState(), "
					+ "no chane to write state");
		}else{
			s_streamState = state;
		}
	}

	/**
	 * Starts the AndroidConnectionHelper thread
	 */
	public void start(StreamState state){
		this.start(false, state);
	}

	/**
	 * Starts the AndroidConnectionHelper thread
	 * <br>(accounts for running program for testing)
	 * @param isTesting
	 */
	public void start(boolean isTesting, StreamState streamState){

		if(s_connectionState != ConnectionState.PREINIT) {    // This should never happen
			System.out.println("Error: in AndroidConnectionHelper.start(), "
					+ "connection is already initialized");
		}

		if(s_running){	// This should never happen
			System.out.println("Error: in AndroidConnectionHelper.start(), "
					+ "thread is already running");
		}

		this.SetState(ConnectionState.STARTING_SERVER);
		s_running = true;
		s_streamState = streamState;
		s_visionTable = NetworkTable.getTable("vision");
		this.mTesting = isTesting;

		System.out.println("Starting Thread: AndroidConnectionHelper ");
		(new Thread(this, "AndroidConnectionHelper")).start();

	}

	/**
	 * Initializes RIOdroid and RIOadb
	 * @return The state after execution
	 */
	private ConnectionState InitializeServer() {
		boolean connected = false;

		if(s_adbServerCreated){	// This should never happen
			System.out.print("Error: in AndroidConnectionHelper.InitializeServer(), "
					+ "adb server already connected (or this function was called before)");
			return ConnectionState.IDLE;
		}else {
			try {    // RIOadb.init() possible error is not being handled, sketchily fix later
				// Initializes RIOdroid usb and RIOadb adb daemon
				if(!this.mTesting) {
					System.out.println("Step 1");
					RIOdroid.init();

					if(s_streamState.equals(StreamState.BROADCAST)){

						// Forward the port and start the server socket
						System.out.println("Step 2");
						RIOdroid.executeCommand("adb reverse tcp:" +
								Constants.kAndroidServerSocketPort + " tcp:" +
								Constants.kAndroidServerSocketPort);
						System.out.println("Starting BroadcastServerThread");
						BroadcastServerThread.getInstance().start(Constants.kAndroidServerSocketPort);
					}
				}else{
					AdbComputerBridge.getInstance().init();

					if(s_streamState.equals(StreamState.BROADCAST)){
						
						// Forward the port and start the server socket
						AdbComputerBridge.getInstance().exec("adb reverse tcp:" +
								Constants.kAndroidServerSocketPort + " tcp:" +
								Constants.kAndroidServerSocketPort);
						System.out.println("Starting BroadcastServerThread");
						BroadcastServerThread.getInstance().start(Constants.kAndroidServerSocketPort);
					}
				}

				connected = true;
			} catch (Exception e) {
				System.out.println("Error: in AndroidConnectionHelper.InitializeServer(), "
						+ "could not connect.\n" + e.getStackTrace());
			}

			// Let it retry connection for 10 seconds, then give in
			if (s_secondsAlive - s_stateAliveTime > 10 && !connected) {
				System.out.println("Error: in AndroidConnectionHelper.InitializeServer(), "
						+ "connection timed out");
			}

			if (connected) {
				s_adbServerCreated = true;
				return ConnectionState.IDLE;
			} else {
				return s_connectionState;
			}
		}
	}

	/**
	 * Starts up the vision app
	 */
	public void StartVisionApp(){
		if(s_visionRunning){	// This should never happen, but easily can due to outside calling
			System.out.println("Warning: On call AndroidConnectionHelper.StartVisionApp(), "
					+ "vision app already running (or function has been called before)");
		}else{
			if(s_connectionState.equals(ConnectionState.STARTING_SERVER)){
				while(!s_connectionState.equals(ConnectionState.IDLE)){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}else if(!s_connectionState.equals(ConnectionState.IDLE)){
				System.out.println("Error: in AndroidConnectionHelper.StartVisionApp(), "
						+ "connection not in a state to start app");
			}

			System.out.println("Starting vision app");
			this.SetState(ConnectionState.START_VISION_APP);
		}
	}

	/**
	 * Sends command to boot up the vision app
	 * @return The state after execution
	 */
	private ConnectionState VisionInit(){
		boolean connected = false;

		try {	// RIOadb.init() possible error is not being handled, sketchily fix later
			// Starts app through adb shell, and outputs the returned console message
			if(!this.mTesting) {
				System.out.println(RIOdroid.executeCommand(
						"adb shell am start -n " + Constants.kPackageName + "/" +
								Constants.kPackageName + "." + Constants.kActivityName));
			}else{
				System.out.println(AdbComputerBridge.getInstance().exec(
						"adb shell am start -n " + Constants.kPackageName + "/" +
						Constants.kPackageName + "." + Constants.kActivityName));
			}
			
			System.out.println("Vision app started");

			connected = true;
		}catch (Exception e) {
			System.out.println("Error: in AndroidConnectionHelper.VisionInit(), "
					+ "could not connect.\n" + e.getStackTrace());
		}

		// Let it retry connection for 10 seconds, then give in
		if (s_secondsAlive - s_stateAliveTime > 10) {
			System.out.println("Error: in AndroidConnectionHelper.VisionInit(), "
					+ "connection timed out");
		}

		if(connected) {
			s_visionRunning = true;
			System.out.println("Starting Vision Stream");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ConnectionState.STREAMING;
		} else {
			return s_connectionState;
		}
	}

	/**
	 * Streams in the vision data
	 * @return The state after execution
	 */
	private ConnectionState StreamVision(){
		if(!s_visionRunning){	// This should never happen
			System.out.println("Error: in AndroidConnectionHelper.StreamVision(), "
					+ "vision program i not running (or has not been initialized inside this program)");
		}

		switch (s_streamState){
		case IDLE:
			System.out.println("Error: in AndroidConnectionHelper.StreamVision(), "
					+ "streaming in IDLE state, nothing streaming");
			break;
		case JSON:
			this.StreamJSON();
			break;
		case BROADCAST:
			this.StreamBroadcast();
			break;
		}

		return s_connectionState;
	}

	/**
	 * Streams vision data via sending a broadcast and
	 * receiving the output data through a socket
	 */
	private void StreamBroadcast(){
		System.out.println("Stream broadcast");
		BroadcastServerThread.getInstance().AwaitClient();

		// Broadcast an Intent to the app signaling the call to get data
		/*if(!mTesting){
			System.out.println("Execute command");
			RIOdroid.executeCommand("adb shell am broadcast -a "+Constants.kPackageName+".GET_DATA --es filler text");
		}else{
			AdbComputerBridge.getInstance().exec("adb shell am broadcast -a "+Constants.kPackageName+".GET_DATA --es filler text");
		}*/
		
		System.out.println("Dammit");
		AdbComputerBridge.getInstance().exec("adb shell am broadcast -a "+Constants.kPackageName+".GET_DATA --es filler text");

		System.out.println("Awaiting output");
		// Receive data from android client
		String raw_data = BroadcastServerThread.getInstance().AwaitOutput();

		System.out.println("Parsing JSON");
		parseJSON(raw_data);
	}

	/**
	 * Streams vision data via pulling a JSON file with
	 * data written to it
	 */
	private void StreamJSON(){
		System.out.println("Sreaming vision as JSON");
		String raw_data;

		// Read the JSON file which stores the vision data
		if(!this.mTesting){
			System.out.println("adb shell run-as "+Constants.kPackageName+" cat /data/data/"+ Constants.kPackageName
					+ "/files/data.json");
			raw_data = RIOdroid.executeCommand("adb shell run-as "+Constants.kPackageName+" cat /data/data/"+ Constants.kPackageName
					+ "/files/data.json");
			/*System.out.println(RIOdroid.executeCommand("adb shell run-as "+Constants.kPackageName+" cat /data/data/"+ Constants.kPackageName
					+ "/files/data.json"));*/
		}else{
			raw_data = AdbComputerBridge.getInstance().exec("adb shell run-as "+Constants.kPackageName+" cat /data/data/"+ Constants.kPackageName
					+ "/files/data.json");
		}

		System.out.println("Parsing");
		parseJSON(raw_data);
	}

	/**
	 * Computes parsing of streamed data (for now just prints to console)
	 * @param raw_data Raw JSON formatted data (String)
	 */
	private void parseJSON(String raw_data){
		if(raw_data == null  || raw_data.equals("")){
			return;
		}

		// Create JSONObject from the raw String data
		JSONObject json = null;

		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(raw_data);
		} catch (ParseException e) {
			// This is spammy
//			e.printStackTrace();
		}

		// Compute based on app state (given in the data)
		if(json != null){
			System.out.println("parsing");
			String state = (String) json.get("state");
			if(!(state == null) && !state.equals("")){	// Handle based on state
				switch(state){
				case "STREAMING":
					// Get image data
					String data_s = ((String) json.get("image_rgb"));

					System.out.println("streaming");
					//System.out.println("data" + data_s);
					// Convert image data to bytes
					if(!(data_s == null || data_s.equals(""))) {
//						s_imageData  = DatatypeConverter.parseBase64Binary(data_s);
						s_visionTable.putString("image_rgb", data_s);
						
					}
					break;

				case "PAUSED":
					System.out.println("Vision Paused");
					break;

				case "TERMINATED":
					System.out.println("Vision Terminated");
					break;

				case "STARTUP":
					System.out.println("Vision Starting Up");
					break;

				default:
					System.out.println("WHAT");
					break;
				}
			}
		}
	}

	/**
	 * Updates the thread at {@link Constants#kAndroidConnectionUpdateRate} ms
	 */
	@Override
	public void run() {
		while(s_running){
			System.out.println(s_connectionState.name());
			ConnectionState initState = s_connectionState;
			switch(s_connectionState){

			case PREINIT:	// Shouldn't happen, but can due to error
				System.out.println("Error: in AndroidConnectionHelper.run(), "
						+ "thread running on preinit state");
				break;

			case STARTING_SERVER:	// Triggered by start(), should be called externally
				this.SetState(this.InitializeServer());
				break;

			case START_VISION_APP:	// Triggered by StartVisionApp(), should be called externally
				this.SetState(this.VisionInit());
				break;

			case STREAMING:
				this.SetState(this.StreamVision());
				break;

			case IDLE:
				break;
			}

			// Reset state start time if state changed
			if (!initState.equals(s_connectionState)) {
				s_stateAliveTime = s_secondsAlive;
			}

			// Handle thread sleeping, sleep for set constant update delay
			try {
				Thread.sleep(Constants.kAndroidConnectionUpdateRate);
				s_secondsAlive += Constants.kAndroidConnectionUpdateRate/1000.0;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
