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
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Robert Moore
 * 
 */
public class CaptureDiskGroup implements Drawable {

  public final Collection<CaptureDisk> disks = new HashSet<CaptureDisk>();

  @Override
  public void draw(Graphics2D g, float scaleX, float scaleY) {
    // Color origColor = g.getColor();
    // g.setColor(Color.YELLOW);
    for (CaptureDisk disk : this.disks) {
      disk.disk.draw(g, scaleX, scaleY, true);
    }
    // g.setColor(origColor);
  }

  public int getRank() {
    return this.disks.size();
  }

  public boolean intersects(CaptureDiskGroup g) {
    if(this.disks.size() == 0 || g.disks.size() == 0){
      return false;
    }
    for (CaptureDisk d1 : this.disks) {
      for (CaptureDisk d2 : g.disks) {
        if (!d1.disk.intersects(d2.disk)) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    for(CaptureDisk d : this.disks){
      sb.append(d).append(",");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o){
    if(o instanceof CaptureDiskGroup){
      return this.equals((CaptureDiskGroup)o);
    }
    return super.equals(o);
  }
  
  public boolean equals(CaptureDiskGroup g){
    return this.disks.containsAll(g.disks);
  }
  
  @Override
  public int hashCode(){
    int hashcode = 0;
    for(CaptureDisk d : this.disks){
      hashcode |= d.hashCode();
    }
    return hashcode;
  }
  
}
