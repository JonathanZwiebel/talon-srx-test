package org.usfirst.frc.team8.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	Drivetrain drivetrain;
	Intake intake;
	
	public Robot() {
		drivetrain = new Drivetrain();
		intake = new Intake();
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
	}
	
	@Override
	public void teleopPeriodic() {
		drivetrain.update();
		intake.update();
	}
}

