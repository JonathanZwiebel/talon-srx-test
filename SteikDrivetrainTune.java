package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;

/**
 * A class to test basic mechanical functionality and tune
 * loop values for the drivetrain on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikDrivetrainTune {	
	public static final double NATIVE_UPDATES = 100;														// From documentation
	public static final double NATIVE_RATE = 1000 / NATIVE_UPDATES;											// Calculated
	public static final double TICKS_PER_INCH = 360 / (3.95 * 3.1415);										// 3.95" wheels	
	public static final double SPEED_UNIT_CONVERSION = TICKS_PER_INCH / NATIVE_RATE;						// Calculated
	public static final double INCHES_PER_DEGREE = 21.5f / 90.0f;											// Measured
	
	CANTalon left_a; // Master
	CANTalon left_b;
	CANTalon left_c;
	CANTalon right_a;
	CANTalon right_b;
	CANTalon right_c; // Master
	Robot robot;
	
	Joystick drive_stick;
	Joystick turn_stick;	
	
	PowerDistributionPanel pdp = new PowerDistributionPanel();
	
	public SteikDrivetrainTune(Robot robot) {
		this.robot = robot;
		left_a = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_A_TALON_DEVICE_ID);
		left_b = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_B_TALON_DEVICE_ID);
		left_c = new CANTalon(SteikConstants.DRIVETRAIN_LEFT_C_TALON_DEVICE_ID);
		right_a = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_A_TALON_DEVICE_ID);
		right_b = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_B_TALON_DEVICE_ID);
		right_c = new CANTalon(SteikConstants.DRIVETRAIN_RIGHT_C_TALON_DEVICE_ID);
		drive_stick = new Joystick(SteikConstants.DRIVE_STICK_PORT);
		turn_stick = new Joystick(SteikConstants.TURN_STICK_PORT);
		
		left_a.configMaxOutputVoltage(+12.0f);
		left_b.configMaxOutputVoltage(+12.0f);
		left_c.configMaxOutputVoltage(+12.0f);
		right_a.configMaxOutputVoltage(+12.0f);
		right_b.configMaxOutputVoltage(+12.0f);
		right_c.configMaxOutputVoltage(+12.0f);
	}
	
	public void init() {

		left_a.setCloseLoopRampRate(Integer.MAX_VALUE);
		right_c.setCloseLoopRampRate(Integer.MAX_VALUE);
		
		left_a.setVoltageRampRate(Integer.MAX_VALUE);
		right_c.setCloseLoopRampRate(Integer.MAX_VALUE);
		
		left_a.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		right_c.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);		
		
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
		

		left_a.reverseOutput(false);
		right_c.reverseOutput(true);
		
		left_a.setInverted(false);
		right_c.setInverted(true);
		
		left_a.reverseSensor(false);
		right_c.reverseSensor(true);
		
		left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);

		left_a.configPeakOutputVoltage(+8.0f, -8.0f);
		right_c.configPeakOutputVoltage(+8.0f, -8.0f);
		
		// GENERAL CLOSED-LOOP
		// MAX_OUTPUT = 8.0
		// MIN_OUTPUT = -8.0
		
		// POSITION
		// P = 0.5
		// I = 0.0025
		// D = 12.0
		// F = 0
		// Izone = 125
		
		// VELOCITY
		// P = 6.0
		// I = 0.002
		// D = 85
		// F = 2.624
		// Izone = 800
		
		// STRAIGHT MOTION PROFILE
		// P = 4.5
		// I = 0.01
		// D = 150
		// F = 2.5
		// Izone = 25
		// Acceleration = 36 in/s^2
		// Cruise Velocity = 36 in/s
		
		// TURNING MOTION PROFILE
		// P = 4.5
		// I = 0.01
		// D = 150
		// F = 2.5
		// Izone = 25
		// Acceleration = 144 in/s^2
		// Cruise Velocity = 72 in/s
		// Clockwise is left positive and right negative
		
//		left_a.setPID(0.5, 0.0025, 12.0, 0, 125, 0, 0);
//		right_c.setPID(0.5, 0.0025, 12.0, 0, 125, 0, 0);
//		left_a.changeControlMode(CANTalon.TalonControlMode.Position);
//		right_c.changeControlMode(CANTalon.TalonControlMode.Position);
//		left_a.set(120 * TICKS_PER_INCH);
//		right_c.set(120 * TICKS_PER_INCH);
		
//		left_a.setPID(6.0, 0.002, 85, 2.624, 800, 0, 0);
//		right_c.setPID(6.0, 0.002, 85, 2.624, 800, 0, 0);
//		left_a.chaeControlMode(CANTalon.TalonControlMode.Speed);
//		right_c.changeControlMode(CANTalon.TalonControlMode.Speed);
//		left_a.set(-24 * SPEED_UNIT_CONVERSION);
//		right_c.set(24 * SPEED_UNIT_CONVERSION);
		
//		left_a.setPID(4.5, 0.01, 150, 2.5, 25, 0, 0);
//		right_c.setPID(4.5, 0.01, 150, 2.5, 25, 0, 0);
//		left_a.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
//		right_c.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
//		left_a.setMotionMagicAcceleration(36 * SPEED_UNIT_CONVERSION);
//		right_c.setMotionMagicAcceleration(36* SPEED_UNIT_CONVERSION);
//		left_a.setMotionMagicCruiseVelocity(36 * SPEED_UNIT_CONVERSION);
//		right_c.setMotionMagicCruiseVelocity(36 * SPEED_UNIT_CONVERSION);
//		left_a.set(180 * TICKS_PER_INCH);
//		right_c.set(180 * TICKS_PER_INCH);
	
//		left_a.setPID(4.5, 0.01, 150, 2.5, 25, 0, 0);
//		right_c.setPID(4.5, 0.01, 150, 2.5, 25, 0, 0);
//		left_a.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
//		right_c.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
//		left_a.setMotionMagicAcceleration(144 * SPEED_UNIT_CONVERSION);
//		right_c.setMotionMagicAcceleration(144 * SPEED_UNIT_CONVERSION);
//		left_a.setMotionMagicCruiseVelocity(72 * SPEED_UNIT_CONVERSION);
//		right_c.setMotionMagicCruiseVelocity(72 * SPEED_UNIT_CONVERSION);		
//		left_a.set(360 * INCHES_PER_DEGREE * TICKS_PER_INCH);
//		right_c.set(360 * -INCHES_PER_DEGREE * TICKS_PER_INCH);
	}
	
	public void update() {
//		System.out.println("Left Drivetrain Voltage: " + left_a.getOutputVoltage());
//		System.out.println("Right Drivetrain Voltage: " + right_a.getOutputVoltage());
		System.out.println("Left Drivetrain Position: " + left_a.getPosition() / TICKS_PER_INCH);
		System.out.println("Right Drivetrain Positon: " + right_c.getPosition() / TICKS_PER_INCH);
		System.out.println("Left Drivtrain Speed: " + left_a.getSpeed() / SPEED_UNIT_CONVERSION);
		System.out.println("Right Drivtrain Speed: " + right_c.getSpeed() / SPEED_UNIT_CONVERSION);
		
		Robot.table.putString("status", left_a.getOutputVoltage() + "," + right_c.getOutputVoltage() + "," + left_a.getPosition()  + "," + right_c.getPosition() + "," + left_a.getSpeed()  + "," + right_c.getSpeed() + "," + left_a.getClosedLoopError()  + "," + right_c.getClosedLoopError() + "\n");
		updateTable();
	}
	
	public void setupDriveStraight() {
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);	
		
		left_a.changeControlMode(CANTalon.TalonControlMode.Position);
		right_c.changeControlMode(CANTalon.TalonControlMode.Position);
		
		left_a.configPeakOutputVoltage(+6.0f, -6.0f);
		right_c.configPeakOutputVoltage(+6.0f, -6.0f);
		
		left_a.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		right_c.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		
		//left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		//right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);	
		
		try {
			Thread.sleep(101);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setupTurnAngle() {
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);	
		
		left_a.changeControlMode(CANTalon.TalonControlMode.Position);
		right_c.changeControlMode(CANTalon.TalonControlMode.Position);
		
		left_a.configPeakOutputVoltage(+8.0f, -8.0f);
		right_c.configPeakOutputVoltage(+8.0f, -8.0f);
		
		left_a.setPID(0.75f, 0.002f, 9.0f, 0.05f, 150, 0, 0);
		right_c.setPID(0.75f, 0.002f, 9.0f, 0.05f, 150, 0, 0);
		
		//left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		//right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);	
		
		try {
			Thread.sleep(101);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setupDriveEnd() {
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);	
		
		left_a.changeControlMode(CANTalon.TalonControlMode.Position);
		right_c.changeControlMode(CANTalon.TalonControlMode.Position);
		
		left_a.configPeakOutputVoltage(+8.0f, -8.0f);
		right_c.configPeakOutputVoltage(+8.0f, -8.0f);
		
		left_a.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		right_c.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		
		//left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		//right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		
		try {
			Thread.sleep(101);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setupVelocityHold() {
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);	
		
		left_a.changeControlMode(CANTalon.TalonControlMode.Speed);
		right_c.changeControlMode(CANTalon.TalonControlMode.Speed);
		right_c.reverseOutput(false);
		
		left_a.configPeakOutputVoltage(+8.0f, -8.0f);
		right_c.configPeakOutputVoltage(+8.0f, -8.0f);
		
		left_a.setPID(0, 0, 0, 1.02711f, 0, 0, 0);
		right_c.setPID(0, 0, 0, 1.02711f, 0, 0, 0);
		
		//left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		//right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		
		try {
			Thread.sleep(101);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disabledInit() {
		
	}
	
	public void updateTable() {
		Robot.dashboardTable.putString("driveSpeedUpdate", left_a.getSpeed() + ", " + right_c.getSpeed());
		Robot.dashboardTable.putString("leftdriveencoder", left_a.getPosition() + "");
		Robot.dashboardTable.putString("rightdriveencoder", right_c.getPosition() + "");	
	}
}
