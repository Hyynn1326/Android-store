package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import com.example.hyynnstore.controller.AdminController;
import com.example.hyynnstore.model.User;
import com.example.hyynnstore.utils.Ui;

public class AdminUsersActivity extends Activity {
    private AdminController admin;
    private LinearLayout list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        admin = new AdminController(this);
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý người dùng", 28));
        root.addView(Ui.text(this, "Admin không bị khóa. Khi khóa user cần ghi rõ lý do.", 14));
        Ui.gap(this, root, 12);
        list = Ui.vertical(this);
        root.addView(list);
        setContentView(Ui.scroll(this, root));
        load();
    }

    private void load() {
        list.removeAllViews();
        for (User u : admin.users()) {
            LinearLayout card = Ui.card(this);
            card.addView(Ui.title(this, u.name + " (" + u.role + ")", 18));
            card.addView(Ui.text(this, u.email + " | " + u.phone, 14));
            card.addView(Ui.text(this, "Trạng thái: " + u.status, 14));
            if (u.lockReason != null && !u.lockReason.trim().isEmpty()) {
                TextView reason = Ui.text(this, "Lý do khóa: " + u.lockReason, 14);
                reason.setTextColor(Color.parseColor("#DC2626"));
                card.addView(reason);
            }

            if ("admin".equalsIgnoreCase(u.role)) {
                TextView note = Ui.text(this, "Tài khoản admin được bảo vệ, không hiển thị nút khóa.", 13);
                note.setGravity(Gravity.CENTER);
                note.setTextColor(Color.parseColor("#6B7280"));
                card.addView(note);
            } else if ("active".equals(u.status)) {
                EditText reasonInput = Ui.input(this, "Nhập lý do khóa tài khoản...");
                card.addView(reasonInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
                Ui.gap(this, card, 8);
                Button lock = Ui.button(this, "Khóa tài khoản");
                card.addView(lock, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
                lock.setOnClickListener(v -> {
                    String reason = reasonInput.getText().toString().trim();
                    if (reason.isEmpty()) { Ui.toast(this, "Vui lòng nhập lý do khóa tài khoản"); return; }
                    admin.lock(u.id, reason);
                    Ui.toast(this, "Đã khóa tài khoản user");
                    load();
                });
            } else {
                Button unlock = Ui.darkButton(this, "Mở khóa tài khoản");
                card.addView(unlock, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
                unlock.setOnClickListener(v -> { admin.unlock(u.id); Ui.toast(this, "Đã mở khóa tài khoản"); load(); });
            }
            list.addView(card);
        }
    }
}
