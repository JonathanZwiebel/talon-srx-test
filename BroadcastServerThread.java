package org.usfirst.frc.team8.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.usfirst.frc.team8.robot.AndroidConnectionHelper;

/**
 * Supplies wrapper methods for using adb to control the Android
 *
 * <h1><b>Fields</b></h1>
 * 	<ul>
 * 		<li>Instance and State variables:
 * 			<ul>
 * 				<li>{@link BroadcastServerThread#s_instance}: Private static instance of this class (Singleton)</li>
 * 				<li>{@link BroadcastServerThread#s_socketState}: Current state of socket connection (private)</li>
 * 				<li><b>See:</b>{@link SocketState}</li>
 * 			</ul>
 * 		</li>
 * 		<li>Utility variables:
 * 			<ul>
 * 				<li>{@link BroadcastServerThread#s_secondsAlive}: Private count of seconds the program has run for</li>
 * 				<li>{@link BroadcastServerThread#s_stateAliveTime}: Private count of seconds the state has run for</li>
 * 				<li>{@link BroadcastServerThread#s_port}: Port that the ServerSocket listens on (private)</li>
 * 				<li>{@link BroadcastServerThread#s_running}: Private boolean representing whether the thread is running</li>
 * 				<li>{@link BroadcastServerThread#s_awaitingOutput}: Private boolean representing whether the thread is waiting to output data</li>
 * 				<li>{@link BroadcastServerThread#s_server}: Server Socket object that listens for android client and receives data (private)</li>
 * 				<li>{@link BroadcastServerThread#mOutput}: Stores output from the client (private)</li>
 * 			</ul>
 * 		</li>
 * 	</ul>
 *
 * <h1><b>Accessors and Mutators</b></h1>
 * 	<ul>
 * 		<li>{@link AndroidConnectionHelper#getInstance()}</li>
 * 		<li>{@link BroadcastServerThread#SetState(SocketState)}</li>
 * 	</ul>
 *
 * <h1><b>External Access Functions</b>
 * 	<br><BLOCKQUOTE>For using as a wrapper for RIOdroid</BLOCKQUOTE></h1>
 * 	<ul>
 * 		<li>{@link BroadcastServerThread#start(int)}</li>
 * 		<li>{@link BroadcastServerThread#AwaitClient()}</li>
 * 		<li>{@link BroadcastServerThread#AwaitOutput()} </li>
 * 	</ul>
 *
 * 	<h1><b>Internal Functions</b>
 * 	 <br><BLOCKQUOTE>Paired with external access functions. These compute the actual function for the external access</BLOCKQUOTE></h1>
 * 	 <ul>
 * 	     <li>{@link BroadcastServerThread#AcceptConnection()}</li>
 * 	 </ul>
 *
 * @see SocketState
 * @author Alvin
 *
 */
public class BroadcastServerThread implements Runnable{

	/**
	 * State of connection between the roboRIO and nexus
	 *
	 * <ul>
	 *     <li>{@link SocketState#PREINIT}</li>
	 *     <li>{@link SocketState#IDLE}</li>
	 *     <li>{@link SocketState#RECEIVING}</li>
	 *     <li>{@link SocketState#OUTPUTTING}</li>
	 * </ul>
	 */
    public enum SocketState{
        PREINIT, IDLE, RECEIVING, OUTPUTTING
    }

    // Instance and state variables
    private static BroadcastServerThread s_instance;
    private static SocketState s_socketState = SocketState.PREINIT;

    // Utility variables
    private static double s_secondsAlive = 0;
    private static double s_stateAliveTime = 0;
    private static int s_port;
    private static boolean s_running = false;
    private static boolean s_awaitingOutput = false;
    private static ServerSocket s_server;
    private String mOutput = "";

	/**
	 * Creates a BroadcastServerThread instance
	 * Cannot be called outside as a Singleton
	 */
	private BroadcastServerThread(){}

	/**
	 * @return The instance of the BST
	 */
    public static BroadcastServerThread getInstance(){
        if(s_instance == null){
            s_instance = new BroadcastServerThread();
        }
        return s_instance;
    }

