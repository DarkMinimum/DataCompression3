package org.example.haff;

import static org.example.util.ColorUtils.zigZagMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.colorSpace.ColorSpaceYCbCr;

public class HaffmanEncoding {

    public static List<Map<String, String>> maps = new ArrayList<>();

    private static Node findAndRemoveMin(List<Node> list) {
        if (list.isEmpty()) {
            return null;
        }
        var entry = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            if (entry.weight() >= list.get(i).weight()) {
                entry = list.get(i);
            }
        }
        list.remove(entry);
        return entry;
    }

    public static String encodeString(Node root, String codedWord) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));
            result.append(root.findPath(symbol));
        }
        return result.toString();
    }

    public static List<Node> buildAlphabet(String codedWord) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));
            if (nodes.stream().anyMatch(n -> n.symbol().equals(symbol))) {
                var node = nodes.stream().filter(n -> n.symbol().equals(symbol)).findFirst().get();
                node.incrementWeight();
                continue;
            }
            nodes.add(new Node(symbol, 1, null, null));
        }
        return nodes;
    }

    public static Node buildTree(List<Node> nodes) {
        while (!nodes.isEmpty() && nodes.size() != 1) {
            var r = findAndRemoveMin(nodes);
            var l = findAndRemoveMin(nodes);
            nodes.add(new Node(l.symbol() + r.symbol(), l.weight() + r.weight(), l, r));
        }
        return nodes.get(0);
    }

    private static Map<String, String> createCoversationMap(Node root, String word) {
        var map = new HashMap<String, String>();
        for (int i = 0; i < word.length(); i++) {
            var symbol = String.valueOf(word.charAt(i));
            map.put(symbol, root.findPath(symbol));
        }
        return map;
    }

    public static String decodeString(String encodedWord, int index) {
        var result = new StringBuilder();
        var map = maps.get(index);
        while (encodedWord.length() != 0) {
            var keys = map.keySet();
            for (String key : keys) {
                if (encodedWord.startsWith(map.get(key))) {
                    result.append(key);
                    encodedWord = encodedWord.substring(map.get(key).length());
                    break;
                }
            }
        }
        return result.toString();
    }

    public static String testHaffMethod(String word) {
        var tree = buildTree(new ArrayList<>(buildAlphabet(word)));
        var encoded = encodeString(tree, word);
        var map = createCoversationMap(tree, word);
        maps.add(map);
        return encoded;
    }

    public static String encodeWithHuffman(ColorSpaceYCbCr readyToDecode) {
        var length = readyToDecode.Y().length;
        var width = readyToDecode.Y()[0].length;
        var y = zigZagMatrix(readyToDecode.Y(), length, width);
        var cb = zigZagMatrix(readyToDecode.Cb(), length, width);
        var cr = zigZagMatrix(readyToDecode.Cr(), readyToDecode.Cr().length, readyToDecode.Cr()[0].length);

        var decodedY = HaffmanEncoding.testHaffMethod(rleString(y));
        var decodedCb = HaffmanEncoding.testHaffMethod(rleString(cb));
        var decodedCr = HaffmanEncoding.testHaffMethod(rleString(cr));

        return new StringBuilder()
                .append(decodedY).append("\n")
                .append(decodedCb).append("\n")
                .append(decodedCr).toString();
    }

    public static String rleString(String source) {
        var symbols = source.split("\\.");
        var counter = 1;
        var result = new StringBuilder();

        var current = symbols[0];
        for (int i = 1; i < symbols.length; i++) {
            var word = symbols[i];

            if (word.equals(current)) {
                counter++;
            } else {
                if (counter > 1) {
                    result.append(counter).append("@").append(current);
                } else {
                    result.append(current);
                }
                counter = 1;
                result.append(".");

            }

            if (i == symbols.length - 1) {
                if (word.equals(current)) {
                    counter++;
                    result.append(counter).append("@").append(word);
                } else {
                    result.append(word);
                }

                break;
            }

            current = word;
        }

        return result.toString();
    }

    public static String inverseRleString(String source) {
        var combs = source.split("\\.");
        var res = new StringBuilder();
        for (int pos = 0; pos < combs.length; ++pos) {
            var word = combs[pos];
            var comb = word.split("@");
            if (comb.length == 1) {
                res.append(word);
            } else {
                int number = Integer.parseInt(comb[0]);
                for (int i = 0; i < number; i++) {
                    res.append(comb[1]);

                    if (i != number - 1) {
                        res.append(".");
                    }
                }
            }

            if (pos != combs.length - 1) {
                res.append(".");
            }
        }
        return res.toString();
    }

    public static ColorSpaceYCbCr decode(String content) {
        var layers = content.split("\n");
        var ycbcrLayers = new ArrayList<double[][]>();
        for (int l = 0; l < layers.length; l++) {
            var raw = inverseRleString(decodeString(layers[l], l)).split("\\.");
            //add drle
            var size = (int) Math.sqrt(raw.length);
            var array = new double[size][size];
            fillMatrix(array, raw);
            ycbcrLayers.add(array);
        }
        return new ColorSpaceYCbCr(ycbcrLayers.get(0), ycbcrLayers.get(1), ycbcrLayers.get(2));
    }

    public static void fillMatrix(double[][] matrix, String[] values) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        var startValue = 0;

        for (int i = 0; i < rows + cols - 1; i++) {
            if (i % 2 == 0) { // Even rows (from top to bottom)
                for (int row = Math.min(i, rows - 1); row >= 0 && i - row < cols; row--) {
                    matrix[row][i - row] = Integer.parseInt(values[startValue++]);
                }
            } else { // Odd rows (from bottom to top)
                for (int col = Math.min(i, cols - 1); col >= 0 && i - col < rows; col--) {
                    matrix[i - col][col] = Integer.parseInt(values[startValue++]);
                }
            }
        }
    }
}
