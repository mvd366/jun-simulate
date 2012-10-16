/*
 * Copyright (C) 2012 Robert Moore and Rutgers University
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Robert Moore
 *
 */
public class Receiver extends Point2D.Float implements Drawable{
  
  Collection<CaptureDisk> coveringDisks = new LinkedList<CaptureDisk>();
  
  public void draw(Graphics2D g){
    AffineTransform origTransform = g.getTransform();
    Color origColor = g.getColor();
//    g.translate((int)this.getX(),(int)this.getY());
   
    g.setColor(Color.YELLOW);
    for(CaptureDisk d : this.coveringDisks){
      Line2D line = new Line2D.Double(d.disk.getCenterX(), d.disk.getCenterY(), this.getX(), this.getY());
      g.draw(line);
    }
    
    g.setColor(origColor);
    g.drawString("R"+this.coveringDisks.size(), (int)this.getX(),(int)this.getY());
    g.setTransform(origTransform);
  }
}
