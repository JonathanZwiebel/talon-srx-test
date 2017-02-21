package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.StatusFrameRate;

import edu.wpi.first.wpilibj.Joystick;

/**
 * A class to test basic mechanical functionality and tune
 * loop values for the drivetrain on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikDrivetrain {	
	public static final double NATIVE_UPDATES = 100;														// From documentation
	public static final double NATIVE_RATE = 1000 / NATIVE_UPDATES;											// Calculated
	public static final double INCHES_TO_TICKS = 1400 / (2 * 3.1415 * 2);									// 2 very roughly taken from 4" diameter wheels									// Very roughly estimated
	public static final double INCHES_PER_SECOND_TO_TICKS_PER_SECOND = INCHES_TO_TICKS / NATIVE_RATE;		// Calculated

	String auto_action;
	
	CANTalon left_a; // Master
	CANTalon left_b;
	CANTalon left_c;
	CANTalon right_a;
	CANTalon right_b;
	CANTalon right_c; // Master
	Robot robot;
	
	Joystick drive_stick;
	Joystick turn_stick;
	
	CheeseSteikDrive csd;
	
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
		csd = new CheeseSteikDrive();
	}
	
	public void init() {
		left_a.configPeakOutputVoltage(+12.0f, -12.0f);
		right_c.configPeakOutputVoltage(+12.0f, -12.0f);

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
	}
	
	public void update() {
		csd.update(-drive_stick.getY(), turn_stick.getX(), turn_stick.getRawButton(1), true);
		
		float left_power = (float) csd.left_power;
		float right_power = (float) csd.right_power;
		
		left_power = Math.max(Math.min(left_power, SteikConstants.DRIVETRAIN_MAX_OUTPUT), -SteikConstants.DRIVETRAIN_MAX_OUTPUT);
		right_power = Math.max(Math.min(right_power, SteikConstants.DRIVETRAIN_MAX_OUTPUT), -SteikConstants.DRIVETRAIN_MAX_OUTPUT);
		
		left_a.set(left_power);
		right_c.set(right_power);
				
//		System.out.println("Left Drivetrain Voltage: " + left_a.getOutputVoltage());
//		System.out.println("Right Drivetrain Voltage: " + right_a.getOutputVoltage());
		System.out.println("Left Drivetrain Position: " + left_a.getPosition());
		System.out.println("Right Drivetrain Positon: " + right_c.getPosition());
		System.out.println("Left Drivetrain Speed: " + left_a.getSpeed());
		System.out.println("Right Drivtrain Speed: " + right_c.getSpeed());
		
		updateTable();
	}
	
	public void autoInit() {
		auto_action = "Drive Straight";
		setupDriveStraight();
	}
	
	public void setupDriveStraight() {
		left_a.setPosition(0.0f);
		right_c.setPosition(0.0f);	
		
		left_a.changeControlMode(CANTalon.TalonControlMode.Position);
		right_c.changeControlMode(CANTalon.TalonControlMode.Position);
		
		left_a.configPeakOutputVoltage(+8.0f, -8.0f);
		right_c.configPeakOutputVoltage(+8.0f, -8.0f);
		
		left_a.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		right_c.setPID(0.4f, 0.0025f, 8.0f, 0, 100, 0, 0);
		
		left_a.set(2800f);
		right_c.set(2800f);
		
		left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);		
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
		
		left_a.set(-400f);
		right_c.set(400f);
		
		left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);		
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
		
		left_a.set(800f);
		right_c.set(800f);
		
		left_a.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);
		right_c.setStatusFrameRateMs(StatusFrameRate.Feedback, 1);		
	}
	
	public void autoUpdate() {
		switch(auto_action) {
		case "Drive Straight":
			if(left_a.getSpeed() == 0 && right_c.getSpeed() == 0 && left_a.getClosedLoopError() <= 20 && right_c.getClosedLoopError() <= 20) {
				auto_action = "Turn Angle";
				setupTurnAngle();
			}
			break;
		case "Turn Angle":
			if(left_a.getSpeed() <= 10 && right_c.getSpeed() <= 10 && left_a.getClosedLoopError() <= 100 && right_c.getClosedLoopError() <= 100) {
				auto_action = "Drive End";
				setupDriveEnd();
			}
			break;
		case "Drive End":
			break;
		default:
			System.out.println("Illegal auto action");
			System.exit(1);
			break;
		}
		
		System.out.println("Left Drivetrain Position: " + left_a.getPosition());
		System.out.println("Right Drivetrain Positon: " + right_c.getPosition());
		System.out.println("Left Drivetrain Speed: " + left_a.getSpeed());
		System.out.println("Right Drivtrain Speed: " + right_c.getSpeed());
		Robot.table.putString("status", left_a.getOutputVoltage() + "," + right_c.getOutputVoltage() + "," + left_a.getPosition() + "," + right_c.getPosition() + "," + left_a.getSpeed() + "," + right_c.getSpeed() + "," + left_a.getClosedLoopError() + "," + right_c.getClosedLoopError() + "\n");
		updateTable();
	}
	
	public void updateTable() {
		Robot.dashboardTable.putString("driveSpeedUpdate", left_a.getSpeed() + ", " + right_c.getSpeed());
		Robot.dashboardTable.putString("leftdriveencoder", left_a.getPosition() + "");
		Robot.dashboardTable.putString("rightdriveencoder", right_c.getPosition() + "");	
	}
}
