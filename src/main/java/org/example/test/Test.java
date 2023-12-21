package org.example.test;

import org.decimal4j.util.DoubleRounder;

public class Test {

    public static final int N = 8;

    public static int[][] numbers = {
            {154, 123, 123, 123, 123, 123, 123, 136},
            {192, 180, 136, 154, 154, 154, 136, 110},
            {254, 198, 154, 154, 180, 154, 123, 123},
            {239, 180, 136, 180, 180, 166, 123, 123},
            {180, 154, 136, 167, 166, 149, 136, 136},
            {128, 136, 123, 136, 154, 180, 198, 154},
            {123, 105, 110, 149, 136, 136, 180, 166},
            {110, 136, 123, 123, 123, 136, 154, 136}
    };

    public static int[][] Q90 = {
            {3, 2, 2, 3, 5, 8, 10, 12},
            {2, 2, 3, 4, 5, 12, 12, 11},
            {3, 3, 3, 5, 8, 11, 14, 11},
            {3, 3, 4, 6, 10, 17, 16, 12},
            {4, 4, 7, 11, 14, 22, 21, 15},
            {5, 7, 11, 13, 16, 12, 23, 18},
            {10, 13, 16, 17, 21, 24, 24, 21},
            {14, 18, 19, 20, 22, 20, 20, 20}
    };

    public static int[][] Q50 = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}
    };

    public static int[][] Q10 = {
            {80, 60, 50, 80, 120, 200, 255, 255},
            {55, 60, 70, 95, 130, 255, 255, 255},
            {70, 65, 80, 120, 200, 255, 255, 255},
            {70, 85, 110, 145, 255, 255, 255, 255},
            {90, 110, 185, 255, 255, 255, 255, 255},
            {120, 175, 255, 255, 255, 255, 255, 255},
            {245, 255, 255, 255, 255, 255, 255, 255},
            {255, 255, 255, 255, 255, 255, 255, 255}
    };

    public static double C(int k) {
        return k == 0 ? 1.0 / Math.sqrt(2.0) : 1.0;
    }

    public static double cos(int x, int i) {
        var numenator = (2 * x + 1) * i * Math.PI;
        var denumenator = 16.0;
        return Math.cos(numenator / denumenator);
    }

//    public static double D(int i, int j, int[][] block) {
//        var Ci = C(i);
//        var Cj = C(j);
//
//        var sum = 0.0;
//        for (int x = 0; x < N; x++) {
//            for (int y = 0; y < N; y++) {
//                sum += block[x][y] * cos(x, i) * cos(y, j);
//            }
//        }
//
//        return 0.25 * Ci * Cj * sum;
//    }
//
//    //TODO: where I made a mistake????
//    public static double ID(int i, int j, double[][] block) {
//        var Ci = C(i);
//        var Cj = C(j);
//
//        var sum = 0.0;
//        for (int x = 0; x < N; x++) {
//            for (int y = 0; y < N; y++) {
//                sum += block[x][y] * cos(x, i) * cos(y, j);
//            }
//        }
//
//        return sum / (0.25 * Ci * Cj);
//    }
//
//    public static double inverseDCT(int u, int v, double[][] dctCoefficients) {
//        double sum = 0.0;
//
//        for (int i = 0; i < N; i++) {
//            for (int j = 0; j < N; j++) {
//                double Ci = C(i);
//                double Cj = C(j);
//                double cosU = cos(u, i);
//                double cosV = cos(v, j);
//
//                sum += Ci * Cj * dctCoefficients[i][j] * cosU * cosV;
//            }
//        }
//
//        return 0.25 * sum;
//    }

    private static double[][] dct(int[][] numbers) {
        var res = new double[N][N];
        //shift -128
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                numbers[i][j] -= 128;
            }
        }

        //compression by itself
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                var Ci = C(i);
                var Cj = C(j);
                var sum = 0.0;
                for (int x = 0; x < N; x++) {
                    for (int y = 0; y < N; y++) {
                        sum += numbers[x][y] * cos(x, i) * cos(y, j);
                    }
                }

                var value = DoubleRounder.round(0.25 * Ci * Cj * sum, 1);

                //quantanization by Q10
                res[i][j] = (int) (value / Q90[i][j]);
            }
        }

        return res;
    }

    private static int[][] idct(double[][] numbers) {
        var res = new int[N][N];
        //quantanization by Q10
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                numbers[i][j] = (numbers[i][j] * Q90[i][j]);
            }
        }

        //compression by itself
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        double Ci = C(i);
                        double Cj = C(j);
                        double cosU = cos(u, i);
                        double cosV = cos(v, j);
                        sum += Ci * Cj * numbers[i][j] * cosU * cosV;
                    }
                }
                var value = DoubleRounder.round(0.25 * sum, 1);
                //shift +128
                res[u][v] = (int) value + 128;
            }
        }

        return res;
    }

    public static void main(String[] args) {
        var array = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                array[i][j] = numbers[i][j];
            }
        }

        var res = dct(array);
        var res2 = idct(res);
        System.out.println();
    }

}
