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
  float squareWidth = 1820f;
  float squareHeight = 980f;

  float universeWidth = 1920f;
  float universeHeight = 1080f;

  /**
   * Seed value for randomizing.
   */
  long randomSeed = 0l;

  /**
   * Number of simulation trials to run in non-display mode.
   */
  long numTrials = 1l;

  /**
   * File name for output statistics in non-display mode.
   */
  String outputFileName = "to-simulate.txt";

  /**
   * Number of worker threads for concurrent execution. Careful with large
   * numbers of transmitters!
   */
  int numThreads = 1;

  /**
   * Whether or not to remove solution points related to capture disks that
   * are removed.
   */
  boolean stripSolutionPoints = false;

  /**
   * Maximum range (in meters) that a transmitter may be heard by a receiver.
   */
  float maxRangeMeters = 100f;

  /**
   * Number of points per grid unit.
   */
  float gridDensity = 25f;

  /**
   * The location of the rendering configuration file.
   */
  String renderConfig = "src/main/resources/render.xml";

  /**
   * Type of experiment to run.
   * "basic", "binned", "grid", or "recursive"
   */
  String experimentType = "basic";
  
  /**
   * Whether or not to randomize grid points.
   */
  boolean randomized=false;

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

  public long getNumTrials() {
    return numTrials;
  }

  public void setNumTrials(long numTrials) {
    this.numTrials = numTrials;
  }

  public String getOutputFileName() {
    return outputFileName;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }

  public int getNumThreads() {
    return numThreads;
  }

  public void setNumThreads(int numThreads) {
    this.numThreads = numThreads;
  }

  public float getGridDensity() {
    return gridDensity;
  }

  public void setGridDensity(float gridDensity) {
    this.gridDensity = gridDensity;
  }

  public String getRenderConfig() {
    return renderConfig;
  }

  public void setRenderConfig(String renderConfig) {
    this.renderConfig = renderConfig;
  }

  public boolean isRandomized() {
    return randomized;
  }

  public void setRandomized(boolean randomized) {
    this.randomized = randomized;
  }
}
