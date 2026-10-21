package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.Constants.Companion.FAR_VELOCITY

@TeleOp(name = "TeleOp V2")
class TeleOp : LinearOpMode() {
    private var flywheel: DcMotor? = null
    // private var intake: DcMotor? = null       // Hidden until intake is mounted
    private var servo: CRServo? = null
    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null

    // Toggle Variables for the Flywheel
    private var flywheelOn = false             // Remembers if the flywheel switch is ON or OFF
    private var lastXButtonState = false       // Tracking variable to handle button edge detection

    override fun runOpMode() {
        flywheel = hardwareMap.get(DcMotor::class.java, "flywheel")
        // intake = hardwareMap.get(DcMotor::class.java, "intake") // Hidden until intake mounted
        servo = hardwareMap.get(CRServo::class.java, "servo")
        frontLeft = hardwareMap.get(DcMotor::class.java, "FL")
        frontRight = hardwareMap.get(DcMotor::class.java, "FR")
        backLeft = hardwareMap.get(DcMotor::class.java, "BL")
        backRight = hardwareMap.get(DcMotor::class.java, "BR")

        // Establishing the direction and mode for the auxiliary systems
        flywheel!!.mode = DcMotor.RunMode.RUN_USING_ENCODER
        flywheel!!.direction = DcMotorSimple.Direction.REVERSE

        servo!!.power = 0.0

        motorSettings()

        waitForStart()

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                // Evaluates if the X button was pressed to turn the flywheel on/off
                updateFlywheelToggle()

                if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                    servo!!.power = 0.0
                }

                controlIntakeAndServo()
                mecanumDrive()

                telemetry.addData("Flywheel Switch", if (flywheelOn) "ON" else "OFF")
                telemetry.addData("Flywheel Velocity", (flywheel as DcMotorEx).velocity)
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

    private fun controlIntakeAndServo() {
        if (gamepad1.right_bumper) {
            // intake!!.power = 1.0   // Ready, waiting for intake
            servo!!.power = -2.0
        } else if (gamepad1.left_bumper) {
            // intake!!.power = -1.0  // Ready, waiting for intake
            servo!!.power = 2.0
        } else {
            // intake!!.power = 0.0   // Ready, waiting for intake

            if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                servo!!.power = 0.0
            }
        }
    }

    private fun motorSettings() {
        frontLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontLeft!!.direction = DcMotorSimple.Direction.FORWARD
        frontLeft!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE // Active braking update

        frontRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        frontRight!!.direction = DcMotorSimple.Direction.REVERSE
        frontRight!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE // Active braking update

        backLeft!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backLeft!!.direction = DcMotorSimple.Direction.FORWARD
        backLeft!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE // Active braking update

        backRight!!.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        backRight!!.direction = DcMotorSimple.Direction.REVERSE
        backRight!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE // Active braking update
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
