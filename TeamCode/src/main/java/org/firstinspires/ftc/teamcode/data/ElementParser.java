package org.firstinspires.ftc.teamcode.data;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * We were having the problem of the camera seeing items in the pit (and hence the robot couldn't really act on what it saw correctly).
 * This will take the two biggest objects that the robot sees and return that to the main autonomous to help aid this problem.
 * @since 12/15/18
 * @see RecognitionComparator
 */
public class ElementParser {
    public static List<Recognition> parseElements(List<Recognition> elements) {
        // sort in ascending order using the comparator
        Collections.sort(elements, new RecognitionComparator());

        // now make a new list that has the two biggest
        List<Recognition> out = new ArrayList<>();
        out.add(elements.get(0));
        out.add(elements.get(1));

        return out;
    }
}