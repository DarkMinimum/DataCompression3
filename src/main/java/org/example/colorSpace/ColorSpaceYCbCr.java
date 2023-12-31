package org.example.colorSpace;

import static org.example.pixel.PixelYCbCr.rgbToYCbCr;

import org.example.pixel.PixelRGB;

public record ColorSpaceYCbCr(double[][] Y, double[][] Cb, double[][] Cr) {

    public static ColorSpaceYCbCr toYCbCr(ColorSpaceRGB array) {
        var length = array.R().length;
        var width = array.R()[0].length;
        var Y = new double[length][width];
        var Cb = new double[length][width];
        var Cr = new double[length][width];
        for (int xPixel = 0; xPixel < length; xPixel++) {
            for (int yPixel = 0; yPixel < width; yPixel++) {
                var r = array.R()[xPixel][yPixel];
                var g = array.G()[xPixel][yPixel];
                var b = array.B()[xPixel][yPixel];
                var pixel = rgbToYCbCr(new PixelRGB(r, g, b));
                Y[xPixel][yPixel] = pixel.Y();
                Cb[xPixel][yPixel] = pixel.Cb();
                Cr[xPixel][yPixel] = pixel.Cr();
            }
        }
        return new ColorSpaceYCbCr(Y, Cb, Cr);
    }

    public void shiftYCbCrByValue(int shiftValue) {
        for (int i = 0; i < this.Y().length; i++) {
            for (int j = 0; j < this.Y()[0].length; j++) {
                this.Y()[i][j] -= shiftValue;
                this.Cb()[i][j] -= shiftValue;
            }
        }
        for (int i = 0; i < this.Cr().length; i++) {
            for (int j = 0; j < this.Cr()[0].length; j++) {
                this.Cr[i][j] -= shiftValue;
            }
        }
    }
}