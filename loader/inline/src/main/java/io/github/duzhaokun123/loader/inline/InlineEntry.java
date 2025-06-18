package io.github.duzhaokun123.loader.inline;

@SuppressWarnings("unused")
public class InlineEntry {
    static String HOOKER = "pine";

    /**
     * Inline entry point.
     *
     * @param modulePath the path to the module
     */
    static void entry1(String modulePath) {
        entry2(modulePath, HOOKER);
    }

    /**
     * Inline entry point with hooker.
     *
     * @param modulePath the path to the module
     * @param hooker the hooker to use
     */
    static void entry2(String modulePath, String hooker) {
        InlineEntryKt.entry(modulePath, hooker);
    }
}
