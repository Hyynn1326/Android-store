package com.example.hyynnstore.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import com.example.hyynnstore.controller.ReviewController;
import com.example.hyynnstore.model.*;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.Ui;

import java.io.InputStream;
import java.util.List;

public class AdminReviewsActivity extends Activity {
    private ReviewController reviews;
    private LinearLayout list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.applySystemBars(this);
        reviews = new ReviewController(this);
        LinearLayout root = Ui.page(this);
        root.addView(Ui.title(this, "Quản lý đánh giá", 28));
        root.addView(Ui.text(this, "Xem đánh giá, hình ảnh và trả lời phản hồi của người dùng.", 14));
        Ui.gap(this, root, 12);
        list = Ui.vertical(this);
        root.addView(list);
        setContentView(Ui.scroll(this, root));
        load();
    }

    private void load() {
        list.removeAllViews();
        List<Review> data = reviews.all();
        if (data == null || data.isEmpty()) {
            list.addView(Ui.text(this, "Chưa có đánh giá", 15));
            return;
        }

        for (Review rv : data) {
            LinearLayout c = Ui.card(this);
            c.addView(Ui.title(this, rv.productName == null ? "Sản phẩm" : rv.productName, 18));
            c.addView(Ui.text(this, "Người đánh giá: " + (rv.userName == null ? "Không rõ" : rv.userName), 14));
            c.addView(Ui.text(this, MoneyUtils.stars(rv.rating) + " | " + rv.createdAt, 15));
            c.addView(Ui.text(this, rv.comment == null || rv.comment.trim().isEmpty() ? "Không có nội dung" : rv.comment, 15));

            List<ReviewImage> reviewImages = reviews.images(rv.id);
            if (reviewImages != null && !reviewImages.isEmpty()) {
                HorizontalScrollView hsv = new HorizontalScrollView(this);
                hsv.setHorizontalScrollBarEnabled(false);
                LinearLayout imgs = new LinearLayout(this);
                imgs.setOrientation(LinearLayout.HORIZONTAL);
                imgs.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));

                for (ReviewImage ri : reviewImages) {
                    ImageView im = new ImageView(this);
                    im.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    im.setBackgroundColor(Color.parseColor("#E5E7EB"));
                    loadReviewImage(im, ri.imageUri);
                    LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(Ui.dp(this, 82), Ui.dp(this, 82));
                    imgParams.setMargins(0, 0, Ui.dp(this, 8), 0);
                    imgs.addView(im, imgParams);
                }
                hsv.addView(imgs);
                c.addView(hsv);
            }

            TextView reply = Ui.text(this, "", 14);
            if (rv.adminReply != null && !rv.adminReply.trim().isEmpty()) {
                reply.setText("Admin trả lời: " + rv.adminReply);
                reply.setTextColor(Color.parseColor("#F97316"));
                c.addView(reply);
                Ui.gap(this, c, 6);
            }

            EditText replyInput = Ui.input(this, "Nhập phản hồi của admin...");
            if (rv.adminReply != null) replyInput.setText(rv.adminReply);
            c.addView(replyInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));
            Ui.gap(this, c, 8);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            Button replyBtn = Ui.button(this, "Trả lời");
            Button del = Ui.darkButton(this, "Xóa đánh giá");
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
            p1.setMargins(0, 0, Ui.dp(this, 6), 0);
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1);
            p2.setMargins(Ui.dp(this, 6), 0, 0, 0);
            row.addView(replyBtn, p1);
            row.addView(del, p2);
            c.addView(row);

            Ui.gap(this, c, 8);
            boolean done = reviews.isDone(rv.id);
            Button tick = done ? Ui.darkButton(this, "✓ Đã hoàn thành thông tin") : Ui.button(this, "✓ Tick đã xử lý");
            tick.setEnabled(!done);
            c.addView(tick, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

            replyBtn.setOnClickListener(v -> {
                String replyText = replyInput.getText().toString().trim();
                reviews.reply(rv.id, replyText);
                Ui.toast(this, "Đã trả lời đánh giá");
                // Không reload toàn bộ danh sách để tránh lag, chỉ cập nhật text tại card hiện tại.
                if (!replyText.isEmpty()) {
                    reply.setText("Admin trả lời: " + replyText);
                    reply.setTextColor(Color.parseColor("#F97316"));
                    if (reply.getParent() == null) {
                        c.addView(reply, c.indexOfChild(replyInput));
                    }
                } else {
                    reply.setText("");
                }
            });

            tick.setOnClickListener(v -> {
                reviews.done(rv.id);
                Ui.toast(this, "Đã đánh dấu xử lý đánh giá");
                tick.setText("✓ Đã hoàn thành thông tin");
                tick.setEnabled(false);
                tick.setBackgroundColor(Color.parseColor("#111827"));
                tick.setTextColor(Color.WHITE);
            });

            del.setOnClickListener(v -> {
                reviews.del(rv.id);
                list.removeView(c);
                if (list.getChildCount() == 0) list.addView(Ui.text(this, "Chưa có đánh giá", 15));
            });
            list.addView(c);
        }
    }

    private void loadReviewImage(ImageView imageView, String imageUri) {
        try {
            if (imageUri == null || imageUri.trim().isEmpty()) {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                return;
            }

            if (imageUri.startsWith("asset://")) {
                String assetPath = imageUri.replace("asset://", "");
                InputStream inputStream = getAssets().open(assetPath);
                Drawable drawable = Drawable.createFromStream(inputStream, null);
                imageView.setImageDrawable(drawable);
                inputStream.close();
                return;
            }

            if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                imageView.setImageURI(Uri.parse(imageUri));
                return;
            }

            imageView.setImageURI(Uri.parse(imageUri));
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
}
