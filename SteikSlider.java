package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

import edu.wpi.first.wpilibj.AnalogInput;
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
	public static final int RIGHT_POT_POS = 2296;
	public static final int LEFT_POT_POS = 3341;
	public static final int CENTER_POT_POS = (RIGHT_POT_POS + LEFT_POT_POS) / 2;
	
	AnalogInput potentiometer;
	CANTalon talon;
	String mode;
	Joystick stick;
	
	public SteikSlider() {
		talon = new CANTalon(SteikConstants.SLIDER_TALON_DEVICE_ID);
		stick = new Joystick(SteikConstants.SLIDER_STICK_PORT);
		potentiometer = new AnalogInput(SteikConstants.SLIDER_POTENTIOMETER_PORT);
		mode = "Position";
	}
	
	public void init() {		
		// Reset and turn on the Talon 
		talon.reset();
		talon.clearStickyFaults();
		talon.enable();
		talon.enableControl();
		
		// Limit the Talon output
		talon.configMaxOutputVoltage(SteikConstants.SLIDER_MAX_OUTPUT);
		talon.configPeakOutputVoltage(SteikConstants.SLIDER_MAX_OUTPUT, -SteikConstants.SLIDER_MAX_OUTPUT);
		talon.setVoltageRampRate(Integer.MAX_VALUE);

		// Set up the Talon to read from a relative CTRE mag encoder sensor
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
		talon.setPosition(0);
		talon.setEncPosition(0);
		talon.setPulseWidthPosition(0);
		
		
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
			float current_pot_pos = potentiometer.getValue();
			float distance_to_center = current_pot_pos - CENTER_POT_POS;
			float to_move = (distance_to_center / 4096.0f) * 10.0f;
			talon.setPID(0.8, 0.01, 8, 0, 60, 0, 0);
			talon.set(to_move);
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
			talon.set(stick.getX() * SteikConstants.SLIDER_MAX_OUTPUT);
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
//		System.out.println("Slider getSetpoint(): " + talon.getSetpoint());
//		System.out.println("Slider getPulseWidthPosition() : " + talon.getPulseWidthPosition());
//		System.out.println("Slider getEncPosition() : " + talon.getEncPosition());
		System.out.println("Slider getPositon(): " + talon.getPosition());
//		System.out.println("Slider getEncVelocity() (Native Unit - Tics per Min): " + talon.getEncVelocity());
		System.out.println("Slider getOutputVoltage() : " + talon.getOutputVoltage());
		System.out.println("Slider Speed (RPM): " + talon.getSpeed());
//		System.out.println("Slider Adjusted getEncVelocity: " + talon.getEncVelocity() * 600.0f / 4096);
//		System.out.println("Slider Adjusted getClosedLoopError(): " + talon.getClosedLoopError() * 600.0f / 4096);
//		System.out.println("Slider Percent Error: " + (talon.getClosedLoopError()) / talon.getSetpoint());
//		System.out.println("Slider Fwd Switch: " + talon.isFwdLimitSwitchClosed() + " | Rev Switch: " + talon.isRevLimitSwitchClosed());
		System.out.println("Slider pot.getValue(): " + potentiometer.getValue());
	}
	
	public void logData() {
		Robot.table.putString("status", talon.getOutputVoltage() + "," + talon.getPosition() + "," + talon.getSpeed() + "," + talon.getClosedLoopError() + "\n");
	}
}