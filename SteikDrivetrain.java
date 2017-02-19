package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Joystick;

/**
 * A class to test basic mechanical functionality and tune
 * loop values for the drivetrain on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikDrivetrain {	
	CANTalon left_a; // Master
	CANTalon left_b;
	CANTalon left_c;
	CANTalon right_a;
	CANTalon right_b;
	CANTalon right_c; // Master
	Robot robot;
	
	Joystick drive_stick;
	Joystick turn_stick;
	
	public SteikDrivetrain(Robot robot) {
		this.robot = robot;
		left_a = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_A_TALON_DEVICE_ID);
		left_b = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_B_TALON_DEVICE_ID);
		left_c = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_C_TALON_DEVICE_ID);
		right_a = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_A_TALON_DEVICE_ID);
		right_b = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_B_TALON_DEVICE_ID);
		right_c = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_C_TALON_DEVICE_ID);
		drive_stick = new Joystick(SteikConstants.DRIVE_STICK_PORT);
		turn_stick = new Joystick(SteikConstants.TURN_STICK_PORT);
	}
	
	public void init() {
		left_a.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
		right_c.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
		
		left_b.changeControlMode(CANTalon.TalonControlMode.Follower);
		left_c.changeControlMode(CANTalon.TalonControlMode.Follower);
		left_b.set(left_a.getDeviceID());
		left_c.set(left_a.getDeviceID());
		
		right_a.changeControlMode(CANTalon.TalonControlMode.Follower);
		right_b.changeControlMode(CANTalon.TalonControlMode.Follower);
		right_a.set(right_c.getDeviceID());
		right_b.set(right_c.getDeviceID());
		
		left_a.setPosition(0);
		right_c.setPosition(0);
		
		left_a.reverseOutput(true);
		right_c.reverseSensor(true);
	}
	
	public void update() {
		float fwd_power = (float) drive_stick.getY();
		float turn_power = (float) turn_stick.getX();
		
		float left_power = fwd_power - turn_power;
		float right_power = fwd_power + turn_power;
		
		left_power = Math.max(Math.min(left_power, SteikConstants.DRIVETRAIN_MAX_OUTPUT), -SteikConstants.DRIVETRAIN_MAX_OUTPUT);
		right_power = Math.max(Math.min(right_power, SteikConstants.DRIVETRAIN_MAX_OUTPUT), -SteikConstants.DRIVETRAIN_MAX_OUTPUT);
		
		left_a.set(left_power);
		right_c.set(right_power);
		
		Robot.dashboardTable.putString("driveSpeedUpdate", left_a.getSpeed() + ", " + right_a.getSpeed());
		Robot.dashboardTable.putString("leftdriveencoder", left_a.getPosition());
		Robot.dashboardTable.putString("rightdriveencoder", right_a.getPosition());
		
//		System.out.println("Left Drivetrain Voltage: " + left_a.getOutputVoltage());
//		System.out.println("Right Drivetrain Voltage: " + right_a.getOutputVoltage());
//		System.out.println("Left Drivetrain Position: " + left_a.getPosition());
//		System.out.println("Right Drivetrain Positon: " + right_c.getPosition());
//		System.out.println("Left Drivetrain Speed: " + left_a.getSpeed());
//		System.out.println("Right Drivtrain Speed: " + right_c.getSpeed());
	}
}
