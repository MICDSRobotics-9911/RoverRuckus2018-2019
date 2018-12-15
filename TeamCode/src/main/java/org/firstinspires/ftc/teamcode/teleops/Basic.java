package org.firstinspires.ftc.teamcode.teleops;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.data.ElevatorStatus;
import org.firstinspires.ftc.teamcode.robotplus.gamepadwrapper.Controller;
import org.firstinspires.ftc.teamcode.robotplus.hardware.IMUWrapper;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;
import org.firstinspires.ftc.teamcode.robotplus.robodata.AccessControl;

/**
 * Regular Teleop
 */
@TeleOp(name = "Basic DriveTrain", group = "")
public class Basic extends OpMode {

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private DcMotor grabber;
    private DcMotor elevator;
    private DcMotor extender;

    private boolean samplerOn;
    private CRServo dumper;
    private CRServo sampler;
    private IMUWrapper imuWrapper;
    private ElevatorStatus elevatorStatus = ElevatorStatus.STOPPED;

    private Controller p1;
    private Controller p2;
    private AccessControl accessControl;
    private boolean halfSpeed = false;

    public void init() {
        telemetry.addData("Status", "Initializing");
        p1 = new Controller(gamepad1);
        p2 = new Controller(gamepad2);

        // hardware map
        robot = new Robot(hardwareMap);
        elevator = hardwareMap.get(DcMotor.class, "elevator");
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");
        dumper = hardwareMap.get(CRServo.class, "dumper");
        sampler = hardwareMap.get(CRServo.class, "sampler");
        imuWrapper = new IMUWrapper(hardwareMap);
        accessControl = new AccessControl();
        extender = hardwareMap.get(DcMotor.class, "extender");
        samplerOn = false;

        // settings
        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.dumper.setDirection(DcMotorSimple.Direction.REVERSE);
        this.sampler.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void loop() {
        telemetry.addData("Grabber Positiion", grabber.getCurrentPosition());
        telemetry.addData("Angle", imuWrapper.getOrientation().toAngleUnit(AngleUnit.RADIANS).firstAngle);
        telemetry.addData("Half-Speed", halfSpeed);

        // change control of mecanum drive
        if (p1.start.isDown() || p2.start.isDown()) {
            this.accessControl.changeAccess();
        }

        // half-speed
        if (p1.y.isDown() || p2.y.isDown()) {
            halfSpeed = !halfSpeed;
        }

        if (accessControl.isG1Primary()) {
            if (halfSpeed) {
                mecanumDrive.complexDrive(p1.getOriginalPad(), telemetry, 0.5);
            }
            else {
                mecanumDrive.complexDrive(p1.getOriginalPad(), telemetry);
            }
        }
        else {
            mecanumDrive.complexDrive(p2.getOriginalPad(), telemetry);
        }

        // grabber
        if (p1.a.isDown() || p2.a.isDown()) {
            grabber.setPower(1);
        }
        else if (gamepad1.b || gamepad2.b) {
            grabber.setPower(-1);
        }
        else {
            grabber.setPower(0);
        }

        // extender
        if (gamepad1.dpad_right || gamepad2.dpad_right) {
            extender.setPower(1);
        }
        else if (gamepad1.dpad_left || gamepad2.dpad_left) {
            extender.setPower(-1);
        }
        else {
            extender.setPower(0);
        }

        // dumper
        /*if (p1.x.isDown() || p2.x.isDown()) {
            dumper.setPower(-1);
        }
        else {
            dumper.setPower(0);
        }*/

        // elevator, need to get p1 working before i get p2 working
        if (gamepad2.dpad_up && (elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(1);
            elevatorStatus = ElevatorStatus.RAISING;
        }
        else if (gamepad2.dpad_down && (elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(-1);
            elevatorStatus = ElevatorStatus.LOWERING;
        }
        else if ((gamepad1.dpad_down || p1.dpadUp.isDown())&& (!elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(0);
            elevatorStatus = ElevatorStatus.STOPPED;
        }

        // sampler
        if ((gamepad1.x || gamepad2.x)) {
            this.sampler.setPower(1);
        }
        else {
            this.sampler.setPower(0);
        }

        telemetry.addData("Elevator", elevatorStatus.toString());
        p1.update();
    }
}
