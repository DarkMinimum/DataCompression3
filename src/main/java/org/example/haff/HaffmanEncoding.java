package org.example.haff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.colorSpace.ColorSpaceYCbCr;

public class HaffmanEncoding {

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
        String result = "";
        for (int i = 0; i < codedWord.length(); i++) {
            var symbol = String.valueOf(codedWord.charAt(i));
            result += root.findPath(symbol);
        }

        return result;
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

    public static String decodeString(Map<String, String> map, String encodedWord) {
        var result = new StringBuilder();
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

//        System.out.println(String.format(MSG_ALGOS, word, encoded, decodeString(createCoversationMap(tree, word), encoded)));

        return encodeString(tree, word);
    }

    public static String encodeWithHuffman(ColorSpaceYCbCr readyToDecode, int downsampleCoefTheColor) {
        var y = new StringBuilder();
        var cb = new StringBuilder();
        var cr = new StringBuilder();
        var length = readyToDecode.Y().length;
        var width = readyToDecode.Y()[0].length;
        var crLength = length / downsampleCoefTheColor;
        var crWidth = width / downsampleCoefTheColor;

        //should be iterated as zigzag???
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                y.append((int) readyToDecode.Y()[i][j]);
                cb.append((int) readyToDecode.Cb()[i][j]);
                if (i != length - 1 && j != width - 1) {
                    y.append(".");
                    cb.append(".");
                }
                if (crLength > i && crWidth > j) {
                    cr.append((int) readyToDecode.Cr()[i][j]);
                    if (i != crLength - 1 && j != crWidth - 1) {
                        cr.append(".");
                    }
                }
            }
        }

        var decodedY = HaffmanEncoding.testHaffMethod(y.toString());
        var decodedCb = HaffmanEncoding.testHaffMethod(cb.toString());
        var decodedCr = HaffmanEncoding.testHaffMethod(cr.toString());

        return new StringBuilder()
            .append(decodedY).append("\n")
            .append(decodedCb).append("\n")
            .append(decodedCr).toString();
    }
}
