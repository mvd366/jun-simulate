/*
 * Owl Platform
 * Copyright (C) 2012 Robert Moore and the Owl Platform
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JPanel;

/**
 * @author Robert Moore
 * 
 */
public class DisplayPanel extends JPanel {

  private Collection<Drawable> devices = new LinkedList<Drawable>();
  private Collection<CaptureDisk> disks = new LinkedList<CaptureDisk>();
  private Collection<Point2D> points = new LinkedList<Point2D>();
  private Collection<Receiver> receiverPoints = new LinkedList<Receiver>();

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    float scaleX = this.getWidth() / Main.config.universeSize;
    
    float scaleY = this.getHeight() / Main.config.universeSize;

    Graphics2D g2 = (Graphics2D) g;

    AffineTransform origTransform = g2.getTransform();
    
    g2.scale(scaleX, scaleY);
    
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, (int)Main.config.universeSize+1, (int)Main.config.universeSize+1);
    g2.setColor(Color.WHITE);

    for (Drawable d : devices) {
      d.draw(g2);
    }

    g2.setColor(Color.RED);
    
    for (CaptureDisk d : disks) {
      d.draw(g2);
    }

    g2.setColor(Color.GREEN);
    
    for (Point2D p : points) {
      g2.fillOval((int) p.getX(), (int) p.getY(), 2, 2);
    }
    
    g2.setColor(Color.BLUE);
    for(Receiver p : receiverPoints){
      p.draw(g2);
    }
    
    g2.setTransform(origTransform);
  }

  public void setTransmitters(Collection<Transmitter> devices) {
    this.devices.clear();
    this.devices.addAll(devices);
    this.repaint(10);
  }

  public void clear() {
    this.devices.clear();
    this.disks.clear();
    this.points.clear();
    this.repaint(10);
  }

  public void setCaptureDisks(Collection<CaptureDisk> disks) {
    this.disks.clear();
    this.disks.addAll(disks);
    this.repaint(10);
  }

  public void setSolutionPoints(Collection<Point2D> points) {
    this.points.clear();
    this.points.addAll(points);
    this.repaint(10);
  }
  
  public void setReceiverPoints(Collection<Receiver> points){
    this.receiverPoints.clear();
    this.receiverPoints.addAll(points);
    this.repaint(10);
  }
}
