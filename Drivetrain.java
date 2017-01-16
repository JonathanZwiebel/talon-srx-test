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
	//public static final double DISTANCE_PER_REV = 20.32;
	public static final double INCHES_TO_TICKS = 1400 / (2 * 3.1415 * 3.5);
	public static final double INCHES_TO_DEGREES = 42 / 180.0;
	
	private enum State {
		FORWARD_DRIVE,
		HUMAN_DRIVE,
		TURN_ANGLE,
		STOP
	}
	
	State state;
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
		//Sets the slave controllers to follow the masters
		left_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		left_slave.set(left_master.getDeviceID());
		right_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		right_slave.set(right_master.getDeviceID());

		
		//Sets the masters to use the encoders that are directly plugged into them
		left_master.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		right_master.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		left_master.reverseSensor(true);
		right_master.reverseOutput(true);
		
		
		//Zeroes encoders
		left_master.setEncPosition(0);
		right_master.setEncPosition(0);
		
		
		state = State.TURN_ANGLE;
		
		switch(state) {
		case FORWARD_DRIVE:
			right_master.setPID(0.8, 0, 0, 0, 0, 0, 0);
			left_master.setPID(0.8, 0, 0, 0, 0, 0, 0);
			left_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.setSetpoint(72 * INCHES_TO_TICKS);
			left_master.setSetpoint(72 * INCHES_TO_TICKS);
			break;
		case TURN_ANGLE:
			right_master.setPID(1.6, 0, 0, 0, 0, 0, 0);
			left_master.setPID(1.6, 0, 0, 0, 0, 0, 0);
			left_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.setSetpoint(40 * INCHES_TO_TICKS);
			left_master.setSetpoint(-40 * INCHES_TO_TICKS);
		default:
			System.out.println("No open-loop command");
			break;
		}
	}
	
	public void update() {
		System.out.println("Drivetrain Update");
		System.out.println(state);
		System.out.println("Left inches: "+ left_master.getPosition() / INCHES_TO_TICKS);
		System.out.println("Right inches: "+ right_master.getPosition() / INCHES_TO_TICKS);
		switch(state) {
		case HUMAN_DRIVE:
			double forward = drive_stick.getY() * -1;
			double turn = turn_stick.getX();
			
			double left = forward + turn;
			double right = forward - turn;
			
			right *= -1;
			
			left_master.set(left);
			right_master.set(right);
			break;
		case STOP:
			left_master.set(0);
			right_master.set(0);
		default:
			System.out.println("No State");
		}
	}
}
