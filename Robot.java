package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

//Author: Jonathan Zwiebel
public class Robot extends IterativeRobot {
	Drivetrain drivetrain;
	Intake intake;
	NetworkTable table;
	
	public Robot() {
		drivetrain = new Drivetrain();
		intake = new Intake();
		table = NetworkTable.getTable("robot_table");
	}
	
	@Override
	public void robotInit() {
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
		drivetrain.init();
		intake.init();
		try {
			table.putString("start", "start_val");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
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
			table.putString("end", "end_val");
		}
		catch(Exception e) {
			e.printStackTrace();
		}	}
}

