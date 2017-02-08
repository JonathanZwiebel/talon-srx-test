package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

//Author: Jonathan Zwiebel
public class Robot extends IterativeRobot {
	//Drivetrain drivetrain;
	Intake intake;
	Drivetrain drivetrain;
	MasterTalon master_talon;
	
	static NetworkTable table;
	
	public Robot() {
		System.out.println("Constructing Robot");
		master_talon = new MasterTalon(7);
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
		master_talon.init();
		System.out.println("Done with Teleop Init");
	}
	
	@Override
	public void teleopPeriodic() {
		master_talon.update();
	}
	
	@Override
	public void disabledInit() {
		master_talon.disable();
	}
}

