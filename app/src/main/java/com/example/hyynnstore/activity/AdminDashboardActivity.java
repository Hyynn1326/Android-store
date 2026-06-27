package com.example.hyynnstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import android.view.View;

import com.example.hyynnstore.controller.AdminController;
import com.example.hyynnstore.controller.NotificationController;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

public class AdminDashboardActivity extends Activity {
    private AdminController admin;
    private SessionManager session;
    private NotificationController notifications;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        admin = new AdminController(this);
        notifications = new NotificationController(this);
        session = new SessionManager(this);
        draw();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (admin != null) draw();
    }

    private void draw() {
        LinearLayout root = Ui.page(this);
        LinearLayout header = Ui.card(this);
        header.setBackground(Ui.round(this, Color.parseColor("#111827"), 22, Color.parseColor("#1F2937"), 1));
        TextView adminTitle = new TextView(this);
        adminTitle.setText("Admin Hyynn Store");
        adminTitle.setTextSize(27);
        adminTitle.setTypeface(Typeface.DEFAULT_BOLD);
        adminTitle.setTextColor(Color.WHITE);
        TextView adminSub = new TextView(this);
        adminSub.setText("Quản trị sản phẩm, banner, danh mục, đơn hàng và báo cáo.");
        adminSub.setTextSize(14);
        adminSub.setTextColor(Color.parseColor("#D1D5DB"));
        header.addView(adminTitle);
        header.addView(adminSub);
        root.addView(header);
        Ui.gap(this, root, 8);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        stats.addView(statRow(statCard("Sản phẩm", String.valueOf(admin.count("Products"))), statCard("User", String.valueOf(admin.count("Users")))));
        stats.addView(statRow(statCard("Đơn hàng", String.valueOf(admin.count("Orders"))), statCard("Đánh giá", String.valueOf(admin.count("Reviews")))));
        stats.addView(statRow(statCard("Thông báo", String.valueOf(admin.count("Notifications"))), statCard("Báo cáo SP", String.valueOf(admin.count("ProductReports")))));
        stats.addView(statRow(statCard("Doanh thu", MoneyUtils.vnd(admin.revenue())), statCard("Chờ xử lý", String.valueOf(admin.count("Orders")))));
        root.addView(stats);

        Ui.gap(this, root, 12);
        root.addView(Ui.title(this, "Danh mục quản trị", 20));
        root.addView(menuRow(menuButton("Sản phẩm", AdminProductsActivity.class, 0), menuButton("Danh mục", AdminCategoriesActivity.class, 0)));
        root.addView(menuRow(menuButton("Banner", AdminBannersActivity.class, 0), menuButton("Thông báo / sale", AdminNotificationsActivity.class, 0)));
        root.addView(menuRow(menuButton("Đơn hàng", AdminOrdersActivity.class, admin.pendingOrders()), menuButton("Người dùng", AdminUsersActivity.class, 0)));
        root.addView(menuRow(menuButton("Đánh giá", AdminReviewsActivity.class, admin.pendingReviews()), menuButton("Báo cáo SP", AdminProductReportsActivity.class, admin.pendingReports())));
        

        Ui.gap(this, root, 14);
        Button out = Ui.darkButton(this, "Đăng xuất");
        root.addView(out, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        out.setOnClickListener(v -> { session.logout(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        setContentView(Ui.scroll(this, root));
    }

    private LinearLayout statRow(LinearLayout a, LinearLayout b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 72), 1);
        lp1.setMargins(0, 0, Ui.dp(this, 8), Ui.dp(this, 8));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 72), 1);
        lp2.setMargins(0, 0, 0, Ui.dp(this, 8));
        row.addView(a, lp1);
        row.addView(b, lp2);
        return row;
    }

    private LinearLayout statCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));
        card.setBackground(Ui.round(this, Color.parseColor("#111827"), 12, Color.parseColor("#1F2937"), 1));
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor("#F97316"));
        v.setTextSize(value.length() > 10 ? 15 : 20);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#D1D5DB"));
        l.setTextSize(12);
        card.addView(v);
        card.addView(l);
        return card;
    }

    private LinearLayout menuRow(View a, View b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 64), 1);
        lp1.setMargins(0, 0, Ui.dp(this, 8), Ui.dp(this, 8));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 64), 1);
        lp2.setMargins(0, 0, 0, Ui.dp(this, 8));
        row.addView(a, lp1);
        row.addView(b, lp2);
        return row;
    }

    private FrameLayout menuButton(String text, Class<?> target, int badgeCount) {
        FrameLayout wrap = new FrameLayout(this);
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setTextColor(Color.WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(Ui.round(this, Color.parseColor("#F97316"), 16, Color.TRANSPARENT, 0));
        wrap.addView(btn, new FrameLayout.LayoutParams(-1, -1));
        wrap.setClickable(true);
        wrap.setOnClickListener(v -> startActivity(new Intent(this, target)));

        if (badgeCount > 0) {
            TextView badge = new TextView(this);
            badge.setText(String.valueOf(badgeCount));
            badge.setTextSize(11);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextColor(Color.WHITE);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(Ui.round(this, Color.parseColor("#DC2626"), 20, Color.WHITE, 1));
            FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26), Gravity.RIGHT | Gravity.TOP);
            bp.setMargins(0, Ui.dp(this, -2), Ui.dp(this, -2), 0);
            wrap.addView(badge, bp);
        }
        return wrap;
    }
}
