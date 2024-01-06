package es.upm.fi.cig.multictbnc.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;

import javax.swing.*;
import java.awt.*;
import java.util.Date;


/**
 * A class for creating and managing a time series chart using the JFreeChart library. This class is designed to
 * display time series data in a dynamic and interactive chart.
 *
 * @author Carlos Villa Blanco
 */
public class TimeSeriesChart extends ApplicationFrame {
    private static final int COUNT = 2 * 60;
    /**
     * Dataset used for dynamically storing and managing the time series data displayed in the chart.
     */
    private DynamicTimeSeriesCollection dataset;

    /**
     * Constructs a TimeSeriesChart instance.
     *
     * @param titleWindow the title of the application window
     * @param labelX      the label for the X-axis
     * @param labelY      the label for the Y-axis
     * @param rangeY      the range for the Y-axis
     * @param seriesNames the names of the series to be displayed in the chart
     */
    public TimeSeriesChart(String titleWindow, String labelX, String labelY, int[] rangeY, String... seriesNames) {
        super(titleWindow);
        createDataset(seriesNames);
        createChart(labelX, labelY, rangeY);
    }

    /**
     * Updates the chart providing the Y axis value of several time series.
     *
     * @param newData new data points to be added to the chart, one for each series
     */
    public void update(double[] newData) {
        float[] floats = new float[newData.length];
        for (int i = 0; i < newData.length; i++)
            floats[i] = (float) newData[i];
        this.dataset.advanceTime();
        this.dataset.appendData(floats);
    }

    /**
     * Updates the chart with a new value for the Y axis.
     *
     * @param newData new data point to be added to the chart
     */
    public void update(double newData) {
        this.dataset.advanceTime();
        this.dataset.appendData(new float[]{(float) newData});
    }

    /**
     * Adds time series to the dataset.
     *
     * @param seriesNames names of the series to be added
     */
    private void addTimeSeries(String... seriesNames) {
        for (int idxSeries = 0; idxSeries < seriesNames.length; idxSeries++)
            this.dataset.addSeries(new float[1], idxSeries, seriesNames[idxSeries]);
    }

    /**
     * Creates the chart with specified axis labels and Y-axis range.
     *
     * @param labelX the label for the X-axis
     * @param labelY the label for the Y-axis
     * @param rangeY the range for the Y-axis
     */
    private void createChart(String labelX, String labelY, int[] rangeY) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(labelY, labelX, labelY, this.dataset, true, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(0xffffe0));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.lightGray);
        if (rangeY != null) {
            ValueAxis yaxis = plot.getRangeAxis();
            yaxis.setRange(rangeY[0], rangeY[1]);
        }
        chart.setBackgroundPaint(Color.LIGHT_GRAY);
        final JPanel content = new JPanel(new BorderLayout());
        final ChartPanel chartPanel = new ChartPanel(chart);
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
        int numSeries = seriesNames.length;
        this.dataset = new DynamicTimeSeriesCollection(numSeries, COUNT, new Second());
        Date date = new Date();
        this.dataset.setTimeBase(new Second(date));
        addTimeSeries(seriesNames);
    }

}
