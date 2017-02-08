package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Joystick;

//Author: Jonathan Zwiebel
public class Intake {
	public static final int DERICA_MECANUM = 7;
	public static final int DERICA_STORAGE = 8;
	public static final int INTAKE_STICK = 2;
	
	public static final int MECANUM_OUT_BUTTON = 5;
	public static final int MECANUM_IN_BUTTON = 3;
	public static final int STORAGE_OUT_BUTTON = 6;
	public static final int STORAGE_IN_BUTTON = 4;
	
	CANTalon mecanum_motor;
	CANTalon storage_motor;
	Joystick intake_stick;
	
	public Intake() {
		intake_stick = new Joystick(INTAKE_STICK);
		mecanum_motor = new CANTalon(DERICA_MECANUM);
		storage_motor = new CANTalon(DERICA_STORAGE);
	}
	
	public void init() {
		
	}
	
	public void update() {
		float intake_power_boost = (float) intake_stick.getY();
		float mecanum_power_boost = (float) intake_stick.getX();
		
		if(intake_power_boost < 0.02f && intake_power_boost > -0.02f) {
			intake_power_boost = 0;
		}
		
		if(mecanum_power_boost < 0.02f && mecanum_power_boost > -0.02f) {
			mecanum_power_boost = 0;
		}
		
		if(intake_stick.getRawButton(MECANUM_OUT_BUTTON)) {
			mecanum_motor.set(1.0f);
		}
		else if(intake_stick.getRawButton(MECANUM_IN_BUTTON)) {
			mecanum_motor.set(-1.0f);
		}
		else {
			mecanum_motor.set(mecanum_power_boost);
		}
		
		if(intake_stick.getRawButton(STORAGE_OUT_BUTTON)) {
			storage_motor.set(1.0f);
		}
		else if(intake_stick.getRawButton(STORAGE_IN_BUTTON)) {
			storage_motor.set(-1.0f);
		}
		else {
			storage_motor.set(intake_power_boost);
		}
	}
}
