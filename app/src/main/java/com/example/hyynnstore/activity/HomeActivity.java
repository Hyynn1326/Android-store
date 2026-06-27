package com.example.hyynnstore.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import com.example.hyynnstore.controller.AuthController;
import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.controller.BannerController;
import com.example.hyynnstore.model.Category;
import com.example.hyynnstore.model.Product;
import com.example.hyynnstore.model.BannerItem;
import com.example.hyynnstore.model.User;
import com.example.hyynnstore.utils.MoneyUtils;
import com.example.hyynnstore.utils.SessionManager;
import com.example.hyynnstore.utils.Ui;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends Activity {
    private ProductController productController;
    private BannerController bannerController;
    private AuthController authController;
    private SessionManager session;
    private SharedPreferences favoritePrefs;

    private LinearLayout productGrid;
    private LinearLayout categoryRow;
    private HorizontalScrollView bannerScroll;
    private LinearLayout bannerRow;
    private Handler bannerHandler = new Handler();
    private int bannerIndex = 0;
    private EditText searchInput;
    private int selectedCategoryId = 0;
    private boolean onlyFavorite = false;
    private int tabMode = 0; // 0: Dành cho bạn, 1: Khuyến mãi, 2: Mới nhất
    private Set<Integer> favoriteProductIds = new HashSet<>();

    private final int YELLOW = Color.rgb(255, 211, 0);
    private final int DARK = Color.rgb(17, 24, 39);
    private final int RED = Color.rgb(225, 29, 72);
    private final int GRAY_TEXT = Color.rgb(107, 114, 128);
    private final int BG = Color.rgb(245, 246, 248);

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(Ui.isDark(this) ? Color.parseColor("#111827") : YELLOW);
        productController = new ProductController(this);
        bannerController = new BannerController(this);
        authController = new AuthController(this);
        session = new SessionManager(this);
        favoritePrefs = getSharedPreferences("hyynn_favorites", Context.MODE_PRIVATE);
        loadFavoriteIds();
        drawHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteIds();
        loadProducts();
    }

    private void drawHome() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg(this));
        root.addView(createHeader());

        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(12), dp(90));
        content.setBackgroundColor(Ui.bg(this));
        content.addView(createBannerBlock());
        content.addView(createCategoryBlock());
        content.addView(createTabRow());

        productGrid = new LinearLayout(this);
        productGrid.setOrientation(LinearLayout.VERTICAL);
        content.addView(productGrid);
        scrollView.addView(content);

        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(createBottomNavigation());
        setContentView(root);
        loadProducts();
    }

    private LinearLayout createHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10), statusBarHeight() + dp(10), dp(10), dp(12));
        header.setBackgroundColor(YELLOW);

        IconButton menu = new IconButton(this, "menu");
        header.addView(menu, new LinearLayout.LayoutParams(dp(44), dp(46)));
        menu.setOnClickListener(v -> showUserPopup());

        LinearLayout searchBox = new LinearLayout(this);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setPadding(dp(12), 0, dp(12), 0);
        searchBox.setBackground(round(Color.WHITE, dp(24), Color.TRANSPARENT, 0));

        IconButton searchIcon = new IconButton(this, "search");
        searchBox.addView(searchIcon, new LinearLayout.LayoutParams(dp(28), dp(42)));

        searchInput = new EditText(this);
        searchInput.setHint("Tìm sản phẩm...");
        searchInput.setSingleLine(true);
        searchInput.setTextSize(15);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setPadding(dp(4), 0, 0, 0);
        searchBox.addView(searchInput, new LinearLayout.LayoutParams(0, -1, 1));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { loadProducts(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        searchParams.setMargins(dp(8), 0, 0, 0);
        header.addView(searchBox, searchParams);
        return header;
    }

    private LinearLayout createBannerBlock() {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, 0, 0, dp(14));

        bannerScroll = new HorizontalScrollView(this);
        bannerScroll.setHorizontalScrollBarEnabled(false);
        bannerRow = new LinearLayout(this);
        bannerRow.setOrientation(LinearLayout.HORIZONTAL);
        renderBanners();
        bannerScroll.addView(bannerRow);
        wrapper.addView(bannerScroll, new LinearLayout.LayoutParams(-1, dp(168)));
        startBannerAutoSlide();
        return wrapper;
    }

    private void renderBanners() {
        if (bannerRow == null) return;
        bannerRow.removeAllViews();
        List<BannerItem> banners = bannerController.active();
        if (banners.isEmpty()) {
            LinearLayout banner = new LinearLayout(this);
            banner.setOrientation(LinearLayout.VERTICAL);
            banner.setGravity(Gravity.CENTER_VERTICAL);
            banner.setPadding(dp(18), 0, dp(18), 0);
            banner.setBackground(round(Ui.isDark(this) ? Color.rgb(31, 41, 55) : Color.WHITE, dp(18), Ui.strokeColor(this), 1));
            TextView t = label("Hyynn Store - Gear, PC, Laptop", 20, Ui.textColor(this), true);
            TextView sub = label("Thêm banner ở Admin → Banner để hiển thị ảnh tại đây", 13, Ui.mutedColor(this), false);
            banner.addView(t);
            banner.addView(sub);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - dp(24), dp(158));
            bannerRow.addView(banner, params);
            return;
        }
        int width = getResources().getDisplayMetrics().widthPixels - dp(24);
        for (BannerItem item : banners) {
            LinearLayout banner = new LinearLayout(this);
            banner.setOrientation(LinearLayout.VERTICAL);
            banner.setGravity(Gravity.CENTER);
            banner.setBackground(round(Ui.cardBg(this), dp(18), Ui.strokeColor(this), 1));
            if (item.imageUri != null && !item.imageUri.trim().isEmpty()) {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                loadImageInto(imageView, item.imageUri);
                banner.addView(imageView, new LinearLayout.LayoutParams(-1, -1));
            } else {
                banner.setPadding(dp(18), 0, dp(18), 0);
                banner.addView(label(item.title == null || item.title.isEmpty() ? "Banner Hyynn Store" : item.title, 22, Ui.textColor(this), true));
                banner.addView(label("Chọn ảnh banner trong Admin để ảnh tự fit theo khung", 13, Ui.mutedColor(this), false));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(158));
            params.setMargins(0, 0, dp(10), 0);
            bannerRow.addView(banner, params);
        }
    }

    private void startBannerAutoSlide() {
        bannerHandler.removeCallbacksAndMessages(null);
        bannerHandler.postDelayed(new Runnable() {
            @Override public void run() {
                if (bannerScroll != null && bannerRow != null && bannerRow.getChildCount() > 1) {
                    bannerIndex = (bannerIndex + 1) % bannerRow.getChildCount();
                    int scrollX = bannerIndex * (getResources().getDisplayMetrics().widthPixels - dp(14));
                    bannerScroll.smoothScrollTo(scrollX, 0);
                    bannerHandler.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }

    private LinearLayout createCategoryBlock() {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, 0, 0, dp(14));

        TextView title = label("Danh mục công nghệ", 18, Ui.textColor(this), true);
        title.setPadding(dp(4), dp(2), 0, dp(8));
        wrapper.addView(title);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        categoryRow = new LinearLayout(this);
        categoryRow.setOrientation(LinearLayout.HORIZONTAL);
        renderCategoryItems();
        hsv.addView(categoryRow);
        wrapper.addView(hsv);
        return wrapper;
    }

    private void renderCategoryItems() {
        if (categoryRow == null) return;
        categoryRow.removeAllViews();
        addCategoryItem(categoryRow, 0, "⌂", "Tất cả");
        List<Category> categories = productController.categories();
        for (Category category : categories) {
            addCategoryItem(categoryRow, category.id, categoryIcon(category.name), category.name);
        }
    }

    private void addCategoryItem(LinearLayout row, int categoryId, String icon, String name) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(6), dp(8), dp(6), dp(8));
        item.setBackground(round(categoryId == selectedCategoryId ? Color.rgb(255, 248, 202) : Color.WHITE, dp(15), categoryId == selectedCategoryId ? YELLOW : Color.rgb(232, 232, 232), 1));

        int categoryTextColor = categoryId == selectedCategoryId ? DARK : Ui.textColor(this);
        TextView iconView = label(icon, 30, categoryTextColor, false);
        iconView.setGravity(Gravity.CENTER);
        TextView nameView = label(shortName(name), 12, categoryTextColor, true);
        nameView.setGravity(Gravity.CENTER);
        nameView.setMaxLines(2);

        item.addView(iconView, new LinearLayout.LayoutParams(-1, dp(38)));
        item.addView(nameView, new LinearLayout.LayoutParams(-1, dp(36)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(86), dp(92));
        params.setMargins(dp(3), dp(3), dp(7), dp(7));
        row.addView(item, params);

        item.setOnClickListener(v -> {
            selectedCategoryId = categoryId;
            onlyFavorite = false;
            renderCategoryItems();
            loadProducts();
        });
    }

    private LinearLayout createTabRow() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(0, dp(2), 0, dp(10));
        tabs.addView(tabText("Dành cho bạn", 0), new LinearLayout.LayoutParams(0, dp(40), 1));
        tabs.addView(tabText("Khuyến mãi", 1), new LinearLayout.LayoutParams(0, dp(40), 1));
        tabs.addView(tabText("Mới nhất", 2), new LinearLayout.LayoutParams(0, dp(40), 1));
        return tabs;
    }

    private TextView tabText(String text, int mode) {
        boolean active = tabMode == mode;
        TextView t = label(text, active ? 19 : 17, active ? Ui.textColor(this) : Ui.mutedColor(this), true);
        t.setGravity(Gravity.CENTER);
        t.setOnClickListener(v -> {
            tabMode = mode;
            onlyFavorite = false;
            drawHome();
        });
        return t;
    }

    private void loadProducts() {
        if (productGrid == null || searchInput == null) return;
        productGrid.removeAllViews();
        String keyword = searchInput.getText().toString().trim();
        List<Product> products = productController.list(keyword, selectedCategoryId);
        LinearLayout currentRow = null;
        int countInRow = 0;
        int shownCount = 0;

        for (Product product : products) {
            if (onlyFavorite && !favoriteProductIds.contains(product.id)) continue;
            if (tabMode == 1 && !product.hasSale()) continue;
            if (tabMode == 2 && product.isNew != 1) continue;
            if (currentRow == null || countInRow == 2) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                productGrid.addView(currentRow, new LinearLayout.LayoutParams(-1, -2));
                countInRow = 0;
            }
            View card = createProductCard(product);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, dp(276), 1);
            cardParams.setMargins(dp(4), dp(5), dp(4), dp(12));
            currentRow.addView(card, cardParams);
            countInRow++;
            shownCount++;
        }

        if (currentRow != null && countInRow == 1) {
            Space empty = new Space(this);
            currentRow.addView(empty, new LinearLayout.LayoutParams(0, 1, 1));
        }
        if (shownCount == 0) {
            LinearLayout emptyBox = new LinearLayout(this);
            emptyBox.setGravity(Gravity.CENTER);
            emptyBox.setPadding(dp(18), dp(35), dp(18), dp(35));
            emptyBox.setBackground(round(Ui.cardBg(this), dp(18), Ui.strokeColor(this), 1));
            emptyBox.addView(label(onlyFavorite ? "Chưa có sản phẩm yêu thích" : "Không có sản phẩm phù hợp", 17, Ui.mutedColor(this), true));
            productGrid.addView(emptyBox, new LinearLayout.LayoutParams(-1, -2));
        }
    }

    private LinearLayout createProductCard(Product product) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, dp(8));
        card.setGravity(Gravity.TOP);
        card.setBackground(round(Ui.cardBg(this), dp(16), Ui.strokeColor(this), 1));

        LinearLayout imageBox = new LinearLayout(this);
        imageBox.setOrientation(LinearLayout.VERTICAL);
        imageBox.setGravity(Gravity.CENTER);
        imageBox.setPadding(dp(8), dp(8), dp(8), dp(8));
        imageBox.setBackground(round(Ui.isDark(this) ? Color.rgb(31, 41, 55) : Color.rgb(238, 242, 247), dp(16), Color.TRANSPARENT, 0));

        IconButton heart = new IconButton(this, favoriteProductIds.contains(product.id) ? "heartFill" : "heart");
        heart.setIconColor(favoriteProductIds.contains(product.id) ? RED : Color.WHITE);
        heart.setShadow(true);
        LinearLayout heartRow = new LinearLayout(this);
        heartRow.setGravity(Gravity.RIGHT);
        heartRow.addView(heart, new LinearLayout.LayoutParams(dp(38), dp(34)));
        imageBox.addView(heartRow, new LinearLayout.LayoutParams(-1, dp(30)));

        View productImage = createProductImage(product.image, 50);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(-1, dp(62));
        imageBox.addView(productImage, imgParams);

        TextView badge = label(product.isHot == 1 ? "HOT" : product.isNew == 1 ? "NEW" : "HYNN", 10, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(product.isHot == 1 ? RED : Color.rgb(37, 99, 235), dp(20), Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(54), dp(23));
        badgeParams.gravity = Gravity.LEFT;
        imageBox.addView(badge, badgeParams);
        card.addView(imageBox, new LinearLayout.LayoutParams(-1, dp(138)));

        TextView name = label(product.name, 15, Ui.textColor(this), false);
        name.setMaxLines(2);
        name.setPadding(dp(8), dp(8), dp(8), 0);
        card.addView(name, new LinearLayout.LayoutParams(-1, dp(48)));
        TextView spec = label(product.brand + " · " + shortSpec(product.spec), 11, Ui.mutedColor(this), false);
        spec.setMaxLines(1);
        spec.setPadding(dp(8), 0, dp(8), 0);
        card.addView(spec, new LinearLayout.LayoutParams(-1, dp(22)));
        if (product.hasSale()) {
            LinearLayout priceBox = new LinearLayout(this);
            priceBox.setOrientation(LinearLayout.VERTICAL);
            priceBox.setPadding(dp(8), dp(2), dp(8), 0);
            TextView oldPrice = label(MoneyUtils.vnd(product.price), 12, Ui.mutedColor(this), false);
            oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            TextView salePrice = label(MoneyUtils.vnd(product.salePrice), 17, RED, true);
            TextView discount = label("Giảm " + product.discountPercent() + "%", 11, Color.WHITE, true);
            discount.setGravity(Gravity.CENTER);
            discount.setBackground(round(RED, dp(12), Color.TRANSPARENT, 0));
            LinearLayout saleRow = new LinearLayout(this);
            saleRow.setGravity(Gravity.CENTER_VERTICAL);
            saleRow.addView(salePrice, new LinearLayout.LayoutParams(0, dp(25), 1));
            saleRow.addView(discount, new LinearLayout.LayoutParams(dp(66), dp(22)));
            priceBox.addView(oldPrice);
            priceBox.addView(saleRow);
            card.addView(priceBox, new LinearLayout.LayoutParams(-1, dp(46)));
        } else {
            TextView price = label(MoneyUtils.vnd(product.price), 17, RED, true);
            price.setPadding(dp(8), dp(9), dp(8), 0);
            card.addView(price, new LinearLayout.LayoutParams(-1, dp(46)));
        }
        TextView stock = label("• Còn " + product.stock + " sản phẩm", 12, Ui.mutedColor(this), false);
        stock.setPadding(dp(8), 0, dp(8), 0);
        card.addView(stock, new LinearLayout.LayoutParams(-1, dp(22)));

        heart.setOnClickListener(v -> {
            toggleFavorite(product.id);
            heart.setType(favoriteProductIds.contains(product.id) ? "heartFill" : "heart");
            heart.setIconColor(favoriteProductIds.contains(product.id) ? RED : Color.WHITE);
            if (onlyFavorite) loadProducts();
        });
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("productId", product.id);
            startActivity(intent);
        });
        return card;
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

    private void loadImageInto(ImageView imageView, String path) {
        path = firstImage(path);
        if (path == null || path.trim().isEmpty()) { imageView.setImageResource(android.R.drawable.ic_menu_gallery); return; }
        if (path.startsWith("asset://")) { loadAssetInto(imageView, path); return; }
        if (path.startsWith("content://") || path.startsWith("file://")) { imageView.setImageURI(Uri.parse(path)); return; }
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
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

    private LinearLayout createBottomNavigation() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6), dp(7), dp(6), dp(7));
        nav.setBackground(round(Color.WHITE, dp(22), Color.rgb(230, 230, 230), 1));

        nav.addView(bottomItem("home", "Trang chủ", !onlyFavorite, () -> { selectedCategoryId = 0; onlyFavorite = false; drawHome(); }), new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(bottomItem("heart", "Yêu thích", onlyFavorite, () -> { onlyFavorite = true; loadProducts(); }), new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(bottomItem("cart", "Giỏ hàng", false, () -> startActivity(new Intent(this, CartActivity.class))), new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(bottomItem("bell", "Thông báo", false, () -> startActivity(new Intent(this, NotificationActivity.class))), new LinearLayout.LayoutParams(0, dp(62), 1));
        nav.addView(bottomItem("user", "Tài khoản", false, () -> startActivity(new Intent(this, InfoActivity.class))), new LinearLayout.LayoutParams(0, dp(62), 1));
        return nav;
    }

    private LinearLayout bottomItem(String iconType, String text, boolean active, Runnable action) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        IconButton icon = new IconButton(this, active && "heart".equals(iconType) ? "heartFill" : iconType);
        icon.setIconColor(active ? DARK : Color.rgb(145, 145, 145));
        TextView textView = label(text, 11, active ? DARK : Color.rgb(145, 145, 145), true);
        textView.setGravity(Gravity.CENTER);
        item.addView(icon, new LinearLayout.LayoutParams(-1, dp(32)));
        item.addView(textView, new LinearLayout.LayoutParams(-1, dp(24)));
        item.setOnClickListener(v -> action.run());
        return item;
    }

    private void showUserPopup() {
        User user = authController.getUser(session.userId());
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(20), dp(18), dp(20), dp(20));
        panel.setBackground(round(Ui.cardBg(this), dp(22), Color.TRANSPARENT, 0));

        TextView close = label("✕", 22, Ui.textColor(this), true);
        close.setGravity(Gravity.RIGHT);
        panel.addView(close, new LinearLayout.LayoutParams(-1, dp(34)));

        IconButton avatar = new IconButton(this, "userCircle");
        avatar.setIconColor(DARK);
        panel.addView(avatar, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView title = label("Thông tin người dùng", 21, Ui.textColor(this), true);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        TextView email = label("Email: " + user.email, 14, Ui.mutedColor(this), false);
        email.setPadding(0, dp(10), 0, dp(4));
        panel.addView(email);

        EditText name = input("Họ tên");
        name.setText(user.name == null ? "" : user.name);
        EditText phone = input("Số điện thoại");
        phone.setText(user.phone == null ? "" : user.phone);
        EditText address = input("Địa chỉ");
        address.setText(user.address == null ? "" : user.address);
        panel.addView(name, new LinearLayout.LayoutParams(-1, dp(50)));
        panel.addView(phone, new LinearLayout.LayoutParams(-1, dp(50)));
        panel.addView(address, new LinearLayout.LayoutParams(-1, dp(50)));
        addGap(panel, 8);

        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setGravity(Gravity.CENTER_VERTICAL);
        themeRow.setPadding(dp(12), 0, dp(12), 0);
        themeRow.setBackground(round(Ui.isDark(this) ? Color.rgb(31, 41, 55) : Color.rgb(248, 249, 250), dp(15), Ui.strokeColor(this), 1));
        TextView themeText = label("Chế độ tối", 15, Ui.textColor(this), true);
        Switch darkSwitch = new Switch(this);
        darkSwitch.setChecked(Ui.isDark(this));
        themeRow.addView(themeText, new LinearLayout.LayoutParams(0, dp(50), 1));
        themeRow.addView(darkSwitch, new LinearLayout.LayoutParams(dp(70), dp(50)));
        panel.addView(themeRow, new LinearLayout.LayoutParams(-1, dp(52)));
        addGap(panel, 8);

        Button save = yellowButton("Lưu chỉnh sửa");
        Button openInfo = darkButton("Mở trang tài khoản");
        Button logout = grayButton("Đăng xuất");
        panel.addView(save, new LinearLayout.LayoutParams(-1, dp(46)));
        addGap(panel, 8);
        panel.addView(openInfo, new LinearLayout.LayoutParams(-1, dp(46)));
        addGap(panel, 8);
        panel.addView(logout, new LinearLayout.LayoutParams(-1, dp(46)));

        PopupWindow popup = new PopupWindow(panel, (int)(getResources().getDisplayMetrics().widthPixels * 0.86f), -2, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(12));
        popup.showAtLocation(getWindow().getDecorView(), Gravity.TOP | Gravity.LEFT, dp(10), statusBarHeight() + dp(10));

        darkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("hyynn_settings", Context.MODE_PRIVATE).edit().putBoolean("dark_mode", isChecked).apply();
            popup.dismiss();
            drawHome();
        });
        close.setOnClickListener(v -> popup.dismiss());
        save.setOnClickListener(v -> {
            user.name = name.getText().toString().trim();
            user.phone = phone.getText().toString().trim();
            user.address = address.getText().toString().trim();
            authController.updateUser(user);
            Ui.toast(this, "Đã cập nhật thông tin");
            popup.dismiss();
        });
        openInfo.setOnClickListener(v -> { popup.dismiss(); startActivity(new Intent(this, InfoActivity.class)); });
        logout.setOnClickListener(v -> { popup.dismiss(); session.logout(); startActivity(new Intent(this, LoginActivity.class)); finish(); });
    }

    private EditText input(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setTextSize(15);
        editText.setPadding(dp(12), 0, dp(12), 0);
        editText.setTextColor(Ui.textColor(this));
        editText.setHintTextColor(Ui.mutedColor(this));
        editText.setBackground(round(Ui.cardBg(this), dp(13), Ui.strokeColor(this), 1));
        return editText;
    }

    private Button yellowButton(String text) { Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT_BOLD); b.setTextColor(DARK); b.setTextSize(15); b.setBackground(round(YELLOW, dp(16), Color.TRANSPARENT, 0)); return b; }
    private Button darkButton(String text) { Button b = yellowButton(text); b.setTextColor(Color.WHITE); b.setBackground(round(DARK, dp(16), Color.TRANSPARENT, 0)); return b; }
    private Button grayButton(String text) { Button b = yellowButton(text); b.setBackground(round(Color.rgb(238, 238, 238), dp(16), Color.TRANSPARENT, 0)); return b; }

    private TextView label(String text, int sp, int color, boolean bold) { TextView t = new TextView(this); t.setText(text); t.setTextSize(sp); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT_BOLD); return t; }
    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidthDp) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); if (strokeWidthDp > 0) d.setStroke(dp(strokeWidthDp), strokeColor); return d; }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private int statusBarHeight() { int id = getResources().getIdentifier("status_bar_height", "dimen", "android"); return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24); }
    private void addGap(LinearLayout layout, int heightDp) { Space s = new Space(this); layout.addView(s, new LinearLayout.LayoutParams(1, dp(heightDp))); }

    private String categoryIcon(String name) {
        String lower = name.toLowerCase();
        // Trả lại bộ icon cũ theo phong cách đồ án Java: dễ nhìn, đúng chủ đề công nghệ.
        if (lower.contains("laptop")) return "💻";
        if (lower.contains("pc")) return "🖥️";
        if (lower.contains("màn")) return "🖥️";
        if (lower.contains("phím")) return "⌨️";
        if (lower.contains("chuột")) return "🖱️";
        if (lower.contains("tai")) return "🎧";
        if (lower.contains("linh")) return "🧩";
        if (lower.contains("phụ")) return "🔌";
        return "🛒";
    }
    private String shortName(String name) { if (name == null) return ""; return name.length() <= 13 ? name : name.substring(0, 12) + "..."; }
    private String shortSpec(String spec) { if (spec == null) return ""; return spec.length() <= 24 ? spec : spec.substring(0, 23) + "..."; }

    private void loadFavoriteIds() { favoriteProductIds.clear(); String raw = favoritePrefs.getString("ids", ""); if (raw == null || raw.trim().isEmpty()) return; for (String part : raw.split(",")) { try { if (!part.trim().isEmpty()) favoriteProductIds.add(Integer.parseInt(part.trim())); } catch (Exception ignored) { } } }
    private void toggleFavorite(int productId) { if (favoriteProductIds.contains(productId)) favoriteProductIds.remove(productId); else favoriteProductIds.add(productId); saveFavoriteIds(); }
    private void saveFavoriteIds() { StringBuilder b = new StringBuilder(); for (Integer id : favoriteProductIds) { if (b.length() > 0) b.append(","); b.append(id); } favoritePrefs.edit().putString("ids", b.toString()).apply(); }

    public class IconButton extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private String type;
        private int iconColor = DARK;
        private boolean shadow = false;
        public IconButton(Context context, String type) { super(context); this.type = type; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        public void setType(String type) { this.type = type; invalidate(); }
        public void setIconColor(int iconColor) { this.iconColor = iconColor; invalidate(); }
        public void setShadow(boolean shadow) { this.shadow = shadow; invalidate(); }
        @Override protected void onDraw(Canvas canvas) { super.onDraw(canvas); float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h / 2f; paint.reset(); paint.setAntiAlias(true); paint.setColor(iconColor); paint.setStrokeWidth(dp(2)); paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); if (shadow) paint.setShadowLayer(dp(3), 0, dp(1), Color.rgb(90, 90, 90));
            if ("menu".equals(type)) { canvas.drawLine(dp(8), cy-dp(9), w-dp(8), cy-dp(9), paint); canvas.drawLine(dp(8), cy, w-dp(8), cy, paint); canvas.drawLine(dp(8), cy+dp(9), w-dp(8), cy+dp(9), paint); }
            else if ("search".equals(type)) { canvas.drawCircle(cx-dp(2), cy-dp(2), dp(8), paint); canvas.drawLine(cx+dp(5), cy+dp(5), cx+dp(13), cy+dp(13), paint); }
            else if ("home".equals(type)) { Path p=new Path(); p.moveTo(cx-dp(11),cy); p.lineTo(cx,cy-dp(10)); p.lineTo(cx+dp(11),cy); p.lineTo(cx+dp(11),cy+dp(12)); p.lineTo(cx-dp(11),cy+dp(12)); p.close(); canvas.drawPath(p,paint); }
            else if ("heart".equals(type)||"heartFill".equals(type)) { Path p=heartPath(cx,cy+dp(2),dp(12)); if("heartFill".equals(type)) paint.setStyle(Paint.Style.FILL_AND_STROKE); canvas.drawPath(p,paint); }
            else if ("cart".equals(type)) { Path p=new Path(); p.moveTo(cx-dp(14),cy-dp(9)); p.lineTo(cx-dp(10),cy-dp(9)); p.lineTo(cx-dp(6),cy+dp(6)); p.lineTo(cx+dp(10),cy+dp(6)); p.lineTo(cx+dp(14),cy-dp(5)); p.lineTo(cx-dp(7),cy-dp(5)); canvas.drawPath(p,paint); canvas.drawCircle(cx-dp(4),cy+dp(12),dp(2),paint); canvas.drawCircle(cx+dp(9),cy+dp(12),dp(2),paint); }
            else if ("bell".equals(type)) { RectF arc=new RectF(cx-dp(10),cy-dp(11),cx+dp(10),cy+dp(12)); canvas.drawArc(arc,200,140,false,paint); canvas.drawLine(cx-dp(12),cy+dp(8),cx+dp(12),cy+dp(8),paint); canvas.drawCircle(cx,cy+dp(13),dp(2),paint); canvas.drawLine(cx,cy-dp(15),cx,cy-dp(12),paint); paint.setStyle(Paint.Style.FILL); canvas.drawCircle(cx+dp(11),cy-dp(11),dp(3),paint); }
            else if ("user".equals(type)||"userCircle".equals(type)) { if("userCircle".equals(type)) canvas.drawCircle(cx,cy,dp(27),paint); canvas.drawCircle(cx,cy-dp(7),dp(7),paint); RectF body=new RectF(cx-dp(14),cy+dp(4),cx+dp(14),cy+dp(24)); canvas.drawArc(body,200,140,false,paint); }
        }
        private Path heartPath(float cx, float cy, float size) { Path p=new Path(); p.moveTo(cx, cy+size*0.72f); p.cubicTo(cx-size*1.25f, cy-size*0.05f, cx-size*0.95f, cy-size*0.95f, cx, cy-size*0.35f); p.cubicTo(cx+size*0.95f, cy-size*0.95f, cx+size*1.25f, cy-size*0.05f, cx, cy+size*0.72f); return p; }
    }
}
