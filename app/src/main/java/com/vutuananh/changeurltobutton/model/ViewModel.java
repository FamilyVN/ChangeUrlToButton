package com.vutuananh.changeurltobutton.model;

/**
 * Created by FRAMGIA\vu.tuan.anh on 02/05/2018.
 */

public class ViewModel {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_URL = 2;
    private String text;
    private int type;

    public ViewModel(String text) {
        this.text = text;
        type = TYPE_TEXT;
    }

    public ViewModel(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public static class UrlPosition {
        private String url;
        private int start;
        private int end;

        public UrlPosition(String url, int start, int end) {
            this.url = url;
            this.start = start;
            this.end = end;
        }

        public String getUrl() {
            return url;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}
