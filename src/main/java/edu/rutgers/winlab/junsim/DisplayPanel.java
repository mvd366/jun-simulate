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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
  private Collection<CaptureDiskGroup> groups = new LinkedList<CaptureDiskGroup>();

  public DisplayPanel() {
    super();
    this.setPreferredSize(new Dimension(640, 480));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!this.isVisible()) {
      return;
    }
    this.render(g, this.getWidth(), this.getHeight());

  }

  public void render(Graphics g, int width, int height) {
    

    // Figure-out the scaling based on aspect-ratios

    float displayRatio = 1f * width / height;
    float scale = 1;
    // float scaleY = 1;
    // Widescreen

    Graphics2D g2 = (Graphics2D) g;

    AffineTransform origTransform = g2.getTransform();
    
    

    if (displayRatio > 1.001f) {
      scale = height / Main.config.universeHeight;
      int marginX = width - (int) (Main.config.universeWidth * scale);
      g2.translate(marginX / 2, 0);
    }
    // Tall-screen
    else if (displayRatio < 0.999f) {
      scale = width / Main.config.universeWidth;
      int marginY = height - (int) (Main.config.universeHeight * scale);
      g2.translate(0, marginY / 2);
    }

    // g2.scale(scaleX, scaleY);

    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, (int) Main.config.universeWidth + 1,
        (int) Main.config.universeHeight + 1);
    g2.setColor(Color.WHITE);

    for (Drawable d : devices) {
      d.draw(g2, scale, scale);
    }

    g2.setColor(Color.RED);

    for (CaptureDisk d : disks) {
      d.draw(g2, scale, scale);
    }

    g2.setColor(Color.GREEN);

    for (Point2D p : points) {
      g2.fillOval((int) (p.getX() * scale)-1, (int) (p.getY() * scale)-1, 2, 2);
    }

    g2.setColor(Color.BLUE);
    for (Receiver p : receiverPoints) {
      p.draw(g2, scale, scale);
    }
    float numGroups = this.groups.size();
    int i = 0;
    for(CaptureDiskGroup grp : this.groups){
      float hue = i/numGroups;
      Color c = Color.getHSBColor(hue, .9f, .9f);
      g2.setColor(c);
      grp.draw(g2,scale,scale);
      ++i;
    }

    g2.setColor(Color.WHITE);

    g2.setTransform(origTransform);
    g2.drawString(
        "T" + this.devices.size() + " R" + this.receiverPoints.size(), 0,
        height);
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

  public void setReceiverPoints(Collection<Receiver> points) {
    this.receiverPoints.clear();
    this.receiverPoints.addAll(points);
    this.repaint(10);
  }

  public void setCaptureDiskGroups(Collection<CaptureDiskGroup> groups) {
    this.groups.clear();
    this.groups.addAll(groups);
    this.repaint(10);
  }
}
