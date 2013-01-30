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

/**
 * @author Robert Moore
 */
public class ColorSet {
  private static final Color BACKGROUND_COLOR = Color.WHITE;
  private static final Color FONT_COLOR = Color.BLACK;

  public static final float[] RING_LIMITS = new float[] { 0.25f, 0.5f, 0.9f };
  
  public static final float[] LINE_THICKNESS = new float[] {1f, 2f, 3f, 4f};

  public static final Color[] RING_COLORS = new Color[] { Color.LIGHT_GRAY,
      Color.RED, Color.ORANGE, Color.GREEN };

  public static final Color[] RING_GRAYS = new Color[] {
      Color.getHSBColor(0, 0, 0.85f), Color.getHSBColor(0, 0, 0.6f),
      Color.getHSBColor(0, 0, 0.4f), Color.getHSBColor(0, 0, 0) };

  public static final float[] RING_RADII = new float[] { 2.5f, 5f, 7.5f, 10f };

  private static final Color STROKE_COLOR = Color.BLACK;
  
  private static final Color RECEIVER_COLOR = Color.BLUE;
  
  private static final Color RECEIVER_GRAY = Color.getHSBColor(0, 0, 0.5f);

  private transient boolean grayscale = false;

  public void setGrayscale(final boolean grayscale) {
    this.grayscale = grayscale;
  }

  public Color getStrokeColor() {
    return STROKE_COLOR;
  }

  public Color getBackgroundColor() {
    return BACKGROUND_COLOR;
  }

  public Color getFontColor() {
    return FONT_COLOR;
  }
  
  public Color getReceiverColor(){
    return this.grayscale ? RECEIVER_GRAY : RECEIVER_COLOR;
  }

  public Color getColorForPercent(final float percent) {

    Color returnedColor = this.grayscale ? RING_GRAYS[0] : RING_COLORS[0];
    for (int i = RING_LIMITS.length - 1; i >= 0; --i) {
      if (percent >= RING_LIMITS[i]) {
        returnedColor = this.grayscale ? RING_GRAYS[i+1] : RING_COLORS[i + 1];
        break;
      }
    }
    return returnedColor;
  }
  
  public float getThicknessForPercent(final float percent) {
    float thickness = LINE_THICKNESS[0];
    for(int i = RING_LIMITS.length -1; i >= 0; --i){
      if (percent >= RING_LIMITS[i]) {
        thickness = LINE_THICKNESS[i];
        break;
      }
    }
    return thickness;
  }

  public float getRadiusForPercent(final float percent) {
    float returnedRadius = RING_RADII[0];

    for (int i = RING_LIMITS.length - 1; i >= 0; --i) {
      if (percent >= RING_LIMITS[i]) {
        returnedRadius = RING_RADII[i + 1];
        break;
      }
    }
    return returnedRadius;
  }
}
