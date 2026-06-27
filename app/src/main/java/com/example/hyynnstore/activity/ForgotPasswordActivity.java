package com.example.hyynnstore.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.utils.Ui;

public class ForgotPasswordActivity extends Activity {
    private AuthController auth;
    private LinearLayout root;
    private LinearLayout card;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirm;
    private TextView tvMessage;
    private String verifiedEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        auth = new AuthController(this);
        Ui.applySystemBars(this);
        showEmailStep();
    }

    private void setupPage() {
        root = Ui.centerPage(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(Ui.dp(this, 16), Ui.statusBar(this) + Ui.dp(this, 34), Ui.dp(this, 16), Ui.dp(this, 24));

        TextView appName = Ui.title(this, "Hyynn Store", 30);
        appName.setGravity(Gravity.CENTER);
        root.addView(appName, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = Ui.text(this, "Khôi phục tài khoản của bạn", 15);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, root, 14);

        card = Ui.card(this);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 16));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = Ui.scroll(this, root);
        scroll.setFillViewport(true);
        setContentView(scroll);
    }

    private void showEmailStep() {
        setupPage();

        TextView title = Ui.title(this, "Quên mật khẩu", 21);
        title.setGravity(Gravity.CENTER);
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView guide = Ui.text(this, "Nhập Gmail đã đăng ký để tiếp tục.", 14);
        guide.setGravity(Gravity.CENTER);
        card.addView(guide, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, card, 12);

        edtEmail = Ui.input(this, "Gmail");
        edtEmail.setSingleLine(true);
        edtEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        card.addView(edtEmail, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        tvMessage = Ui.text(this, "", 14);
        tvMessage.setVisibility(View.GONE);
        card.addView(tvMessage, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, card, 14);

        Button btnContinue = Ui.button(this, "Tiếp tục");
        card.addView(btnContinue, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));

        Ui.gap(this, card, 10);

        Button btnBack = Ui.darkButton(this, "Quay lại đăng nhập");
        card.addView(btnBack, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));

        btnContinue.setOnClickListener(v -> checkEmail());
        btnBack.setOnClickListener(v -> finish());
    }

    private void checkEmail() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            showMessage("Vui lòng nhập Gmail hợp lệ.");
            return;
        }

        if (!auth.emailExists(email)) {
            showMessage("Gmail không tồn tại trong hệ thống.");
            return;
        }

        verifiedEmail = email;
        showPasswordStep();
    }

    private void showPasswordStep() {
        setupPage();

        TextView title = Ui.title(this, "Tạo mật khẩu mới", 21);
        title.setGravity(Gravity.CENTER);
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView guide = Ui.text(this, "Gmail: " + verifiedEmail, 14);
        guide.setGravity(Gravity.CENTER);
        card.addView(guide, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, card, 12);

        edtPassword = Ui.password(this, "Mật khẩu mới");
        edtPassword.setSingleLine(true);
        card.addView(edtPassword, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        Ui.gap(this, card, 8);

        edtConfirm = Ui.password(this, "Xác nhận mật khẩu mới");
        edtConfirm.setSingleLine(true);
        card.addView(edtConfirm, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        tvMessage = Ui.text(this, "", 14);
        tvMessage.setVisibility(View.GONE);
        card.addView(tvMessage, new LinearLayout.LayoutParams(-1, -2));

        Ui.gap(this, card, 14);

        Button btnUpdate = Ui.button(this, "Cập nhật mật khẩu");
        card.addView(btnUpdate, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));

        Ui.gap(this, card, 10);

        Button btnChangeEmail = Ui.darkButton(this, "Nhập Gmail khác");
        card.addView(btnChangeEmail, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));

        btnUpdate.setOnClickListener(v -> updatePassword());
        btnChangeEmail.setOnClickListener(v -> showEmailStep());
    }

    private void updatePassword() {
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();

        if (password.length() < 6) {
            showMessage("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("Xác nhận mật khẩu không khớp.");
            return;
        }

        boolean ok = auth.forgot(verifiedEmail, password);
        if (!ok) {
            showMessage("Cập nhật thất bại. Vui lòng thử lại.");
            return;
        }

        Ui.toast(this, "Đã cập nhật mật khẩu");
        finish();
    }

    private void showMessage(String msg) {
        tvMessage.setText(msg);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setTextColor(android.graphics.Color.parseColor("#DC2626"));
    }
}
