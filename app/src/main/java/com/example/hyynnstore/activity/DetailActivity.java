package com.example.hyynnstore.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import com.example.hyynnstore.controller.CartController;
import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.controller.ReviewController;
import com.example.hyynnstore.controller.ProductReportController;
import com.example.hyynnstore.model.Product;
import com.example.hyynnstore.model.Review;
import com.example.hyynnstore.model.ReviewImage;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DetailActivity extends Activity {
    private int productId;
    private Product product;
    private ProductController productController;
    private CartController cartController;
    private ReviewController reviewController;
    private ProductReportController reportController;
    private SessionManager session;
    private SharedPreferences favoritePrefs;

    private LinearLayout reviewBox;
    private LinearLayout selectedImageBox;
    private EditText commentInput;
    private EditText quantityInput;
    private TextView averageText;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private ArrayList<TextView> starViews = new ArrayList<>();
    private Set<Integer> favoriteProductIds = new HashSet<>();
    private int selectedRating = 0;

    private static final int PICK_IMAGE_REQUEST = 12;
    private final int DARK = Color.rgb(17, 24, 39);
    private final int ORANGE = Color.rgb(249, 115, 22);
    private final int YELLOW = Color.rgb(255, 211, 0);
    private final int RED = Color.rgb(225, 29, 72);
    private final int BG = Color.rgb(245, 246, 248);
    private final int MUTED = Color.rgb(107, 114, 128);

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Ui.applySystemBars(this);
        productId = getIntent().getIntExtra("productId", 0);
        productController = new ProductController(this);
        cartController = new CartController(this);
        reviewController = new ReviewController(this);
        reportController = new ProductReportController(this);
        session = new SessionManager(this);
        favoritePrefs = getSharedPreferences("hyynn_favorites", Context.MODE_PRIVATE);
        loadFavoriteIds();
        product = productController.get(productId);
        draw();
    }

    private void draw() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg(this));
        root.setPadding(dp(12), statusBarHeight() + dp(10), dp(12), dp(24));

        if (product == null) {
            root.addView(title("Không tìm thấy sản phẩm", 22));
            setContentView(root);
            return;
        }

        root.addView(createTopBar());

        LinearLayout productCard = card();
        View productImage = createProductImage(product.image, 64);
        productImage.setBackground(round(Ui.isDark(this) ? Color.parseColor("#111827") : Color.rgb(238, 242, 247), dp(18), Color.TRANSPARENT, 0));
        productCard.addView(productImage, new LinearLayout.LayoutParams(-1, dp(150)));

        productCard.addView(title(product.name, 23));
        if (product.hasSale()) {
            TextView oldPrice = label(MoneyUtils.vnd(product.price), 15, Ui.mutedColor(this), false);
            oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            productCard.addView(oldPrice);
            TextView salePrice = label(MoneyUtils.vnd(product.salePrice) + "  -" + product.discountPercent() + "%", 23, RED, true);
            productCard.addView(salePrice);
        } else {
            TextView price = label(MoneyUtils.vnd(product.price), 23, ORANGE, true);
            productCard.addView(price);
        }
        productCard.addView(label("Thương hiệu: " + product.brand, 15, Ui.mutedColor(this), false));
        productCard.addView(label("Thông số: " + product.spec, 15, Ui.textColor(this), false));
        productCard.addView(label("Mô tả: " + product.description, 15, Ui.textColor(this), false));
        averageText = label("Đánh giá trung bình: " + String.format(Locale.US, "%.1f", reviewController.avg(productId)) + "/5", 15, Ui.textColor(this), true);
        productCard.addView(averageText);

        LinearLayout qtyRow = new LinearLayout(this);
        qtyRow.setOrientation(LinearLayout.HORIZONTAL);
        qtyRow.setGravity(Gravity.CENTER_VERTICAL);
        qtyRow.setPadding(0, dp(10), 0, dp(6));
        qtyRow.addView(label("Số lượng", 15, Ui.textColor(this), true), new LinearLayout.LayoutParams(0, dp(46), 1));
        quantityInput = input("1");
        quantityInput.setText("1");
        quantityInput.setGravity(Gravity.CENTER);
        qtyRow.addView(quantityInput, new LinearLayout.LayoutParams(dp(80), dp(46)));
        productCard.addView(qtyRow);

        Button addCart = orangeButton("Thêm vào giỏ hàng");
        productCard.addView(addCart, new LinearLayout.LayoutParams(-1, dp(48)));
        addCart.setOnClickListener(v -> addToCart());
        root.addView(productCard);

        LinearLayout reviewForm = card();
        reviewForm.addView(title("Viết đánh giá", 20));
        reviewForm.addView(label("Chọn từ 1 đến 5 sao", 14, Ui.mutedColor(this), false));
        reviewForm.addView(createFiveStars());
        LinearLayout commentRow = new LinearLayout(this);
        commentRow.setOrientation(LinearLayout.HORIZONTAL);
        commentRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout imageActions = new LinearLayout(this);
        imageActions.setOrientation(LinearLayout.VERTICAL);
        Button pickImages = darkButton("+ Máy");
        Button pickPoolImages = orangeButton("Pool");
        LinearLayout.LayoutParams smallBtnParams = new LinearLayout.LayoutParams(dp(74), dp(38));
        smallBtnParams.setMargins(0, 0, 0, dp(6));
        imageActions.addView(pickImages, smallBtnParams);
        imageActions.addView(pickPoolImages, new LinearLayout.LayoutParams(dp(74), dp(38)));
        commentRow.addView(imageActions, new LinearLayout.LayoutParams(dp(78), dp(82)));

        commentInput = input("Viết comment đánh giá sản phẩm...");
        commentInput.setSingleLine(false);
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(0, dp(82), 1);
        commentParams.setMargins(dp(8), 0, 0, 0);
        commentRow.addView(commentInput, commentParams);
        reviewForm.addView(commentRow);
        TextView pickHint = label("Bấm + Máy để chọn từ thư viện, hoặc Pool để chọn nhiều ảnh có sẵn trong project", 13, Ui.mutedColor(this), false);
        reviewForm.addView(pickHint);
        addGap(reviewForm, 8);
        selectedImageBox = new LinearLayout(this);
        selectedImageBox.setOrientation(LinearLayout.HORIZONTAL);
        reviewForm.addView(selectedImageBox);
        Button sendReview = orangeButton("Gửi đánh giá");
        reviewForm.addView(sendReview, new LinearLayout.LayoutParams(-1, dp(48)));
        root.addView(reviewForm);

        LinearLayout listCard = card();
        listCard.addView(title("Danh sách đánh giá", 20));
        reviewBox = new LinearLayout(this);
        reviewBox.setOrientation(LinearLayout.VERTICAL);
        listCard.addView(reviewBox);
        root.addView(listCard);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Ui.bg(this));
        scrollView.addView(root);
        setContentView(scrollView);

        pickImages.setOnClickListener(v -> pickImages());
        pickPoolImages.setOnClickListener(v -> showReviewImagePoolDialog());
        sendReview.setOnClickListener(v -> sendReview());
        loadReviews();
    }

    private LinearLayout createTopBar() {
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(0, 0, 0, dp(10));

        TextView screenTitle = title("Chi tiết sản phẩm", 21);
        screenTitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, dp(42), 1);
        titleParams.setMargins(0, 0, dp(6), 0);
        top.addView(screenTitle, titleParams);

        CircleIconButton report = new CircleIconButton(this, "report");
        top.addView(report, new LinearLayout.LayoutParams(dp(42), dp(42)));
        report.setOnClickListener(v -> showReportDialog());

        CircleIconButton heart = new CircleIconButton(this, favoriteProductIds.contains(productId) ? "heartFill" : "heart");
        LinearLayout.LayoutParams heartParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        heartParams.setMargins(dp(8), 0, 0, 0);
        top.addView(heart, heartParams);
        heart.setOnClickListener(v -> {
            toggleFavorite(productId);
            heart.setType(favoriteProductIds.contains(productId) ? "heartFill" : "heart");
            Ui.toast(this, favoriteProductIds.contains(productId) ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích");
        });
        return top;
    }

    private void showReportDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        box.setPadding(pad, dp(10), pad, 0);
        TextView hint = label("Chọn/nhập lý do báo cáo sản phẩm", 14, MUTED, false);
        box.addView(hint);
        EditText reasonInput = input("Lý do báo cáo: sai thông tin, hình ảnh không đúng, giá ảo...");
        reasonInput.setSingleLine(false);
        box.addView(reasonInput, new LinearLayout.LayoutParams(-1, dp(110)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Báo cáo sản phẩm")
                .setView(box)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Gửi", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button send = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            send.setTextColor(ORANGE);
            Button cancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            cancel.setTextColor(DARK);
            send.setOnClickListener(v -> {
                String reason = reasonInput.getText().toString().trim();
                if (reason.isEmpty()) { Ui.toast(this, "Nhập lý do báo cáo"); return; }
                reportController.add(session.userId(), productId, reason);
                Ui.toast(this, "Đã gửi báo cáo sản phẩm");
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private LinearLayout createFiveStars() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.LEFT);
        row.setPadding(0, dp(8), 0, dp(10));
        starViews.clear();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            TextView star = label("☆", 36, Color.rgb(180, 180, 180), false);
            star.setGravity(Gravity.CENTER);
            star.setOnClickListener(v -> setRating(rating));
            row.addView(star, new LinearLayout.LayoutParams(dp(48), dp(50)));
            starViews.add(star);
        }
        return row;
    }

    private void setRating(int rating) {
        selectedRating = rating;
        for (int i = 0; i < starViews.size(); i++) {
            TextView star = starViews.get(i);
            star.setText(i < rating ? "★" : "☆");
            star.setTextColor(i < rating ? Color.rgb(255, 193, 7) : Color.rgb(180, 180, 180));
        }
    }

    private void addToCart() { int qty = 1; try { qty = Integer.parseInt(quantityInput.getText().toString().trim()); } catch (Exception ignored) { } if (qty < 1) qty = 1; cartController.add(session.userId(), product.id, qty, product.finalPrice()); Ui.toast(this, "Đã thêm vào giỏ"); }
    private void pickImages() { Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.setType("image/*"); intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đánh giá"), PICK_IMAGE_REQUEST); }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) addSelectedImage(data.getClipData().getItemAt(i).getUri().toString());
            } else if (data.getData() != null) addSelectedImage(data.getData().toString());
            renderSelectedImages();
        }
    }

    private void addSelectedImage(String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (!selectedImages.contains(value)) selectedImages.add(value);
    }

    private void showReviewImagePoolDialog() {
        ArrayList<String> pool = getReviewPoolImages();
        if (pool.isEmpty()) { Ui.toast(this, "Chưa có ảnh trong assets/review_images hoặc product_images"); return; }

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(8), dp(12), dp(8));
        TextView note = label("Bấm nhiều ảnh để thêm vào đánh giá, sau đó bấm Xong.", 14, MUTED, false);
        box.addView(note);
        Ui.gap(this, box, 8);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout row = null;
        for (int i = 0; i < pool.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row, new LinearLayout.LayoutParams(-1, dp(132)));
            }
            final String assetUri = pool.get(i);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(4), dp(4), dp(4), dp(4));
            item.setBackground(round(Color.WHITE, dp(14), selectedImages.contains(assetUri) ? ORANGE : Color.rgb(230, 230, 230), selectedImages.contains(assetUri) ? 2 : 1));
            ImageView thumb = new ImageView(this);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadImageInto(thumb, assetUri);
            item.addView(thumb, new LinearLayout.LayoutParams(-1, dp(92)));
            TextView name = label(assetUri.substring(assetUri.lastIndexOf('/') + 1), 11, DARK, false);
            name.setSingleLine(true);
            name.setGravity(Gravity.CENTER);
            item.addView(name, new LinearLayout.LayoutParams(-1, dp(28)));
            item.setOnClickListener(v -> {
                addSelectedImage(assetUri);
                v.setBackground(round(Color.WHITE, dp(14), ORANGE, 2));
                renderSelectedImages();
            });
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, dp(124), 1);
            ip.setMargins(dp(4), dp(4), dp(4), dp(4));
            row.addView(item, ip);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(grid);
        box.addView(scroll, new LinearLayout.LayoutParams(-1, dp(390)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh từ pool")
                .setView(box)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xong", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ORANGE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(DARK);
        });
        dialog.show();
    }

    private ArrayList<String> getReviewPoolImages() {
        ArrayList<String> images = new ArrayList<>();
        addAssetImages(images, "review_images");
        if (images.isEmpty()) addAssetImages(images, "product_images");
        return images;
    }

    private void addAssetImages(ArrayList<String> images, String folder) {
        try {
            String[] files = getAssets().list(folder);
            if (files == null) return;
            for (String file : files) {
                String lower = file.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                    images.add("asset://" + folder + "/" + file);
                }
            }
        } catch (Exception ignored) { }
    }

    private void renderSelectedImages() {
        selectedImageBox.removeAllViews();
        for (String imagePath : selectedImages) {
            ImageView imageView = new ImageView(this);
            loadImageInto(imageView, imagePath);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackground(round(Color.rgb(238, 242, 247), dp(12), Color.rgb(225, 225, 225), 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(74), dp(74));
            params.setMargins(0, 0, dp(8), dp(10));
            selectedImageBox.addView(imageView, params);
        }
    }

    private void sendReview() {
        if (selectedRating < 1 || selectedRating > 5) { Ui.toast(this, "Vui lòng chọn từ 1 đến 5 sao"); return; }
        long reviewId = reviewController.add(session.userId(), productId, selectedRating, commentInput.getText().toString().trim());
        for (String imagePath : selectedImages) {
            if (imagePath.startsWith("content://")) {
                try { getContentResolver().takePersistableUriPermission(Uri.parse(imagePath), Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) { }
            }
            reviewController.img(reviewId, imagePath);
        }
        Ui.toast(this, "Đã gửi đánh giá");
        selectedRating = 0; setRating(0); commentInput.setText(""); selectedImages.clear(); selectedImageBox.removeAllViews(); loadReviews(); averageText.setText("Đánh giá trung bình: " + String.format(Locale.US, "%.1f", reviewController.avg(productId)) + "/5");
    }

    private void loadReviews() {
        reviewBox.removeAllViews();
        for (Review review : reviewController.list(productId)) {
            LinearLayout reviewCard = new LinearLayout(this);
            reviewCard.setOrientation(LinearLayout.VERTICAL);
            reviewCard.setPadding(dp(12), dp(10), dp(12), dp(10));
            reviewCard.setBackground(round(Color.rgb(249, 250, 251), dp(14), Color.rgb(232, 232, 232), 1));
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2); cardParams.setMargins(0, 0, 0, dp(10)); reviewCard.setLayoutParams(cardParams);
            reviewCard.addView(label(review.userName + "  " + MoneyUtils.stars(review.rating), 15, DARK, true));
            reviewCard.addView(label(review.comment == null || review.comment.trim().isEmpty() ? "Không có nội dung" : review.comment, 14, DARK, false));
            if (review.adminReply != null && !review.adminReply.trim().isEmpty()) {
                TextView reply = label("Admin trả lời: " + review.adminReply, 14, ORANGE, true);
                reviewCard.addView(reply);
            }
            reviewCard.addView(label(review.createdAt, 12, MUTED, false));
            HorizontalScrollView hsv = new HorizontalScrollView(this); LinearLayout images = new LinearLayout(this); images.setOrientation(LinearLayout.HORIZONTAL);
            for (ReviewImage reviewImage : reviewController.images(review.id)) { ImageView imageView = new ImageView(this); loadImageInto(imageView, reviewImage.imageUri); imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); imageView.setBackground(round(Color.rgb(238, 242, 247), dp(12), Color.rgb(225, 225, 225), 1)); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(86), dp(86)); params.setMargins(0, dp(8), dp(8), 0); images.addView(imageView, params); }
            hsv.addView(images); reviewCard.addView(hsv); reviewBox.addView(reviewCard);
        }
        if (reviewBox.getChildCount() == 0) { TextView empty = label("Chưa có đánh giá", 15, MUTED, false); empty.setPadding(0, dp(14), 0, dp(14)); reviewBox.addView(empty); }
    }


    private void loadImageInto(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }
        if (imagePath.startsWith("asset://")) {
            loadAssetInto(imageView, imagePath);
            return;
        }
        if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
            imageView.setImageURI(Uri.parse(imagePath));
            return;
        }
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    private View createProductImage(String value, int iconSize) {
        value = firstImage(value);
        if (value != null && value.startsWith("asset://")) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadAssetInto(imageView, value);
            return imageView;
        }
        if (value != null && (value.startsWith("content://") || value.startsWith("file://"))) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(Uri.parse(value));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return imageView;
        }
        TextView image = label(value == null || value.trim().isEmpty() ? "▣" : value, iconSize, DARK, false);
        image.setGravity(Gravity.CENTER);
        return image;
    }

    private String firstImage(String value) {
        if (value == null) return "";
        String[] parts = value.split("\\|\\|");
        return parts.length > 0 ? parts[0] : value;
    }

    private void loadAssetInto(ImageView imageView, String assetUri) {
        try {
            String assetPath = assetUri.replace("asset://", "");
            InputStream inputStream = getAssets().open(assetPath);
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            imageView.setImageDrawable(drawable);
            inputStream.close();
        } catch (Exception e) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private LinearLayout card() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(14), dp(14), dp(14), dp(14)); l.setBackground(round(Ui.cardBg(this), dp(18), Ui.strokeColor(this), 1)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, 0, 0, dp(12)); l.setLayoutParams(p); return l; }
    private TextView title(String text, int sp) { return label(text, sp, Ui.textColor(this), true); }
    private TextView label(String text, int sp, int color, boolean bold) { TextView v = new TextView(this); v.setText(text); v.setTextSize(sp); v.setTextColor(color); if (bold) v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(4), 0, dp(4)); return v; }
    private EditText input(String hint) { EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(true); e.setTextSize(15); e.setTextColor(Ui.textColor(this)); e.setHintTextColor(Ui.isDark(this) ? Color.parseColor("#9CA3AF") : Color.parseColor("#6B7280")); e.setPadding(dp(12), 0, dp(12), 0); e.setBackground(round(Ui.cardBg(this), dp(14), Ui.strokeColor(this), 1)); return e; }
    private Button orangeButton(String text) { Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT_BOLD); b.setBackground(round(ORANGE, dp(16), Color.TRANSPARENT, 0)); return b; }
    private Button darkButton(String text) { Button b = orangeButton(text); b.setBackground(round(DARK, dp(16), Color.TRANSPARENT, 0)); return b; }
    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidthDp) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if (strokeWidthDp > 0) d.setStroke(dp(strokeWidthDp), strokeColor); return d; }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private int statusBarHeight() { int id = getResources().getIdentifier("status_bar_height", "dimen", "android"); return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24); }
    private void addGap(LinearLayout layout, int heightDp) { Space s = new Space(this); layout.addView(s, new LinearLayout.LayoutParams(1, dp(heightDp))); }

    private void loadFavoriteIds() { favoriteProductIds.clear(); String raw = favoritePrefs.getString("ids", ""); if (raw == null || raw.trim().isEmpty()) return; for (String part : raw.split(",")) { try { if (!part.trim().isEmpty()) favoriteProductIds.add(Integer.parseInt(part.trim())); } catch (Exception ignored) { } } }
    private void toggleFavorite(int productId) { if (favoriteProductIds.contains(productId)) favoriteProductIds.remove(productId); else favoriteProductIds.add(productId); saveFavoriteIds(); }
    private void saveFavoriteIds() { StringBuilder b = new StringBuilder(); for (Integer id : favoriteProductIds) { if (b.length() > 0) b.append(","); b.append(id); } favoritePrefs.edit().putString("ids", b.toString()).apply(); }

    public class CircleIconButton extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); private String type;
        public CircleIconButton(Context c, String type) { super(c); this.type = type; }
        public void setType(String type) { this.type = type; invalidate(); }
        @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); float w=getWidth(), h=getHeight(), cx=w/2f, cy=h/2f; paint.reset(); paint.setAntiAlias(true); paint.setStrokeWidth(dp(2)); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStyle(Paint.Style.FILL); paint.setColor(Ui.cardBg(DetailActivity.this)); canvas.drawCircle(cx, cy, Math.min(w, h)/2f - dp(2), paint); paint.setStyle(Paint.Style.STROKE); paint.setColor(Ui.strokeColor(DetailActivity.this)); canvas.drawCircle(cx, cy, Math.min(w, h)/2f - dp(2), paint); paint.setColor("heartFill".equals(type)?RED:Ui.textColor(DetailActivity.this)); paint.setStrokeWidth(dp(2.3f));
            if ("report".equals(type)) {
                Path tri = new Path();
                tri.moveTo(cx, cy-dp(12));
                tri.lineTo(cx+dp(13), cy+dp(11));
                tri.lineTo(cx-dp(13), cy+dp(11));
                tri.close();
                canvas.drawPath(tri, paint);
                canvas.drawLine(cx, cy-dp(4), cx, cy+dp(4), paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy+dp(8), dp(1.5f), paint);
                paint.setStyle(Paint.Style.STROKE);
            }
            else if ("heart".equals(type)||"heartFill".equals(type)) { Path p=heartPath(cx,cy+dp(2),dp(11)); if("heartFill".equals(type)) paint.setStyle(Paint.Style.FILL_AND_STROKE); canvas.drawPath(p,paint); }
        }

        private int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
        private Path heartPath(float cx, float cy, float size) { Path p=new Path(); p.moveTo(cx,cy+size*0.72f); p.cubicTo(cx-size*1.25f,cy-size*0.05f,cx-size*0.95f,cy-size*0.95f,cx,cy-size*0.35f); p.cubicTo(cx+size*0.95f,cy-size*0.95f,cx+size*1.25f,cy-size*0.05f,cx,cy+size*0.72f); return p; }
    }
}
