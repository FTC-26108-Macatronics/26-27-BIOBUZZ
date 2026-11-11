package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.Constants.Companion.FAR_VELOCITY

@TeleOp(name = "TeleOp")
class TeleOp : LinearOpMode() {
    private var flywheel: DcMotor? = null
    private var intake: DcMotor? = null
    private var servo: CRServo? = null
    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null

    // Toggle Variables for the Flywheel
    private var flywheelOn = false             // Remembers if the flywheel switch is ON or OFF
    private var lastXButtonState = false       // Tracking variable to handle button edge detection

    // Toggle Variables for the Intake
    private var intakeOn = false               // Remembers if the intake switch is ON or OFF
    private var lastRightBumperState = false   // Tracking variable to handle bumper edge detection

    override fun runOpMode() {
        flywheel = hardwareMap.get(DcMotor::class.java, "flywheel")
        intake = hardwareMap.get(DcMotor::class.java, "intake")
        servo = hardwareMap.get(CRServo::class.java, "servo")
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")

        // Establishing the direction and mode for the auxiliary systems
        flywheel!!.mode = DcMotor.RunMode.RUN_USING_ENCODER
        flywheel!!.direction = DcMotorSimple.Direction.REVERSE
        intake!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        servo!!.power = 0.0

        motorSettings()

        waitForStart()

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                updateFlywheelToggle()
                controlIntake()
                controlServo()
                mecanumDrive()

                telemetry.addData("Flywheel Switch", if (flywheelOn) "ON" else "OFF")
                telemetry.addData("Flywheel Velocity", (flywheel as DcMotorEx).velocity)
                telemetry.addData("Intake Switch", if (intakeOn) "ON" else "OFF")
                telemetry.update()
            }
        }
    }

    private fun updateFlywheelToggle() {
        val currentXButtonState = gamepad1.x

        // Edge detection math: checks for a clean, single button press action
        if (currentXButtonState && !lastXButtonState) {
            flywheelOn = !flywheelOn
        }

        lastXButtonState = currentXButtonState

        // Command velocity parameters based on the toggle's memory switch state
        if (flywheelOn) {
            (flywheel as DcMotorEx).velocity = FAR_VELOCITY.toDouble()
        } else {
            (flywheel as DcMotorEx).velocity = 0.0
        }
    }

    private fun controlIntake() {
        val currentRightBumper = gamepad1.right_bumper

        // Edge detection to toggle intake ON/OFF with a single press
        if (currentRightBumper && !lastRightBumperState) {
            intakeOn = !intakeOn
        }
        lastRightBumperState = currentRightBumper

        // Left bumper acts as an instant reverse override to clear jams
        if (gamepad1.left_bumper) {
            intake!!.power = -1.0
        } else if (intakeOn) {
            intake!!.power = 1.0
        } else {
            intake!!.power = 0.0
        }
    }

    private fun controlServo() {
        if (gamepad1.right_trigger > 0.1) {
            servo!!.power = -1.0
        } else if (gamepad1.left_trigger > 0.1) {
            servo!!.power = 1.0
        } else {
            servo!!.power = 0.0
        }
    }

    private fun motorSettings() {
        frontLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontLeft!!.direction = DcMotorSimple.Direction.FORWARD
        frontLeft!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        frontRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontRight!!.direction = DcMotorSimple.Direction.REVERSE
        frontRight!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        backLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backLeft!!.direction = DcMotorSimple.Direction.FORWARD
        backLeft!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        backRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backRight!!.direction = DcMotorSimple.Direction.REVERSE
        backRight!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    private fun mecanumDrive() {
        val forwardBack = -gamepad1.left_stick_y
        val strafe = gamepad1.left_stick_x
        val turn = gamepad1.right_stick_x

        val leftFrontPower = forwardBack + strafe + turn
        val rightFrontPower = (forwardBack - strafe) - turn
        val leftBackPower = (forwardBack - strafe) + turn
        val rightBackPower = (forwardBack + strafe) - turn

        frontLeft!!.power = leftFrontPower.toDouble()
        frontRight!!.power = rightFrontPower.toDouble()
        backLeft!!.power = leftBackPower.toDouble()
        backRight!!.power = rightBackPower.toDouble()
    }
}
