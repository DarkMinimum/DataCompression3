package org.example;

import static org.example.colorSpace.ColorSpaceRGB.convertYCbCrToRGB;
import static org.example.colorSpace.ColorSpaceYCbCr.toYCbCr;
import static org.example.haff.HaffmanEncoding.encodeWithHuffman;
import static org.example.jpeg.JpegCore.DOWNSAMPLE_COEF_THE_COLOR;
import static org.example.jpeg.JpegCore.dct;
import static org.example.jpeg.JpegCore.downsampleMatrix;
import static org.example.util.ColorUtils.pathToRGB;
import static org.example.util.ColorUtils.saveImage;
import static org.example.util.ColorUtils.saveMyJpeg;

import java.io.IOException;

import org.example.colorSpace.ColorSpaceYCbCr;

public class Main {

    public static final String PATH = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\512\\512.bmp";
    public static final String PATH_CHROMO = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\512\\chromatic_downsample.bmp";
    public static final String PATH_MY_JPEG = "D:\\ideaProj\\DataCompression3\\src\\main\\resources\\512\\512.mjpeg";

    public static void main(String[] args) {
        try {
            var bmp = pathToRGB(PATH);
            var ycbcr = toYCbCr(bmp);
            var downsampleCr = downsampleMatrix(ycbcr.Cr());
            var readyToDCT = new ColorSpaceYCbCr(ycbcr.Y(), ycbcr.Cb(), downsampleCr);
            saveImage(convertYCbCrToRGB(readyToDCT, DOWNSAMPLE_COEF_THE_COLOR), PATH_CHROMO);

            var readyToDecode = dct(readyToDCT);
            var content = encodeWithHuffman(readyToDecode, DOWNSAMPLE_COEF_THE_COLOR);
            saveMyJpeg(content, PATH_MY_JPEG);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}