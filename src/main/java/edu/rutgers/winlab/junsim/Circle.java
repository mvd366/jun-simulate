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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Robert Moore
 * 
 */
public class Circle implements Drawable {
  static int nextId = 0;
  private static synchronized int getId(){
    return nextId++;
  }
  
  public float radius = 0f;
  public Point2D.Float center = new Point2D.Float();
  
  private final int id;
  
  public Circle(){
    super();
    this.id = getId();
  }
  
  @Override
  public String toString(){
    return "C " + this.id;
  }

  @Override
  public void draw(Graphics2D g, float scaleX, float scaleY) {
    this.draw(g, scaleX, scaleY, false);
  }

  public void draw(Graphics2D g, float scaleX, float scaleY, boolean fill) {
    AffineTransform origTransform = g.getTransform();

    if (fill) {
      Composite origComposite = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
      g.fillOval((int) ((this.center.getX() - this.radius) * scaleX),
          (int) ((this.center.getY() - radius) * scaleY),
          (int) (this.radius * 2 * scaleX), (int) (this.radius * 2 * scaleY));
      g.setComposite(origComposite);
    }

    g.drawOval((int) ((this.center.getX() - this.radius) * scaleX),
        (int) ((this.center.getY() - radius) * scaleY),
        (int) (this.radius * 2 * scaleX), (int) (this.radius * 2 * scaleY));

    g.setTransform(origTransform);
  }

  public boolean contains(Point2D p) {
    float dist = (float) Math.sqrt(Math.pow(p.getX() - this.center.getX(), 2)
        + Math.pow(p.getY() - this.center.getY(), 2));
    return this.radius >= dist;
  }

  public boolean intersects(Circle c) {
    double dist = Math.sqrt(Math.pow(this.center.getX() - c.center.getX(), 2)
        + Math.pow(this.center.getY() - c.center.getY(), 2));
    return dist <= (this.radius + c.radius);
  }

  public double getCenterX() {
    return this.center.getX();
  }

  public double getCenterY() {
    return this.center.getY();
  }

}
