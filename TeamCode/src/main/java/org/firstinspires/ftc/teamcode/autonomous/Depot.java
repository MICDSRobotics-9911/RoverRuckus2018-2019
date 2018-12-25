/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.autonomous;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.sun.tools.javac.comp.Lower;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.data.GoldPosition;
import org.firstinspires.ftc.teamcode.robotplus.autonomous.TimeOffsetVoltage;
import org.firstinspires.ftc.teamcode.robotplus.hardware.IMUWrapper;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;

import java.util.List;

/**
 * Depot will knock the gold, park in the square, and drop the team marker. This isn't the one that lowers the arm into the pit
 */
@Autonomous(name = "Depot", group = "Concept")
public class Depot extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private DcMotor grabber;
    private DcMotor elevator;
    private IMUWrapper imuWrapper;
    private CRServo dumper;
    private GoldPosition goldPosition = GoldPosition.UNKNOWN;
    private int step = 0;

    private static final String VUFORIA_KEY = "AZDepIf/////AAAAGfXxylZkt0YriAZz29imD+JnpWB4sxwIldmqfmE2S0NQ5QJ+R8FF9kqvBAeUoFLVcXawrLuNS1salfES/URf32WEkCus6PRLYzToyuvGnoBHtXJBW9nr94CSnAFvWjPrYVMEQhy7kZeuMEkhvUn8O/4DZ7f8vP1hPC7xKugpmGY0LTvxd/umhQxy9dl28mkUQWHcselYnHrOgrW4XvNq5exF67YoK3cQDjrodu02wmmFcoeHr78xyabZqOif8hk9Lk+F/idAMZcB1un86Goawbto6qTP7/SnXAbAedRrSKCGp/UuYa02c2Y5rteZMMtdSE7iL824A4kmwVZtg5biQy3jE0zAjsFQD7tztRiMGLxt";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the Tensor Flow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    @Override
    public void runOpMode() {
        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        /** Wait for the game to begin */
        telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();

        robot = new Robot(hardwareMap);
        elevator = hardwareMap.get(DcMotor.class, "elevator");
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");
        dumper = hardwareMap.get(CRServo.class, "dumper");
        imuWrapper = new IMUWrapper(hardwareMap);

        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        waitForStart();

        if (opModeIsActive()) {
            // lower the robot, close elevator, and move back
            if (step == 0) {
                Lowering.lowerRobot(this, this.elevator);
                this.mecanumDrive.complexDrive(MecanumDrive.Direction.UP.angle(), 0.5, 0);
                sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 19));
                //sleep(250);
                this.mecanumDrive.stopMoving();
                Lowering.raiseRobot(this, elevator);
                this.mecanumDrive.complexDrive(MecanumDrive.Direction.DOWN.angle(), 0.5, 0);
                sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 14));
                //sleep(250);
                this.mecanumDrive.stopMoving();
                step++;
            }

            /** Activate Tensor Flow Object Detection. */
            if (tfod != null) {
                tfod.activate();
            }

            while (opModeIsActive()) {
                telemetry.addData("Angle", imuWrapper.getOrientation().toAngleUnit(AngleUnit.RADIANS).firstAngle);
                // detect the elements
                if (tfod != null && step == 1) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if (updatedRecognitions != null) {
                        telemetry.addData("# Object Detected", updatedRecognitions.size());
                        if (updatedRecognitions.size() == 2) {
                            int goldMineralX = -1;
                            int silverMineral1X = -1;
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                } else {
                                    silverMineral1X = (int) recognition.getLeft();
                                }
                            }

                            if (goldMineralX == -1) {
                                telemetry.addData("Gold Mineral Position", "Left");
                                goldPosition = GoldPosition.LEFT;
                                step++;
                            } else {
                                if (goldMineralX < silverMineral1X) {
                                    telemetry.addData("Gold Mineral Position", "Center");
                                    goldPosition = GoldPosition.CENTER;
                                    step++;
                                } else {
                                    telemetry.addData("Gold Mineral Position", "Right");
                                    goldPosition = GoldPosition.RIGHT;
                                    step++;
                                }
                            }
                        }
                        else if (updatedRecognitions.size() == 3) {
                            int goldMineralX = -1;
                            int silverMineral1X = -1;
                            int silverMineral2X = -1;
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                } else if (silverMineral1X == -1) {
                                    silverMineral1X = (int) recognition.getLeft();
                                } else {
                                    silverMineral2X = (int) recognition.getLeft();
                                }
                            }
                            if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                                if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                                    telemetry.addData("Gold Mineral Position", "Left");
                                    Log.i("[Pit]", "Left");
                                    goldPosition = GoldPosition.LEFT;
                                    step++;
                                } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                                    telemetry.addData("Gold Mineral Position", "Right");
                                    Log.i("[Pit]", "Right");
                                    goldPosition = GoldPosition.RIGHT;
                                    step++;
                                } else {
                                    telemetry.addData("Gold Mineral Position", "Center");
                                    Log.i("[Pit]", "Center");
                                    goldPosition = GoldPosition.CENTER;
                                    step++;
                                }
                            }
                        }
                        telemetry.update();
                    }
                }

                // rotate towards the correct element
                if (!goldPosition.equals(goldPosition.UNKNOWN) && step == 2) {
                    switch (goldPosition) {
                        case LEFT:
                            this.mecanumDrive.complexDrive(0, 0, 0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 31));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        case CENTER:
                            step++;
                            break;
                        case RIGHT:
                            this.mecanumDrive.complexDrive(0, 0, -0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 31));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }

                // move towards the element
                if (step == 3) {
                    Lowering.raiseRobot(this, elevator);
                    this.mecanumDrive.stopMoving();
                    this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                    sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 65));
                    this.mecanumDrive.stopMoving();
                    step++;
                }

                // rotate towards the rail
                if (step == 4) {
                    switch (goldPosition) {
                        case LEFT:
                            this.mecanumDrive.complexDrive(0, 0, -0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 48));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        case CENTER:
                            this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 30));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        case RIGHT:
                            this.mecanumDrive.complexDrive(0, 0, 0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 48));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                    }
                }

                // drop the team marker into the pit
                if (step == 5) {
                    if (goldPosition.equals(GoldPosition.CENTER)) {
                        this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                        sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 35));
                        this.mecanumDrive.stopMoving();
                        dumper.setDirection(DcMotorSimple.Direction.REVERSE);
                        dumper.setPower(1);
                        this.sleep(1200);
                        dumper.setPower(0);
                    }
                    else {
                        this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                        sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 45));
                        this.mecanumDrive.stopMoving();
                        dumper.setDirection(DcMotorSimple.Direction.REVERSE);
                        dumper.setPower(1);
                        this.sleep(1200);
                        dumper.setPower(0);
                    }
                }
                telemetry.update();
            }
        }

        if (tfod != null) {
            tfod.shutdown();
        }
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}
