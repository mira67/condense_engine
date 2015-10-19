package helper_classes.algorithms;

import helper_classes.GriddedVector;

/**
 * Created by Richard McAllister on 10/13/15.
 */
public class Algorithm1 implements Algorithm {

    private GriddedVector[][] data;
    private double[][] mean;
    private double[][] sd;
    private double sdThreshold;
    private double minValue;
    private double maxValue;

    public Algorithm1(GriddedVector[][] data,
                      double[][] mean,
                      double[][] sd,
                      double sdThreshold,
                      double minValue,
                      double maxValue) {
        this.data = data;
        this.mean = mean;
        this.sd = sd;
        this.sdThreshold = sdThreshold;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public GriddedVector[][] execute() {
        // A place to store the anomalous pixels
        GriddedVector[][] condensedData = condenseData();
        return extractAnomalies(condensedData);
    }

    protected GriddedVector[][] condenseData() {

        int rows = data.length;
        int cols = data[0].length;

        // A place to store the anomalous pixels
        GriddedVector[][] condensedData = new GriddedVector[ rows ][ cols ];

        // PART 1...
        // Keep only the pixels that exceed the threshold value (# of standard deviations)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                condenseCell(condensedData, r, c);
            }
        }
        return condensedData;
    }

    protected void condenseCell(GriddedVector[][] condensedData, int r, int c) {
        // Don't store data that is clearly bad.
        if (data[r][c].data() < minValue || data[r][c].data() > maxValue) return;

        // Calculate a low and high threshold using the mean and standard deviation
        double low = mean[r][c] - sdThreshold * sd[r][c];
        double high = mean[r][c] + sdThreshold * sd[r][c];

        // If the pixel value exceeds either threshold, keep it. It's an anomaly.
        // TODO: should we offer different high and low thresholds?
        if (data[r][c].data() < low || data[r][c].data() > high) {
            condensedData[r][c] = data[r][c];
        }
    }

    protected GriddedVector[][] extractAnomalies(GriddedVector[][] condensedData) {

        int rows = data.length;
        int cols = data[0].length;

        // PART 2...
        // Filter: If a pixel doesn't have enough adjacent anomalous pixels, it
        // might be noise -- ignore it.

        // A new place to put the newly filtered pixels
        GriddedVector[][] anomalies = new GriddedVector[ rows ][ cols ];

        // Number of adjacent anomalous pixels necessary for deciding whether
        // any one pixel is a keeper?
        final int minAnomalies = 1;

        // Go through every pixel location
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                extractCellAnomaly(condensedData, anomalies, r, c, rows, cols, minAnomalies);
            }
        }

        return anomalies;

    }

    protected void extractCellAnomaly(GriddedVector[][] condensedData,
                                      GriddedVector[][] anomalies,
                                      int r,
                                      int c,
                                      int rows,
                                      int cols,
                                      int minAnomalies) {

        // Is there an anomalous pixel here? If not, don't bother.
        if (condensedData[r][c] == null) return;

        // How many anomalous pixels are adjacent to this pixel?
        int adjacentAnomalies = 0;

        // Look at adjacent pixels, counting the anomalies
        for (int r1 = r-1; r1 < r+2; r1++) {
            for (int c1 = c-1; c1 < c+2; c1++) {

                // Don't compare the pixel with itself
                if (r1 == r && c1 == c) continue;

                // Don't exceed the array bounds.
                if (r1 < 0 || c1 < 0 || r1 > rows-1 || c1 > cols-1) continue;

                // Found one. Count it.
                if (condensedData[r1][c1] != null) adjacentAnomalies++;
            }

            // Did we find enough adjacent anomalous pixels?
            if (adjacentAnomalies >= minAnomalies) {
                // Yup.
                anomalies[r][c] = condensedData[r][c];

                break;
            }
        }
    }

}
