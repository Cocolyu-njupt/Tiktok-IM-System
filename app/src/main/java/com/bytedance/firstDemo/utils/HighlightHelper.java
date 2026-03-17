package com.bytedance.firstDemo.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class HighlightHelper {

    public static SpannableString highlight(String text, String keyword, int color) {
        if (text == null || text.isEmpty() || keyword == null || keyword.isEmpty()) {
            return new SpannableString(text == null ? "" : text);
        }

        SpannableString ss = new SpannableString(text);

        String lowerText = text.toLowerCase();
        String lowerKey = keyword.toLowerCase();

        int index = lowerText.indexOf(lowerKey);

        while (index != -1) {
            ss.setSpan(
                    new ForegroundColorSpan(color),
                    index,
                    index + keyword.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            index = lowerText.indexOf(lowerKey, index + 1);
        }

        return ss;
    }
}
