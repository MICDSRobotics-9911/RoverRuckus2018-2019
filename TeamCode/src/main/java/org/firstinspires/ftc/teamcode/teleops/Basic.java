package org.firstinspires.ftc.teamcode.teleops;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.robotplus.gamepadwrapper.Controller;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;
import org.firstinspires.ftc.teamcode.teleops.lib.ElevatorStatus;

import java.util.ResourceBundle;

@TeleOp(name = "Basic DriveTrain", group = "")
public class Basic extends OpMode {

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private DcMotor grabber;
    private DcMotor elevator;

    private Controller p1;

    private ElevatorStatus elevatorStatus = new ElevatorStatus();

    public void init() {
        telemetry.addData("Status", "Initializing");
        p1 = new Controller(gamepad1);

        // hardware map
        robot = new Robot(hardwareMap);
        elevator = hardwareMap.get(DcMotor.class, "elevator");
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");

        // settings
        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void loop() {
        mecanumDrive.complexDrive(p1.getOriginalPad(), telemetry);
        telemetry.addData("Grabber Positiion", grabber.getCurrentPosition());

        if (gamepad1.a) {
            grabber.setPower(1);
        }
        else if (!gamepad1.a) {
            grabber.setPower(0);
        }
        if (gamepad1.b) {
            grabber.setPower(-1);
        }
        else if (!gamepad1.b) {
            grabber.setPower(0);
        }

        // elevator
        if (gamepad1.dpad_up) {
            elevator.setPower(1);
        }
        else if (!gamepad1.dpad_up) {
            elevator.setPower(0);
        }
        if (gamepad1.dpad_down) {
            elevator.setPower(-1);
        }
        else if (!gamepad1.dpad_down) {
            elevator.setPower(0);
        }
    }
}
