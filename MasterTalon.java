package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

// 700 RPM is full power

// Position unit is revolutions
// Velocity unit is radians/minute? :'( why CTRE?

public class MasterTalon {
	CANTalon talon;
	CANTalon other_talon;
	String mode;
	
	public MasterTalon(int port) {
		talon = new CANTalon(port);
		talon.reset();
	}
	
	public void init() {
		talon.clearStickyFaults();
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
		talon.enable();
		talon.enableControl();
		talon.configNominalOutputVoltage(-12.0f, +12.0f);
		talon.configMaxOutputVoltage(12.0f);
		talon.setVoltageRampRate(3.0f);
		
		talon.setEncPosition(0);
		talon.setPulseWidthPosition(0);
		
		talon.setForwardSoftLimit(3);
		talon.setReverseSoftLimit(-3);
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
			
		talon.setPID(0.8, 0.0, 8.0, 0.2441461, 0, 0, 0);
		
		talon.changeControlMode(CANTalon.TalonControlMode.Speed);
		
		
		talon.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		talon.setStatusFrameRateMs(StatusFrameRate.General, 1);
	}
	
	public void update() {
		System.out.println("getEncPosition() : " + talon.getEncPosition());
		System.out.println("getEncVelocity() (Native Unit - Tics per Min): " + talon.getEncVelocity());
		System.out.println("Speed (RPM): " + talon.getSpeed());
		System.out.println("Adjusted getEncVelocity: " + talon.getEncVelocity() * 600.0f / 4096);
		System.out.println("Adjusted getClosedLoopError(): " + talon.getClosedLoopError() * 600.0f / 4096);
		System.out.println("Percent Error: " + (talon.getClosedLoopError() * 600.0f / 4096) / talon.getSetpoint());
		
		talon.set(-600.0f);
		
		if(talon.isRevLimitSwitchClosed()) {
			talon.setPosition(0);
			talon.setEncPosition(0);
		}
	}	
	
	public void disable() {
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
	}
}