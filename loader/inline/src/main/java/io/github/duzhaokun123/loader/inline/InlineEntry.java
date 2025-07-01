package io.github.duzhaokun123.loader.inline;

import android.app.Application;

@SuppressWarnings("unused")
public class InlineEntry {
    static String HOOKER = "noop";

    /**
     * set application so we needn't wait
     */
    public static Application application = null;

    /**
     * name of previous stage loader
     * maybe null if you don't want set
     */
    public static String previousStageLoader = null;

    /**
     * Inline entry point.
     */
    public static void entry0() {
        entry1(HOOKER);
    }

    /**
     * Inline entry point with hooker.
     *
     * @param hooker the hooker to use
     */
    public static void entry1(String hooker) {
        InlineEntryKt.entry(hooker);
    }
}
