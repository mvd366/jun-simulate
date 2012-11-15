/*
 * Copyright (C) 2012 Bernhard Firner and Rutgers University
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.util.Comparator;
import java.awt.geom.Point2D;

public class PointComparator implements Comparator<Point2D>{

    @Override
    public int compare(Point2D p1, Point2D p2) {
        if (p1.getY() < p2.getY()) return -1;
        if (p1.getY() > p2.getY()) return +1;
        if (p1.getX() < p2.getX()) return -1;
        if (p1.getX() > p2.getX()) return +1;
        return 0;
    }

    public boolean equals(Point2D p1, Point2D p2) {
        if (p1 == null || p2 == null) return false;
        if (p1 == p2) return true;
        return p1.getX() == p2.getX() && p1.getY() == p2.getY();
    }
}
