package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Joystick;


/**
 * A class to test basic mechanical functionality and tune
 * loop values for the slider on our 2017 robot
 * 
 * @author Jonathan Zwiebel (frc8)
 *
 */
public class SteikSlider {
	// Aegir: 2172 RIGHT  |  3452 LEFT
	// Vali: 2036 RIGHT | 3314 LEFT
	
	public static final int RIGHT_POT_POS = 2036;
	public static final int LEFT_POT_POS = 3314;
	public static final float CENTER_POT_POS = (RIGHT_POT_POS + LEFT_POT_POS) / 2;
	
	// Revolutions with 0 as the center
	public static final float CENTER_SCORING_POS = 0.0f;
	public static final float LEFT_SCORING_POS = -1.0f;
	public static final float RIGHT_SCORING_POS = +1.0f;
	
	public static final int TOLERANCE = 40;
	
	AnalogInput potentiometer;
	CANTalon talon;
	String mode;
	Joystick stick;
	Robot robot;
	
	public SteikSlider(Robot robot) {
		this.robot = robot;
		talon = new CANTalon(SteikConstants.SLIDER_TALON_DEVICE_ID);
		stick = new Joystick(SteikConstants.SLIDER_STICK_PORT);
		potentiometer = new AnalogInput(SteikConstants.SLIDER_POTENTIOMETER_PORT);
		mode = "Human";
	}
	
	public void init() {		
		// Reset and turn on the Talon 
		//talon.reset();
		talon.clearStickyFaults();
		talon.enable();
		talon.enableControl();
		
		
		// Limit the Talon output
		talon.configMaxOutputVoltage(SteikConstants.SLIDER_MAX_OUTPUT);
		talon.configPeakOutputVoltage(+4.0f, -4.0f);
		talon.setVoltageRampRate(Integer.MAX_VALUE);
		

		// Set up the Talon to read from a relative CTRE mag encoder sensor
		talon.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);	
		float current_pot_pos = potentiometer.getValue();
		float distance_to_center = current_pot_pos - CENTER_POT_POS;
		float position_in_rev = (distance_to_center / 4096.0f) * 10.0f;
		talon.setPosition(-position_in_rev); // Negative because pot and encoder are different signage
		
		switch(mode) {
		case "Velocity":
			setVelocityMode();
			break;
		case "Constant":
			setConstantMode();
			break;
		case "Center Position":
			setCenterPositionMode();
			break;
		case "Left Position":
			setLeftPositionMode();
			break;
		case "Right Position":
			setRightPositionMode();
			break;
		case "Human":
			setHumanMode();
			break;
		case "Motion Magic":
			setMotionMagic();
			break;
		default:
			System.err.println("Illegal SteikSlider Mode!");
			System.exit(1);
		}		
	}
	
	public void update() {
		printData();
		logData();
		
		if(stick.getRawButton(2)) {
			mode = "Human";
			setHumanMode();
		}
		
		switch(mode) {
		case "Velocity":
			break;
		case "Constant":
			talon.set(0.0f);
			break;
		case "Center Position":
			if(positionLoopDone()) {
				mode = "Human";
				setHumanMode();
			}
			break;
		case "Left Position":
			if(positionLoopDone()) {
				mode = "Human";
				setHumanMode();
			}
			break;
		case "Right Position":
			if(positionLoopDone()) {
				mode = "Human";
				setHumanMode();
			}
			break;
		case "Human":
			if(stick.getRawButton(4)) {
				mode = "Left Position";
				setLeftPositionMode();
			}
			else if(stick.getRawButton(3)) {
				mode = "Center Position";
				setCenterPositionMode();
			}
			else if(stick.getRawButton(5)) {
				mode = "Right Position";
				setRightPositionMode();
			}
			else {
				talon.set(stick.getX() * SteikConstants.SLIDER_MAX_OUTPUT);
			}
			break;
		case "Motion Magic":
			break;
		default:
			System.err.println("Illegal MasterTalon Mode!");
			System.exit(1);
		}	
		
		updateTable();

		// Zero on reverse encoder trigger
//		if(talon.isRevLimitSwitchClosed()) {
//			talon.setPosition(0);
//			talon.setEncPosition(0);
//		}
	}	
	
	public void updateTable() {		
		Robot.dashboardTable.putString("sliderDistance", talon.getPosition() + "");
		Robot.dashboardTable.putString("speed-pos", talon.getSpeed() + "," + talon.getPosition());
		Robot.dashboardTable.putString("slider-pot", potentiometer.getValue() + "");
	}
	
	public void disable() {
		talon.enableForwardSoftLimit(false);
		talon.enableReverseSoftLimit(false);
	}
	
	public void printData() {	
		System.out.println("Slider getSetpoint(): " + talon.getSetpoint());
//		System.out.println("Slider getPulseWidthPosition() : " + talon.getPulseWidthPosition());
		System.out.println("Slider getEncPosition() : " + talon.getEncPosition());
		System.out.println("Slider getPositon(): " + talon.getPosition());
//		System.out.println("Slider getEncVelocity() (Native Unit - Tics per Min): " + talon.getEncVelocity());
		System.out.println("Slider getOutputVoltage() : " + talon.getOutputVoltage());
		System.out.println("Slider Speed (RPM): " + talon.getSpeed());
//		System.out.println("Slider Adjusted getEncVelocity: " + talon.getEncVelocity() * 600.0f / 4096);
//		System.out.println("Slider Adjusted getClosedLoopError(): " + talon.getClosedLoopError() * 600.0f / 4096);
//		System.out.println("Slider Percent Error: " + (talon.getClosedLoopError()) / talon.getSetpoint());
//		System.out.println("Slider Fwd Switch: " + talon.isFwdLimitSwitchClosed() + " | Rev Switch: " + talon.isRevLimitSwitchClosed());
		System.out.println("Slider pot.getValue(): " + potentiometer.getValue());
	}
	
	public void logData() {
		Robot.table.putString("status", talon.getOutputVoltage() + "," + talon.getPosition() + "," + talon.getSpeed() + "," + talon.getClosedLoopError() + "\n");
		
	}
	
	public boolean positionLoopDone() {
		boolean stopped = talon.getSpeed() == 0;
		boolean at_target = Math.abs(talon.getClosedLoopError()) < TOLERANCE; 
		
		return stopped && at_target;
	}
	
	public void setVelocityMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.Speed);
		talon.setPID(0, 0, 0, 0, 0, 0, 0);
		talon.set(0.0f);
	}
	
	public void setConstantMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
	}
	
	public void setCenterPositionMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.Position);
		talon.setPID(0.8, 0.0066, 8, 0, 120, 0, 0);
		talon.set(CENTER_SCORING_POS);
	}
	
	public void setLeftPositionMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.Position);
		talon.setPID(0.8, 0.0066, 8, 0, 120, 0, 0);
		talon.set(LEFT_SCORING_POS);
	}
	
	public void setRightPositionMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.Position);
		talon.setPID(0.8, 0.0066, 8, 0, 120, 0, 0);
		talon.set(RIGHT_SCORING_POS);
	}
	
	public void setHumanMode() {
		talon.changeControlMode(CANTalon.TalonControlMode.Voltage);
	}
	
	public void setMotionMagic() {
		talon.changeControlMode(CANTalon.TalonControlMode.MotionMagic);
		talon.setPID(0, 0, 0, 0, 0, 0, 0);
		talon.setMotionMagicAcceleration(0.0f);
		talon.setMotionMagicCruiseVelocity(0.0f);
	}
}