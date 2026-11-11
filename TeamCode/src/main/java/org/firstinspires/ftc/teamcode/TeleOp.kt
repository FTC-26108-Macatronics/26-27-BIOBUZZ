package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

@TeleOp(name = "TeleOp")
class TeleOp : LinearOpMode() {
    private lateinit var mechanisms: RobotMechanisms
    private lateinit var frontLeft: DcMotor
    private lateinit var frontRight: DcMotor
    private lateinit var backLeft: DcMotor
    private lateinit var backRight: DcMotor

    // Toggle Variables for the Flywheel
    private var flywheelOn = false
    private var lastXButtonState = false

    // Toggle Variables for the Intake
    private var intakeOn = false
    private var lastRightBumperState = false

    override fun runOpMode() {
        mechanisms = RobotMechanisms(hardwareMap)
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")

        motorSettings()

        waitForStart()

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                updateFlywheelToggle()
                controlIntake()
                controlServo()
                mecanumDrive()

                telemetry.addData("Flywheel Switch", if (flywheelOn) "ON" else "OFF")
                telemetry.addData("Flywheel Velocity", mechanisms.flywheel.velocity)
                telemetry.addData("Intake Switch", if (intakeOn) "ON" else "OFF")
                telemetry.update()
            }
        }
    }

    private fun updateFlywheelToggle() {
        val currentXButtonState = gamepad1.x
        if (currentXButtonState && !lastXButtonState) {
            flywheelOn = !flywheelOn
        }
        lastXButtonState = currentXButtonState

        mechanisms.setFlywheel(flywheelOn)
    }

    private fun controlIntake() {
        val currentRightBumper = gamepad1.right_bumper
        if (currentRightBumper && !lastRightBumperState) {
            intakeOn = !intakeOn
        }
        lastRightBumperState = currentRightBumper

        if (gamepad1.left_bumper) {
            mechanisms.setIntakePower(-1.0)
        } else if (intakeOn) {
            mechanisms.setIntakePower(1.0)
        } else {
            mechanisms.setIntakePower(0.0)
        }
    }

    private fun controlServo() {
        if (gamepad1.right_trigger > 0.1) {
            mechanisms.setFeederPower(-1.0)
        } else if (gamepad1.left_trigger > 0.1) {
            mechanisms.setFeederPower(1.0)
        } else {
            mechanisms.setFeederPower(0.0)
        }
    }

    private fun motorSettings() {
        frontLeft.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontLeft.direction = DcMotorSimple.Direction.FORWARD
        frontLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        frontRight.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontRight.direction = DcMotorSimple.Direction.REVERSE
        frontRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        backLeft.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backLeft.direction = DcMotorSimple.Direction.FORWARD
        backLeft.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        backRight.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backRight.direction = DcMotorSimple.Direction.REVERSE
        backRight.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

    private fun mecanumDrive() {
        val forwardBack = -gamepad1.left_stick_y
        val strafe = gamepad1.left_stick_x
        val turn = gamepad1.right_stick_x

        val leftFrontPower = forwardBack + strafe + turn
        val rightFrontPower = (forwardBack - strafe) - turn
        val leftBackPower = (forwardBack - strafe) + turn
        val rightBackPower = (forwardBack + strafe) - turn

        frontLeft.power = leftFrontPower.toDouble()
        frontRight.power = rightFrontPower.toDouble()
        backLeft.power = leftBackPower.toDouble()
        backRight.power = rightBackPower.toDouble()
    }
}
