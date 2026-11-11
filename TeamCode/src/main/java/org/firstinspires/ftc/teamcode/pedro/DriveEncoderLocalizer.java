package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.HashMap;
import java.util.Map;

public class DriveEncoderLocalizer implements Localizer {
    private final DcMotorEx fl, bl, fr, br;
    private final IMU imu;

    // Matches your Constants.kt: (28 * 30.21) / (3 * Math.PI)
    private static final double INCHES_PER_TICK = (3.0 * Math.PI) / (28.0 * 30.21);
    private static final double LATERAL_MULTIPLIER = 1.0;

    private Pose currentPose = new Pose(0, 0, 0);
    private Twist currentTwist = new Twist(0, 0, 0);

    private int lastFl, lastBl, lastFr, lastBr;
    private double lastYaw = 0;
    private long lastTimeNanos = System.nanoTime();

    public DriveEncoderLocalizer(HardwareMap hardwareMap) {
        fl = hardwareMap.get(DcMotorEx.class, "FL");
        bl = hardwareMap.get(DcMotorEx.class, "BL");
        fr = hardwareMap.get(DcMotorEx.class, "FR");
        br = hardwareMap.get(DcMotorEx.class, "BR");

        // Match the forward directions from your TeleOp.kt
        fl.setDirection(DcMotorSimple.Direction.FORWARD);
        bl.setDirection(DcMotorSimple.Direction.FORWARD);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        br.setDirection(DcMotorSimple.Direction.REVERSE);

        // Initialize built-in Control Hub IMU
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        )));
        imu.resetYaw();

        resetEncoders();
    }

    private void resetEncoders() {
        lastFl = fl.getCurrentPosition();
        lastBl = bl.getCurrentPosition();
        lastFr = fr.getCurrentPosition();
        lastBr = br.getCurrentPosition();
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        double dt = (now - lastTimeNanos) / 1e9;
        lastTimeNanos = now;

        int curFl = fl.getCurrentPosition();
        int curBl = bl.getCurrentPosition();
        int curFr = fr.getCurrentPosition();
        int curBr = br.getCurrentPosition();

        double dFl = (curFl - lastFl) * INCHES_PER_TICK;
        double dBl = (curBl - lastBl) * INCHES_PER_TICK;
        double dFr = (curFr - lastFr) * INCHES_PER_TICK;
        double dBr = (curBr - lastBr) * INCHES_PER_TICK;

        lastFl = curFl;
        lastBl = curBl;
        lastFr = curFr;
        lastBr = curBr;

        // Mecanum forward (+X) and left (+Y) kinematics
        double dForward = (dFl + dBl + dFr + dBr) / 4.0;
        double dLeft = ((-dFl + dBl + dFr - dBr) / 4.0) * LATERAL_MULTIPLIER;

        double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        double dTheta = normalizeAngle(currentYaw - lastYaw);
        lastYaw = currentYaw;

        // Rotate robot-centric deltas into global field coordinates
        double avgHeading = currentPose.heading() + (dTheta / 2.0);
        double globalDx = dForward * Math.cos(avgHeading) - dLeft * Math.sin(avgHeading);
        double globalDy = dForward * Math.sin(avgHeading) + dLeft * Math.cos(avgHeading);

        currentPose = new Pose(
                currentPose.x() + globalDx,
                currentPose.y() + globalDy,
                normalizeAngle(currentPose.heading() + dTheta)
        );

        if (dt > 0) {
            currentTwist = new Twist(globalDx / dt, globalDy / dt, dTheta / dt);
        }
    }

    @Override
    public MotionState state() {
        return MotionState.ofTwist(currentPose, currentTwist);
    }

    @Override
    public void setPose(Pose pose) {
        currentPose = pose;
        lastYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
        resetEncoders();
    }

    @Override
    public void reset() {
        imu.resetYaw();
        lastYaw = 0;
        currentPose = new Pose(0, 0, 0);
        currentTwist = new Twist(0, 0, 0);
        resetEncoders();
    }

    @Override
    public void setX(double x) {
        currentPose = new Pose(x, currentPose.y(), currentPose.heading());
    }

    @Override
    public void setY(double y) {
        currentPose = new Pose(currentPose.x(), y, currentPose.heading());
    }

    @Override
    public void setHeading(double heading) {
        currentPose = new Pose(currentPose.x(), currentPose.y(), normalizeAngle(heading));
        lastYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
    }

    @Override
    public Twist twist() {
        return currentTwist;
    }

    @Override
    public Map<String, Object> debug() {
        Map<String, Object> map = new HashMap<>();
        map.put("x", currentPose.x());
        map.put("y", currentPose.y());
        map.put("heading", Math.toDegrees(currentPose.heading()));
        return map;
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2.0 * Math.PI;
        while (angle < -Math.PI) angle += 2.0 * Math.PI;
        return angle;
    }
}
