package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.MotionProfileStatus;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

// Author: Jonathan Zwiebel
public class Drivetrain {
	public static final int DERICA_LEFT_MASTER = 3;
	public static final int DERICA_LEFT_SLAVE = 2;
	public static final int DERICA_RIGHT_MASTER = 1;
	public static final int DERICA_RIGHT_SLAVE = 4;
	public static final int DRIVE_STICK = 0;
	public static final int TURN_STICK = 1;
	
	public static final double NATIVE_UPDATES = 100;														// From documentation
	public static final double NATIVE_RATE = 1000 / NATIVE_UPDATES;											// Calculated
	public static final double INCHES_TO_TICKS = 1400 / (2 * 3.1415 * 3.5);									// 3.5 very roughly taken from 7" diameter wheels
	public static final double INCHES_TO_DEGREES = 42 / 180.0;												// Very roughly estimated
	public static final double INCHES_PER_SECOND_TO_TICKS_PER_SECOND = INCHES_TO_TICKS / NATIVE_RATE;		// Calculated
	
	boolean verbose = false;
	NetworkTable table;
	
	// The maximum voltage that the motors will output in all code execution
	public double PEAK_VOLTAGE = 12.0f;
	
	private enum State {
		FORWARD_DRIVE,
		HUMAN_DRIVE,
		TURN_ANGLE,
		VELOCITY_TARGET,
		MOTION_PROFILE,
		STOP
	}
	
	State state;
	Joystick drive_stick;
	Joystick turn_stick;
	CANTalon left_master;
	CANTalon left_slave;
	CANTalon right_master;
	CANTalon right_slave;
	
	Notifier notifier;
	
	public Drivetrain() {
		drive_stick = new Joystick(DRIVE_STICK);
		turn_stick = new Joystick(TURN_STICK);
		left_master = new CANTalon(DERICA_LEFT_MASTER);
		left_slave = new CANTalon(DERICA_LEFT_SLAVE);
		right_master = new CANTalon(DERICA_RIGHT_MASTER);
		right_slave = new CANTalon(DERICA_RIGHT_SLAVE);
		notifier = new Notifier(new PeriodicRunnable());
		table = NetworkTable.getTable("robot_table_2");
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
		
		left_master.configPeakOutputVoltage(PEAK_VOLTAGE, -PEAK_VOLTAGE);
		right_master.configPeakOutputVoltage(PEAK_VOLTAGE, -PEAK_VOLTAGE);
		
		
		state = State.VELOCITY_TARGET;
		
		left_master.changeMotionControlFramePeriod(5);
		right_master.changeMotionControlFramePeriod(5);
		notifier.startPeriodic(0.005);
		
		switch(state) {
		case HUMAN_DRIVE:
			left_master.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
			right_master.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
//			left_master.setVoltageRampRate(12.3);
//			right_master.setVoltageRampRate(12.3);
			
			left_master.enableForwardSoftLimit(false);
			left_master.enableReverseSoftLimit(false);
			right_master.enableForwardSoftLimit(false);
			right_master.enableReverseSoftLimit(false);

//			left_master.setForwardSoftLimit(15 * INCHES_TO_TICKS);
//			left_master.setReverseSoftLimit(-15 * INCHES_TO_TICKS);
//			right_master.setForwardSoftLimit(15 * INCHES_TO_TICKS);
//			right_master.setReverseSoftLimit(-15 * INCHES_TO_TICKS);
//			left_master.enableForwardSoftLimit(true);
//			left_master.enableReverseSoftLimit(true);
//			right_master.enableReverseSoftLimit(true);
//			right_master.enableForwardSoftLimit(true);
			
			break;
		case VELOCITY_TARGET:
			right_master.setPID(4.0, 0, 25.0, 2.122, 0, 0, 0); // Tuned via CTRE method (no steady-state)
			left_master.setPID(4.0, 0, 25.0, 2.122, 0, 0, 0); // Tuned via CTRE method (no steady-state)
			left_master.changeControlMode(CANTalon.TalonControlMode.Speed);
			right_master.changeControlMode(CANTalon.TalonControlMode.Speed);
			
			right_master.setSetpoint(-18 * INCHES_PER_SECOND_TO_TICKS_PER_SECOND); // -10 needed to match native Talon value
			left_master.setSetpoint(-18 * INCHES_PER_SECOND_TO_TICKS_PER_SECOND); // -10 needed to match native Talon value
			break;
		case FORWARD_DRIVE:
			right_master.setPID(0.4, 0, 4, 0, 0, 0, 0); // Semi-tuned
			left_master.setPID(0.4, 0, 4, 0, 0, 0, 0); // Semi-tuned
			left_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.setSetpoint(-72 * INCHES_TO_TICKS);
			left_master.setSetpoint(-72 * INCHES_TO_TICKS);
			break;
		case TURN_ANGLE:
			right_master.setPID(1.6, 0, 0, 0, 0, 0, 0); // Semi-tuned
			left_master.setPID(1.6, 0, 0, 0, 0, 0, 0); // Semi-tuned
			left_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.changeControlMode(CANTalon.TalonControlMode.Position);
			right_master.setSetpoint(40 * INCHES_TO_TICKS);
			left_master.setSetpoint(-40 * INCHES_TO_TICKS);
			break;
		case MOTION_PROFILE:
			right_master.changeControlMode(CANTalon.TalonControlMode.MotionProfile);
			left_master.changeControlMode(CANTalon.TalonControlMode.MotionProfile);
			fill1DProfile(DrivetrainMotionProfile.Points, DrivetrainMotionProfile.kNumPoints);
		default:
			System.out.println("No open-loop command");
			break;
		}
		
		// Brake mode on while the robot is in use
		left_master.enableBrakeMode(true);
		left_slave.enableBrakeMode(true);
		right_master.enableBrakeMode(true);
		right_slave.enableBrakeMode(true);
	}
	
