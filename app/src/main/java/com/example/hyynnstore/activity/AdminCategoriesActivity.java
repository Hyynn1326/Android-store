package com.example.hyynnstore.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.model.Category;
import com.example.hyynnstore.utils.Ui;

public class AdminCategoriesActivity extends Activity {
    private ProductController controller;
    private LinearLayout listBox;
    private EditText nameInput;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        controller = new ProductController(this);
        draw();
    }

    private void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý danh mục", 24));
        root.addView(Ui.text(this, "Thêm, sửa, xóa danh mục. Danh mục sau khi sửa sẽ hiện ngay ở giao diện người dùng.", 14));
        Ui.gap(this, root, 12);

        LinearLayout form = Ui.card(this);
        form.addView(Ui.title(this, "Thêm danh mục mới", 18));
        nameInput = Ui.input(this, "Tên danh mục, ví dụ: Laptop Gaming");
        form.addView(nameInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, form, 10);
        Button add = Ui.button(this, "+ Thêm danh mục");
        form.addView(add, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        root.addView(form);

        root.addView(Ui.title(this, "Danh sách danh mục", 20));
        listBox = Ui.vertical(this);
        root.addView(listBox);
        setContentView(Ui.scroll(this, root));

        add.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) { Ui.toast(this, "Nhập tên danh mục"); return; }
            controller.addCategory(name);
            nameInput.setText("");
            Ui.toast(this, "Đã thêm danh mục");
            load();
        });
        load();
    }

    private void load() {
        listBox.removeAllViews();
        java.util.List<Category> categories = controller.categories();
        if (categories.isEmpty()) {
            LinearLayout empty = Ui.card(this);
            TextView msg = Ui.text(this, "Chưa có danh mục", 15);
            msg.setGravity(Gravity.CENTER);
            empty.addView(msg, new LinearLayout.LayoutParams(-1, Ui.dp(this, 68)));
            listBox.addView(empty);
            return;
        }
        for (Category category : categories) listBox.addView(row(category));
    }

    private LinearLayout row(Category category) {
        LinearLayout card = Ui.card(this);
        card.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        TextView name = Ui.title(this, category.name, 17);
        card.addView(name);
        TextView note = Ui.text(this, "ID: " + category.id + "  |  Tên dùng để lọc sản phẩm ở trang người dùng", 13);
        note.setTextColor(Color.parseColor("#6B7280"));
        card.addView(note);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, Ui.dp(this, 12), 0, 0);
        Button edit = Ui.button(this, "Sửa");
        Button del = Ui.darkButton(this, "Xóa");
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1);
        ep.setMargins(0, 0, Ui.dp(this, 8), 0);
        actions.addView(edit, ep);
        actions.addView(del, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1));
        card.addView(actions);

        edit.setOnClickListener(v -> showEditDialog(category));
        del.setOnClickListener(v -> showDeleteDialog(category));
        return card;
    }

    private void showEditDialog(Category category) {
        EditText input = Ui.input(this, "Tên danh mục");
        input.setText(category.name);
        input.setSelection(input.getText().length());
        int pad = Ui.dp(this, 18);
        LinearLayout box = Ui.vertical(this);
        box.setPadding(pad, Ui.dp(this, 8), pad, 0);
        box.addView(input, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa danh mục")
                .setView(box)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F97316"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#111827"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) { Ui.toast(this, "Nhập tên danh mục"); return; }
                controller.updateCategory(category.id, newName);
                Ui.toast(this, "Đã cập nhật danh mục");
                dialog.dismiss();
                load();
            });
        });
        dialog.show();
    }

    private void showDeleteDialog(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Xóa danh mục \"" + category.name + "\"? Các sản phẩm thuộc danh mục này sẽ bị bỏ tag danh mục cũ.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, which) -> {
                    controller.deleteCategory(category.id);
                    Ui.toast(this, "Đã xóa danh mục");
                    load();
                })
                .show();
    }
}
