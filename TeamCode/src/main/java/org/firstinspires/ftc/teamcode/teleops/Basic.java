package org.firstinspires.ftc.teamcode.teleops;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.robotplus.gamepadwrapper.Controller;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;

import java.util.ResourceBundle;

@TeleOp(name = "Basic DriveTrain", group = "")
public class Basic extends OpMode {

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private DcMotor grabber;

    private Controller p1;

    public void init() {
        telemetry.addData("Status", "Initializing");
        p1 = new Controller(gamepad1);

        robot = new Robot(hardwareMap);
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");
    }

    public void loop() {
        mecanumDrive.complexDrive(p1.getOriginalPad(), telemetry);

        if (p1.a.isDown()) {
            grabber.setPower(1);
        }
        else if (!p1.a.isDown()) {
            grabber.setPower(0);
        }

        if (p1.b.isDown()) {
            grabber.setPower(-1);
        }
        else if (!p1.b.isDown()) {
            grabber.setPower(0);
        }
    }
}
