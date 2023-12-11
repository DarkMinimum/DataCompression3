package org.example.colorSpace;

import static org.example.pixel.PixelRGB.yCbCrToRgb;

import org.example.pixel.PixelYCbCr;

public record ColorSpaceRGB(int[][] R, int[][] B, int[][] G) {
    public static ColorSpaceRGB convertYCbCrToRGB(ColorSpaceYCbCr image, int factor) {
        var length = image.Y().length;
        var width = image.Y()[0].length;
        var R = new int[length][width];
        var G = new int[length][width];
        var B = new int[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                var Y = (int) image.Y()[i][j];
                var Cb = (int) image.Cb()[i][j];

                int di = i / factor;
                int dj = j / factor;
                var Cr = (int) image.Cr()[di][dj];

                var rgbPixel = yCbCrToRgb(new PixelYCbCr(Y, Cb, Cr));
                R[i][j] = rgbPixel.R();
                G[i][j] = rgbPixel.G();
                B[i][j] = rgbPixel.B();
            }
        }

        return new ColorSpaceRGB(R, G, B);
    }
}