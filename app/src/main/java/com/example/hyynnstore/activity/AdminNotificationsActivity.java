package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hyynnstore.controller.NotificationController;
import com.example.hyynnstore.model.NotificationItem;
import com.example.hyynnstore.utils.Ui;

public class AdminNotificationsActivity extends Activity {
    private NotificationController controller;
    private LinearLayout list;
    private EditText titleInput, contentInput, typeInput;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Ui.applySystemBars(this);
        controller = new NotificationController(this);

        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý thông báo", 28));
        root.addView(Ui.text(this, "Tạo thông báo sale, khuyến mãi hoặc sản phẩm mới cho người dùng.", 14));
        Ui.gap(this, root, 12);

        LinearLayout form = Ui.card(this);
        form.addView(Ui.title(this, "Thêm thông báo giảm giá / sale", 20));
        Ui.gap(this, form, 6);

        titleInput = Ui.input(this, "Tiêu đề thông báo");
        contentInput = Ui.input(this, "Nội dung thông báo");
        typeInput = Ui.input(this, "Loại thông báo");
        typeInput.setText("Khuyến mãi");

        addInput(form, titleInput);
        addInput(form, contentInput);
        addInput(form, typeInput);

        Button add = Ui.button(this, "+ Thêm thông báo");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        btnParams.setMargins(0, Ui.dp(this, 8), 0, 0);
        form.addView(add, btnParams);
        root.addView(form);

        list = Ui.vertical(this);
        list.setPadding(0, 0, 0, 0);
        root.addView(list);
        setContentView(Ui.scroll(this, root));

        add.setOnClickListener(v -> addNotification());
        load();
        controller.markAdminRead();
    }

    private void addInput(LinearLayout parent, EditText input) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, Ui.dp(this, 48));
        p.setMargins(0, 0, 0, Ui.dp(this, 8));
        parent.addView(input, p);
    }

    private void addNotification() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        if (title.isEmpty()) {
            Ui.toast(this, "Nhập tiêu đề thông báo");
            return;
        }
        controller.add(title, content, type.isEmpty() ? "Thông báo" : type);
        titleInput.setText("");
        contentInput.setText("");
        typeInput.setText("Khuyến mãi");
        Ui.toast(this, "Đã thêm thông báo");
        load();
    }

    private void load() {
        list.removeAllViews();
        list.addView(Ui.title(this, "Danh sách thông báo", 20));
        java.util.List<NotificationItem> items = controller.list();
        if (items.isEmpty()) {
            list.addView(Ui.text(this, "Chưa có thông báo", 14));
            return;
        }
        for (NotificationItem item : items) {
            LinearLayout card = Ui.card(this);
            TextView type = Ui.text(this, item.type, 13);
            type.setTextColor(Color.parseColor("#F97316"));
            card.addView(type);
            card.addView(Ui.title(this, item.title, 18));
            card.addView(Ui.text(this, item.content, 14));
            card.addView(Ui.text(this, item.createdAt, 12));
            Ui.gap(this, card, 6);
            Button delete = Ui.darkButton(this, "Xóa thông báo");
            card.addView(delete, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
            delete.setOnClickListener(v -> {
                controller.delete(item.id);
                load();
            });
            list.addView(card);
        }
    }
}
