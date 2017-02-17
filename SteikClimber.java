package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;

/**
 * A class to test basic mechanical functionality and tune
 * loop values for the climber on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikClimber {
	
	CANTalon talon;	
	Joystick stick;
	PowerDistributionPanel pdp;
	
	public SteikClimber() {
		stick = new Joystick(SteikConstants.CLIMBER_STICK_PORT);
		talon = new CANTalon(SteikConstants.CLIMBER_TALON_DEVICE_ID);
		pdp = new PowerDistributionPanel();
	}
	
	public void init() {
		talon.changeControlMode(CANTalon.TalonControlMode.Voltage);
		talon.ConfigRevLimitSwitchNormallyOpen(true); // Prevent the motor from driving backwards
	}
	
	public void update() {
		float output = (float) stick.getY() * SteikConstants.SLIDER_MAX_OUTPUT;
		talon.set(output);
		
//		System.out.println("Climber Talon Voltage: " + talon.getOutputVoltage());
//		System.out.println("Climber Talon Speed: " + talon.getSpeed());
//		System.out.println("Climber Talon Current: " + talon.getOutputCurrent());
//		System.out.println("Climber PDP Current: " + pdp.getCurrent(SteikConstants.CLIMBER_PDP_PORT));
	}
}
