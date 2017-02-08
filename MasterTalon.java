package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

public class MasterTalon {
	CANTalon talon;
	CANTalon other_talon;
	String mode;
	
	public MasterTalon(int port) {
		talon = new CANTalon(7);
		talon.reset();
		talon.clearStickyFaults();
	}
	
	public void init() {
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
		talon.enable();
		talon.enableControl();
		talon.configNominalOutputVoltage(-12.0f, +12.0f);
		talon.configMaxOutputVoltage(12.0f);
		talon.setVoltageRampRate(3.0f);
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
		
		talon.setEncPosition(0);
		
		talon.setForwardSoftLimit(3);
		talon.setReverseSoftLimit(-3);
		
		talon.enableForwardSoftLimit(true);
		talon.enableReverseSoftLimit(true);
		
		talon.enableForwardSoftLimit(true);
		talon.enableReverseSoftLimit(true);
		
		talon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
	}
	
	public void update() {
		System.out.println("getPosition(): " + talon.getPosition());
		System.out.println("getEncPosition() : " + talon.getEncPosition());
		System.out.println("getPulseWidthPosition(): " + talon.getPulseWidthPosition());
		System.out.println("getEncVelocity(): " + talon.getEncVelocity());
		System.out.println("isFwdLimitSwitchClosed(): " + talon.isFwdLimitSwitchClosed());
		System.out.println("isRevLimitSwitchClosed(): " + talon.isRevLimitSwitchClosed());
		talon.set(0.25f);
	}	
	
	public void disable() {
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
	}
}