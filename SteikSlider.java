package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.networktables.NetworkTable;


/**
 * A class to test basic mechanical functionality and tune
 * loop values for the slider on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikSlider {
	CANTalon talon;
	String mode;
	Joystick stick;
	
	public static final float MAX_OUTPUT = 4.0f;
	
	public SteikSlider(int port) {
		talon = new CANTalon(port);
		mode = "Human";
		stick = new Joystick(0);
	}
	
	public void init() {		
		// Reset and turn on the Talon 
		talon.reset();
		talon.clearStickyFaults();
		talon.enable();
		talon.enableControl();
		
		// Limit the Talon output
		talon.configMaxOutputVoltage(MAX_OUTPUT);
		talon.configPeakOutputVoltage(MAX_OUTPUT, -MAX_OUTPUT);
		talon.setVoltageRampRate(Integer.MAX_VALUE);

		// Set up the Talon to read from a relative CTRE mag encoder sensor
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
		talon.setPosition(0);
		talon.setEncPosition(0);
		talon.setPulseWidthPosition(0);
		
		// Set up Talon update rate 
		talon.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		talon.setStatusFrameRateMs(StatusFrameRate.General, 1);
		
		switch(mode) {
		case "Velocity":
			talon.changeControlMode(CANTalon.TalonControlMode.Speed);
			talon.setPID(0, 0, 0, 0, 0, 0, 0);
			talon.set(0.0f);
			break;
		case "Constant":
			talon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
			break;
		case "Position":
			talon.changeControlMode(CANTalon.TalonControlMode.Position);
			talon.setPID(0, 0, 0, 0, 0, 0, 0);
			talon.set(0.0f);
			break;
		case "Human":
			talon.changeControlMode(CANTalon.TalonControlMode.Voltage);
			break;
		case "Motion Magic":
			talon.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
			talon.setPID(0, 0, 0, 0, 0, 0, 0);
			talon.setMotionMagicAcceleration(0.0f);
			talon.setMotionMagicCruiseVelocity(0.0f);
			talon.set(0.0f);
			break;
		default:
			System.err.println("Illegal SteikSlider Mode!");
			System.exit(1);
		}		
	}
	
	public void update() {
		printData();
		logData();
		
		switch(mode) {
		case "Velocity":
			break;
		case "Constant":
			talon.set(0.0f);
			break;
		case "Position":
			break;
		case "Human":
			talon.set(stick.getX() * MAX_OUTPUT);
			break;
		case "Motion Magic":
			break;
		default:
			System.err.println("Illegal MasterTalon Mode!");
			System.exit(1);
		}	
		
		// Zero on reverse encoder trigger
		if(talon.isRevLimitSwitchClosed()) {
			talon.setPosition(0);
			talon.setEncPosition(0);
		}
	}	
	
	public void disable() {
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
	}
	
	public void printData() {	
		System.out.println("getSetpoint(): " + talon.getSetpoint());
		System.out.println("getPulseWidthPosition() : " + talon.getPulseWidthPosition());
		System.out.println("getEncPosition() : " + talon.getEncPosition());
		System.out.println("getEncVelocity() (Native Unit - Tics per Min): " + talon.getEncVelocity());
		System.out.println("getOutputVoltage() : " + talon.getOutputVoltage());
		System.out.println("Speed (RPM): " + talon.getSpeed());
//		System.out.println("Adjusted getEncVelocity: " + talon.getEncVelocity() * 600.0f / 4096);
//		System.out.println("Adjusted getClosedLoopError(): " + talon.getClosedLoopError() * 600.0f / 4096);
//		System.out.println("Percent Error: " + (talon.getClosedLoopError()) / talon.getSetpoint());
//		System.out.println("Fwd Switch: " + talon.isFwdLimitSwitchClosed() + " | Rev Switch: " + talon.isRevLimitSwitchClosed());
	}
	
	public void logData() {
		Robot.table.putString("status", talon.getOutputVoltage() + "," + talon.getPosition() + "," + talon.getSpeed() + "," + talon.getClosedLoopError() + "\n");
	}
}