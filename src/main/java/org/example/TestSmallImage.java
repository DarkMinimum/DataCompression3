package org.example;

import org.decimal4j.util.DoubleRounder;
import org.example.colorSpace.ColorSpaceYCbCr;

import java.io.IOException;

import static org.example.Test.*;
import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.decode;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.jpeg.JpegCore.DOWNSAMPLE_COEF_THE_COLOR;
import static org.example.util.ColorUtils.*;

public class TestSmallImage {

    public static final int N = 8;
    private static final String PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "\\" + N + ".bmp";
    private static final String PATH_MY_JPEG = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "\\" + N + ".myjpeg";
    private static final String PATH_DECOMPRESSED_JPEG = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources\\" + N + "\\" + N + "_dec.bmp";

    public static void main(String[] args) throws IOException {
        var bmp = pathToRGB(PATH);
        var ycbcr = toYCbCr(bmp);

        var y = new double[N][N];
        dct(ycbcr.Y(), y);
        var Cb = new double[N][N];
        dct(ycbcr.Cb(), Cb);
        var Cr = new double[N][N];
        dct(ycbcr.Cr(), Cr);

        var content = encodeWithHuffman(new ColorSpaceYCbCr(y, Cb, Cr), DOWNSAMPLE_COEF_THE_COLOR);
        saveMyJpeg(content, PATH_MY_JPEG);
        var rawYCbCr = decode(content);

        var cY = new double[N][N];
        idct(rawYCbCr.Y(), cY);
        var cCb = new double[N][N];
        dct(rawYCbCr.Cb(), cCb);
        var cCr = new double[N][N];
        idct(rawYCbCr.Cr(), cCr);

        var decompressed = new ColorSpaceYCbCr(cY, ycbcr.Cb(), ycbcr.Cr());
        saveImage(convertYCbCrToRGB(decompressed, DOWNSAMPLE_COEF_THE_COLOR), PATH_DECOMPRESSED_JPEG);
    }

    private static void dct(double[][] numbers, double[][] target) {
        //copy initial
        var copy = copyArray(numbers);

        //shift -128
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                copy[i][j] -= 128;
            }
        }

        //compression by itself
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                var Ci = C(i);
                var Cj = C(j);
                var sum = 0.0;
                for (int x = 0; x < N; x++) {
                    for (int y = 0; y < N; y++) {
                        sum += copy[x][y] * cos(x, i) * cos(y, j);
                    }
                }

                target[i][j] = (int) DoubleRounder.round(0.25 * Ci * Cj * sum, 1);

                //quantanization by Q10
                target[i][j] /= Q90[i][j];
            }
        }

    }


    private static void idct(double[][] numbers, double[][] target) {
        //copy
        var copy = copyArray(numbers);

        //quantanization by Q10
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                copy[i][j] *= Q90[i][j];
            }
        }

        //compression by itself
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        double Ci = C(i);
                        double Cj = C(j);
                        double cosU = cos(u, i);
                        double cosV = cos(v, j);
                        sum += Ci * Cj * copy[i][j] * cosU * cosV;
                    }
                }
                target[u][v] = (int) DoubleRounder.round(0.25 * sum, 1);
                //shift +128
                target[u][v] += 128;
            }
        }
    }

    private static double[][] copyArray(double[][] numbers) {
        var copy = new double[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                copy[u][v] = numbers[u][v];
            }
        }
        return copy;
    }

}
