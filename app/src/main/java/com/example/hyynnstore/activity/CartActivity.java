package com.example.hyynnstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import com.example.hyynnstore.controller.CartController;
import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.model.CartItem;
import com.example.hyynnstore.model.Product;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

public class CartActivity extends Activity {
    CartController cart;
    ProductController products;
    SessionManager session;
    LinearLayout list;
    TextView totalText;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        cart = new CartController(this);
        products = new ProductController(this);
        session = new SessionManager(this);
        draw();
    }

    void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(topBar());

        list = Ui.vertical(this);
        list.setPadding(0, 0, 0, 0);
        root.addView(list);

        LinearLayout summary = Ui.card(this);
        totalText = Ui.title(this, "Tổng tiền: 0 đ", 18);
        totalText.setTextColor(Color.parseColor("#F97316"));
        summary.addView(totalText);

        Button pay = Ui.button(this, "Thanh toán");
        Button back = Ui.darkButton(this, "Tiếp tục mua hàng");
        summary.addView(pay, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, summary, 8);
        summary.addView(back, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        root.addView(summary);

        setContentView(Ui.scroll(this, root));
        pay.setOnClickListener(v -> {
            if (cart.list(session.userId()).isEmpty()) Ui.toast(this, "Giỏ hàng rỗng");
            else startActivity(new Intent(this, PaymentActivity.class));
        });
        back.setOnClickListener(v -> finish());
        load();
    }

    LinearLayout topBar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, Ui.dp(this, 8));
        TextView title = Ui.title(this, "Giỏ hàng", 24);
        title.setGravity(Gravity.LEFT);
        row.addView(title, new LinearLayout.LayoutParams(-1, Ui.dp(this, 42)));
        return row;
    }

    @Override protected void onResume() {
        super.onResume();
        if (list != null) load();
    }

    void load() {
        list.removeAllViews();
        java.util.List<CartItem> items = cart.list(session.userId());
        if (items.isEmpty()) {
            LinearLayout empty = Ui.card(this);
            TextView icon = Ui.text(this, "▱", 40);
            icon.setGravity(Gravity.CENTER);
            TextView msg = Ui.text(this, "Giỏ hàng đang trống", 15);
            msg.setGravity(Gravity.CENTER);
            empty.addView(icon);
            empty.addView(msg);
            list.addView(empty);
        } else {
            for (CartItem i : items) list.addView(cartRow(i));
        }
        totalText.setText("Tổng tiền: " + MoneyUtils.vnd(cart.total(session.userId())));
    }


    LinearLayout cartRow(CartItem i) {
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView image = new TextView(this);
        image.setText(i.image == null ? "▣" : i.image);
        image.setTextSize(34);
        image.setGravity(Gravity.CENTER);
        image.setBackground(Ui.round(this, Color.parseColor("#F3F4F6"), 16, Color.TRANSPARENT, 0));
        top.addView(image, new LinearLayout.LayoutParams(Ui.dp(this, 78), Ui.dp(this, 78)));

        LinearLayout info = Ui.vertical(this);
        info.setPadding(Ui.dp(this, 12), 0, 0, 0);
        TextView name = Ui.title(this, i.productName, 16);
        Product product = products.get(i.productId);
        info.addView(name);
        if (product != null && product.hasSale()) {
            TextView oldPrice = Ui.text(this, MoneyUtils.vnd(product.price), 13);
            oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            TextView saleLine = Ui.text(this, "Khuyến mãi: " + MoneyUtils.vnd(i.price) + " (-" + product.discountPercent() + "%)", 15);
            saleLine.setTextColor(Color.parseColor("#E11D48"));
            saleLine.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(oldPrice);
            info.addView(saleLine);
        } else {
            TextView price = Ui.text(this, MoneyUtils.vnd(i.price), 16);
            price.setTextColor(Color.parseColor("#F97316"));
            price.setTypeface(Typeface.DEFAULT_BOLD);
            info.addView(price);
        }
        TextView qty = Ui.text(this, "Số lượng: " + i.quantity, 14);
        info.addView(qty);
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(top);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        actions.setPadding(0, Ui.dp(this, 12), 0, 0);

        TextView minus = actionButton("−", false);
        TextView plus = actionButton("+", true);
        TextView del = actionButton("Xóa", false);
        del.setTextColor(Color.WHITE);
        del.setBackground(Ui.round(this, Color.parseColor("#111827"), 14, Color.TRANSPARENT, 0));

        LinearLayout.LayoutParams one = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        one.setMargins(0, 0, Ui.dp(this, 6), 0);
        actions.addView(minus, one);
        LinearLayout.LayoutParams two = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        two.setMargins(0, 0, Ui.dp(this, 6), 0);
        actions.addView(plus, two);
        actions.addView(del, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1));
        card.addView(actions);

        minus.setOnClickListener(v -> { cart.qty(i.id, i.quantity - 1); load(); });
        plus.setOnClickListener(v -> { cart.qty(i.id, i.quantity + 1); load(); });
        del.setOnClickListener(v -> { cart.del(i.id); load(); });
        return card;
    }

    TextView actionButton(String text, boolean orange) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(orange ? Color.WHITE : Color.parseColor("#111827"));
        t.setBackground(Ui.round(this, orange ? Color.parseColor("#F97316") : Color.WHITE, 14, Color.parseColor("#E5E7EB"), orange ? 0 : 1));
        t.setClickable(true);
        return t;
    }

}
