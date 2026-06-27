package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hyynnstore.controller.NotificationController;
import com.example.hyynnstore.model.NotificationItem;
import com.example.hyynnstore.utils.Ui;

public class NotificationActivity extends Activity {
    private NotificationController controller;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Ui.applySystemBars(this);
        controller = new NotificationController(this);

        LinearLayout root = Ui.page(this);
        root.addView(topBar());
        list = Ui.vertical(this);
        list.setPadding(0, Ui.dp(this, 6), 0, 0);
        root.addView(list);
        setContentView(Ui.scroll(this, root));
        load();
    }

    private LinearLayout topBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, Ui.dp(this, 8));
        TextView title = Ui.title(this, "Thông báo", 24);
        title.setGravity(Gravity.LEFT);
        row.addView(title, new LinearLayout.LayoutParams(-1, Ui.dp(this, 42)));
        return row;
    }

    private void load() {
        list.removeAllViews();
        for (NotificationItem item : controller.list()) {
            LinearLayout card = Ui.card(this);
            TextView type = Ui.text(this, item.type == null ? "Thông báo" : item.type, 13);
            type.setTextColor(Color.parseColor("#F97316"));
            card.addView(type);
            card.addView(Ui.title(this, item.title, 18));
            card.addView(Ui.text(this, item.content == null ? "" : item.content, 15));
            card.addView(Ui.text(this, item.createdAt == null ? "" : item.createdAt, 12));
            list.addView(card);
        }
        if (list.getChildCount() == 0) {
            LinearLayout empty = Ui.card(this);
            TextView msg = Ui.text(this, "Chưa có thông báo", 16);
            msg.setGravity(Gravity.CENTER);
            empty.addView(msg, new LinearLayout.LayoutParams(-1, Ui.dp(this, 72)));
            list.addView(empty);
        }
    }
}
