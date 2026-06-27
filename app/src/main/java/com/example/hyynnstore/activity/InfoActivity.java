package com.example.hyynnstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.controller.OrderController;
import com.example.hyynnstore.model.Order;
import com.example.hyynnstore.model.User;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

public class InfoActivity extends Activity {
    AuthController auth;
    OrderController orders;
    SessionManager session;
    User u;
    EditText name, phone, address;
    LinearLayout orderBox;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        auth = new AuthController(this);
        orders = new OrderController(this);
        session = new SessionManager(this);
        u = auth.getUser(session.userId());
        if (u == null) {
            Ui.toast(this, "Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại");
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        draw();
    }

    void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(topBar());

        LinearLayout profile = Ui.card(this);
        TextView avatar = new TextView(this);
        avatar.setText("◯");
        avatar.setTextSize(46);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(Ui.round(this, Ui.isDark(this) ? Color.parseColor("#374151") : Color.parseColor("#FFF7ED"), 999, Ui.isDark(this) ? Color.parseColor("#4B5563") : Color.parseColor("#FED7AA"), 1));
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(Ui.dp(this, 78), Ui.dp(this, 78));
        avp.gravity = Gravity.CENTER_HORIZONTAL;
        profile.addView(avatar, avp);

        TextView email = Ui.text(this, u.email == null ? "" : u.email, 15);
        email.setGravity(Gravity.CENTER);
        profile.addView(email);

        name = Ui.input(this, "Họ tên");
        phone = Ui.input(this, "Số điện thoại");
        address = Ui.input(this, "Địa chỉ");
        name.setText(u.name == null ? "" : u.name);
        phone.setText(u.phone == null ? "" : u.phone);
        address.setText(u.address == null ? "" : u.address);
        profile.addView(name, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, profile, 8);
        profile.addView(phone, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, profile, 8);
        profile.addView(address, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, profile, 10);

        Button save = Ui.button(this, "Cập nhật thông tin");
        Button logout = Ui.darkButton(this, "Đăng xuất");
        profile.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, profile, 8);
        profile.addView(logout, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        root.addView(profile);

        root.addView(Ui.title(this, "Lịch sử đơn hàng", 22));
        orderBox = Ui.vertical(this);
        orderBox.setPadding(0, 0, 0, 0);
        root.addView(orderBox);
        setContentView(Ui.scroll(this, root));

        save.setOnClickListener(v -> {
            u.name = name.getText().toString().trim();
            u.phone = phone.getText().toString().trim();
            u.address = address.getText().toString().trim();
            if (u.name.isEmpty()) { Ui.toast(this, "Nhập họ tên"); return; }
            auth.updateUser(u);
            Ui.toast(this, "Đã cập nhật");
        });
        logout.setOnClickListener(v -> { session.logout(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
        loadOrders();
    }

    LinearLayout topBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = Ui.title(this, "Thông tin cá nhân", 24);
        row.addView(title, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }


    void loadOrders() {
        orderBox.removeAllViews();
        java.util.List<Order> list = orders.list(session.userId(), false);
        if (list.isEmpty()) {
            TextView empty = Ui.text(this, "Chưa có đơn hàng", 15);
            empty.setGravity(Gravity.CENTER);
            orderBox.addView(empty, new LinearLayout.LayoutParams(-1, Ui.dp(this, 64)));
            return;
        }
        for (Order o : list) {
            LinearLayout c = Ui.card(this);
            c.addView(Ui.title(this, "Đơn #" + o.id + " - " + o.status, 18));
            c.addView(Ui.text(this, "Ngày: " + o.date, 14));
            c.addView(Ui.text(this, "Tổng: " + MoneyUtils.vnd(o.totalAmount), 16));
            orderBox.addView(c);
        }
    }
}
