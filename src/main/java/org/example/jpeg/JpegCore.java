package org.example.jpeg;

import org.example.colorSpace.ColorSpaceYCbCr;

public class JpegCore {

    public static final int SHIFT_VALUE = 128;
    public static final int DOWNSAMPLE_COEF_THE_COLOR = 1;
    public static int[][] QUANTIZATION_TABLE = {{16, 11, 10, 16, 24, 40, 51, 61}, {12, 12, 14, 19, 26, 58, 60, 55}, {14, 13, 16, 24, 40, 57, 69, 56}, {14, 17, 22, 29, 51, 87, 80, 62}, {18, 22, 37, 56, 68, 109, 103, 77}, {24, 35, 55, 64, 81, 104, 113, 92}, {49, 64, 78, 87, 103, 121, 120, 101}, {72, 92, 95, 98, 112, 100, 103, 99}};
    public static final int COS_SIZE = 8;

    public static double[][] downsampleMatrix(double[][] matrix) {
        int originalRows = matrix.length;
        int originalCols = matrix[0].length;
        int downsampledRows = (int) Math.ceil((double) originalRows / DOWNSAMPLE_COEF_THE_COLOR);
        int downsampledCols = (int) Math.ceil((double) originalCols / DOWNSAMPLE_COEF_THE_COLOR);
        double[][] downsampledArray = new double[downsampledRows][downsampledCols];
        for (int i = 0, row = 0; i < originalRows; i += DOWNSAMPLE_COEF_THE_COLOR, row++) {
            for (int j = 0, col = 0; j < originalCols; j += DOWNSAMPLE_COEF_THE_COLOR, col++) {
                downsampledArray[row][col] = matrix[i][j];
            }
        }
        return downsampledArray;
    }

    public static ColorSpaceYCbCr dctAndQuantization(ColorSpaceYCbCr image) {
        var length = image.Y().length;
        var width = image.Y()[0].length;
        var crLength = length / DOWNSAMPLE_COEF_THE_COLOR;
        var crWidth = width / DOWNSAMPLE_COEF_THE_COLOR;

        image.shiftYCbCrByValue(SHIFT_VALUE);

        var Y2dRes = new double[length][width];
        var Cb2DRes = new double[length][width];
        var CrRes = new double[length / DOWNSAMPLE_COEF_THE_COLOR][width / DOWNSAMPLE_COEF_THE_COLOR];

        for (int i = 0; i < length; i += COS_SIZE) {
            for (int j = 0; j < width; j += COS_SIZE) {
                applyDCTAndQuantize(image.Y(), i, j, Y2dRes);
                applyDCTAndQuantize(image.Cb(), i, j, Cb2DRes);
                if (crLength > i && crWidth > j) {
                    applyDCTAndQuantize(image.Cr(), i, j, CrRes);
                }
            }
        }
        return new ColorSpaceYCbCr(Y2dRes, Cb2DRes, CrRes);
    }

    public static void applyDCTAndQuantize(double[][] block, int start, int end, double[][] result) {
        for (int u = start; u < start + COS_SIZE; u++) {
            for (int v = end; v < end + COS_SIZE; v++) {
                double cu = (u == 0) ? 1 / Math.sqrt(2) : 1;
                double cv = (v == 0) ? 1 / Math.sqrt(2) : 1;
                double sum = 0.0;
                for (int x = start; x < start + COS_SIZE; x++) {
                    for (int y = end; y < end + COS_SIZE; y++) {
                        double cosTerm1 = Math.cos((2 * x + 1) * u * Math.PI / (2 * COS_SIZE));
                        double cosTerm2 = Math.cos((2 * y + 1) * v * Math.PI / (2 * COS_SIZE));
                        sum += block[x][y] * cosTerm1 * cosTerm2;
                    }
                }
                result[u][v] = (int) ((0.25 * cu * cv * sum) / QUANTIZATION_TABLE[u - start][v - end]);
            }
        }
    }

    public static ColorSpaceYCbCr reQuantizeAndReDCT(ColorSpaceYCbCr image) {
        var length = image.Y().length;
        var width = image.Y()[0].length;
        var crLength = length / DOWNSAMPLE_COEF_THE_COLOR;
        var crWidth = width / DOWNSAMPLE_COEF_THE_COLOR;
        var Y2dRes = new double[length][width];
        var Cb2DRes = new double[length][width];
        var CrRes = new double[length / DOWNSAMPLE_COEF_THE_COLOR][width / DOWNSAMPLE_COEF_THE_COLOR];
        for (int i = 0; i < length; i += COS_SIZE) {
            for (int j = 0; j < width; j += COS_SIZE) {
                applyQuantizeAndDCT(image.Y(), i, j, Y2dRes);
                applyQuantizeAndDCT(image.Cb(), i, j, Cb2DRes);
                if (crLength > i && crWidth > j) {
                    applyQuantizeAndDCT(image.Cr(), i, j, CrRes);
                }
            }
        }
        var yCbCr = new ColorSpaceYCbCr(Y2dRes, Cb2DRes, CrRes);
        image.shiftYCbCrByValue(-SHIFT_VALUE);
        return yCbCr;
    }

    public static void applyQuantizeAndDCT(double[][] block, int start, int end, double[][] result) {
        for (int x = start; x < start + COS_SIZE; x++) {
            for (int y = end; y < end + COS_SIZE; y++) {
                double sum = 0.0;

                for (int u = start; u < start + COS_SIZE; u++) {
                    for (int v = end; v < end + COS_SIZE; v++) {
                        double cu = (u == 0) ? 1 / Math.sqrt(2) : 1;
                        double cv = (v == 0) ? 1 / Math.sqrt(2) : 1;
                        double cosTerm1 = Math.cos((2 * x + 1) * u * Math.PI / (2 * COS_SIZE));
                        double cosTerm2 = Math.cos((2 * y + 1) * v * Math.PI / (2 * COS_SIZE));
                        sum += cu * cv * block[u][v] * QUANTIZATION_TABLE[u - start][v - end] * cosTerm1 * cosTerm2;
                    }
                }

                result[x][y] = sum;
            }
        }
    }
}
