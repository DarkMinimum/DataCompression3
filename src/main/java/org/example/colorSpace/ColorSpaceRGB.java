package org.example.colorSpace;

import static org.example.pixel.PixelRGB.yCbCrToRgb;

import org.example.pixel.PixelYCbCr;

public record ColorSpaceRGB(int[][] R, int[][] B, int[][] G) {
    public static ColorSpaceRGB convertYCbCrToRGB(ColorSpaceYCbCr image, double[][] downsampledCr, int factor) {
        var length = image.Y().length;
        var width = image.Y()[0].length;
        var R = new int[length][width];
        var G = new int[length][width];
        var B = new int[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                int di = i / factor;
                int dj = j / factor;
                var Y = (int) image.Y()[i][j];
                var Cb = (int) image.Cb()[i][j];
                var Cr = (int) image.Cr()[i][j];
                var rgbPixel = yCbCrToRgb(new PixelYCbCr(Y, Cb, Cr), downsampledCr[di][dj]);
                R[i][j] = rgbPixel.R();
                G[i][j] = rgbPixel.G();
                B[i][j] = rgbPixel.B();
            }
        }

        return new ColorSpaceRGB(R, G, B);
    }
}