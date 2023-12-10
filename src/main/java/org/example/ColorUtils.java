package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ColorUtils {
    private ColorUtils() {

    }

    public static Main.ColorSpaceRGB parseRGB(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        return new Main.ColorSpaceRGB(red, green, blue);
    }

    public static int combineRGB(Main.ColorSpaceRGB colorSpaceRgb) {
        // Ensure that the values are within the valid range [0, 255]
        var red = Math.min(255, Math.max(0, colorSpaceRgb.R()));
        var green = Math.min(255, Math.max(0, colorSpaceRgb.G()));
        var blue = Math.min(255, Math.max(0, colorSpaceRgb.B()));

        // Pack the RGB values into a single int using bitwise operations
        return (red << 16) | (green << 8) | blue;
    }

    public static Main.ColorSpaceRGB[][] pathToRGB(String BMPFileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(BMPFileName));

        Main.ColorSpaceRGB[][] pixels = new Main.ColorSpaceRGB[image.getWidth()][image.getHeight()];

        for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
            for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
                int color = image.getRGB(xPixel, yPixel);
                pixels[xPixel][yPixel] = parseRGB(color);
            }
        }

        return pixels;
    }

    public static Main.ColorSpaceYCbCr[][] toYCbCr(Main.ColorSpaceRGB[][] array) {
        Main.ColorSpaceYCbCr[][] result = new Main.ColorSpaceYCbCr[array.length][array[0].length];

        for (int xPixel = 0; xPixel < array.length; xPixel++) {
            for (int yPixel = 0; yPixel < array[0].length; yPixel++) {
                var rgb = array[xPixel][yPixel];
                result[xPixel][yPixel] = rgbToYCbCr(rgb);
            }
        }

        return result;
    }

    // Function to convert RGB to YCbCr
    public static Main.ColorSpaceYCbCr rgbToYCbCr(Main.ColorSpaceRGB colorSpaceRgb) {
        int r = colorSpaceRgb.R();
        int g = colorSpaceRgb.G();
        int b = colorSpaceRgb.B();

        long y = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        long cb = Math.round(128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b);
        long cr = Math.round(128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b);

        return new Main.ColorSpaceYCbCr(y, cb, cr);
    }

    // Function to convert YCbCr to RGB
    public static Main.ColorSpaceRGB yCbCrToRgb(Main.ColorSpaceYCbCr ycbcr, double downSampleCr) {
        double y = ycbcr.Y();
        double cb = ycbcr.Cb();
        double cr = downSampleCr;

        long r = Math.round(y + 1.402 * (cr - 128.0));
        long g = Math.round(y - 0.344136 * (cb - 128.0) - 0.714136 * (cr - 128.0));
        long b = Math.round(y + 1.772 * (cb - 128.0));

        return new Main.ColorSpaceRGB((int) r, (int) g, (int) b);
    }

    public static Main.ColorSpaceRGB[][] convertYCbCrtoRGB(Main.ColorSpaceYCbCr[][] image, double[][] downsampledCr, int factor) {
        Main.ColorSpaceRGB[][] colorSpaceRgbImage = new Main.ColorSpaceRGB[image.length][image[0].length];

        var length = image.length;
        var width = image[0].length;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {

                int di = i / factor;
                int dj = j / factor;

                colorSpaceRgbImage[i][j] = yCbCrToRgb(image[i][j], downsampledCr[di][dj]);
            }
        }

        return colorSpaceRgbImage;
    }

    public static int[][] toSingleValue2D(Main.ColorSpaceRGB[][] colorSpaceRgb) {
        var image = new int[colorSpaceRgb.length][colorSpaceRgb[0].length];

        for (int y = 0; y < colorSpaceRgb.length; y++) {
            for (int x = 0; x < colorSpaceRgb[0].length; x++) {
                image[y][x] = combineRGB(colorSpaceRgb[y][x]);
            }
        }

        return image;
    }

    public static void saveImage(Main.ColorSpaceRGB[][] colorSpaceRgbs, String outputPath) {
        var rgb = toSingleValue2D(colorSpaceRgbs);
        BufferedImage image = new BufferedImage(rgb.length, rgb[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < rgb.length; y++) {
            for (int x = 0; x < rgb[0].length; x++) {
                image.setRGB(y, x, rgb[y][x]);
            }
        }

        // Save the BufferedImage to a file
        try {
            File outputFile = new File(outputPath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Image saved successfully to: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
