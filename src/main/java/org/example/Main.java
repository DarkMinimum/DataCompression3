package org.example;

import org.example.colorSpace.ColorSpaceYCbCr;

import java.io.IOException;

import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.decode;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.jpeg.JpegCore.*;
import static org.example.util.ColorUtils.*;

public class Main {

    public static final String CORE_PATH = "C:\\Users\\danil\\IdeaProjects\\DataCompression3\\src\\main\\resources";
    public static final String PATH = CORE_PATH + "\\64\\64.bmp";
    public static final String PATH_CHROMO = CORE_PATH + "\\64\\chromatic_downsample.bmp";
    public static final String PATH_MY_JPEG = CORE_PATH + "\\64\\64.mjpeg";
    public static final String PATH_DECOMPRESSED_JPEG = CORE_PATH + "\\64\\decompressed.bmp";

    public static void main(String[] args) {
        try {
            var bmp = pathToRGB(PATH);
            var ycbcr = toYCbCr(bmp);
            var downsampleCr = downsampleMatrix(ycbcr.Cr());
            var readyToDCT = new ColorSpaceYCbCr(ycbcr.Y(), ycbcr.Cb(), downsampleCr);
            saveImage(convertYCbCrToRGB(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR), PATH_CHROMO);
            var readyToDecode = dctAndQuantization(readyToDCT);

            var content = encodeWithHuffman(readyToDecode);

            saveMyJpeg(content, PATH_MY_JPEG);
            var rawYCbCr = decode(content);
            var res = reQuantizeAndReDCT(rawYCbCr);
            saveImage(convertYCbCrToRGB(res, DOWNSAMPLE_COEF_THE_COLOR), PATH_DECOMPRESSED_JPEG);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}