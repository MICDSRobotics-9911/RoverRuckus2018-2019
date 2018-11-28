package org.firstinspires.ftc.teamcode.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Lowering {
    public static void raiseRobot(LinearOpMode lop, DcMotor elevator) {
        elevator.setPower(-1);
        lop.sleep(1700);
        elevator.setPower(0);
    }

    public static void lowerRobot(LinearOpMode lop, DcMotor elevator) {
        elevator.setPower(1);
        lop.sleep(1700);
        elevator.setPower(0);
    }
}
