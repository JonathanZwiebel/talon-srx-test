package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * A class to test basic pneumatic functionality on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikSpatula {
	Joystick stick;
	DoubleSolenoid solenoid;
	Robot robot;
	
	public SteikSpatula(Robot robot) {
		this.robot = robot;
		solenoid = new DoubleSolenoid(0, 1);
		stick = new Joystick(SteikConstants.SLIDER_STICK_PORT);
	}
	
	public void init() {
		
	}
	
	public void update() {
		if(stick.getRawButton(8)) { // Up
			robot.steik_slider.talon.enableControl();
			solenoid.set(DoubleSolenoid.Value.kForward);
		}
		if(stick.getRawButton(9)) { // Down
			robot.steik_slider.talon.disableControl();
			solenoid.set(DoubleSolenoid.Value.kReverse);
		}
	}
}