	public void update() {	
		
		System.out.println("Drivetrain Update");
		//System.out.println(state);
		//System.out.println("Left inches: "+ left_master.getPosition() / INCHES_TO_TICKS);
		//System.out.println("Right inches: "+ right_master.getPosition() / INCHES_TO_TICKS);
		System.out.println("Target: " + 5 * INCHES_TO_TICKS);
		System.out.println("Left speed: " + left_master.getSpeed());
		System.out.println("Right speed: " + right_master.getSpeed());
		System.out.println("Left error: " + left_master.getClosedLoopError());
		System.out.println("Right error: " + right_master.getClosedLoopError());
		
		try {
			table.putString("data_table", "" + (5 * INCHES_TO_TICKS) + "," + left_master.getSpeed() + "," + right_master.getSpeed() + "\n");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
		//System.out.println("Left outputVoltageDrop: " + left_master.getOutputVoltage());
		//System.out.println("Right outputVoltageDrop: " + right_master.getOutputVoltage());

		//System.out.println("Left percentVBus: " + left_master.getOutputVoltage() / left_master.getBusVoltage());
		//System.out.println("Right percentVBus: " + right_master.getOutputVoltage() / right_master.getBusVoltage());	
		
		if(verbose) {
			System.out.println("Left currentAmps: " + left_master.getOutputCurrent());
			System.out.println("Left outputVoltageDrop: " + left_master.getOutputVoltage());
			System.out.println("Left busVoltageDrop: " + left_master.getBusVoltage());
			System.out.println("Left outputPercent: " + left_master.getOutputVoltage() / left_master.getBusVoltage());
		}
				
		switch(state) {
		case HUMAN_DRIVE:
			double forward = drive_stick.getY() * -1;
			double turn = turn_stick.getX();
			
			double left = forward + turn;
			double right = forward - turn;
			
			right *= -1;
			
			System.out.println("Left: " + left);
			System.out.println("Right: " + right);
			
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
	
	public void disable() {
		left_master.enableBrakeMode(false);
		left_slave.enableBrakeMode(false);
		right_master.enableBrakeMode(false);
		right_slave.enableBrakeMode(false);
	}
	
	public void fill1DProfile(double[][] motion_profile, int count) {
		CANTalon.TrajectoryPoint point = new CANTalon.TrajectoryPoint();
		
		left_master.clearMotionProfileTrajectories();
		right_master.clearMotionProfileTrajectories();
		
		for(int i = 0; i < count; i++) {
			point.position = motion_profile[i][0];
			point.velocity = motion_profile[i][1];
			point.timeDurMs = (int) motion_profile[i][2];
			point.profileSlotSelect = 0;
			point.velocityOnly = false;

			if(i == 0) {
				point.zeroPos = true;
			}
			else {
				point.zeroPos = false;
			}
			
			if(i + 1 == count) {
				point.isLastPoint = true;
			}
			else {
				point.isLastPoint = false;
			}
			left_master.pushMotionProfileTrajectory(point);
			right_master.pushMotionProfileTrajectory(point);
		}
	}
	
	class PeriodicRunnable implements java.lang.Runnable {
		public void run() {
			left_master.processMotionProfileBuffer();
			right_master.processMotionProfileBuffer();
		}
	}
}
