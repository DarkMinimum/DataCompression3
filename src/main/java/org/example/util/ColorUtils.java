package org.example.util;

import static org.example.pixel.PixelRGB.combineRGB;
import static org.example.pixel.PixelRGB.parseRGB;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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

    public static String zigZagMatrix(double arr[][], int n, int m) {
        int row = 0, col = 0;

        boolean row_inc = false;
        var result = new StringBuilder();
        int mn = Math.min(m, n);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                result.append((int)arr[row][col]).append(".");
                if (i + 1 == len) {
                    break;
                }
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    --row;
                    ++col;
                }
            }

            if (len == mn) {
                break;
            }

            if (row_inc) {
                ++row;
                row_inc = false;
            } else {
                ++col;
                row_inc = true;
            }
        }

        if (row == 0) {
            if (col == m - 1) {
                ++row;
            } else {
                ++col;
            }
            row_inc = true;
        } else {
            if (row == n - 1) {
                ++col;
            } else {
                ++row;
            }
            row_inc = false;
        }

        int MAX = Math.max(m, n) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            if (diag > mn) {
                len = mn;
            } else {
                len = diag;
            }

            for (int i = 0; i < len; ++i) {
                result.append((int)arr[row][col]).append(".");

                if (i + 1 == len) {
                    break;
                }

                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    ++col;
                    --row;
                }
            }

            if (row == 0 || col == m - 1) {
                if (col == m - 1) {
                    ++row;
                } else {
                    ++col;
                }
                row_inc = true;
            } else if (col == 0 || row == n - 1) {
                if (row == n - 1) {
                    ++col;
                } else {
                    ++row;
                }
                row_inc = false;
            }
        }

        return result.toString();
    }

    /**
     * Perform a rotation of the provided BufferedImage using degrees of
     * 90, 180, or 270.
     *
     * @param bi BufferedImage to be rotated
     * @param degree
     * @return rotated BufferedImage instance
     */
    public static BufferedImage rotate(BufferedImage bi, int degree) {
        int width = bi.getWidth();
        int height = bi.getHeight();

        BufferedImage biFlip;
        if (degree == 90 || degree == 270) {
            biFlip = new BufferedImage(height, width, bi.getType());
        } else if (degree == 180) {
            biFlip = new BufferedImage(width, height, bi.getType());
        } else {
            return bi;
        }

        if (degree == 90) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(height - j - 1, i, bi.getRGB(i, j));
                }
            }
        }

        if (degree == 180) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(width - i - 1, height - j - 1, bi.getRGB(i, j));
                }
            }
        }

        if (degree == 270) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    biFlip.setRGB(j, width - i - 1, bi.getRGB(i, j));
                }
            }
        }

        bi.flush();
        bi = null;

        return biFlip;
    }

    /**
     * Flips the supplied BufferedImage vertically. This is often a necessary
     * conversion step to display a Java2D image correctly with OpenGL and vice
     * versa.
     *
     * @param theImage the image to flip
     */
    public static void flipImageVertically(BufferedImage theImage) {
        WritableRaster raster = theImage.getRaster();
        Object scanline1 = null;
        Object scanline2 = null;

        for (int i = 0; i < theImage.getHeight() / 2; i++) {
            scanline1 = raster.getDataElements(0, i, theImage.getWidth(),
                1, scanline1);
            scanline2 = raster.getDataElements(0, theImage.getHeight() - i
                - 1, theImage.getWidth(), 1, scanline2);
            raster.setDataElements(0, i, theImage.getWidth(), 1, scanline2);
            raster.setDataElements(0, theImage.getHeight() - i - 1,
                theImage.getWidth(), 1, scanline1);
        }
    }
}
