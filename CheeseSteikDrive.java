package org.usfirst.frc.team8.robot;

/**
 * A clone of Team 254's Cheesy Drive set up to run on Steik
 * @author Jonathan Zwiebel
 *
 */
public class CheeseSteikDrive {
	public static final float hg_sensitivity = 0.85f; // Originally 0.85
	public static final float qs_alpha = 0.3f; // Originally 0.3
	public static final float qs_change_value = 0.5f; // Originally 1.0
	
	private double mOldWheel, mQuickStopAccumulator;
	private final double kWheelStickDeadband = 0.01;
	private final double kThrottleStickDeadband = 0.01;
	
	public double left_power = 0, right_power = 0;
	
	public void update(double throttle, double wheel, boolean isQuickTurn, boolean isHighGear) {
		double wheelNonLinearity;

		wheel = handleDeadband(wheel, kWheelStickDeadband);
		throttle = handleDeadband(throttle, kThrottleStickDeadband);

		double negInertia = wheel - mOldWheel;
		mOldWheel = wheel;

		if (isHighGear) {
			wheelNonLinearity = 0.6;
			// Apply a sin function that's scaled to make it feel better.
			wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
					/ Math.sin(Math.PI / 2.0 * wheelNonLinearity);
			wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
					/ Math.sin(Math.PI / 2.0 * wheelNonLinearity);
		} else {
			wheelNonLinearity = 0.5;
			// Apply a sin function that's scaled to make it feel better.
			wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
					/ Math.sin(Math.PI / 2.0 * wheelNonLinearity);
			wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
					/ Math.sin(Math.PI / 2.0 * wheelNonLinearity);
			wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel)
					/ Math.sin(Math.PI / 2.0 * wheelNonLinearity);
		}

		double leftPwm, rightPwm, overPower;
		double sensitivity;

		double angularPower;
		double linearPower;

		// Negative inertia!
		double negInertiaAccumulator = 0.0;
		double negInertiaScalar;
		if (isHighGear) {
			negInertiaScalar = 4.0;
			sensitivity = hg_sensitivity;
		} else {
			if (wheel * negInertia > 0) {
				negInertiaScalar = 2.5;
			} else {
				if (Math.abs(wheel) > 0.65) {
					negInertiaScalar = 5.0;
				} else {
					negInertiaScalar = 3.0;
				}
			}
			sensitivity = hg_sensitivity; 
		}
		double negInertiaPower = negInertia * negInertiaScalar;
		negInertiaAccumulator += negInertiaPower;

		wheel = wheel + negInertiaAccumulator;
		if (negInertiaAccumulator > 1) {
			negInertiaAccumulator -= 1;
		} else if (negInertiaAccumulator < -1) {
			negInertiaAccumulator += 1;
		} else {
			negInertiaAccumulator = 0;
		}
		linearPower = throttle;

		// Quickturn!
		if (isQuickTurn) {
			if (Math.abs(linearPower) < 0.2) {
				// Can be tuned
				double alpha = qs_alpha;
				mQuickStopAccumulator = (1 - alpha) * mQuickStopAccumulator
						+ alpha * limit(wheel, 1.0) * 5;
			}
			overPower = 1.0;
			if (isHighGear) {
				sensitivity = 1.0;
			} else {
				sensitivity = 1.0;
			}
			angularPower = wheel;
		} else {
			overPower = 0.0;
			angularPower = Math.abs(throttle) * wheel * sensitivity
					- mQuickStopAccumulator;
			if (mQuickStopAccumulator > qs_change_value) {
				mQuickStopAccumulator -= qs_change_value;
			} else if (mQuickStopAccumulator < -qs_change_value) {
				mQuickStopAccumulator += qs_change_value;
			} else {
				mQuickStopAccumulator = 0.0;
			}
		}

		rightPwm = leftPwm = linearPower;
		
		leftPwm += angularPower;
		rightPwm -= angularPower;

		if (leftPwm > 1.0) {
			rightPwm -= overPower * (leftPwm - 1.0);
			leftPwm = 1.0;
		} else if (rightPwm > 1.0) {
			leftPwm -= overPower * (rightPwm - 1.0);
			rightPwm = 1.0;
		} else if (leftPwm < -1.0) {
			rightPwm += overPower * (-1.0 - leftPwm);
			leftPwm = -1.0;
		} else if (rightPwm < -1.0) {
			leftPwm += overPower * (-1.0 - rightPwm);
			rightPwm = -1.0;
		}
		
		left_power = leftPwm;
		right_power = rightPwm;
	}
	
	private double handleDeadband(double val, double deadband) {
		return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
	}
	
	private double limit(double wheel, double d) {
		if(wheel > d) {
			return d;
		}
		if(wheel < -d) {
			return - d;
		}
		return wheel;
	}
}
