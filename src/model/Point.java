package model;

import java.util.Objects;

public class Point {
    public final int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getValue() {
        if (x == 0 || x == 7 || y == 0 || y == 7) {
            return 2;
        }
        return 1;
    }

    public double getValueTo() {
        if (x == 0 || x == 7) {
            if (y == 0 || y == 7) {
                return 0.8;
            }
            return 0.4;
        }
        if (y == 0 || y == 7) {
            return 0.4;
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("%c%d", 'a' + y, 8 - x);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Point other = (Point) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
