package utils;

import entities.Rectangle;

import java.util.Comparator;

public class HeightComparator implements Comparator<Rectangle> {
    @Override
    public int compare(Rectangle rectangle1, Rectangle rectangle2) {
        return Integer.compare(rectangle2.getHeight(), rectangle1.getHeight());
    }
}
