package org.example;

import org.decimal4j.util.DoubleRounder;
import org.example.colorSpace.ColorSpaceYCbCr;

import java.io.IOException;

import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.decode;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.jpeg.JpegCore.*;
import static org.example.util.ColorUtils.*;

public class Test {

    private static String PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\8\\8.bmp";

    public static final String PATH_MY = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\8\\file.myjpeg";
    public static final String PATH_DECOMPRESSED_JPEG = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\8\\deco.bmp";

    public static final int N = 8;

    public static int[][] numbers = {
            {154, 123, 123, 123, 123, 123, 123, 136},
            {192, 180, 136, 154, 154, 154, 136, 110},
            {254, 198, 154, 154, 180, 154, 123, 123},
            {239, 180, 136, 180, 180, 166, 123, 123},
            {180, 154, 136, 167, 166, 149, 136, 136},
            {128, 136, 123, 136, 154, 180, 198, 154},
            {123, 105, 110, 149, 136, 136, 180, 166},
            {110, 136, 123, 123, 123, 136, 154, 136}
    };

    public static double[][] targetBeforeQuant = {
            {162.3, 40.6, 20.0, 72.3, 30.3, 12.5, -19.7, -11.5},
            {30.5, 108.4, 10.5, 32.3, 27.7, -15.5, 18.4, -2.0},
            {-94.1, -60.1, 12.3, -43.4, -31.3, 6.1, -3.3, 7.1},
            {-38.6, -83.4, -5.4, -22.2, -13.5, 15.5, -1.3, 3.5},
            {-31.3, 17.9, -5.5, -12.4, 14.3, -6.0, 11.5, -6.0},
            {-0.9, -11.8, 12.8, 0.2, 28.1, 12.6, 8.4, 2.9},
            {4.6, -2.4, 12.2, 6.6, -18.7, -12.8, 7.7, 12.0},
            {-10.0, 11.2, 7.8, 16.3, 21.5, 0.0, 5.9, 10.7}
    };

    public static int[][] Q90 = {
            {3, 2, 2, 3, 5, 8, 10, 12},
            {2, 2, 3, 4, 5, 12, 12, 11},
            {3, 3, 3, 5, 8, 11, 14, 11},
            {3, 3, 4, 6, 10, 17, 16, 12},
            {4, 4, 7, 11, 14, 22, 21, 15},
            {5, 7, 11, 13, 16, 12, 23, 18},
            {10, 13, 16, 17, 21, 24, 24, 21},
            {14, 18, 19, 20, 22, 20, 20, 20}
    };

    public static int[][] Q50 = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}
    };

    public static int[][] Q10 = {
            {80, 60, 50, 80, 120, 200, 255, 255},
            {55, 60, 70, 95, 130, 255, 255, 255},
            {70, 65, 80, 120, 200, 255, 255, 255},
            {70, 85, 110, 145, 255, 255, 255, 255},
            {90, 110, 185, 255, 255, 255, 255, 255},
            {120, 175, 255, 255, 255, 255, 255, 255},
            {245, 255, 255, 255, 255, 255, 255, 255},
            {255, 255, 255, 255, 255, 255, 255, 255}
    };

    public static double cos(int x, int i) {
        var numenator = (2 * x + 1) * i * Math.PI;
        var denumenator = 16.0;
        return Math.cos(numenator / denumenator);
    }

    public static double C(int u) {
        if (u == 0) {
            return 1.0 / Math.sqrt(2.0);
        } else {
            return 1;
        }
    }

    public static double D(int i, int j, double[][] block) {
        var root = 0.25;
        var Ci = C(i);
        var Cj = C(j);

        var sum = 0.0;
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                sum += block[x][y] * cos(x, i) * cos(y, j);
            }
        }

        return root * Ci * Cj * sum;
    }

    public static void main(String[] args) throws IOException {

        var bmp = pathToRGB(PATH);
        var ycbcr = toYCbCr(bmp);
        ycbcr.shiftYCbCrByValue(128);

        double[][] resultsD = new double[N][N];
        double[][] res1 = new double[N][N];
        dct(ycbcr.Y(), resultsD, res1);

        resultsD = new double[N][N];
        double[][] res2 = new double[N][N];
        dct(ycbcr.Cb(), resultsD, res2);

        resultsD = new double[N][N];
        double[][] res3 = new double[N][N];
        dct(ycbcr.Cr(), resultsD, res3);

        var readyToDecode = new ColorSpaceYCbCr(res1, res2, res3);
        var content = encodeWithHuffman(readyToDecode, DOWNSAMPLE_COEF_THE_COLOR);
        saveMyJpeg(content, PATH_MY);
        var rawYCbCr = decode(content);
        var res = reQuantizeAndReDCT(rawYCbCr);
        saveImage(convertYCbCrToRGB(res, DOWNSAMPLE_COEF_THE_COLOR), PATH_DECOMPRESSED_JPEG, true);

    }

    private static void dct(double[][] numbers, double[][] resultsD, double[][] res) {
        //shift -128
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                numbers[i][j] -= 128;
//            }
//        }

        //compression by itself
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                resultsD[i][j] = DoubleRounder.round(D(i, j, numbers), 1);
            }
        }

        //check if everything is fine
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                var diff = Math.abs(Math.abs(resultsD[i][j]) - Math.abs(targetBeforeQuant[i][j]));
//                if (diff > 1.0) {
//                    throw new RuntimeException("Boarder exceeded");
//                }
//            }
//        }

        //quantanization by Q10
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                res[i][j] = (int) (resultsD[i][j] / Q10[i][j]);
            }
        }
    }

    private static void idct(double[][] numbers, double[][] res) {
        //quantanization by Q10
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                res[i][j] = (numbers[i][j] * Q10[i][j]);
            }
        }



    }
}
