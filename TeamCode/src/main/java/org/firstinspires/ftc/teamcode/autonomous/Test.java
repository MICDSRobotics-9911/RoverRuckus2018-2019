package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.robotplus.autonomous.TimeOffsetVoltage;
import org.firstinspires.ftc.teamcode.robotplus.hardware.IMUWrapper;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;

@Autonomous(name = "TestAuto", group = "")
public class Test extends LinearOpMode {

    private Robot robot;
    private MecanumDrive mecanumDrive;
    private IMUWrapper imuWrapper;

    private DcMotor grabber;
    private DcMotor elevator;
    private CRServo dumper;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initializing");

        // hardware map
        robot = new Robot(hardwareMap);
        elevator = hardwareMap.get(DcMotor.class, "elevator");
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");
        imuWrapper = new IMUWrapper(hardwareMap);
        dumper = hardwareMap.get(CRServo.class, "dumper");
        //imuWrapper.getIMU().initialize(imuWrapper.getInitilizationParameters());

        // settings
        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();
    }
}
