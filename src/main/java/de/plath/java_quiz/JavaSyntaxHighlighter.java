package de.plath.java_quiz;

public class JavaSyntaxHighlighter {

    private static final String[] KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    };

    public static String highlight(String code) {
        // Escape HTML first
        String result = code
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");

        // Strings (double quotes)
        result = result.replaceAll(
            "\"(.*?)\"",
            "<span style='color: #ce9178'>\"$1\"</span>"
        );

        // Single-line comments
        result = result.replaceAll(
            "//(.*)",
            "<span style='color: #6a9955'>//$1</span>"
        );

        // Multi-line comments
        result = result.replaceAll(
            "/\\*(.*?)\\*/",
            "<span style='color: #6a9955'>/*$1*/</span>"
        );

        // Keywords (use word boundaries)
        for (String keyword : KEYWORDS) {
            result = result.replaceAll(
                "\\b" + keyword + "\\b",
                "<span style='color: #569cd6'>" + keyword + "</span>"
            );
        }

        return "<pre style='font-family: monospace;'>" + result + "</pre>";
    }

    // Example usage
    public static void main(String[] args) {
        String code = """
            public class Test {
                public static void main(String[] args) {
                    // Print message
                    System.out.println("Hello, world!");
                }
            }
            """;

        System.out.println(highlight(code));
    }
}
