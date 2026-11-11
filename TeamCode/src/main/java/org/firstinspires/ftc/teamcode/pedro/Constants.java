package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("FL");
        c.backLeftName.set("BL");
        c.frontRightName.set("FR");
        c.backRightName.set("BR");

        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);

        c.manualBrakeMode.set(true);
    });

    // After Foresight AutoTune, paste ForesightConfig here

    //public static Follower create(HardwareMap hardwareMap) {
        //return new Follower(
                //new Mecanum(hardwareMap, drivetrainConfig),
                //new DriveEncoderLocalizer(hardwareMap),
                //foresightConfig
        //);
    //}

}
