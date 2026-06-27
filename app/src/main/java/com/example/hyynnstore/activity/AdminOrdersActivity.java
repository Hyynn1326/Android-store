package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.hyynnstore.controller.OrderController;
import com.example.hyynnstore.model.Order;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.Ui;

public class AdminOrdersActivity extends Activity {
    private OrderController orders;
    private LinearLayout list;
    private final String[] statuses = {"Chờ xác nhận", "Đã xác nhận", "Đang giao", "Hoàn thành", "Đã hủy"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        orders = new OrderController(this);

        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý đơn hàng", 28));
        root.addView(Ui.text(this, "Xem và cập nhật trạng thái đơn hàng của người dùng.", 14));
        Ui.gap(this, root, 12);

        list = Ui.vertical(this);
        list.setPadding(0, 0, 0, 0);
        root.addView(list);
        setContentView(Ui.scroll(this, root));
        load();
    }

    private void load() {
        list.removeAllViews();
        for (Order o : orders.list(0, true)) {
            LinearLayout c = Ui.card(this);
            c.addView(Ui.title(this, "Đơn #" + o.id + " - " + o.userName, 18));
            c.addView(Ui.text(this, "Ngày: " + o.date, 14));
            c.addView(Ui.text(this, "Tổng: " + MoneyUtils.vnd(o.totalAmount), 15));
            c.addView(Ui.text(this, "Địa chỉ: " + o.address, 14));

            Spinner sp = new Spinner(this);
            ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
            sp.setAdapter(ad);
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equals(o.status)) sp.setSelection(i);
            }
            c.addView(sp);
            Ui.gap(this, c, 8);

            Button save = Ui.button(this, "Cập nhật trạng thái");
            c.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
            save.setOnClickListener(v -> {
                orders.status(o.id, sp.getSelectedItem().toString());
                Ui.toast(this, "Đã cập nhật trạng thái");
            });

            Ui.gap(this, c, 8);
            boolean done = orders.isDone(o.id);
            Button tick = done ? Ui.darkButton(this, "✓ Đã hoàn thành thông tin") : Ui.button(this, "✓ Tick đã xử lý");
            tick.setEnabled(!done);
            c.addView(tick, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
            tick.setOnClickListener(v -> {
                orders.done(o.id);
                Ui.toast(this, "Đã đánh dấu xử lý đơn hàng");
                tick.setText("✓ Đã hoàn thành thông tin");
                tick.setEnabled(false);
                tick.setBackgroundColor(Color.parseColor("#111827"));
                tick.setTextColor(Color.WHITE);
            });
            list.addView(c);
        }
        if (list.getChildCount() == 0) list.addView(Ui.text(this, "Chưa có đơn hàng", 15));
    }
}
