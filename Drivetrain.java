package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;

public class Drivetrain {
	public static final int DERICA_LEFT_A = 3;
	public static final int DERICA_LEFT_B = 2;
	public static final int DERICA_RIGHT_A = 1;
	public static final int DERICA_RIGHT_B = 4;
	public static final int DRIVE_STICK = 0;
	public static final int TURN_STICK = 1;
	public static final double DISTANCE_PER_REV = 20.32;
	
	String state;
	
	Joystick drive_stick;
	Joystick turn_stick;
	CANTalon left_master;
	CANTalon left_slave;
	CANTalon right_master;
	CANTalon right_slave;
	
	public Drivetrain() {
		drive_stick = new Joystick(DRIVE_STICK);
		turn_stick = new Joystick(TURN_STICK);
		left_master = new CANTalon(DERICA_LEFT_A);
		left_slave = new CANTalon(DERICA_LEFT_B);
		right_master = new CANTalon(DERICA_RIGHT_A);
		right_slave = new CANTalon(DERICA_RIGHT_B);
	}
	
	public void init() {
		System.out.println("Drivetrain Init");
		left_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		left_slave.set(left_master.getDeviceID());
		right_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		right_slave.set(right_master.getDeviceID());
		left_master.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		right_master.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		
		left_master.configEncoderCodesPerRev(360);
		right_master.configEncoderCodesPerRev(360);
		
		left_master.setEncPosition(0);
		right_master.setEncPosition(0);
		
		right_master.reverseSensor(true);
		
		state = "Human Drive";
	}
	
	public void update() {
		System.out.println("Drivetrain Update");
		System.out.println(state);
		System.out.println("Left: "+ left_master.getEncPosition() * DISTANCE_PER_REV);
		System.out.println("Right: "+ right_master.getEncPosition() * DISTANCE_PER_REV);
		switch(state) {
		case "Human Drive":
			double forward = drive_stick.getY();
			double turn = turn_stick.getX();
			
			double left = forward + turn;
			double right = forward - turn;
			
			right *= -1;
			
			left_master.set(left);
			right_master.set(right);
			break;
		case "Stop":
			left_master.set(0);
			right_master.set(0);
		default:
			System.out.println("No State");
		}
	}
}
