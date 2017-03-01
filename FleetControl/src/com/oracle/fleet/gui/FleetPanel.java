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

package com.oracle.fleet.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import oracle.kv.Direction;

import com.oracle.fleet.constant.TableConstants;
import com.oracle.fleet.dao.MileageDAO;
import com.oracle.fleet.dao.FleetDAO;
import com.oracle.fleet.exception.DAOException;
import com.oracle.fleet.gui.chart.DialPanel;
import com.oracle.fleet.gui.chart.LineChartPanel;
import com.oracle.fleet.to.MileageTO;
import com.oracle.fleet.to.FleetTO;

/**
 * Two dials on one panel.
 */
public class FleetPanel extends JPanel implements ActionListener, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String label = "miles/G (mileage) ";

	// private DialPanel a = new DialPanel("24[8x3] Node Cluster", WRITE_OPS);
	private DialPanel a = new DialPanel("", label, 50);
	private DialPanel b = new DialPanel("", label, 50);
	private DialPanel c = new DialPanel("", label, 50);

	private LineChartPanel lineChartPanel = new LineChartPanel(label,
			"1st Fleet", "2nd Fleet", "3rd Fleet");
	private ImageIcon topIcon = null;
	private ImageIcon bottomIcon = null;
	private ImageIcon stopIcon = null;
	private ImageIcon startIcon = null;
	private JButton modeButton = null;
	private static JButton actionButton = null;
	private boolean loopFlag = true;

	private double avg1 = 0;
	private double avg2 = 0;
	private double avg3 = 0;
	private int speed1 = 0;
	private int speed2 = 0;
	private int speed3 = 0;
	private int winSize = 24;

	private JLabel leftVINTitle = null;
	private JLabel centerVINTitle = null;
	private JLabel rightVINTitle = null;

	private JLabel leftDialTitle = null;
	private JLabel centerDialTitle = null;
	private JLabel rightDialTitle = null;
	private String[] graphStr = { "Mileage", "Fuel", "Distance" };
	private JComboBox graphCombo = null;

	// Data points
	private List<MileageTO> mileageTOList = null;
	private MileageTO aggregateTO = null;

	private MileageDAO mileageDAO = new MileageDAO();
	private FleetDAO fleetDAO = new FleetDAO();
	private String date = null;

	private Direction direction = Direction.REVERSE;

	/**
	 * Creates a new instance.
	 *
	 * @param title
	 *            the frame title.
	 */

	public FleetPanel(String title) {

		graphCombo = new JComboBox(graphStr);
		topIcon = new ImageIcon(getClass().getResource("/images/top.new.jpg"));
		bottomIcon = new ImageIcon(getClass().getResource(
				"/images/bottom.new.jpg"));
		stopIcon = new ImageIcon(getClass().getResource(
				"/images/stop-button.new.JPG"));
		startIcon = new ImageIcon(getClass().getResource(
				"/images/start-button.new.JPG"));

		graphCombo.setSelectedIndex(0);
		graphCombo.setMaximumSize(new Dimension(10, 2));
		graphCombo.addActionListener(this);
	}

	public static void setActionButtonEnabled(boolean actionEnabled) {
		actionButton.setEnabled(actionEnabled);
	}

	public JSplitPane getSplitPane() {

		JPanel topPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel topLabelPanel = new JPanel(new GridLayout(3, 3));
		JPanel bottomLabelPanel = new JPanel(new GridLayout(2, 3));
		JPanel combolPanel = new JPanel(new GridLayout(1, 3));

		JPanel labelDialPanel = new JPanel();
		labelDialPanel
				.setLayout(new BoxLayout(labelDialPanel, BoxLayout.Y_AXIS));

		// Add Label followed by Dial into the BoxLayout
		labelDialPanel.add(topLabelPanel);
		labelDialPanel.add(this.getDialPanel());
		labelDialPanel.add(bottomLabelPanel);
		// labelDialPanel.add(sFactorLabelPanel);

		JLabel ondbTitle = new JLabel("Oracle NoSQL Database");
		ondbTitle.setHorizontalAlignment(JLabel.CENTER);
		ondbTitle.setForeground(Color.RED);
		ondbTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 32));

		JLabel tabTitle = new JLabel("Fleet Dashboard");
		tabTitle.setHorizontalAlignment(JLabel.CENTER);
		tabTitle.setForeground(Color.BLUE);
		tabTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 24));

		// Dial top title
		leftDialTitle = new JLabel("1st Fleet");
		leftDialTitle.setHorizontalAlignment(JLabel.CENTER);
		leftDialTitle.setForeground(Color.BLACK);
		leftDialTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 16));

		centerDialTitle = new JLabel("2nd Fleet");
		centerDialTitle.setHorizontalAlignment(JLabel.CENTER);
		centerDialTitle.setForeground(Color.BLACK);
		centerDialTitle
				.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 16));

		rightDialTitle = new JLabel("3rd Fleet");
		rightDialTitle.setHorizontalAlignment(JLabel.CENTER);
		rightDialTitle.setForeground(Color.BLACK);
		rightDialTitle
				.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 16));

		// Bottom Labels under each dial
		JLabel centerTitle = new JLabel("Central");
		centerTitle.setHorizontalAlignment(JLabel.CENTER);
		centerTitle.setForeground(Color.RED);
		centerTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 20));

		JLabel eastTitle = new JLabel("Eastern");
		eastTitle.setHorizontalAlignment(JLabel.CENTER);
		eastTitle.setForeground(Color.BLUE);
		eastTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 20));

		JLabel westTitle = new JLabel("Western");
		westTitle.setHorizontalAlignment(JLabel.CENTER);
		westTitle.setForeground(Color.MAGENTA);
		westTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 20));

		// Add scalability factor
		leftVINTitle = new JLabel("VIN: ");
		leftVINTitle.setHorizontalAlignment(JLabel.CENTER);
		leftVINTitle.setForeground(Color.RED);
		leftVINTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 10));

		centerVINTitle = new JLabel("VIN: ");
		centerVINTitle.setHorizontalAlignment(JLabel.CENTER);
		centerVINTitle.setForeground(Color.BLUE);
		centerVINTitle
				.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 10));

		rightVINTitle = new JLabel("VIN: ");
		rightVINTitle.setHorizontalAlignment(JLabel.CENTER);
		rightVINTitle.setForeground(Color.MAGENTA);
		rightVINTitle.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 10));

		// first add combo box in the 6th column of combolPanel
		combolPanel.add(new JLabel(""));
		combolPanel.add(new JLabel(""));
		combolPanel.add(graphCombo);

		// now add the combo box panel and other titles into the topLabelPanel
		topLabelPanel.add(new JLabel(""));
		topLabelPanel.add(ondbTitle);
		topLabelPanel.add(combolPanel);
		// add combo box to the right
		topLabelPanel.add(new JLabel(""));
		topLabelPanel.add(tabTitle);
		topLabelPanel.add(new JLabel(""));

		topLabelPanel.add(leftDialTitle);
		topLabelPanel.add(centerDialTitle);
		topLabelPanel.add(rightDialTitle);

		// Add data center labels
		bottomLabelPanel.add(leftVINTitle);
		bottomLabelPanel.add(centerVINTitle);
		bottomLabelPanel.add(rightVINTitle);
		// add empty row
		bottomLabelPanel.add(new JLabel(""));
		bottomLabelPanel.add(new JLabel(""));
		bottomLabelPanel.add(new JLabel(""));

		// Dimension for Dial Graphs
		Dimension d1 = new Dimension(1280, 550);
		// Dimension for Line Graphs
		Dimension d2 = new Dimension(1280, 300);

		// Add dial to topPanel
		topPanel.add(labelDialPanel);
		topPanel.setMinimumSize(d1);

		// Add Line graphs and buttons to bottomPanel
		bottomPanel.add(this.createLinePanel());

		bottomPanel.setMinimumSize(d2);
		bottomPanel.setMaximumSize(d2);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
				topPanel, bottomPanel);

		splitPane.setOneTouchExpandable(true);

		return splitPane;

	}

	private JPanel createLinePanel() {
		JPanel bottomPanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

		modeButton = new JButton(topIcon);
		modeButton.addActionListener(this);
		modeButton.setName("Top");
		// modeButton.setPreferredSize();

		actionButton = new JButton(this.startIcon);
		actionButton.addActionListener(this);
		actionButton.setName("Start");
		actionButton.setEnabled(true);
		actionButton.setPreferredSize(new Dimension(100, 60));

		// Add buttons to buttonPanel
		buttonPanel.add(modeButton);
		buttonPanel.add(actionButton);

		bottomPanel.add(lineChartPanel.create3LinePanel());
		// bottomPanel.add(modeButton);
		bottomPanel.add(buttonPanel);

		return bottomPanel;
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() instanceof JButton) {

			JButton clickedButton = (JButton) event.getSource();
			String buttonName = clickedButton.getName();

			// System.out.println(buttonName);
			if ("Top".equals(buttonName)) {
				clickedButton.setIcon(bottomIcon);
				clickedButton.setName("Bottom");

				// set direction forward
				direction = Direction.FORWARD;

				// Change the throughput label in dials and line series
				this.setLabelAndScale(label, 50, 100);

			} else if ("Bottom".equals(buttonName)) {
				clickedButton.setIcon(topIcon);
				clickedButton.setName("Top");

				// set direction reverse
				direction = Direction.REVERSE;

				// Change the throughput label in dials and line series
				this.setLabelAndScale(label, 50, 100);

			} else if ("Stop".equals(buttonName)) {
				clickedButton.setIcon(startIcon);
				clickedButton.setName("Start");
				this.modeButton.setEnabled(true);

				// stop the tests
				this.loopFlag = false;

			} else if ("Start".equals(buttonName)) {
				clickedButton.setIcon(stopIcon);
				clickedButton.setName("Stop");
				this.modeButton.setEnabled(false);
				// reset everything before fresh start
				this.initGraph();

				// toggle the loopFlag
				this.loopFlag = true;

				// Start a separate thread to plot the values on graph
				Thread t = new Thread(this);
				t.start();

			}
		}// EOF if

	}// actionPerformed

	/**
	 * Creates a demo panel. This method is called by SuperDemo.java.
	 *
	 * @return A demo panel.
	 */
	public JPanel getDialPanel() {
		JPanel demoPanel = new JPanel(new GridLayout(1, 3));

		demoPanel.add(a);
		demoPanel.add(b);
		demoPanel.add(c);

		return demoPanel;
	}

	public void setLabelAndScale(String label, int scale, int yAxisRange) {
		// Change the throughput label on the dials
		this.a.setLabel(label);
		this.b.setLabel(label);
		this.c.setLabel(label);

		System.out.println("Scale: " + scale);

		// change the scale of dials to 100
		this.a.setScale(scale);
		this.b.setScale(scale);
		this.c.setScale(scale);

		// Chane line y-axis lable
		this.lineChartPanel.setLabel(label);
		this.initGraph();

	}

	public void initGraph() {
		// Reset dial needles
		// Reset dial needles
		this.a.setNeedle(0, 0);
		this.b.setNeedle(0, 0);
		this.c.setNeedle(0, 0);

		// reset series data
		this.lineChartPanel.clear();
	}

	public void run() {

		System.out.println("loopFlag: " + loopFlag);
		String comboSelection = this.graphCombo.getSelectedItem().toString();

		while (loopFlag) {

			try {
				// Based on graphCombo selection and direction get the top or
				// bottom 3 Fleets
				if (comboSelection.equalsIgnoreCase("Mileage"))
					mileageTOList = mileageDAO.getOrderedMileage(date,
							direction, TableConstants.MPG_INDEX);
				else if (comboSelection.equalsIgnoreCase("Fuel"))
					mileageTOList = mileageDAO.getOrderedMileage(date,
							direction, TableConstants.FUEL_INDEX);
				else if (comboSelection.equalsIgnoreCase("Distance"))
					mileageTOList = mileageDAO.getOrderedMileage(date,
							direction, TableConstants.DIST_INDEX);

				this.plot(winSize);

			} catch (Exception e) {

				e.printStackTrace();
				this.reset();
				// Click the Stop button
				actionButton.doClick();

			}
		} // EOF while

	}// run

	public void plot(int winSize) {
		String title = null;
		String vin1 = null;
		String vin2 = null;
		String vin3 = null;
		FleetTO fleetTO = null;

		try {

			/**
			 * For those 3 fleets get details based on graphCombo and plot the
			 * dials and line graphs
			 */
			if (mileageTOList.size() > 2) {
				// first element
				aggregateTO = mileageTOList.get(0);
				speed1 = Math.round(aggregateTO.getAvgSpeed());
				avg1 = this.getDataPoint(aggregateTO);
				vin1 = aggregateTO.getVin();
				fleetTO = fleetDAO.getFleet(vin1);
				title = fleetTO.getMake() + "/" + fleetTO.getModel()
						+ ", Year: " + fleetTO.getYear();
				// update labels
				leftDialTitle.setText(title);
				leftVINTitle.setText("VIN: " + vin1);

				// Second element
				aggregateTO = mileageTOList.get(1);
				speed2 = Math.round(aggregateTO.getAvgSpeed());
				avg2 = this.getDataPoint(aggregateTO);
				vin2 = aggregateTO.getVin();
				fleetTO = fleetDAO.getFleet(vin2);
				title = fleetTO.getMake() + "/" + fleetTO.getModel()
						+ ", Year: " + fleetTO.getYear();
				// update labels
				centerDialTitle.setText(title);
				centerVINTitle.setText("VIN: " + vin2);

				// Third element
				aggregateTO = mileageTOList.get(2);
				speed3 = Math.round(aggregateTO.getAvgSpeed());
				avg3 = this.getDataPoint(aggregateTO);
				vin3 = aggregateTO.getVin();
				fleetTO = fleetDAO.getFleet(vin3);
				title = fleetTO.getMake() + "/" + fleetTO.getModel()
						+ ", Year: " + fleetTO.getYear();
				// update labels
				rightDialTitle.setText(title);
				rightVINTitle.setText("VIN: " + vin3);

				// update dials and line graphs
				this.addToSeries(avg1, avg2, avg3, speed1, speed2, speed3,
						vin1, vin2, vin3);

				avg1 = 0;
				avg2 = 0;
				avg3 = 0;
				speed1 = 0;
				speed2 = 0;
				speed3 = 0;
			}// if(mileageTOList.size()>3)
			
			Thread.sleep(1000);
			// after 3 seconds clear the line graph
			// this.lineChartPanel.clear();

		} catch (InterruptedException ex) {
			Logger.getLogger(ChartPlotter.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}// plot

	public void addToSeries(double avg1, double avg2, double avg31, int speed1,
			int speed2, int speed3, String vin1, String vin2, String vin3) {

		int len = 0;
		int date = 0;
		double data = 0;

		List<MileageTO> aggregateTOList1 = null;
		List<MileageTO> aggregateTOList2 = null;
		List<MileageTO> aggregateTOList3 = null;

		// Change the dials with latest values
		this.a.setNeedle(avg1, speed1);
		this.b.setNeedle(avg2, speed2);
		this.c.setNeedle(avg3, speed3);

		/**
		 * To create the line graph fetch average value for each VIN
		 */
		try {

			// Get all the data point for 3 series first
			aggregateTOList1 = mileageDAO.get(vin1);
			aggregateTOList2 = mileageDAO.get(vin2);
			aggregateTOList3 = mileageDAO.get(vin3);

			len = aggregateTOList1.size();
			// System.out.println("Length: " + len);
			// initialize the line graph
			this.lineChartPanel.clear();

			// plot all three line series with historical data
			for (int i = 0; i < len; i++) {

				// first series
				aggregateTO = aggregateTOList1.get(i);
				date = len - i;
				data = this.getDataPoint(aggregateTO);
				this.lineChartPanel.addToSeries1(date, data);

				// second series
				aggregateTO = aggregateTOList2.get(i);
				data = this.getDataPoint(aggregateTO);
				this.lineChartPanel.addToSeries2(date, data);

				// Third series
				aggregateTO = aggregateTOList3.get(i);
				data = this.getDataPoint(aggregateTO);
				this.lineChartPanel.addToSeries3(date, data);

				// Thread.sleep(1000);

			}// for (int i = 0; i < len; i++) {

		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}// addToSeries

	private void reset() {
		avg1 = 0;
		avg2 = 0;
		avg3 = 0;
		speed1 = 0;
		speed2 = 0;
		speed3 = 0;

		// reset titles
		leftDialTitle.setText("Fleet");
		leftVINTitle.setText("VIN");

		centerDialTitle.setText("Fleet");
		centerVINTitle.setText("VIN");

		rightDialTitle.setText("Fleet");
		rightVINTitle.setText("VIN");

	}

	private double getDataPoint(MileageTO avgTO) {
		double data = 0;
		// selection 0 is mileage
		if (this.graphCombo.getSelectedIndex() == 0) {
			data = avgTO.getMpg();
			this.setDialScale(50);
			this.setLabel("miles/G (mileage)");
		}
		// selection 1 is fuel
		else if (this.graphCombo.getSelectedIndex() == 1) {
			data = avgTO.getTotalFuel();
			this.setDialScale(10);
			this.setLabel("gallons (fuel)");
		}
		// selection 2 is distance
		else if (this.graphCombo.getSelectedIndex() == 2) {
			data = avgTO.getTotalDistance();
			this.setDialScale(100);
			this.setLabel("miles (dist)");
		}

		return data;
	}// getDataPoint

	private void setDialScale(int dialScale) {
		this.a.setScale(dialScale);
		this.b.setScale(dialScale);
		this.c.setScale(dialScale);
	}

	private void setLabel(String label) {
		this.a.setLabel(label);
		this.b.setLabel(label);
		this.c.setLabel(label);

		// set line graph Y lable as well
		this.lineChartPanel.setLabel(label);

	}

	/**
	 * Starting point for the demo application.
	 *
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {

		FleetPanel app = new FleetPanel("Oracle NoSQL Database");
		app.setVisible(true);

	}
}
