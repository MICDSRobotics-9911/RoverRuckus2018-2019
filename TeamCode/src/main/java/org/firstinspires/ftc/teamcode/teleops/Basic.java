package org.firstinspires.ftc.teamcode.teleops;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robotplus.gamepadwrapper.Controller;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;

import java.util.ResourceBundle;

@TeleOp(name = "Basic DriveTrain", group = "")
public class Basic extends OpMode {

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private Controller p1;

    public void init() {
        telemetry.addData("Status", "Initializing");
        p1 = new Controller(gamepad1);

        robot = new Robot(hardwareMap);
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
    }

    public void loop() {
        mecanumDrive.complexDrive(p1.getOriginalPad(), telemetry);
    }
}
