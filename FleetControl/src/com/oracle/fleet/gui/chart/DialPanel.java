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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;

import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

/**
 * A sample application showing the use of a {@link DialPlot}.
 */
public class DialPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The first dataset. */
	private DefaultValueDataset dataset1 = null;
	/** The second dataset. */
	private DefaultValueDataset dataset2 = null;
	private DialTextAnnotation annotation = null;
	private DialTextAnnotation annotation2 = null;
	private StandardDialScale dialScale;
	private StandardDialRange range;
	private StandardDialRange range2;
	private StandardDialRange range3;

	/**
	 * Creates a new demo panel.
	 */
	/**
	 * Creates a new demo panel.
	 */
	public DialPanel(String title, String throughput) {
		this(title, throughput, 500);
	}

	public DialPanel(String title, String throughput, int scale) {

		super(new BorderLayout());
		this.dataset1 = new DefaultValueDataset(0.0);
		this.dataset2 = new DefaultValueDataset(0.0);

		// get data for diagrams
		JFreeChart chart = createStandardDialChart(title, throughput,
				this.dataset1, this.dataset1, 0, scale, scale / 10, 4);
		DialPlot plot = (DialPlot) chart.getPlot();

		range3 = new StandardDialRange(0.0, scale / 5, Color.GREEN);
		range3.setInnerRadius(0.52);
		range3.setOuterRadius(0.55);
		plot.addLayer(range3);

		range2 = new StandardDialRange(scale / 5, scale / 2.5, Color.GREEN);
		range2.setInnerRadius(0.52);
		range2.setOuterRadius(0.55);
		plot.addLayer(range2);

		range = new StandardDialRange(scale / 2.5, scale, Color.GREEN);
		range.setInnerRadius(0.52);
		range.setOuterRadius(0.55);

		plot.addLayer(range);

		GradientPaint gp = new GradientPaint(new Point(), new Color(255, 255,
				255), new Point(), new Color(170, 170, 220));
		DialBackground db = new DialBackground(gp);
		db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
				GradientPaintTransformType.VERTICAL));
		plot.setBackground(db);

		// plot.removePointer(0);
		// plot.addPointer(new DialPointer.Pointer());

		ChartPanel cp1 = new ChartPanel(chart);
		cp1.setPreferredSize(new Dimension(400, 400));

		add(cp1);

	}

	/**
	 * Creates a chart displaying a circular dial.
	 *
	 * @param chartTitle
	 *            the chart title.
	 * @param dialLabel
	 *            the dial label.
	 * @param dataset
	 *            the dataset.
	 * @param lowerBound
	 *            the lower bound.
	 * @param upperBound
	 *            the upper bound.
	 * @param increment
	 *            the major tick increment.
	 * @param minorTickCount
	 *            the minor tick count.
	 *
	 * @return A chart that displays a value as a dial.
	 */
	public JFreeChart createStandardDialChart(String chartTitle,
			String dialLabel, ValueDataset dataset1, ValueDataset dataset2,
			double lowerBound, double upperBound, double increment,
			int minorTickCount) {

		// get data for diagrams
		DialPlot plot = new DialPlot();
		plot.setView(0.0, 0.0, 1.0, 1.0);
		plot.setDataset(0, this.dataset1);
		plot.setDataset(1, this.dataset2);

		StandardDialFrame dialFrame = new StandardDialFrame();
		dialFrame.setBackgroundPaint(Color.lightGray);
		dialFrame.setForegroundPaint(Color.darkGray);
		plot.setDialFrame(dialFrame);

		GradientPaint gp = new GradientPaint(new Point(), new Color(255, 255,
				255), new Point(), new Color(170, 170, 220));
		DialBackground db = new DialBackground(gp);
		db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
				GradientPaintTransformType.VERTICAL));
		plot.setBackground(db);

		// Add throughput label
		annotation = new DialTextAnnotation(dialLabel);
		annotation.setFont(new Font("Dialog", Font.BOLD, 14));
		annotation.setRadius(0.7);
		plot.addLayer(annotation);

		// add Latency label
		annotation2 = new DialTextAnnotation("miles/hr (Speed)");
		annotation2.setFont(new Font("Dialog", Font.PLAIN, 14));
		annotation2.setPaint(Color.RED);
		annotation2.setRadius(0.8);
		plot.addLayer(annotation2);

		DialValueIndicator dvi = new DialValueIndicator(0);
		dvi.setFont(new Font("Dialog", Font.PLAIN, 10));
		dvi.setOutlinePaint(Color.darkGray);
		dvi.setRadius(0.6);
		dvi.setAngle(-103.0);
		plot.addLayer(dvi);

		DialValueIndicator dvi2 = new DialValueIndicator(1);
		dvi2.setFont(new Font("Dialog", Font.PLAIN, 10));
		dvi2.setOutlinePaint(Color.red);
		dvi2.setRadius(0.60);
		dvi2.setAngle(-77.0);
		plot.addLayer(dvi2);

		dialScale = new StandardDialScale(lowerBound, upperBound, -120, -300,
				10.0, 4);

		dialScale.setMajorTickIncrement(increment);
		dialScale.setMinorTickCount(minorTickCount);
		dialScale.setTickRadius(0.88);
		dialScale.setTickLabelOffset(0.15);
		dialScale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));
		plot.addScale(0, dialScale);

		StandardDialScale scale2 = new StandardDialScale(0, 100, -120, -300,
				10.0, 4);
		// scale2.setMajorTickIncrement(increment);
		// scale2.setMinorTickCount(minorTickCount);
		scale2.setTickRadius(0.50);
		scale2.setTickLabelOffset(0.15);
		scale2.setTickLabelFont(new Font("Dialog", Font.PLAIN, 10));
		scale2.setMajorTickPaint(Color.red);
		scale2.setMinorTickPaint(Color.red);
		plot.addScale(1, scale2);
		plot.mapDatasetToScale(1, 1);

		DialPointer needle2 = new DialPointer.Pin(1);
		needle2.setRadius(0.55);
		plot.addPointer(needle2);

		DialPointer needle = new DialPointer.Pointer(0);
		plot.addPointer(needle);

		DialCap cap = new DialCap();
		cap.setRadius(0.10);
		plot.setCap(cap);

		return new JFreeChart(chartTitle, plot);

	}

	public void setLabel(String label) {
		this.annotation.setLabel(label);
	}

	/**
	 * Handle a change in the slider by updating the dataset value. This
	 * automatically triggers a chart repaint.
	 *
	 * @param e
	 *            the event.
	 */
	public void setNeedle(double value1, double value2) {
		this.dataset1.setValue(value1);
		this.dataset2.setValue(value2);

		// System.out.println("Outer needle: " + value1 + " inner: " + value2);

	}

	public void setScale(int dialScale) {
		this.dialScale.setUpperBound(dialScale);
		this.dialScale.setMajorTickIncrement(dialScale / 10);

		this.range3.setBounds(0.0, dialScale / 5);
		this.range2.setBounds(dialScale / 5, dialScale / 2.5);
		this.range.setBounds(dialScale / 2.5, dialScale);

	}

}
