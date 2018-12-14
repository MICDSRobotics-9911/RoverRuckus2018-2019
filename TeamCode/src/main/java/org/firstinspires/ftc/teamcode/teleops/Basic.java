package org.firstinspires.ftc.teamcode.teleops;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
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

    private boolean dumperDown;
    private Servo dumper;
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
        dumper = hardwareMap.get(Servo.class, "dumper");
        dumperDown = false;
        imuWrapper = new IMUWrapper(hardwareMap);
        accessControl = new AccessControl();

        // settings
        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.dumper.setDirection(Servo.Direction.FORWARD);
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

        if (gamepad1.a || gamepad2.a) {
            grabber.setPower(1);
        }
        else if (!gamepad1.a || !gamepad2.a) {
            grabber.setPower(0);
        }
        if (gamepad1.b || gamepad2.b) {
            grabber.setPower(-1);
        }
        else if (!gamepad1.b || !gamepad2.b) {
            grabber.setPower(0);
        }

        // dumper
        if (p1.x.isDown() || p2.x.isDown()) {
            dumper.setPosition(.5);
            dumperDown = !dumperDown;
        }
        else {
            dumper.setPosition(1);
        }

        // elevator, need to get p1 working before i get p2 working
        if (p1.dpadUp.isDown() && (elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(1);
            elevatorStatus = ElevatorStatus.RAISING;
        }
        else if (p1.dpadDown.isDown() && (elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(-1);
            elevatorStatus = ElevatorStatus.LOWERING;
        }
        else if ((p1.dpadDown.isDown() || p1.dpadUp.isDown())&& (!elevatorStatus.equals(ElevatorStatus.STOPPED))) {
            elevator.setPower(0);
            elevatorStatus = ElevatorStatus.STOPPED;
        }

        telemetry.addData("Elevator", elevatorStatus.toString());
        telemetry.addData("Dumper", dumper.getPosition());
        p1.update();
    }
}
