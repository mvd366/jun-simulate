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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Robert Moore
 */
public class Transmitter extends Point2D.Float implements Drawable {

  private final Collection<CaptureDisk> disks = new HashSet<CaptureDisk>();

  private final Collection<CaptureDisk> coveredDisks = new HashSet<CaptureDisk>();

  public int getContention(){
    return this.disks.size() - this.coveredDisks.size();
  }

  @Override
  public void draw(Graphics2D g, float scaleX, float scaleY) {
    Font origFont = g.getFont();
    Color origColor = g.getColor();
    float captureRatio = this.getCaptureRatio();
    float x = (float) this.getX() * scaleX;
    float y = (float) this.getY() * scaleY;
    float radius = FileRenderer.getRadiusForPercent(captureRatio);
    Ellipse2D.Float ring = new Ellipse2D.Float(x - radius, y
        - radius, radius * 2, radius * 2);
    Color color = FileRenderer.getColorForPercent(captureRatio);
    g.setColor(color);
    g.fill(ring);
    g.setColor(FileRenderer.getStrokeColor());
    g.draw(ring);
    
    g.setColor(origColor);
    AffineTransform origTransform = g.getTransform();
    g.translate((int) (this.getX() * scaleX)+radius, (int) (this.getY() * scaleY)-radius);
    
    Font myFont = new Font("Serif", Font.BOLD, 12);
    g.setFont(myFont);
    
    FontMetrics metrics = g.getFontMetrics();
    Rectangle2D.Float box = (Rectangle2D.Float)metrics.getStringBounds(String.format("%.2f", this.getCaptureRatio()),null);
//    Rectangle2D.Float box = (Rectangle2D.Float)metrics.getStringBounds(String.format("T(%.2f)", this.getCaptureRatio()),null);
    g.setColor(FileRenderer.colorSet.getBackgroundColor());
    Composite origComposite = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    g.fill(box);
    g.setColor(origColor);
    g.setComposite(origComposite);
//    g.drawString(String.format("T(%.2f)", this.getCaptureRatio()),0,0);
    g.drawString(String.format("%.2f", this.getCaptureRatio()),0,0);
    g.setTransform(origTransform);
    g.setFont(origFont);

  }

  public void addDisk(final CaptureDisk disk) {
    this.disks.add(disk);
  }

  public Collection<CaptureDisk> getDisks() {
    return this.disks;
  }

  public void addCoveredDisk(final CaptureDisk disk) {
    this.coveredDisks.add(disk);
  }

  public float getCaptureRatio() {
    
    return this.disks.isEmpty() ? 0 : ((float) this.coveredDisks.size()) / this.disks.size();
  }

}
