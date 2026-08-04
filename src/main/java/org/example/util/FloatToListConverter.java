package org.example.util;

import java.util.ArrayList;
import java.util.List;

public class FloatToListConverter {

    public static List<Float> toList(float[] vector) {
        List<Float> list = new ArrayList<>(vector.length);
        for (float f : vector) {
            list.add(f);
        }
        return list;
    }

}
