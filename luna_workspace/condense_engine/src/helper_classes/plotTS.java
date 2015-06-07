package helper_classes;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class plotTS {
    public static void tsplot(Number[][] ts1, Number[][] ts2, int index) {
        // Create a simple XY chart
        XYSeries series1 = new XYSeries("XYGraph1");
        for (int i = 0; i < ts1.length; i++){
        	series1.add(ts1[i][0].intValue(), ts1[i][1].intValue());
        }
        
        XYSeries series2 = new XYSeries("XYGraph2");
        for (int i = 0; i < ts2.length; i++){
        	series2.add(ts2[i][0].intValue(), ts2[i][1].intValue());
        }
        
        // Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(
            "XY Chart",
            "x-axis",
            "y-axis",
            dataset,
            PlotOrientation.VERTICAL,  // Plot Orientation
            true,                      // Show Legend
            true,                      // Use tooltips
            false                      // Configure chart to generate URLs?
            );
        try {
        		ChartUtilities.saveChartAsJPEG(new File("/Users/mira67/Google Drive/IceBerg_Project/Condense_Engine/sandbox/original_condense" + index + ".jpg"), chart, 500, 300);
            
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
} }
}
