package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.utils.Ui;

public class RegisterActivity extends Activity {
    EditText name, email, phone, pass, confirm;
    AuthController auth;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        auth = new AuthController(this);
        draw();
    }

    void draw() {
        LinearLayout root = Ui.centerPage(this);
        root.setBackgroundColor(Ui.bg(this));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 14));
        hero.setBackground(Ui.round(this, Ui.isDark(this) ? Color.parseColor("#1F2937") : Color.parseColor("#FACC15"), 26, Color.TRANSPARENT, 0));
        TextView logo = new TextView(this);
        logo.setText("H");
        logo.setTextSize(24);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setGravity(Gravity.CENTER);
        logo.setTextColor(Color.parseColor("#111827"));
        logo.setBackground(Ui.round(this, Color.WHITE, 999, Color.parseColor("#111827"), 2));
        hero.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 58), Ui.dp(this, 58)));
        TextView title = Ui.title(this, "Đăng ký Hyynn Store", 27);
        title.setGravity(Gravity.CENTER);
        hero.addView(title);
        root.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, root, 16);
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18));
        TextView formTitle = Ui.title(this, "Tạo tài khoản mới", 22);
        formTitle.setGravity(Gravity.CENTER);
        card.addView(formTitle);

        name = Ui.input(this, "Họ tên");
        email = Ui.input(this, "Email");
        phone = Ui.input(this, "Số điện thoại");
        pass = Ui.password(this, "Mật khẩu");
        confirm = Ui.password(this, "Xác nhận mật khẩu");
        card.addView(name, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48))); Ui.gap(this, card, 8);
        card.addView(email, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48))); Ui.gap(this, card, 8);
        card.addView(phone, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48))); Ui.gap(this, card, 8);
        card.addView(pass, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48))); Ui.gap(this, card, 8);
        card.addView(confirm, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48))); Ui.gap(this, card, 14);
        Button btn = Ui.button(this, "Đăng ký");
        Button back = Ui.darkButton(this, "Quay lại đăng nhập");
        card.addView(btn, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52))); Ui.gap(this, card, 10);
        card.addView(back, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));

        setContentView(Ui.scroll(this, root));
        btn.setOnClickListener(v -> reg());
        back.setOnClickListener(v -> finish());
    }

    void reg() {
        String n = name.getText().toString().trim();
        String e = email.getText().toString().trim();
        String ph = phone.getText().toString().trim();
        String p = pass.getText().toString();
        String c = confirm.getText().toString();
        if (n.isEmpty() || e.isEmpty() || ph.isEmpty() || p.isEmpty()) { Ui.toast(this, "Không được bỏ trống"); return; }
        if (!e.contains("@")) { Ui.toast(this, "Email không hợp lệ"); return; }
        if (p.length() < 6) { Ui.toast(this, "Mật khẩu tối thiểu 6 ký tự"); return; }
        if (!p.equals(c)) { Ui.toast(this, "Mật khẩu xác nhận không khớp"); return; }
        if (auth.emailExists(e)) { Ui.toast(this, "Email đã tồn tại"); return; }
        auth.register(n, e, ph, p);
        Ui.toast(this, "Đăng ký thành công");
        finish();
    }
}
