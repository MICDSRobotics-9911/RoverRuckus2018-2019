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

import android.media.MediaPlayer;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.data.ElementParser;
import org.firstinspires.ftc.teamcode.data.GoldPosition;
import org.firstinspires.ftc.teamcode.robotplus.autonomous.TimeOffsetVoltage;
import org.firstinspires.ftc.teamcode.robotplus.hardware.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotplus.hardware.Robot;

import java.util.List;

/**
 * Pit is designed to hit the gold and stop
 */
@Autonomous(name = "Full Depot", group = "Comp")
public class Depot extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    private Robot robot;
    private MecanumDrive mecanumDrive;

    private DcMotor grabber;
    private DcMotor elevator;
    private DcMotor extender;
    private DigitalChannel limitswitch;
    private CRServo dumper;
    private CRServo sampler;
    private GoldPosition goldPosition = GoldPosition.UNKNOWN;
    private int step = 0;

    private MediaPlayer player;
    private ElapsedTime elapsedTime;

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
        extender = hardwareMap.get(DcMotor.class, "extender");
        mecanumDrive = (MecanumDrive) robot.getDrivetrain();
        grabber = hardwareMap.get(DcMotor.class, "grabber");
        dumper = hardwareMap.get(CRServo.class, "dumper");
        sampler = hardwareMap.get(CRServo.class, "sampler");
        limitswitch = hardwareMap.get(DigitalChannel.class, "limit");
        player = MediaPlayer.create(hardwareMap.appContext, R.raw.sickomode);


        player.setVolume(100, 100);
        limitswitch.setMode(DigitalChannel.Mode.INPUT);
        this.elevator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.dumper.setDirection(DcMotorSimple.Direction.REVERSE);
        this.sampler.setDirection(DcMotorSimple.Direction.REVERSE);

        this.elapsedTime = new ElapsedTime();

        // this.player.start();
        waitForStart();
        elapsedTime.reset();

        if (opModeIsActive()) {
            if (step == 0) {
                Lowering.lowerRobot(this, this.elevator);
                this.mecanumDrive.complexDrive(MecanumDrive.Direction.UP.angle(), 0.5, 0);
                sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 19));
                this.mecanumDrive.stopMoving();
                Lowering.partialRaise(this, elevator);
                this.mecanumDrive.complexDrive(MecanumDrive.Direction.DOWN.angle(), 0.5, 0);
                sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 19));
                this.mecanumDrive.stopMoving();
                this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 0.5, 0);
                sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 18)); // 12
                this.mecanumDrive.stopMoving();
                step++;
            }

            /** Activate Tensor Flow Object Detection. */
            if (tfod != null) {
                tfod.activate();
                sleep(2000);
            }

            while (opModeIsActive()) {
                if (tfod != null && step == 1) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if (updatedRecognitions != null) {
                        telemetry.addData("# Object Detected", updatedRecognitions.size());
                        telemetry.update();
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
                        else if (updatedRecognitions.size() >= 3) {
                            telemetry.addData("More than three", "true");
                            telemetry.update();
                            updatedRecognitions = ElementParser.parseElements(updatedRecognitions);
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
                        else if (updatedRecognitions.size() <= 1 && this.elapsedTime.time() > 15) {
                            goldPosition = GoldPosition.CENTER;
                            step++;
                        }
                        telemetry.update();
                    }
                }

                // rotate towards the correct element
                if (!goldPosition.equals(goldPosition.UNKNOWN) && step == 2) {
                    switch (goldPosition) {
                        case LEFT:
                            this.mecanumDrive.complexDrive(0, 0, 0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 37)); // 31
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        case CENTER:
                            step++;
                            break;
                        case RIGHT:
                            this.mecanumDrive.complexDrive(0, 0, -0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 34));
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
                    this.mecanumDrive.stopMoving();

                    switch (goldPosition) {
                        case LEFT:
                            this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 58));
                            this.mecanumDrive.stopMoving();
                            break;
                        case CENTER:
                            this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 49));
                            this.mecanumDrive.stopMoving();
                            break;
                        case RIGHT:
                            this.mecanumDrive.complexDrive(MecanumDrive.Direction.LEFT.angle(), 1, 0);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 60));
                            this.mecanumDrive.stopMoving();
                            break;
                    }
                    step++;
                }

                // move back to midway point between pit and landing site
                if (step == 4) {
                    this.mecanumDrive.autoMove(this, hardwareMap, this.mecanumDrive, MecanumDrive.Direction.RIGHT, 27); // 29
                    step++;
                }

                // rotate left towards the other site
                if (step == 5) {
                    switch (goldPosition) {
                        case LEFT:
                            this.mecanumDrive.complexDrive(0, 0, -0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 40)); // this distance needs to adjusted
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                        case CENTER:
                            /*this.mecanumDrive.complexDrive(0, 0, 0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 31));
                            this.mecanumDrive.stopMoving();*/
                            step++;
                            break;
                        case RIGHT:
                            this.mecanumDrive.complexDrive(0, 0, 0.3);
                            sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 31));
                            this.mecanumDrive.stopMoving();
                            step++;
                            break;
                    }
                }

                // make first leg of the trip to depot
                if (step == 6) {
                    this.mecanumDrive.autoMove(this, hardwareMap, mecanumDrive, MecanumDrive.Direction.DOWN, 37);
                    while (!limitswitch.getState() && this.elapsedTime.time() <= 25) {
                        this.mecanumDrive.complexDrive(MecanumDrive.Direction.DOWN.angle(), 0.25, 0);
                        sleep(1);
                    }

                    this.mecanumDrive.stopMoving();
                    this.mecanumDrive.complexDrive(MecanumDrive.Direction.UP.angle(), 0.5, 0);
                    sleep(300);
                    this.mecanumDrive.stopMoving();
                    step++;
                }

                // rotate front towards depot
                if (step == 7) {
                    this.mecanumDrive.complexDrive(0, 0, -0.3);
                    sleep(TimeOffsetVoltage.calculateDistance((hardwareMap.voltageSensor.get("Expansion Hub 10").getVoltage()), 95)); // 100
                    this.mecanumDrive.stopMoving();
                    step++;
                }

                // move to depot
                if (step == 8) {
                    this.mecanumDrive.autoMove(this, hardwareMap, mecanumDrive, MecanumDrive.Direction.DOWN, 60);
                    step++;
                }

                // drop game element
                if (step == 9) {
                    dumper.setDirection(DcMotorSimple.Direction.REVERSE);
                    dumper.setPower(1);
                    this.sleep(1200);
                    dumper.setPower(0);

                    // rotate back
                    this.mecanumDrive.complexDrive(0, 0, 0.3);
                    sleep(400); // 100
                    this.mecanumDrive.stopMoving();
                    step++;
                }

                // move towards pit for drop
                if (step == 10) {
                    this.mecanumDrive.autoMove(this, hardwareMap, mecanumDrive, MecanumDrive.Direction.UP, 56);
                    step++;
                }

                // drop arm into the pit
                if (step == 11) {
                    this.grabber.setPower(1);
                    sleep(2500);
                    this.grabber.setPower(0);
                    step++;
                }

                // start sampling just on the off-chance we can pull in silver or gold
                if (step == 12) {
                    this.sampler.setPower(1);
                    extender.setPower(1);
                    sleep(750);
                    extender.setPower(0);
                    step++;
                }

                telemetry.addData("Step", step);
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
