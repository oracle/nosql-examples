/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database. If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */ 

package com.oracle.fleet.gui.chart;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class LineChartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private XYSeries series1 = null;
	private XYSeries series2 = null;
	private XYSeries series3 = null;
	private XYPlot plot = null;
	private String yAxisLabel = null;
	private String s1;
	private String s2;
	private String s3;

	/**
	 * Creates a new demo instance.
	 *
	 * @param title
	 *            the frame title.
	 */
	public LineChartPanel(String label, String series1, String series2,
			String series3) {
		this.yAxisLabel = label;
		this.s1 = series1;
		this.s2 = series2;
		this.s3 = series3;
	}

	/**
	 * Creates a sample dataset.
	 *
	 * @return A sample dataset.
	 */
	public XYDataset create3SeriesDataset() {

		series1 = new XYSeries(s1);
		series2 = new XYSeries(s2);
		series3 = new XYSeries(s3);

		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);

		return dataset;
	}
	
	public void addToSeries1(int x, double y){
		//System.out.println("series1: " + x + " " + y);
		this.series1.add(x, y);		
	}
	
	public void addToSeries2(int x, double y){
		//System.out.println("series2: " + x + " " + y);
		this.series2.add(x, y);
	}
	
	public void addToSeries3(int x, double y){
		//System.out.println("series3: " + x + " " + y);
		this.series3.add(x, y);
	}

	/**
	 * Creates a sample chart.
	 *
	 * @param dataset
	 *            the dataset.
	 *
	 * @return A sample chart.
	 */
	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart(null, "Date",
				this.yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true,
				false);

		plot = (XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(false);
		// plot.setBackgroundPaint(Color.BLACK);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		plot.setBackgroundPaint(Color.lightGray);
	    plot.setDomainGridlinePaint(Color.white);
	    plot.setRangeGridlinePaint(Color.white);
	        
		// Make Time series in visible on X axis
		//plot.getDomainAxis().setVisible(false);
		//plot.getRangeAxis().setDefaultAutoRange(new Range(0, this.range));

		XYItemRenderer r = plot.getRenderer();
		r.setSeriesPaint(0, Color.RED);
		r.setSeriesPaint(1, Color.BLUE);
		r.setSeriesPaint(2, Color.MAGENTA);

		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesVisible(false);
		}
		// change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
		return chart;
	}

	public JPanel create3LinePanel() {
		JFreeChart chart = createChart(create3SeriesDataset());
		ChartPanel panel = new ChartPanel(chart);
		panel.setMouseWheelEnabled(true);
		panel.setPreferredSize(new Dimension(1050, 250));
		return panel;
	}

	public void setLabel(String label){
		yAxisLabel = label;		
		plot.getRangeAxis().setLabel(label);
	}

	public void clear() {

		this.series1.clear();
		this.series2.clear();
		this.series3.clear();
	}
}
