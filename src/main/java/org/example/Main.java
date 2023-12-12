package org.example;

import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.decode;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.haff.HaffmanEncoding.fillMatrix;
import static org.example.jpeg.JpegCore.DOWNSAMPLE_COEF_THE_COLOR;
import static org.example.jpeg.JpegCore.dctAndQuantization;
import static org.example.jpeg.JpegCore.downsampleMatrix;
import static org.example.jpeg.JpegCore.reQuantizeAndReDCT;
import static org.example.util.ColorUtils.pathToRGB;
import static org.example.util.ColorUtils.saveImage;
import static org.example.util.ColorUtils.saveMyJpeg;
import static org.example.util.ColorUtils.zigZagMatrix;

import java.io.IOException;

import org.example.colorSpace.ColorSpaceYCbCr;

public class Main {

    public static final String CORE_PATH = "D:\\ideaProj\\DataCompression3\\src\\main\\resources";
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
            saveImage(convertYCbCrToRGB(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR), PATH_CHROMO, false);
//            var readyToDecode = dctAndQuantization(readyToDCT);
//            var content = encodeWithHuffman(readyToDecode, DOWNSAMPLE_COEF_THE_COLOR);

            var content = encodeWithHuffman(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR);

            saveMyJpeg(content, PATH_MY_JPEG);
            var rawYCbCr = decode(content);
//            var res = reQuantizeAndReDCT(rawYCbCr);
            //save as decompressed image
            saveImage(convertYCbCrToRGB(rawYCbCr, DOWNSAMPLE_COEF_THE_COLOR), PATH_DECOMPRESSED_JPEG, true);
            System.out.println("yo");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}