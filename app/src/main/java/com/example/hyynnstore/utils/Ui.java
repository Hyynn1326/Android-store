package com.example.hyynnstore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Window;
import android.widget.*;

public class Ui {
    public static final int LIGHT_BG = Color.parseColor("#F3F4F6");
    public static final int DARK_BG = Color.parseColor("#111827");
    public static final int LIGHT_CARD = Color.WHITE;
    public static final int DARK_CARD = Color.parseColor("#1F2937");
    public static final int LIGHT_TEXT = Color.parseColor("#111827");
    public static final int DARK_TEXT = Color.parseColor("#F9FAFB");
    public static final int LIGHT_MUTED = Color.parseColor("#374151");
    public static final int DARK_MUTED = Color.parseColor("#D1D5DB");
    public static final int ORANGE = Color.parseColor("#F97316");

    public static boolean isDark(Context c) {
        SharedPreferences p = c.getSharedPreferences("hyynn_settings", Context.MODE_PRIVATE);
        return p.getBoolean("dark_mode", false);
    }

    public static int bg(Context c) { return isDark(c) ? DARK_BG : LIGHT_BG; }
    public static int cardBg(Context c) { return isDark(c) ? DARK_CARD : LIGHT_CARD; }
    public static int textColor(Context c) { return isDark(c) ? DARK_TEXT : LIGHT_TEXT; }
    public static int mutedColor(Context c) { return isDark(c) ? DARK_MUTED : LIGHT_MUTED; }
    public static int strokeColor(Context c) { return isDark(c) ? Color.parseColor("#374151") : Color.parseColor("#E5E7EB"); }

    public static void applySystemBars(Activity a) {
        Window w = a.getWindow();
        w.setStatusBarColor(bg(a));
        w.setNavigationBarColor(isDark(a) ? Color.parseColor("#0B1220") : LIGHT_BG);
    }

    public static int dp(Activity a, int v) {
        return (int) (v * a.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int statusBar(Activity a) {
        int id = a.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? a.getResources().getDimensionPixelSize(id) : dp(a, 24);
    }

    public static TextView title(Activity a, String text, int size) {
        TextView t = new TextView(a);
        t.setText(text);
        t.setTextSize(size);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(textColor(a));
        t.setPadding(0, dp(a, 8), 0, dp(a, 8));
        return t;
    }

    public static TextView text(Activity a, String text, int size) {
        TextView t = new TextView(a);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(mutedColor(a));
        t.setPadding(0, dp(a, 4), 0, dp(a, 4));
        return t;
    }

    public static Button button(Activity a, String text) {
        Button b = new Button(a);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setMinHeight(dp(a, 48));
        b.setMinimumHeight(dp(a, 48));
        b.setBackground(round(a, ORANGE, 16, Color.TRANSPARENT, 0));
        b.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        return b;
    }

    public static Button darkButton(Activity a, String text) {
        Button b = button(a, text);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(a, isDark(a) ? Color.parseColor("#0B1220") : Color.parseColor("#111827"), 16, Color.TRANSPARENT, 0));
        return b;
    }

    public static Button grayButton(Activity a, String text) {
        Button b = button(a, text);
        b.setTextColor(textColor(a));
        b.setBackground(round(a, isDark(a) ? Color.parseColor("#374151") : Color.parseColor("#E5E7EB"), 16, Color.TRANSPARENT, 0));
        return b;
    }

    public static Button whiteButton(Activity a, String text) {
        Button b = button(a, text);
        b.setTextColor(textColor(a));
        b.setBackground(round(a, cardBg(a), 16, strokeColor(a), 1));
        return b;
    }

    public static Button outlineButton(Activity a, String text) {
        Button b = button(a, text);
        b.setTextColor(textColor(a));
        b.setBackground(round(a, cardBg(a), 16, strokeColor(a), 1));
        return b;
    }

    public static EditText input(Activity a, String hint) {
        EditText e = new EditText(a);
        e.setHint(hint);
        e.setSingleLine(false);
        e.setTextSize(15);
        e.setTextColor(textColor(a));
        e.setHintTextColor(isDark(a) ? Color.parseColor("#9CA3AF") : Color.parseColor("#6B7280"));
        e.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        e.setBackground(round(a, cardBg(a), 14, strokeColor(a), 1));
        return e;
    }

    public static EditText password(Activity a, String hint) {
        EditText e = input(a, hint);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return e;
    }

    public static LinearLayout vertical(Activity a) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(a, 16), dp(a, 16), dp(a, 16), dp(a, 16));
        return l;
    }

    public static LinearLayout centerPage(Activity a) {
        LinearLayout l = vertical(a);
        l.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        l.setPadding(dp(a, 16), statusBar(a) + dp(a, 36), dp(a, 16), dp(a, 24));
        l.setBackgroundColor(bg(a));
        return l;
    }

    public static LinearLayout page(Activity a) {
        LinearLayout l = vertical(a);
        l.setPadding(dp(a, 16), statusBar(a) + dp(a, 30), dp(a, 16), dp(a, 18));
        l.setBackgroundColor(bg(a));
        return l;
    }

    public static LinearLayout card(Activity a) {
        LinearLayout l = vertical(a);
        l.setBackground(round(a, cardBg(a), 18, strokeColor(a), 1));
        l.setPadding(dp(a, 14), dp(a, 12), dp(a, 14), dp(a, 12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 0, 0, dp(a, 12));
        l.setLayoutParams(p);
        return l;
    }

    public static ScrollView scroll(Activity a, LinearLayout content) {
        ScrollView s = new ScrollView(a);
        s.setBackgroundColor(bg(a));
        s.setFillViewport(false);
        s.addView(content);
        return s;
    }

    public static void toast(Activity a, String m) {
        Toast.makeText(a, m, Toast.LENGTH_SHORT).show();
    }

    public static void gap(Activity a, LinearLayout l, int h) {
        Space sp = new Space(a);
        l.addView(sp, new LinearLayout.LayoutParams(1, dp(a, h)));
    }

    public static GradientDrawable round(Activity a, int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(a, radiusDp));
        if (strokeDp > 0) d.setStroke(dp(a, strokeDp), strokeColor);
        return d;
    }
}
