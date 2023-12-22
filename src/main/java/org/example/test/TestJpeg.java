package org.example.test;

import org.decimal4j.util.DoubleRounder;
import org.example.colorSpace.ColorSpaceYCbCr;

import java.io.IOException;

import static org.example.haff.HaffmanEncoding.*;
import static org.example.test.Test.*;
import static org.example.test.Test.cos;
import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.jpeg.JpegCore.COS_SIZE;
import static org.example.util.ColorUtils.*;

public class TestJpeg {

    public static final int DOWNSAMPLE_CR = 128;
    public static final int N = 1024;
    private static final String PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "_\\" + N + ".bmp";
    private static final String PATH_MY_JPEG = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "_\\" + N + ".myjpeg";
    private static final String PATH_DECOMPRESSED_JPEG = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "_\\" + N + "_dec.bmp";

    public static void main(String[] args) throws IOException {

        var bmp = pathToRGB(PATH);
        var ycbcr = toYCbCr(bmp);

        var y = copyArray(ycbcr.Y(), N);
        var resY = new double[N][N];

        var Cb = copyArray(ycbcr.Cb(), N);
        var resCb = new double[N][N];

        //down sample it
        var downsampledLength = N / DOWNSAMPLE_CR;
        var Cr = downsampleMatrix(ycbcr.Cr(), DOWNSAMPLE_CR);
        var resCr = new double[downsampledLength][downsampledLength];

        System.out.println("DCT");
        for (int i = 0; i < N; i += COS_SIZE) {
            for (int j = 0; j < N; j += COS_SIZE) {
                dct(y, i, j, resY);
                dct(Cb, i, j, resCb);
                if (i < downsampledLength && j < downsampledLength) {
                    dct(Cr, i, j, resCr);
                }
            }
        }

        System.out.println("ENCODE");
        var beforeHuffman = new ColorSpaceYCbCr(resY, resCb, resCr);
        var content = encodeWithHuffman(beforeHuffman);
        saveMyJpeg(content, PATH_MY_JPEG);
        System.out.println("DECODE");
        var rawYCbCr = decode(content);

        var cY = copyArray(rawYCbCr.Y(), N);
        var resCY = new double[N][N];

        var cCb = copyArray(rawYCbCr.Cb(), N);
        var resCCb = new double[N][N];

        var cCr = copyArray(rawYCbCr.Cr(), downsampledLength);
        var resCCr = new double[downsampledLength][downsampledLength];

        System.out.println("IDCT");
        for (int i = 0; i < N; i += COS_SIZE) {
            for (int j = 0; j < N; j += COS_SIZE) {
                idct(cY, i, j, resCY);
                idct(cCb, i, j, resCCb);
                if (i < downsampledLength && j < downsampledLength) {
                    idct(cCr, i, j, resCCr);
                }
            }
        }
        saveImage(convertYCbCrToRGB(new ColorSpaceYCbCr(resCY, resCCb, resCCr), DOWNSAMPLE_CR), PATH_DECOMPRESSED_JPEG);
    }

    private static void dct(double[][] numbers, int startX, int startY, double[][] target) {
        var endX = startX + COS_SIZE;
        var endY = startY + COS_SIZE;
        //shift -128
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                numbers[i][j] -= 128;
            }
        }
        //compression by itself
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                var Ci = C(i - startX);
                var Cj = C(j - startY);
                var sum = 0.0;
                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        sum += numbers[x][y] * cos(x - startX, i - startX) * cos(y - startY, j - startY);
                    }
                }
                var value = DoubleRounder.round(0.25 * Ci * Cj * sum, 1);
                //quantanization by Q10
                value /= Q10[i - startX][j - startY];
                target[i][j] = (int) value;
            }
        }
    }

    private static void idct(double[][] numbers, int startX, int startY, double[][] target) {
        var endX = startX + COS_SIZE;
        var endY = startY + COS_SIZE;

        //quantanization by Q10
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                numbers[i][j] *= Q10[i - startX][j - startY];
            }
        }

        //compression by itself
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                double sum = 0.0;
                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        double Ci = C(x - startX);
                        double Cj = C(y - startY);
                        double cosU = cos(i - startX, x - startX);
                        double cosV = cos(j - startY, y - startY);
                        sum += Ci * Cj * numbers[x][y] * cosU * cosV;
                    }
                }
                target[i][j] = (int) DoubleRounder.round(0.25 * sum, 1);
                //shift +128
                target[i][j] += 128;
            }
        }
    }

    private static double[][] copyArray(double[][] numbers, int size) {
        var copy = new double[size][size];
        for (int u = 0; u < size; u++) {
            for (int v = 0; v < size; v++) {
                copy[u][v] = numbers[u][v];
            }
        }
        return copy;
    }

    public static double[][] downsampleMatrix(double[][] matrix, int scale) {
        int originalRows = matrix.length;
        int originalCols = matrix[0].length;
        int downsampledRows = (int) Math.ceil((double) originalRows / scale);
        int downsampledCols = (int) Math.ceil((double) originalCols / scale);
        double[][] downsampledArray = new double[downsampledRows][downsampledCols];
        for (int i = 0, row = 0; i < originalRows; i += scale, row++) {
            for (int j = 0, col = 0; j < originalCols; j += scale, col++) {
                downsampledArray[row][col] = matrix[i][j];
            }
        }
        return downsampledArray;
    }
}
