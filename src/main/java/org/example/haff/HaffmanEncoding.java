package org.example.haff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HaffmanEncoding {

    public static final String PLACEHOLDER = "_";
    public static final String MSG_ALGOS = """
        Method type is %s:
            - initial string was: %s,
            - after encoding it got this form: %s,
            - after decoding it is: %s.
        """;

    public static final String MSG_MEASURE = """
        The coef of compression is:\s
            - encoded with size:    %s
            - initial with size:    %s
            - coef: %s
        """;

    record Mapping(String symbol, String mapping) {

    }

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

            if (i != codedWord.length() - 1) {
                result += PLACEHOLDER;
            }
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
        var result = encodedWord;

        var list = map.entrySet().stream()
            .map(e -> new Mapping(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

        Collections.sort(list, (o1, o2) -> {
            return o2.mapping().length() - o1.mapping().length();
        });

        for (Mapping m : list) {
            var key = String.valueOf(m.symbol());
            result = result.replaceAll(map.get(key), key);
        }

        return result.replaceAll(PLACEHOLDER, "");
    }

    public static void testHaffMethod(String word) {
        var tree = buildTree(new ArrayList<>(buildAlphabet(word)));
        var encoded = encodeString(tree, word);
        var bytesEncoded = Math.ceil(encoded.replace("_", "").length() / 8.0);
        var coef = bytesEncoded / (double) word.getBytes().length * 100.0;

        System.out.println(String.format(MSG_ALGOS, "HAFF", word,
            encoded.replace("_", ""),
            decodeString(createCoversationMap(tree, word), encoded)));
        System.out.println(String.format(MSG_MEASURE,
            bytesEncoded,
            word.getBytes().length,
            coef));
    }
}
