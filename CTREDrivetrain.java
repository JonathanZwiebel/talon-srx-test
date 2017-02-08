/**
 * This Java FRC robot application is meant to demonstrate an example using the Motion Profile control mode
 * in Talon SRX.  The CANTalon class gives us the ability to buffer up trajectory points and execute them
 * as the roboRIO streams them into the Talon SRX.
 * 
 * There are many valid ways to use this feature and this example does not sufficiently demonstrate every possible
 * method.  Motion Profile streaming can be as complex as the developer needs it to be for advanced applications,
 * or it can be used in a simple fashion for fire-and-forget actions that require precise timing.
 * 
 * This application is an IterativeRobot project to demonstrate a minimal implementation not requiring the command 
 * framework, however these code excerpts could be moved into a command-based project.
 * 
 * The project also includes instrumentation.java which simply has debug printfs, and a MotionProfile.java which is generated
 * in @link https://docs.google.com/spreadsheets/d/1PgT10EeQiR92LNXEOEe3VGn737P7WDP4t0CQxQgC8k0/edit#gid=1813770630&vpid=A1
 * 
 * Logitech Gamepad mapping, use left y axis to drive Talon normally.  
 * Press and hold top-left-shoulder-button5 to put Talon into motion profile control mode.
 * This will start sending Motion Profile to Talon while Talon is neutral. 
 * 
 * While holding top-left-shoulder-button5, tap top-right-shoulder-button6.
 * This will signal Talon to fire MP.  When MP is done, Talon will "hold" the last setpoint position
 * and wait for another button6 press to fire again.
 * 
 * Release button5 to allow OpenVoltage control with left y axis.
 */

package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;

public class CTREDrivetrain {

	/** The Talon we want to motion profile. */
	CANTalon _right_talon = new CANTalon(1);
	CANTalon _right_talon_slave = new CANTalon(4);
	CANTalon _left_talon = new CANTalon(3);
	CANTalon _left_talon_slave = new CANTalon(2);

	/** some example logic on how one can manage an MP */
	CTREMotionProfileFollower _example_right = new CTREMotionProfileFollower(_right_talon);
	CTREMotionProfileFollower _example_left = new CTREMotionProfileFollower(_left_talon);

	
	/** joystick for testing */
	Joystick _joy= new Joystick(1);

	/** cache last buttons so we can detect press events.  In a command-based project you can leverage the on-press event
	 * but for this simple example, lets just do quick compares to prev-btn-states */
	boolean [] _btnsLast = {false,false,false,false,false,false,false,false,false,false};
	
	public CTREDrivetrain() {
		
	}
	
	public void init() {
		System.out.println("Drivetrain Init");
		//Sets the slave controllers to follow the masters
		_left_talon_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		_left_talon_slave.set(_left_talon.getDeviceID());
		_right_talon_slave.changeControlMode(CANTalon.TalonControlMode.Follower);
		_right_talon_slave.set(_right_talon.getDeviceID());
		System.out.println("Slave talons set to follower state");
		
		
		//Sets the masters to use the encoders that are directly plugged into them
		_left_talon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		_right_talon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
		_left_talon.reverseSensor(true);
		_right_talon.reverseOutput(true);
		System.out.println("Sensors wired to master Talons");
		
		//Zeroes encoders
		_left_talon.setEncPosition(0);
		_right_talon.setEncPosition(0);
		System.out.println("Encoder position reset");
		
		float PEAK_VOLTAGE = +6.0f;
		_left_talon.configPeakOutputVoltage(PEAK_VOLTAGE, -PEAK_VOLTAGE);
		_right_talon.configPeakOutputVoltage(PEAK_VOLTAGE, -PEAK_VOLTAGE);
		_left_talon.configMaxOutputVoltage(PEAK_VOLTAGE);
		_right_talon.configMaxOutputVoltage(-PEAK_VOLTAGE);
		System.out.println("Peak output voltage set");
	}
	
	/**  function is called periodically during operator control */
    public void teleopPeriodic() {
		Robot.table.putString("status", _left_talon.get() + ", " + _right_talon.get() + "\n");

    	System.out.println("Left " + _left_talon.get() + " | Right " + _right_talon.get());
    	
		/* get buttons */
		boolean [] btns= new boolean [_btnsLast.length];
		for(int i=1;i<_btnsLast.length;++i)
			btns[i] = _joy.getRawButton(i);

		/* get the left joystick axis on Logitech Gampead */
		double leftYjoystick = -1 * _joy.getY(); /* multiple by -1 so joystick forward is positive */

		/* call this periodically, and catch the output.  Only apply it if user wants to run MP. */
		_example_right.control();
		_example_left.control();

		if (btns[5] == false) { /* Check button 5 (top left shoulder on the logitech gamead). */
			/*
			 * If it's not being pressed, just do a simple drive.  This
			 * could be a RobotDrive class or custom drivetrain logic.
			 * The point is we want the switch in and out of MP Control mode.*/
		
			/* button5 is off so straight drive */
			_right_talon.changeControlMode(TalonControlMode.Voltage);
			_left_talon.changeControlMode(TalonControlMode.Voltage);

			_left_talon.set(12.0 * leftYjoystick);
			_right_talon.set(12.0 * leftYjoystick);

			_example_right.reset();
			_example_left.reset();
		} else {
			/* Button5 is held down so switch to motion profile control mode => This is done in MotionProfileControl.
			 * When we transition from no-press to press,
			 * pass a "true" once to MotionProfileControl.
			 */
			_left_talon.changeControlMode(TalonControlMode.MotionProfile);
			_right_talon.changeControlMode(TalonControlMode.MotionProfile);
			
			CANTalon.SetValueMotionProfile setOutputLeft = _example_left.getSetValue();
			CANTalon.SetValueMotionProfile setOutputRight = _example_right.getSetValue();

			_left_talon.set(setOutputLeft.value);
			_right_talon.set(setOutputRight.value);

			/* if btn is pressed and was not pressed last time,
			 * In other words we just detected the on-press event.
			 * This will signal the robot to start a MP */
			if( (btns[6] == true) && (_btnsLast[6] == false) ) {
				/* user just tapped button 6 */
				_example_right.startMotionProfile();
				_example_left.startMotionProfile();
			}
		}

		/* save buttons states for on-press detection */
		for(int i=1;i<10;++i)
			_btnsLast[i] = btns[i];

	}
	/**  function is called periodically during disable */
	public void disabledPeriodic() {
		/* it's generally a good idea to put motor controllers back
		 * into a known state when robot is disabled.  That way when you
		 * enable the robot doesn't just continue doing what it was doing before.
		 * BUT if that's what the application/testing requires than modify this accordingly */
		_left_talon.changeControlMode(TalonControlMode.PercentVbus);
		_right_talon.changeControlMode(TalonControlMode.PercentVbus);

		_left_talon.set( 0 );
		_right_talon.set( 0 );

		/* clear our buffer and put everything into a known state */
		_example_left.reset();
		_example_right.reset();

	}
}