package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Lowering {
    public static void raiseRobot(LinearOpMode lop, DcMotor elevator) {
        elevator.setPower(-0.9);
        lop.sleep(3200);
        elevator.setPower(0);
    }

    public static void lowerRobot(LinearOpMode lop, DcMotor elevator) {
        elevator.setPower(0.90);
        lop.sleep(3200);
        elevator.setPower(0);
    }
}
