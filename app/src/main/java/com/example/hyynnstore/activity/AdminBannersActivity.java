package com.example.hyynnstore.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import com.example.hyynnstore.controller.BannerController;
import com.example.hyynnstore.model.BannerItem;
import com.example.hyynnstore.utils.Ui;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class AdminBannersActivity extends Activity {
    private BannerController controller;
    private LinearLayout listBox;
    private String selectedImage = "";
    private static final int PICK_BANNER = 83;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        controller = new BannerController(this);
        draw();
    }

    private void draw() {
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý banner", 27));
        root.addView(Ui.text(this, "Thêm/chỉnh sửa banner hiển thị ở trang người dùng. Mỗi banner là một ảnh, tự chạy sau 3 giây.", 14));
        Ui.gap(this, root, 10);

        Button add = Ui.button(this, "+ Thêm banner");
        root.addView(add, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        Ui.gap(this, root, 12);

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(listBox);
        setContentView(Ui.scroll(this, root));
        add.setOnClickListener(v -> showForm(null));
        load();
    }

    private void load() {
        listBox.removeAllViews();
        List<BannerItem> list = controller.all();
        if (list.isEmpty()) {
            LinearLayout empty = Ui.card(this);
            TextView t = Ui.text(this, "Chưa có banner", 15);
            t.setGravity(android.view.Gravity.CENTER);
            empty.addView(t);
            listBox.addView(empty);
            return;
        }
        for (BannerItem b : list) {
            LinearLayout card = Ui.card(this);
            card.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadImageInto(img, b.imageUri);
            img.setBackground(Ui.round(this, Color.parseColor("#E5E7EB"), 16, Ui.strokeColor(this), 1));
            card.addView(img, new LinearLayout.LayoutParams(-1, Ui.dp(this, 135)));
            TextView title = Ui.text(this, (b.title == null || b.title.isEmpty() ? "Banner #" + b.id : b.title) + (b.active == 1 ? "  • Đang hiện" : "  • Đang ẩn"), 16);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setTextColor(Ui.textColor(this));
            card.addView(title);
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            Button edit = Ui.button(this, "Sửa");
            Button toggle = Ui.whiteButton(this, b.active == 1 ? "Ẩn" : "Hiện");
            Button del = Ui.darkButton(this, "Xóa");
            actions.addView(edit, weightBtn(0, 6));
            actions.addView(toggle, weightBtn(6, 6));
            actions.addView(del, weightBtn(6, 0));
            card.addView(actions, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
            edit.setOnClickListener(v -> showForm(b));
            toggle.setOnClickListener(v -> { b.active = b.active == 1 ? 0 : 1; controller.save(b); load(); });
            del.setOnClickListener(v -> { controller.delete(b.id); load(); });
            listBox.addView(card);
        }
    }

    private LinearLayout.LayoutParams weightBtn(int left, int right) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
        p.setMargins(Ui.dp(this, left), 0, Ui.dp(this, right), 0);
        return p;
    }

    private void showForm(BannerItem old) {
        selectedImage = old == null ? "" : old.imageUri;
        LinearLayout content = Ui.vertical(this);
        content.setBackground(Ui.round(this, Ui.cardBg(this), 18, Ui.strokeColor(this), 1));
        TextView title = Ui.title(this, old == null ? "Thêm banner" : "Chỉnh sửa banner", 22);
        content.addView(title);
        EditText name = Ui.input(this, "Tên banner");
        name.setText(old == null ? "" : old.title);
        content.addView(name, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
        Ui.gap(this, content, 8);
        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 16, Ui.strokeColor(this), 1));
        loadImageInto(preview, selectedImage);
        content.addView(preview, new LinearLayout.LayoutParams(-1, Ui.dp(this, 145)));
        Ui.gap(this, content, 8);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button chooseDevice = Ui.darkButton(this, "Chọn từ máy");
        Button choosePool = Ui.button(this, "Chọn từ pool");
        row.addView(chooseDevice, weightBtn(0, 6));
        row.addView(choosePool, weightBtn(6, 0));
        content.addView(row);
        Ui.gap(this, content, 8);
        Button save = Ui.button(this, "Lưu banner");
        content.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(content);
        chooseDevice.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh banner"), PICK_BANNER);
            dialog.dismiss();
        });
        choosePool.setOnClickListener(v -> showPoolPicker(path -> { selectedImage = path; loadImageInto(preview, selectedImage); }));
        save.setOnClickListener(v -> {
            if (selectedImage == null || selectedImage.trim().isEmpty()) { Ui.toast(this, "Bạn chưa chọn ảnh banner"); return; }
            int id = old == null ? 0 : old.id;
            int active = old == null ? 1 : old.active;
            controller.save(new BannerItem(id, name.getText().toString().trim(), selectedImage, active));
            dialog.dismiss();
            load();
        });
        dialog.show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BANNER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
            controller.save(new BannerItem(0, "Banner mới", uri.toString(), 1));
            Ui.toast(this, "Đã thêm banner");
            load();
        }
    }

    private interface PickCallback { void onPick(String path); }

    private void showPoolPicker(PickCallback cb) {
        try {
            String folder = "banner_images";
            String[] rawFiles = getAssets().list(folder);
            List<String> imageFiles = filterImageFiles(rawFiles);
            if (imageFiles.isEmpty()) {
                folder = "product_images";
                rawFiles = getAssets().list(folder);
                imageFiles = filterImageFiles(rawFiles);
            }
            if (imageFiles.isEmpty()) { Ui.toast(this, "Chưa có ảnh trong assets/banner_images hoặc product_images"); return; }
            String[] files = imageFiles.toArray(new String[0]);
            final String finalFolder = folder;
            LinearLayout content = Ui.vertical(this);
            TextView title = Ui.title(this, "Chọn ảnh banner từ pool", 20);
            content.addView(title);
            ScrollView scroll = new ScrollView(this);
            LinearLayout grid = new LinearLayout(this);
            grid.setOrientation(LinearLayout.VERTICAL);
            scroll.addView(grid);
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            LinearLayout row = null; int col = 0;
            for (String f : files) {
                if (row == null || col == 2) { row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); grid.addView(row); col = 0; }
                String path = "asset://" + finalFolder + "/" + f;
                ImageView img = new ImageView(this);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                loadImageInto(img, path);
                img.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 14, Ui.strokeColor(this), 1));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, 110), 1);
                lp.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 8));
                row.addView(img, lp);
                img.setOnClickListener(v -> { cb.onPick(path); dialog.dismiss(); });
                col++;
            }
            content.addView(scroll, new LinearLayout.LayoutParams(-1, Ui.dp(this, 350)));
            dialog.setView(content);
            dialog.show();
        } catch (Exception e) { Ui.toast(this, "Không đọc được pool ảnh"); }
    }

    private List<String> filterImageFiles(String[] files) {
        List<String> result = new ArrayList<>();
        if (files == null) return result;
        for (String f : files) {
            if (f == null) continue;
            String name = f.toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
                result.add(f);
            }
        }
        return result;
    }

    private void loadImageInto(ImageView imageView, String path) {
        try {
            if (path == null || path.trim().isEmpty()) { imageView.setImageResource(android.R.drawable.ic_menu_gallery); return; }
            if (path.startsWith("asset://")) {
                InputStream inputStream = getAssets().open(path.replace("asset://", ""));
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                imageView.setImageDrawable(drawable);
                inputStream.close();
            } else if (path.startsWith("content://") || path.startsWith("file://")) {
                imageView.setImageURI(Uri.parse(path));
            } else imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        } catch (Exception e) { imageView.setImageResource(android.R.drawable.ic_menu_gallery); }
    }
}
