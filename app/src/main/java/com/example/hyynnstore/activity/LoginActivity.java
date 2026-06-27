package com.example.hyynnstore.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.model.User;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

public class LoginActivity extends Activity {
    EditText email, pass;
    AuthController auth;
    SessionManager session;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        auth = new AuthController(this);
        session = new SessionManager(this);
        if (session.logged()) { openByRole(session.role()); return; }
        draw();
    }

    void draw() {
        LinearLayout root = Ui.centerPage(this);
        root.setBackgroundColor(Ui.bg(this));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 16));
        hero.setBackground(Ui.round(this, Ui.isDark(this) ? Color.parseColor("#1F2937") : Color.parseColor("#FACC15"), 26, Color.TRANSPARENT, 0));
        TextView logo = new TextView(this);
        logo.setText("H");
        logo.setTextSize(26);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setGravity(Gravity.CENTER);
        logo.setTextColor(Color.parseColor("#111827"));
        logo.setBackground(Ui.round(this, Color.WHITE, 999, Color.parseColor("#111827"), 2));
        hero.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64)));
        TextView title = Ui.title(this, "Hyynn Store", 31);
        title.setGravity(Gravity.CENTER);
        TextView sub = Ui.text(this, "Cửa hàng đồ công nghệ - Gear, PC, Laptop", 14);
        sub.setGravity(Gravity.CENTER);
        hero.addView(title);
        hero.addView(sub);
        root.addView(hero, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, root, 16);
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18));
        TextView formTitle = Ui.title(this, "Đăng nhập tài khoản", 22);
        formTitle.setGravity(Gravity.CENTER);
        card.addView(formTitle);
        email = Ui.input(this, "Email: admin@gmail.com hoặc user@gmail.com");
        pass = Ui.password(this, "Mật khẩu: 123456");
        card.addView(email, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, card, 8);
        card.addView(pass, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, card, 14);
        Button login = Ui.button(this, "Đăng nhập");
        Button reg = Ui.darkButton(this, "Đăng ký tài khoản");
        Button forgot = Ui.whiteButton(this, "Quên mật khẩu");
        card.addView(login, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        Ui.gap(this, card, 10);
        card.addView(reg, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        Ui.gap(this, card, 10);
        card.addView(forgot, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));

        setContentView(Ui.scroll(this, root));
        login.setOnClickListener(v -> doLogin());
        reg.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        forgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    void doLogin() {
        String e = email.getText().toString().trim();
        String p = pass.getText().toString().trim();
        if (e.isEmpty() || p.isEmpty()) { Ui.toast(this, "Vui lòng nhập đầy đủ"); return; }
        User u = auth.login(e, p);
        if (u == null) { Ui.toast(this, "Sai tài khoản hoặc mật khẩu"); return; }
        if ("locked".equals(u.status)) { showLockedDialog(u.lockReason); return; }
        session.save(u.id, u.role);
        openByRole(u.role);
    }

    void showLockedDialog(String reason) {
        String msg = "Tài khoản đã bị khóa.";
        if (reason != null && !reason.trim().isEmpty()) msg += "\nLý do: " + reason.trim();
        msg += "\nVui lòng liên hệ hotline: 08xxxxx để mở khóa.";
        new AlertDialog.Builder(this)
                .setTitle("Không thể đăng nhập")
                .setMessage(msg)
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    void openByRole(String role) {
        startActivity(new Intent(this, "admin".equals(role) ? AdminDashboardActivity.class : HomeActivity.class));
        finish();
    }
}
