package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * A robot project used for testing Talon SRX features
 * 
 * @author Jonathan Zwiebel
 *
 */
public class Robot extends IterativeRobot {
	Compressor compressor;
	SteikDrivetrain steik_drivetrain;
	SteikSlider steik_slider;
	SteikClimber steik_climber;
	SteikSpatula steik_spatula;
	
	static NetworkTable table;
	
	public Robot() {
		System.out.println("Constructing Robot");
		steik_slider = new SteikSlider(this);
		steik_drivetrain = new SteikDrivetrain(this);
		steik_climber = new SteikClimber(this);
		steik_spatula = new SteikSpatula(this);
		compressor = new Compressor();
		System.out.println("Done Constructing Robot");
	}
	
	@Override
	public void robotInit() {
		Robot.table = NetworkTable.getTable("data_table");
		System.out.println("Robot Init");
	}

	@Override
	public void autonomousInit() {
		System.out.println("Autonomous Init");
	}

	@Override
	public void autonomousPeriodic() {
		System.out.println("Autonomous Periodic");
	}

	@Override
	public void teleopInit() {
		System.out.println("Teleop Init");
		try {
			table.putString("start", "true");
			table.putString("end", "false");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		steik_slider.init();
		steik_drivetrain.init();
		steik_climber.init();
		steik_spatula.init();
		System.out.println("Done with Teleop Init");
	}
	
	@Override
	public void teleopPeriodic() {
		steik_slider.update();
		steik_drivetrain.update();
		steik_climber.update();
		steik_spatula.update();
	}
	
	@Override
	public void disabledInit() {
		try {
			table.putString("end", "true");
			table.putString("start", "false");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		steik_slider.disable();
	}
}

