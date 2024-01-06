package es.upm.fi.cig.multictbnc.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * A class for creating and managing an XY line chart using the JFreeChart library. This class is designed to display
 * XY data in a dynamic and interactive line chart.
 *
 * @author Carlos Villa Blanco
 */
public class XYLineChart extends ApplicationFrame {
    /**
     * Main chart used for displaying the series.
     */
    private JFreeChart chart;
    /**
     * Collection of data series that are displayed in the chart.
     */
    private XYSeriesCollection dataset;

    /**
     * Constructs an XYLineChart instance.
     *
     * @param titleWindow the title of the application window
     * @param labelX      the label for the X-axis
     * @param labelY      the label for the Y-axis
     * @param rangeY      the range for the Y-axis
     * @param seriesNames the names of the series to be displayed in the chart
     */
    public XYLineChart(String titleWindow, String labelX, String labelY, int[] rangeY, String... seriesNames) {
        super(titleWindow);
        createDataset(seriesNames);
        createChart(labelX, labelY, rangeY);
    }


    /**
     * Adds a horizontal value marker to the chart.
     *
     * @param y the Y-axis value where the marker will be placed
     */
    public void addHorizontalValueMarker(double y) {
        ValueMarker marker = new ValueMarker(y);
        marker.setPaint(Color.black);
        XYPlot plot = this.chart.getXYPlot();
        plot.addRangeMarker(marker);
    }

    /**
     * Adds new series to the chart.
     *
     * @param seriesNames names of the new series to be added
     */
    public void addSeries(String... seriesNames) {
        for (String seriesName : seriesNames)
            this.dataset.addSeries(new XYSeries(seriesName));
    }

    /**
     * Adds a vertical value marker to the chart.
     *
     * @param x the X-axis value where the marker will be placed
     */
    public void addVerticalValueMarker(double x) {
        ValueMarker marker = new ValueMarker(x);
        marker.setPaint(Color.black);
        XYPlot plot = this.chart.getXYPlot();
        plot.addDomainMarker(marker);
    }

    /**
     * Updates the chart with a new value for the X and Y axis.
     *
     * @param x new value for the X-axis
     * @param y new value for the Y-axis
     */
    public void update(double x, double y) {
        this.dataset.getSeries(0).add(x, y);
    }

    /**
     * Updates the chart providing the X and Y axis values of several series.
     *
     * @param x new value for the X-axis
     * @param y array of new values for the Y-axis, one for each series
     */
    public void update(double x, double[] y) {
        for (int idxSeries = 0; idxSeries < y.length; idxSeries++)
            this.dataset.getSeries(idxSeries).add(x, y[idxSeries]);
    }


    /**
     * Creates the chart with specified axis labels and Y-axis range.
     *
     * @param labelX label for the X-axis
     * @param labelY label for the Y-axis
     * @param rangeY range for the Y-axis
     */
    private void createChart(String labelX, String labelY, int[] rangeY) {
        this.chart = ChartFactory.createXYLineChart(labelY, labelX, labelY, this.dataset);
        XYPlot plot = this.chart.getXYPlot();
        plot.setBackgroundPaint(new Color(0xffffe0));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);
        if (rangeY != null) {
            ValueAxis yaxis = plot.getRangeAxis();
            yaxis.setRange(rangeY[0], rangeY[1]);
        }
        this.chart.setBackgroundPaint(Color.LIGHT_GRAY);
        final JPanel content = new JPanel(new BorderLayout());
        final ChartPanel chartPanel = new ChartPanel(this.chart);
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        setContentPane(content);
    }

    /**
     * Creates the dataset for the chart with the specified series names.
     *
     * @param seriesNames names of the series to be included in the dataset
     */
    private void createDataset(String... seriesNames) {
        this.dataset = new XYSeriesCollection();
        addSeries(seriesNames);
    }

}
