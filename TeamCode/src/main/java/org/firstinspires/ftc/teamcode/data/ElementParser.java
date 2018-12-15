package org.firstinspires.ftc.teamcode.data;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElementParser {
    public static List<Recognition> parseElements(List<Recognition> elements) {
        List<Recognition> out = new ArrayList<>();
        for (int e = 0; e < 3; e++) {
            Recognition biggestElement = elements.get(0);
            for (int i = e; i < elements.size(); i++) {
                if (elements.get(i).getHeight() > biggestElement.getHeight()) {
                    biggestElement = elements.get(i);
                }
            }
            out.add(biggestElement);
        }

        return out;
    }
}
