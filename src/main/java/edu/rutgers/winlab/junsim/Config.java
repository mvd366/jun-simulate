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

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author Robert Moore
 * 
 */
public class Config {
  /**
   * Constant for determining the capture disk of some transmitter in relation
   * to another.
   */
  float beta = 0.5f;
  /**
   * The number of transmitters in the experiment.
   */
  int numTransmitters = 1;
  /**
   * The number of receivers in the experiment.
   */
  int numReceivers = 1;
  /**
   * Scaling factor for the power of the radio. Power at distance r from the
   * transmitter is equal to radioPower/(r^radioAlpha)
   */
  float radioPower = 2;
  /**
   * The signal propagation value, usually between 2-4.
   */
  float radioAlpha = 2.68f;
  /**
   * The length of one side of the square where transmitters can be located.
   */
  private float squareSize = 100f;
  
  
  float universeSize = this.squareSize*1.5f;

  /**
   * Seed value for randomizing.
   */
  long randomSeed = 0l;
  
  /**
   * Whether or not to show the graphical display.
   */
  boolean showDisplay = true;

  public int getNumTransmitters() {
    return numTransmitters;
  }

  public void setNumTransmitters(int numTransmitters) {
    this.numTransmitters = numTransmitters;
  }

  public int getNumReceivers() {
    return numReceivers;
  }

  public void setNumReceivers(int numReceivers) {
    this.numReceivers = numReceivers;
  }

  public float getRadioPower() {
    return radioPower;
  }

  public void setRadioPower(float radioPower) {
    this.radioPower = radioPower;
  }

  public float getRadioAlpha() {
    return radioAlpha;
  }

  public void setRadioAlpha(float radioAlpha) {
    this.radioAlpha = radioAlpha;
  }

  public float getSquareSize() {
    return squareSize;
  }

  public void setSquareSize(float squareSize) {
    this.squareSize = squareSize;
    this.universeSize = this.squareSize*1.5f;
  }

  public long getRandomSeed() {
    return randomSeed;
  }

  public void setRandomSeed(long randomSeed) {
    this.randomSeed = randomSeed;
  }

  public float getBeta() {
    return beta;
  }

  public void setBeta(float beta) {
    this.beta = beta;
  }

  public boolean isShowDisplay() {
    return showDisplay;
  }

  public void setShowDisplay(boolean showDisplay) {
    this.showDisplay = showDisplay;
  }

  public float getUniverseSize() {
    return universeSize;
  }

  public void setUniverseSize(float universeSize) {
    this.universeSize = universeSize;
  }
}
