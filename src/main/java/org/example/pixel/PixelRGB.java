package org.example.pixel;

public record PixelRGB(int R, int G, int B) {

    public static PixelRGB parseRGB(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new PixelRGB(red, green, blue);
    }

    public static int combineRGB(PixelRGB colorSpaceRgb) {
        var red = Math.min(255, Math.max(0, colorSpaceRgb.R()));
        var green = Math.min(255, Math.max(0, colorSpaceRgb.G()));
        var blue = Math.min(255, Math.max(0, colorSpaceRgb.B()));
        return (red << 16) | (green << 8) | blue;
    }

    public static PixelRGB yCbCrToRgb(PixelYCbCr ycbcr, double downSampleCr) {
        double y = ycbcr.Y();
        double cb = ycbcr.Cb();
        double cr = downSampleCr;
        long r = Math.round(y + 1.402 * (cr - 128.0));
        long g = Math.round(y - 0.344136 * (cb - 128.0) - 0.714136 * (cr - 128.0));
        long b = Math.round(y + 1.772 * (cb - 128.0));
        return new PixelRGB((int) r, (int) g, (int) b);
    }

}