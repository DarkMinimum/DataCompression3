package org.example;

import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.decodeString;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.jpeg.JpegCore.DOWNSAMPLE_COEF_THE_COLOR;
import static org.example.jpeg.JpegCore.dctAndQuantization;
import static org.example.jpeg.JpegCore.downsampleMatrix;
import static org.example.jpeg.JpegCore.reQuantizeAndReDCT;
import static org.example.util.ColorUtils.pathToRGB;
import static org.example.util.ColorUtils.saveImage;
import static org.example.util.ColorUtils.saveMyJpeg;

import java.io.IOException;
import java.util.ArrayList;

import org.example.colorSpace.ColorSpaceYCbCr;

public class Main {

    public static final String PATH = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\64\\64.bmp";
    public static final String PATH_CHROMO = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\64\\chromatic_downsample.bmp";
    public static final String PATH_MY_JPEG = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\64\\64.mjpeg";
    public static final String PATH_DECOMPRESSED_JPEG = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\64\\decompressed.bmp";

    public static void main(String[] args) {
        try {
            var bmp = pathToRGB(PATH);
            var ycbcr = toYCbCr(bmp);
            var downsampleCr = downsampleMatrix(ycbcr.Cr());
            var readyToDCT = new ColorSpaceYCbCr(ycbcr.Y(), ycbcr.Cb(), downsampleCr);
            saveImage(convertYCbCrToRGB(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR), PATH_CHROMO);

            var readyToDecode = dctAndQuantization(readyToDCT);
            var content = encodeWithHuffman(readyToDecode, DOWNSAMPLE_COEF_THE_COLOR);
            saveMyJpeg(content, PATH_MY_JPEG);

            var rawYCbCr = decode(content);
            var res = reQuantizeAndReDCT(rawYCbCr);

            //save as decompressed image
            saveImage(convertYCbCrToRGB(res, DOWNSAMPLE_COEF_THE_COLOR), PATH_DECOMPRESSED_JPEG);

            System.out.println("yo");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static ColorSpaceYCbCr decode(String content) {
        var layers = content.split("\n");
        var ycbcrLayers = new ArrayList<double[][]>();
        for (int l = 0; l < layers.length; l++) {
            var raw = decodeString(layers[l], l).split("\\.");
            var size = (int) Math.sqrt(raw.length);

            var array = new double[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    array[i][j] = Integer.parseInt(raw[j * size + i]);
                }
            }
            ycbcrLayers.add(array);
        }
        return new ColorSpaceYCbCr(ycbcrLayers.get(0), ycbcrLayers.get(1), ycbcrLayers.get(2));
    }
}