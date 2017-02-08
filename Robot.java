package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

//Author: Jonathan Zwiebel
public class Robot extends IterativeRobot {
	//Drivetrain drivetrain;
	Intake intake;
	Drivetrain drivetrain;
	
	static NetworkTable table;
	
	public Robot() {
		System.out.println("Constructing Robot");
//		drivetrain = new Drivetrain(table);
		intake = new Intake();
//		table = NetworkTable.getTable("robot_table");
		System.out.println("Done Constructing Robot");
	}
	
	@Override
	public void robotInit() {
		Robot.table = NetworkTable.getTable("data_table");
		drivetrain = new Drivetrain();
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
		drivetrain.init();
		intake.init();
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
		drivetrain.update();
		intake.update();
	}
	
	@Override
	public void disabledInit() {
		drivetrain.disable();
		try {
			table.putString("end", "true");
			table.putString("start", "false");
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
}

