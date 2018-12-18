package org.firstinspires.ftc.teamcode.data;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.Comparator;

/**
 * This is the comparator that is be used in the ElementParser to sort the visual elements in ascending order
 * @since 12/17/18
 * @see ElementParser
 */
public class RecognitionComparator implements Comparator<Recognition> {
    @Override
    public int compare(Recognition r1, Recognition r2) {
        if (r1.getHeight() > r2.getHeight()) {
            return -1;
        }
        else if (r1.getHeight() == r2.getHeight()) {
            return 0;
        }
        else {
            return 1;
        }
    }
}