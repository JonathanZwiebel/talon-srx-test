package org.usfirst.frc.team8.robot;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Notifier;

public class MotionProfileFollower {
	private CANTalon.MotionProfileStatus status = new CANTalon.MotionProfileStatus();
	private CANTalon talon;
	private int state = 0;
	private int timeout = -1;
	private boolean started = false;
	private CANTalon.SetValueMotionProfile set_value = CANTalon.SetValueMotionProfile.Disable;
	private static final int k_min_points = 5;
	private static final int k_timeout_loops = 10;

	class PeriodicRunnable implements java.lang.Runnable {
		@Override
		public void run() {
			talon.processMotionProfileBuffer();
		}
	}
	Notifier notifier = new Notifier(new PeriodicRunnable());

	public MotionProfileFollower(CANTalon talon) {
		this.talon = talon;
		this.talon.changeMotionControlFramePeriod(5);
		
		notifier.startPeriodic(0.005);
	}

	// Called once at start
	public void reset() {
		talon.clearMotionProfileTrajectories();
		set_value = CANTalon.SetValueMotionProfile.Disable;
		state = 0;
		timeout = -1;
		started = false;
	}

	// Called on loop
	public void control() {
		System.out.println("Control Called");
		System.out.println("Timeout: " + timeout + " State: " + state + " Started: " + started);
		System.out.println("Set Value: " + set_value.toString());
		talon.getMotionProfileStatus(status);

		if(timeout < 0) {
			System.out.println("Timeout less than 0");
		}
		else {
			if(timeout == 0) {
				System.out.println("Time is equal to 0");
			}
			else {
				timeout--;
			}
		}

		if(talon.getControlMode() != TalonControlMode.MotionProfile) {
			state = 0;
			timeout = -1;
		}
		else {
			switch(state) {
			case 0:
				if(started) {
					started = false;
					set_value = CANTalon.SetValueMotionProfile.Disable;
					startFill();
					state = 1;
					timeout = k_timeout_loops;
				}
				break;
			case 1:
				if(status.btmBufferCnt > k_min_points) {
					set_value = CANTalon.SetValueMotionProfile.Enable;
					state = 2;
					timeout = k_timeout_loops;
				}
				break;
			case 2:
				if(status.isUnderrun == false) {
					timeout = k_timeout_loops;
				}
				if(status.activePointValid && status.activePoint.isLastPoint) {
					set_value = CANTalon.SetValueMotionProfile.Hold;
					state = 0;
					timeout = -1;
				}
				break;
			}
		}
	}

	private void startFill() {
		startFill(SampleMotionProfile.Points, SampleMotionProfile.kNumPoints);
	}

	private void startFill(double[][] profile, int count) {
		CANTalon.TrajectoryPoint point = new CANTalon.TrajectoryPoint();

		if(status.hasUnderrun) {
			System.out.println("Underrun present");
			talon.clearMotionProfileHasUnderrun();
		}
		talon.clearMotionProfileTrajectories();
		for(int i = 0; i < count; i++) {
			point.position = profile[i][0];
			point.velocity = profile[i][1];
			point.timeDurMs = (int) profile[i][2];
			point.profileSlotSelect = 0;
			point.velocityOnly = false;

			if(i == 0) {
				point.zeroPos = true;
			}
			else {
				point.zeroPos = false;
			}

			if((i + 1) == count) {
				point.isLastPoint = true;
			}
			else {
				point.isLastPoint = false;
			}
			talon.pushMotionProfileTrajectory(point);
		}
	}

	void startProfile() {
		started = true;
	}

	CANTalon.SetValueMotionProfile getSetValue() {
		return set_value;
	}


}
