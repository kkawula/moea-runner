package com.moea.utils;

import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ListIterator;

public class ChartComposer {
    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    /**
     * Combines multiple JFreeChart objects into a single image with a maximum of 2 columns.
     * The function determines the layout dynamically based on the number of charts.
     *
     * <p>
     * Layout Details:
     * <ul>
     *   <li>A maximum of **2 columns** is allowed for the arrangement of the charts.</li>
     *   <li>The number of rows is calculated dynamically:
     *     <ul>
     *       <li>If the number of charts (`size`) is even, rows = size / 2.</li>
     *       <li>If the number of charts (`size`) is odd, rows = (size / 2) + 1.</li>
     *     </ul>
     *   </li>
     * </ul>
     * Total canvas dimensions for the combined image are determined by the following formulas:
     * <ul>
     *   <li>Width = (number of columns) * `WIDTH`</li>
     *   <li>Height = (number of rows) * `HEIGHT`</li>
     * </ul>
     * </p>
     *
     * <p>
     * Example Dimensions:
     * <ul>
     *   <li>1 chart → Layout: 1 column, 1 row (width = `WIDTH`, height = `HEIGHT`)</li>
     *   <li>3 charts → Layout: 2 columns, 2 rows (width = 2 * `WIDTH`, height = 2 * `HEIGHT`)</li>
     *   <li>6 charts → Layout: 2 columns, 3 rows (width = 2 * `WIDTH`, height = 3 * `HEIGHT`)</li>
     * </ul>
     * </p>
     *
     * @param chartList A {@code List<JFreeChart>} containing the charts to be combined.
     *                  Each chart represents an individual plot or visualization.
     * @return A {@code BufferedImage} object containing the combined chart image.
     *         The image can be exported or displayed as a single chart panel.
     */
    public static BufferedImage createCombinedChart(List<JFreeChart> chartList) {
        int size = chartList.size();
        int width = size > 1 ? 2 * WIDTH : WIDTH;
        int height = size % 2 == 0 ? HEIGHT * size / 2 : HEIGHT * (size / 2 + 1);

        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = combinedImage.createGraphics();

        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        ListIterator<JFreeChart> iterator = chartList.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            JFreeChart chart = iterator.next();

            int x = index % 2 == 0 ? 0 : WIDTH;
            int y = HEIGHT * (index / 2);

            chart.draw(g2, new Rectangle(x, y, WIDTH, HEIGHT));
        }

        g2.dispose();

        return combinedImage;
    }
}
