package org.example;

import org.example.colorSpace.ColorSpaceYCbCr;

import java.io.IOException;

import static org.example.util.ColorUtils.*;

public class Main {

    //changing of this would lead to array out of index because the matrix should be 8 pixels per block, but if it is not, it would crash, probably add blank areas.
    public static final int DOWNSAMPLE_COEF_THE_COLOR = 2;
    public static final int SHIFT_VALUE = 128;

    public static final String PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\sample.bmp";
    public static final String PATH_CHROMO = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\compressed.bmp";
    public static final int COS_SIZE = 8;

    public static int[][] QUANTIZATION_TABLE = {
            {
                    16, 11, 10, 16, 24, 40, 51, 61
            },
            {
                    12, 12, 14, 19, 26, 58, 60, 55
            },
            {
                    14, 13, 16, 24, 40, 57, 69, 56
            },
            {
                    14, 17, 22, 29, 51, 87, 80, 62
            },
            {
                    18, 22, 37, 56, 68, 109, 103, 77
            },
            {
                    24, 35, 55, 64, 81, 104, 113, 92
            },
            {
                    49, 64, 78, 87, 103, 121, 120, 101
            },
            {
                    72, 92, 95, 98, 112, 100, 103, 99
            }
    };

    public static double[][] downsample2DArray(ColorSpaceYCbCr[][] originalArray, int downsamplingFactor) {
        int originalRows = originalArray.length;
        int originalCols = originalArray[0].length;

        int downsampledRows = (int) Math.ceil((double) originalRows / downsamplingFactor);
        int downsampledCols = (int) Math.ceil((double) originalCols / downsamplingFactor);

        double[][] downsampledArray = new double[downsampledRows][downsampledCols];

        for (int i = 0, row = 0; i < originalRows; i += downsamplingFactor, row++) {
            for (int j = 0, col = 0; j < originalCols; j += downsamplingFactor, col++) {
                downsampledArray[row][col] = originalArray[i][j].Cr;
            }
        }

        return downsampledArray;
    }


    public static void dct(ColorSpaceYCbCr image, double[][] Cr, int downSampleCoef) {
        var length = image.Y().length;
        var width = image.Y()[0].length;
        var crLength = length / downSampleCoef;
        var crWidth = width / downSampleCoef;


        shiftYCbCrByValue(image, Cr, downSampleCoef);


        //cos transform for each layer and quantize
        var Y2D = new double[length][width];
        var Cb2D = new double[length][width];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                Y2D[i][j] = image.Y()[i][j];
                Cb2D[i][j] = image.Cb()[i][j];
            }
        }

        var Y2dRes = new int[length][width];
        var Cb2DRes = new int[length][width];
        var CrRes = new int[length / downSampleCoef][width / downSampleCoef];


        for (int i = 0; i < length; i += COS_SIZE) {
            for (int j = 0; j < width; j += COS_SIZE) {
                //Y componennt
                applyDCTAndQuantize(Y2D, i, j, COS_SIZE, Y2dRes);
                //Cb component
                applyDCTAndQuantize(Cb2D, i, j, COS_SIZE, Cb2DRes);
                //Cr component
                if (crLength > i && crWidth > j) {
                    applyDCTAndQuantize(Cr, i, j, COS_SIZE, CrRes);
                }
            }
        }

        System.out.println();

    }

    private static void shiftYCbCrByValue(ColorSpaceYCbCr image, double[][] Cr, int factor) {
        //values are shifter
        for (int i = 0; i < image.Y().length; i++) {
            for (int j = 0; j < image.Y()[0].length; j++) {
                image.Y()[i][j] -= SHIFT_VALUE;
                image.Cb()[i][j] -= SHIFT_VALUE;

                int di = i / factor;
                int dj = j / factor;

                Cr[di][dj] -= SHIFT_VALUE;
            }
        }
    }

    public static void applyDCTAndQuantize(double[][] block, int start, int end, int N, int[][] result) {
        for (int u = start; u < start + N; u++) {
            for (int v = end; v < end + N; v++) {

                double cu = (u == 0) ? 1 / Math.sqrt(2) : 1;
                double cv = (v == 0) ? 1 / Math.sqrt(2) : 1;

                double sum = 0.0;

                for (int x = start; x < start + N; x++) {
                    for (int y = end; y < end + N; y++) {
                        double cosTerm1 = Math.cos((2 * x + 1) * u * Math.PI / (2 * N));
                        double cosTerm2 = Math.cos((2 * y + 1) * v * Math.PI / (2 * N));

                        sum += block[x][y] * cosTerm1 * cosTerm2;
                    }
                }

                result[u][v] = (int) (0.25 * cu * cv * sum) / QUANTIZATION_TABLE[u - start][v - end];
            }
        }
    }


    public static void main(String[] args) {
        try {
            var bmp = pathToRGB(PATH);
            var ycbcr = toYCbCr(bmp);
            var downsampleCr = downsample2DArray(ycbcr, DOWNSAMPLE_COEF_THE_COLOR);

            var readyToDCT = new ColorSpaceYCbCr(ycbcr.Y(), ycbcr.Cb(), downsampleCr);
            saveImage(convertYCbCrtoRGB(ycbcr, downsampleCr, DOWNSAMPLE_COEF_THE_COLOR), PATH_CHROMO);
            dct(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR);

        } catch (IOException e) {
            System.out.println(e);
        }

    }
}