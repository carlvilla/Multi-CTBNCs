package es.upm.fi.cig.multictbnc.util;

import es.upm.fi.cig.multictbnc.gui.XYLineChart;

/**
 * Utility class with methods related to the user interface.
 *
 * @author Carlos Villa Blanco
 */
public final class UserInterfaceUtil {

    private UserInterfaceUtil() {
    }

    /**
     * Creates an XY line chart with the specified parameters.
     *
     * @param title       title of the chart
     * @param labelX      label for the X-axis
     * @param LabelY      label for the Y-axis
     * @param rangeY      range of values on the Y-axis
     * @param seriesNames names of the data series to be displayed on the chart
     * @return an instance of XYLineChart
     */
    public static XYLineChart createXYLineChart(String title, String labelX, String LabelY, int[] rangeY,
                                                String... seriesNames) {
        XYLineChart lineChart = new XYLineChart(title, labelX, LabelY, rangeY, seriesNames);
        lineChart.pack();
        lineChart.setVisible(true);
        return lineChart;
    }

}