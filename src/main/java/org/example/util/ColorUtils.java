package org.example.util;

import static org.example.pixel.PixelRGB.combineRGB;
import static org.example.pixel.PixelRGB.parseRGB;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.example.colorSpace.ColorSpaceRGB;
import org.example.pixel.PixelRGB;

public class ColorUtils {

    private ColorUtils() {
    }

    public static ColorSpaceRGB pathToRGB(String BMPFileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(BMPFileName));
        var R = new int[image.getWidth()][image.getHeight()];
        var G = new int[image.getWidth()][image.getHeight()];
        var B = new int[image.getWidth()][image.getHeight()];
        for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
            for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
                int color = image.getRGB(xPixel, yPixel);
                var pixel = parseRGB(color);
                R[xPixel][yPixel] = pixel.R();
                G[xPixel][yPixel] = pixel.G();
                B[xPixel][yPixel] = pixel.B();
            }
        }
        return new ColorSpaceRGB(R, G, B);
    }

    public static int[][] toSingleValue2D(ColorSpaceRGB colorSpaceRgb) {
        var length = colorSpaceRgb.R().length;
        var width = colorSpaceRgb.R()[0].length;
        var image = new int[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                var R = colorSpaceRgb.R()[i][j];
                var G = colorSpaceRgb.G()[i][j];
                var B = colorSpaceRgb.B()[i][j];
                image[i][j] = combineRGB(new PixelRGB(R, G, B));
            }
        }
        return image;
    }

    public static void saveImage(ColorSpaceRGB colorSpaceRgbs, String outputPath) {
        var rgb = toSingleValue2D(colorSpaceRgbs);
        BufferedImage image = new BufferedImage(rgb.length, rgb[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < rgb.length; y++) {
            for (int x = 0; x < rgb[0].length; x++) {
                image.setRGB(y, x, rgb[y][x]);
            }
        }

        try {
            File outputFile = new File(outputPath);
            ImageIO.write(image, "bmp", outputFile);
            System.out.println("Image saved successfully to: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMyJpeg(String content, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
