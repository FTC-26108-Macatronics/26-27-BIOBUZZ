package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.Constants.Companion.FAR_VELOCITY

class RobotMechanisms(hardwareMap: HardwareMap) {
    @JvmField val flywheel: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "flywheel")
    @JvmField val intake: DcMotor = hardwareMap.get(DcMotor::class.java, "intake")
    @JvmField val servo: CRServo = hardwareMap.get(CRServo::class.java, "servo")

    init {
        flywheel.mode = DcMotor.RunMode.RUN_USING_ENCODER
        flywheel.direction = DcMotorSimple.Direction.REVERSE
        intake.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        servo.power = 0.0
    }

    @JvmOverloads
    fun setFlywheel(on: Boolean, targetVelocity: Double = FAR_VELOCITY.toDouble()) {
        flywheel.velocity = if (on) targetVelocity else 0.0
    }

    @JvmOverloads
    fun isFlywheelReady(targetVelocity: Double = FAR_VELOCITY.toDouble()): Boolean {
        return flywheel.velocity >= targetVelocity * 0.95
    }

    fun setIntakePower(power: Double) {
        intake.power = power
    }

    fun setFeederPower(power: Double) {
        servo.power = power
    }
}
