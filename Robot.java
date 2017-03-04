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
	SteikDrivetrainTune steik_drivetrain_tune;
	
	static NetworkTable table;
	public static NetworkTable dashboardTable;
	
	public Robot() {
		System.out.println("Constructing Robot");
		steik_drivetrain_tune = new SteikDrivetrainTune(this);
		System.out.println("Done Constructing Robot");
		
	}
	
	@Override
	public void robotInit() {
		Robot.table = NetworkTable.getTable("data_table");
		Robot.dashboardTable = NetworkTable.getTable("RobotTable");
		System.out.println("Robot Init");
	}

	@Override
	public void autonomousInit() {
		
	}

	@Override
	public void autonomousPeriodic() {

	}
	
	@Override
	public void teleopInit() {
		steik_drivetrain_tune.init();
		System.out.println("Teleop Init");
		try {
			table.putString("start", "true");
			table.putString("end", "false");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done with Teleop Init");
	}
	
	@Override
	public void teleopPeriodic() {
		steik_drivetrain_tune.update();
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
		steik_drivetrain_tune.disabledInit();
	}
}

