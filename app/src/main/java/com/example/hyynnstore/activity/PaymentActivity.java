package com.example.hyynnstore.activity;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.example.hyynnstore.controller.CartController;
import com.example.hyynnstore.controller.OrderController;
import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.model.User;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

public class PaymentActivity extends Activity {
    CartController cart;
    OrderController orders;
    AuthController auth;
    SessionManager session;
    EditText receiverName, address, phone;
    RadioGroup methods;
    Button done;
    boolean placing = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        cart = new CartController(this);
        orders = new OrderController(this);
        auth = new AuthController(this);
        session = new SessionManager(this);
        draw();
    }

    void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(topBar());

        LinearLayout card = Ui.card(this);
        TextView total = Ui.title(this, "Tổng thanh toán: " + MoneyUtils.vnd(cart.total(session.userId())), 20);
        card.addView(total);

        User user = auth.getUser(session.userId());
        receiverName = Ui.input(this, "Người nhận");
        if (user != null && user.name != null) receiverName.setText(user.name);
        phone = Ui.input(this, "Số điện thoại nhận hàng");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        if (user != null && user.phone != null) phone.setText(user.phone);
        address = Ui.input(this, "Địa chỉ giao hàng");
        if (user != null && user.address != null) address.setText(user.address);
        card.addView(receiverName, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, card, 8);
        card.addView(phone, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, card, 8);
        card.addView(address, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        card.addView(Ui.title(this, "Phương thức thanh toán", 18));
        methods = new RadioGroup(this);
        methods.setOrientation(RadioGroup.VERTICAL);
        String[] ms = {"COD", "Chuyển khoản", "Ví điện tử"};
        for (String m : ms) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setText(m);
            rb.setTextSize(15);
            rb.setTextColor(Color.parseColor("#111827"));
            rb.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            rb.setOnClickListener(v -> methods.check(v.getId()));
            methods.addView(rb, new RadioGroup.LayoutParams(-1, Ui.dp(this, 44)));
        }
        methods.check(methods.getChildAt(0).getId());
        card.addView(methods);
        Ui.gap(this, card, 10);

        done = Ui.button(this, "Xác nhận đặt hàng");
        card.addView(done, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        root.addView(card);
        setContentView(Ui.scroll(this, root));
        done.setOnClickListener(v -> place());
    }

    LinearLayout topBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextSize(34);
        back.setTypeface(Typeface.DEFAULT_BOLD);
        back.setGravity(Gravity.CENTER);
        back.setTextColor(Color.parseColor("#111827"));
        back.setBackground(Ui.round(this, Color.WHITE, 999, Color.parseColor("#E5E7EB"), 1));
        row.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));
        TextView title = Ui.title(this, "Thanh toán", 24);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(Ui.dp(this, 8), 0, 0, 0);
        row.addView(title, tp);
        back.setOnClickListener(v -> finish());
        return row;
    }

    void place() {
        if (placing) return;
        if (cart.list(session.userId()).isEmpty()) { Ui.toast(this, "Giỏ hàng đang trống"); finish(); return; }
        String receiverText = receiverName.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();
        String addressText = address.getText().toString().trim();
        if (receiverText.isEmpty()) { Ui.toast(this, "Nhập tên người nhận"); return; }
        if (phoneText.isEmpty()) { Ui.toast(this, "Nhập số điện thoại nhận hàng"); return; }
        if (addressText.isEmpty()) { Ui.toast(this, "Nhập địa chỉ giao hàng"); return; }
        if (methods.getCheckedRadioButtonId() == -1) { Ui.toast(this, "Chọn phương thức thanh toán"); return; }
        RadioButton rb = findViewById(methods.getCheckedRadioButtonId());

        placing = true;
        done.setEnabled(false);
        done.setText("Đang xử lý...");
        String shippingInfo = "Người nhận: " + receiverText + "\nSĐT: " + phoneText + "\nĐịa chỉ: " + addressText;
        long id = orders.place(session.userId(), shippingInfo, rb.getText().toString());
        if (id <= 0) {
            placing = false;
            done.setEnabled(true);
            done.setText("Xác nhận đặt hàng");
            Ui.toast(this, "Không thể tạo đơn hàng");
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Đặt hàng thành công")
                .setMessage("Mã đơn hàng: #" + id)
                .setPositiveButton("Về trang chủ", (d, w) -> {
                    Intent i = new Intent(this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}
