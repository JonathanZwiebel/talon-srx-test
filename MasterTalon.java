package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

// 700 RPM is full power

// Position unit is revolutions
// Velocity unit is radians/minute? :'( why CTRE?

public class MasterTalon {
	CANTalon talon;
	CANTalon other_talon;
	String mode;
	
	float start;
	
	public MasterTalon(int port) {
		talon = new CANTalon(port);
		mode = "Motion Magic";
	}
	
	public void init() {
		start = System.currentTimeMillis();
		
		// Reset and turn on the Talon 
		talon.reset();
		talon.clearStickyFaults();
		talon.enable();
		talon.enableControl();
		
		// Limit the Talon output to 12V with a ramping rate of 3 volts
		talon.configMaxOutputVoltage(12.0f);
		talon.setVoltageRampRate(Integer.MAX_VALUE);
		
		// Set up the Talon to read from a relative CTRE mag encoder sensor
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
		talon.setEncPosition(0);
		talon.setPulseWidthPosition(0);
			
		// Set up Talon update rate 
		talon.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		talon.setStatusFrameRateMs(StatusFrameRate.General, 1);
		
		switch(mode) {
		case "Velocity":
			talon.changeControlMode(CANTalon.TalonControlMode.Speed);
			talon.setPID(0.2f, 0.0f, 2f, 0.2441461f, 0, 0, 0);
			talon.set(360.0f);
			break;
		case "Constant":
			talon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
			break;
		case "Position":
			talon.changeControlMode(CANTalon.TalonControlMode.Position);
			// F in a control loop is a solid addition
			talon.setPID(0.3f, 0.002f, 2.0f, 0, 500, 0, 0);
			talon.configPeakOutputVoltage(+3.0f, -3.0f);
			talon.set(1);
			break;
		case "Motion Magic":
			talon.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
			talon.setPID(0.2f, 0.002f, 2.0f, 0.2441461f, 0, 0, 0);
			talon.configPeakOutputVoltage(+3.0f, -3.0f);
			talon.setMotionMagicAcceleration(60.0f);
			talon.setMotionMagicCruiseVelocity(60.0f);
			talon.set(8000);
			break;
		default:
			System.err.println("Illegal MasterTalon Mode!");
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
			talon.set(0.2f);
			break;
		case "Position":
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
		System.out.println("getEncPosition() : " + talon.getEncPosition());
		System.out.println("getEncVelocity() (Native Unit - Tics per Min): " + talon.getEncVelocity());
		System.out.println("Speed (RPM): " + talon.getSpeed());
		System.out.println("Adjusted getEncVelocity: " + talon.getEncVelocity() * 600.0f / 4096);
		System.out.println("Adjusted getClosedLoopError(): " + talon.getClosedLoopError() * 600.0f / 4096);
		System.out.println("Percent Error: " + (talon.getClosedLoopError()) / talon.getSetpoint());
		System.out.println("Fwd Switch: " + talon.isFwdLimitSwitchClosed() + " | Rev Switch: " + talon.isRevLimitSwitchClosed());
	}
	
	public void logData() {
		Robot.table.putString("status", talon.getOutputVoltage() + "," + talon.getPosition() + "," + talon.getSpeed() + "," + talon.getClosedLoopError() + "\n");
	}
}