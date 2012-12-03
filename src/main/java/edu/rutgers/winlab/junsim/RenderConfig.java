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

}
