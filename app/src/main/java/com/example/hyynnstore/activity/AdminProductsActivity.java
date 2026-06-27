package com.example.hyynnstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import java.io.InputStream;
import android.os.Bundle;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.model.Product;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.Ui;

public class AdminProductsActivity extends Activity {
    private ProductController pc;
    private LinearLayout list;
    private EditText search;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        pc = new ProductController(this);
        draw();
    }

    @Override protected void onResume() {
        super.onResume();
        if (list != null) load();
    }

    private void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý sản phẩm", 28));
        root.addView(Ui.text(this, "Tìm kiếm tự động theo tên sản phẩm, thêm/sửa/xóa sản phẩm.", 14));
        Ui.gap(this, root, 12);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        search = Ui.input(this, "Tìm sản phẩm...");
        Button add = Ui.button(this, "+ Thêm");
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1);
        searchParams.setMargins(0, 0, Ui.dp(this, 8), 0);
        top.addView(search, searchParams);
        top.addView(add, new LinearLayout.LayoutParams(Ui.dp(this, 108), Ui.dp(this, 52)));
        root.addView(top);
        Ui.gap(this, root, 12);

        list = Ui.vertical(this);
        root.addView(list);
        setContentView(Ui.scroll(this, root));

        add.setOnClickListener(v -> startActivity(new Intent(this, AdminProductFormActivity.class)));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { load(); }
            public void afterTextChanged(Editable s) {}
        });
        load();
    }

    private View createProductImage(String value) {
        if (value != null && value.startsWith("asset://")) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try {
                InputStream inputStream = getAssets().open(value.replace("asset://", ""));
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                imageView.setImageDrawable(drawable);
                inputStream.close();
            } catch (Exception e) { imageView.setImageResource(android.R.drawable.ic_menu_gallery); }
            imageView.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 12, Color.parseColor("#E5E7EB"), 1));
            return imageView;
        }
        if (value != null && (value.startsWith("content://") || value.startsWith("file://"))) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(Uri.parse(value));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 12, Color.parseColor("#E5E7EB"), 1));
            return imageView;
        }
        TextView icon = Ui.text(this, value == null || value.trim().isEmpty() ? "▣" : value, 26);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 12, Color.parseColor("#E5E7EB"), 1));
        return icon;
    }

    private void load() {
        list.removeAllViews();
        String keyword = search == null ? "" : search.getText().toString().trim();
        for (Product p : pc.list(keyword, 0)) {
            LinearLayout c = Ui.card(this);
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(createProductImage(p.image), new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 54)));
            LinearLayout info = Ui.vertical(this);
            info.addView(Ui.title(this, p.name, 18));
            if (p.hasSale()) {
                TextView oldPrice = Ui.text(this, MoneyUtils.vnd(p.price), 13);
                oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                TextView salePrice = Ui.text(this, "Khuyến mãi: " + MoneyUtils.vnd(p.salePrice) + "  (-" + p.discountPercent() + "%) | Tồn: " + p.stock, 15);
                salePrice.setTextColor(Color.parseColor("#E11D48"));
                salePrice.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                info.addView(oldPrice);
                info.addView(salePrice);
            } else {
                info.addView(Ui.text(this, MoneyUtils.vnd(p.price) + " | Tồn: " + p.stock, 15));
            }
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, -2, 1);
            infoParams.setMargins(Ui.dp(this, 10), 0, 0, 0);
            head.addView(info, infoParams);
            c.addView(head);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            Button edit = Ui.button(this, "Sửa");
            Button del = Ui.darkButton(this, "Xóa");
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
            p1.setMargins(0, Ui.dp(this, 8), Ui.dp(this, 6), 0);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
            p2.setMargins(Ui.dp(this, 6), Ui.dp(this, 8), 0, 0);
            row.addView(edit, p1);
            row.addView(del, p2);
            c.addView(row);
            edit.setOnClickListener(v -> {
                Intent i = new Intent(this, AdminProductFormActivity.class);
                i.putExtra("productId", p.id);
                startActivity(i);
            });
            del.setOnClickListener(v -> { pc.delete(p.id); load(); });
            list.addView(c);
        }
        if (list.getChildCount() == 0) list.addView(Ui.text(this, "Không tìm thấy sản phẩm", 15));
    }
}
