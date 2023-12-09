package org.example;

import java.io.IOException;

import static org.example.ColorUtils.*;

public class Main {

    //load image
    record RGB(int R, int B, int G) {
    }

    //format it to YCbRb
    record YCbCr(double Y, double Cb, double Cr) {

    }

    public static final String PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\sample.bmp";
    public static final String PATH_CHROMO = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\compressed.bmp";


    public static double[][] downsample2DArray(YCbCr[][] originalArray, int downsamplingFactor) {
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

    //algo
    //get an RGB image
    //convert it to the YCbCr color space+
    // -> downsample Cromanance to reduce the size ++++++++
    //cos transform

    public static int DOWNSAMPLE_COEF_THE_COLOR = 6;

    public static int SHIFT_VALUE = 128;



    public static void main(String[] args) {
        try {
            var bmp = pathToRGB(PATH);
            var ycbcr = toYCbCr(bmp);
            var downsampleCr = downsample2DArray(ycbcr, DOWNSAMPLE_COEF_THE_COLOR);
            var ycbcrDown = convertYCbCrtoRGB(ycbcr, downsampleCr, DOWNSAMPLE_COEF_THE_COLOR);
            saveImage(ycbcrDown, PATH_CHROMO);

        } catch (IOException e) {
            System.out.println(e);
        }

    }
}