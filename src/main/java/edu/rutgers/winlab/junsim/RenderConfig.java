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

import com.thoughtworks.xstream.XStream;

/**
 * @author Robert Moore
 *
 */
public class RenderConfig {
 
  
  /**
   * Whether or not to render images.
   */
  boolean generateImages = false;
  
  /**
   * Pixel width of rendered image.
   */
  int renderWidth = 800;
  /**
   * Pixel height of rendered image.
   */
  int renderHeight = 600;
  
  boolean drawTransmitters = true;
  
  boolean drawReceivers = true;
  
  boolean drawReceiverLines = true;
  
  boolean drawSolutionPoints = true;
  
  boolean drawLegend = true;
  
  boolean useColorMode = true;
  
  boolean drawCaptureDisks = false;
  
  public boolean isGenerateImages() {
    return generateImages;
  }
  public void setGenerateImages(boolean generateImages) {
    this.generateImages = generateImages;
  }
  public int getRenderWidth() {
    return renderWidth;
  }
  public void setRenderWidth(int renderWidth) {
    this.renderWidth = renderWidth;
  }
  public int getRenderHeight() {
    return renderHeight;
  }
  public void setRenderHeight(int renderHeight) {
    this.renderHeight = renderHeight;
  }
  public boolean isDrawTransmitters() {
    return drawTransmitters;
  }
  public void setDrawTransmitters(boolean drawTransmitters) {
    this.drawTransmitters = drawTransmitters;
  }
  public boolean isDrawReceivers() {
    return drawReceivers;
  }
  public void setDrawReceivers(boolean drawReceivers) {
    this.drawReceivers = drawReceivers;
  }
  public boolean isDrawReceiverLines() {
    return drawReceiverLines;
  }
  public void setDrawReceiverLines(boolean drawReceiverLines) {
    this.drawReceiverLines = drawReceiverLines;
  }
  public boolean isDrawSolutionPoints() {
    return drawSolutionPoints;
  }
  public void setDrawSolutionPoints(boolean drawSolutionPoints) {
    this.drawSolutionPoints = drawSolutionPoints;
  }
  public boolean isDrawLegend() {
    return drawLegend;
  }
  public void setDrawLegend(boolean drawLegend) {
    this.drawLegend = drawLegend;
  }
  public boolean isUseColorMode() {
    return useColorMode;
  }
  public void setUseColorMode(boolean useColorMode) {
    this.useColorMode = useColorMode;
  }
  public boolean isDrawCaptureDisks() {
    return drawCaptureDisks;
  }
  public void setDrawCaptureDisks(boolean drawCaptureDisks) {
    this.drawCaptureDisks = drawCaptureDisks;
  }

  public static void main(String[]args){
    XStream x = new XStream();
    System.out.println(x.toXML(new RenderConfig()));
  }
  
}
