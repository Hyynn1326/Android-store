package com.example.hyynnstore.activity;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.content.ContentValues;

import com.example.hyynnstore.database.DatabaseHelper;
import com.example.hyynnstore.utils.Ui;

public class AdminProductReportsActivity extends Activity {
    private DatabaseHelper db;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        db = new DatabaseHelper(this);
        draw();
    }

    private void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Báo cáo sản phẩm", 28));
        root.addView(Ui.text(this, "Xem lý do báo cáo sản phẩm từ người dùng.", 14));
        Ui.gap(this, root, 12);

        list = Ui.vertical(this);
        list.setPadding(0, 0, 0, 0);
        root.addView(list);
        setContentView(Ui.scroll(this, root));
        load();
    }

    private void load() {
        list.removeAllViews();
        Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT r.id,r.reason,r.createdAt,u.name,p.name,IFNULL(r.adminDone,0) " +
                        "FROM ProductReports r " +
                        "LEFT JOIN Users u ON r.userId=u.id " +
                        "LEFT JOIN Products p ON r.productId=p.id " +
                        "ORDER BY r.id DESC", null);
        if (!c.moveToFirst()) {
            LinearLayout empty = Ui.card(this);
            TextView e = Ui.text(this, "Chưa có báo cáo sản phẩm", 15);
            e.setGravity(Gravity.CENTER);
            empty.addView(e);
            list.addView(empty);
            c.close();
            return;
        }
        do {
            int id = c.getInt(0);
            String reason = c.getString(1);
            String date = c.getString(2);
            String user = c.getString(3) == null ? "Không rõ" : c.getString(3);
            String product = c.getString(4) == null ? "Sản phẩm đã xóa" : c.getString(4);
            boolean done = c.getInt(5) == 1;
            list.addView(reportCard(id, product, user, reason, date, done));
        } while (c.moveToNext());
        c.close();
    }

    private LinearLayout reportCard(int id, String product, String user, String reason, String date, boolean done) {
        LinearLayout card = Ui.card(this);
        TextView tag = Ui.text(this, "Báo cáo #" + id, 13);
        tag.setTextColor(Color.parseColor("#DC2626"));
        tag.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(tag);
        card.addView(Ui.title(this, product, 17));
        card.addView(Ui.text(this, "Người báo cáo: " + user, 14));
        card.addView(Ui.text(this, "Lý do: " + reason, 14));
        card.addView(Ui.text(this, "Ngày: " + date, 13));
        Ui.gap(this, card, 8);
        Button tick = done ? Ui.darkButton(this, "✓ Đã hoàn thành thông tin") : Ui.button(this, "✓ Tick đã xử lý");
        tick.setEnabled(!done);
        card.addView(tick, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        tick.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put("adminDone", 1);
            db.getWritableDatabase().update("ProductReports", values, "id=?", new String[]{String.valueOf(id)});
            Ui.toast(this, "Đã đánh dấu xử lý báo cáo");
            tick.setText("✓ Đã hoàn thành thông tin");
            tick.setEnabled(false);
            tick.setBackgroundColor(Color.parseColor("#111827"));
            tick.setTextColor(Color.WHITE);
        });
        return card;
    }
}