	/**
	 * Sets the state of socket connection
	 * @param state State to switch to
	 */
	private void SetState(SocketState state){
        s_socketState = state;
    }

	/**
	 * (DEBUG) Logs the Socket state
	 */
	private void logSocketState(){
        System.out.println("Debug: BroadcastServerThread SocketState - "+s_socketState);
    }

	/**
	 * Starts the BroadcastServerThread thread
	 * <br>Created server socket opens on given port
	 * @param port Port to start Server on
	 */
	public void start(int port){

        if(!s_socketState.equals(SocketState.PREINIT)){ // This should never happen
            System.out.println("Error: in BroadcastServerThread.start(), " +
                    "socket is already initialized");
        }

        if(s_running){  // This should never happen
            System.out.println("Error: in BroadcastServerThread.start(), " +
                    "thread is already running");
        }

        s_port = port;
        try {
            s_server = new ServerSocket(s_port);
            s_server.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.SetState(SocketState.IDLE);
        s_running = true;

        System.out.println("Starting Thread: BroadcastServerThread on port "+port);
        (new Thread(this, "BroadcastServerThread")).start();
    }

	/**
	 * Tells the Server to begin awaiting a client to connect
	 */
	public void AwaitClient() {
        if(!s_socketState.equals(SocketState.IDLE)){
            System.out.println("Error: in BroadcastServerThread.AwaitClient(), " +
                    "thread is not in idle state, cannot await for client");
           this.logSocketState();
            return;
        }

        if(s_awaitingOutput){
            System.out.println("Error: in BroadcastServerThread.AwaitClient(), " +
                    "already awaiting output, cannot await another client");
            return;
        }

        s_awaitingOutput = true;
        this.SetState(SocketState.RECEIVING);
    }

	/**
	 * Waits for the Server to receive data from the client, then
	 * grabs the data
	 * @return The data received from the client
	 */
	public String AwaitOutput() {
        String outp = null;
        if(!s_awaitingOutput){
            System.out.println("Error in BroadcastServerThread.AwaitOutput(), " +
                    "thread is not awaiting an output");
            return null;
        }

        while(s_awaitingOutput){
            switch(s_socketState){
                case IDLE:
                    System.out.println("Error in BroadcastServerThread.AwaitOutput(), " +
                            "thread is in idle state, not awaiting an output");
                    return null;

                case OUTPUTTING:
                    s_awaitingOutput = false;
                    outp = this.mOutput;
                    this.SetState(SocketState.RECEIVING);
                    break;

				case RECEIVING:
                    break;
            }
        }

        this.SetState(SocketState.IDLE);
        return outp;
    }

	/**
	 * Accept connection from a client
	 * @return The state after execution
	 */
	private SocketState AcceptConnection(){
		// Builds the String representation of the data
        StringBuilder builder = new StringBuilder();

        try{
        	// Accept client, then form into a readable object
            Socket client = s_server.accept();
			BufferedReader client_reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

			// Read the data
			String input;
			while((input = client_reader.readLine()) != null){
				builder.append(input);
			}

			client_reader.close();
			client.close();
        } catch (IOException e) {
                e.printStackTrace();
                return SocketState.RECEIVING;
        }

        // Set up for ouputting the received data
        this.mOutput = builder.toString();
        return SocketState.OUTPUTTING;
    }

	/**
	 * Updates the thread at {@link Constants#kAndroidConnectionUpdateRate} ms
	 */
	@Override
    public void run() {
        while(s_running){
            SocketState initState = s_socketState;
            switch (s_socketState){

                case PREINIT:   // This should never happen
                    System.out.println("Error: in BroadcastServerThread.run(), " +
                            "thread running on preinit state");
                    break;

				case RECEIVING:
                    this.SetState(this.AcceptConnection());
                    break;

                case OUTPUTTING:
                    break;

                case IDLE:
                    break;
            }

            // Reset state start time if state changed
            if(!initState.equals(s_socketState)){
                s_stateAliveTime = s_secondsAlive;
            }

            // Handle thread sleeping, sleep for set constant update delay
            try {
                Thread.sleep(50);
                s_secondsAlive += 50/1000.0;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
