package org.example.pixel;

public record PixelYCbCr(int Y, int Cb, int Cr) {
    public static PixelYCbCr rgbToYCbCr(PixelRGB colorSpaceRgb) {
        int r = colorSpaceRgb.R();
        int g = colorSpaceRgb.G();
        int b = colorSpaceRgb.B();
        long y = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        long cb = Math.round(128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b);
        long cr = Math.round(128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b);
        return new PixelYCbCr((int) y, (int) cb, (int) cr);
    }

}