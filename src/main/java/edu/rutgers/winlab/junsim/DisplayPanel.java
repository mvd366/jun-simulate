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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package edu.rutgers.winlab.junsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author Robert Moore
 */
public class DisplayPanel extends JPanel {

  private Collection<Drawable> devices = new LinkedList<Drawable>();
  private Collection<CaptureDisk> disks = new LinkedList<CaptureDisk>();
  private Collection<Point2D> points = new LinkedList<Point2D>();
  private List<Collection<Point2D>> rankedPoints = null;
  private List<Integer> ranks = null;
  private Collection<Receiver> receiverPoints = new LinkedList<Receiver>();
  private Collection<CaptureDiskGroup> groups = new LinkedList<CaptureDiskGroup>();
  
  private Color backgroundColor = Color.BLACK;
  private Color fontColor = Color.WHITE;

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

    // Draw background color
    g2.setColor(this.backgroundColor);
    g2.fillRect(0, 0, (int) Main.config.universeWidth + 1,
        (int) Main.config.universeHeight + 1);

    // Capture disks (for overlapping arcs)
    g2.setColor(Color.RED);

    for (CaptureDisk d : disks) {
      d.draw(g2, scale, scale);
    }

    // Solution points
    g2.setColor(Color.GREEN);

    for (Point2D p : points) {
      g2.fillOval((int) (p.getX() * scale) - 1, (int) (p.getY() * scale) - 1,
          2, 2);
    }

    // Ranked points (for binned experiments)
    if (this.rankedPoints != null) {

      int i = 0;
      float numRanks = this.rankedPoints.size();
      for (Collection<Point2D> points : this.rankedPoints) {
        float hue = (i / numRanks) * 0.9f;
        Color c = Color.getHSBColor(hue, .9f, .9f);
        g2.setColor(c);
        for (Point2D p : points) {
          g2.fillOval((int) (p.getX() * scale) - 1,
              (int) (p.getY() * scale) - 1, 2, 2);
        }

        ++i;
      }
    }

    // Transmitters
    g2.setColor(this.fontColor);
    for (Drawable d : devices) {
      d.draw(g2, scale, scale);
    }

    g2.setColor(Color.BLUE);
    for (Receiver p : receiverPoints) {
      p.draw(g2, scale, scale);
    }

    // Capture disks groups
    if (!this.groups.isEmpty()) {
      float numGroups = this.groups.size();
      int i = 0;
      for (CaptureDiskGroup grp : this.groups) {
        float hue = i / numGroups;
        Color c = Color.getHSBColor(hue, .9f, .9f);
        g2.setColor(c);
        grp.draw(g2, scale, scale);
        ++i;
      }
    }

    g2.setTransform(origTransform);

    /*
     * Legend goes from maxX-30 -> maxX-10
     * from minY+10 -> minY+110
     * Looks like this:
     * +-----+
     * | High|
     * | Med |
     * | Low |
     * +-----+
     */
    if (this.rankedPoints != null) {
      FontMetrics metrics = g2.getFontMetrics();
      int fontHeight = metrics.getHeight();
      int fontWidth = metrics.stringWidth("0000");
      
      
      Rectangle2D legendBox = new Rectangle2D.Float(width-fontWidth-40, 10-fontHeight, width-2, 110+fontHeight);
      
      
      g2.setColor(this.backgroundColor);
      g2.fill(legendBox);
      g2.setColor(this.fontColor);
      g2.draw(legendBox);

      float numRanks = this.rankedPoints.size();

      float rankHeight = 100f / numRanks;
      float currStartY = 110;

     
      float lastFontStart = height;
      
      float maxRankSize = 0;
      for(Collection<Point2D> coll : this.rankedPoints){
        int size = coll.size();
        if(size > maxRankSize){
          maxRankSize = size;
        }
      }
      
      int numRanksInt = this.rankedPoints.size();

      for (int i = 0; i < numRanksInt; ++i, currStartY -= rankHeight) {
        int numPoints = this.rankedPoints.get(i).size();
        float hue = (i / numRanks) * 0.9f;
        Color c = Color.getHSBColor(hue, .9f, .9f);
        g2.setColor(c);
        float barWidth = (numPoints/maxRankSize) * 30;
        if(barWidth < 3){
          barWidth = 3;
        }
        
        Rectangle2D.Float rect = new Rectangle2D.Float(width - fontWidth-35, currStartY,
            barWidth, rankHeight);
        g2.fill(rect);
        if (currStartY < lastFontStart - fontHeight) {
          g2.setColor(this.fontColor);
          g2.drawString(String.format("%d", this.ranks.get(i)), width - fontWidth-5, currStartY+(fontHeight/2f));
          lastFontStart = currStartY;
        }

      }
    }
    g2.setColor(this.fontColor);

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
    this.groups.clear();
    this.rankedPoints = null;
    this.ranks = null;
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

  public void setRankedSolutionPoints(List<Collection<Point2D>> points, List<Integer> ranks) {
    this.rankedPoints = points;
    this.ranks = ranks;
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
