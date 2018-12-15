package org.firstinspires.ftc.teamcode.data;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

public class ElementParser {
    public static List<Recognition> parseElements(List<Recognition> elements) {
        List<Recognition> out = elements;
        float biggestElement = 0;
        for (Recognition r : elements) {
            if (r.getHeight() > biggestElement) {
                biggestElement = r.getHeight();
            }
        }
        for (Recognition r : elements) {
            //something like this: if ((r.getHeight() - elements))
        }
        return elements;
    }
}
