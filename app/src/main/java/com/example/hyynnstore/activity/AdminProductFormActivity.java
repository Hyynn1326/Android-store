package com.example.hyynnstore.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.hyynnstore.controller.ProductController;
import com.example.hyynnstore.model.Category;
import com.example.hyynnstore.model.Product;
import com.example.hyynnstore.utils.Ui;

public class AdminProductFormActivity extends Activity {
    private ProductController pc;
    private int id;
    private EditText name, price, salePrice, image, des, brand, spec, stock;
    private Spinner cateSpinner;
    private List<Category> categoryList = new ArrayList<>();
    private int selectedCategoryId = 0;
    private ImageView preview;
    private LinearLayout previewStrip;
    private String selectedImageUri = "";
    private final ArrayList<String> selectedImages = new ArrayList<>();
    private static final int PICK_PRODUCT_IMAGE = 44;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Ui.applySystemBars(this);

        pc = new ProductController(this);
        id = getIntent().getIntExtra("productId", 0);
        Product p = id > 0 ? pc.get(id) : null;

        LinearLayout root = Ui.page(this);
        root.setPadding(Ui.dp(this, 16), Ui.statusBar(this) + Ui.dp(this, 30), Ui.dp(this, 16), Ui.dp(this, 190));
        root.addView(Ui.title(this, id > 0 ? "Sửa sản phẩm" : "Thêm sản phẩm", 28));
        root.addView(Ui.text(this, "Có thể nhập icon, chọn ảnh từ máy/pool và chỉnh giá khuyến mãi.", 14));
        Ui.gap(this, root, 12);

        preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setAdjustViewBounds(true);
        preview.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 16, Color.parseColor("#E5E7EB"), 1));
        root.addView(preview, new LinearLayout.LayoutParams(-1, Ui.dp(this, 150)));
        Ui.gap(this, root, 8);

        TextView previewHint = Ui.text(this, "Ảnh đã chọn: có thể cuộn ngang để xem nhiều ảnh", 13);
        root.addView(previewHint);
        HorizontalScrollView previewScroll = new HorizontalScrollView(this);
        previewScroll.setHorizontalScrollBarEnabled(false);
        previewStrip = new LinearLayout(this);
        previewStrip.setOrientation(LinearLayout.HORIZONTAL);
        previewScroll.addView(previewStrip);
        root.addView(previewScroll, new LinearLayout.LayoutParams(-1, Ui.dp(this, 92)));
        Ui.gap(this, root, 8);

        LinearLayout imageButtonRow = new LinearLayout(this);
        imageButtonRow.setOrientation(LinearLayout.HORIZONTAL);

        Button chooseImage = Ui.darkButton(this, "Chọn nhiều từ máy");
        Button chooseAssetImage = Ui.button(this, "Chọn nhiều từ pool");

        LinearLayout.LayoutParams imageButtonParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1);
        imageButtonParams.setMargins(0, 0, Ui.dp(this, 6), 0);
        imageButtonRow.addView(chooseImage, imageButtonParams);

        LinearLayout.LayoutParams assetButtonParams = new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 1);
        assetButtonParams.setMargins(Ui.dp(this, 6), 0, 0, 0);
        imageButtonRow.addView(chooseAssetImage, assetButtonParams);

        root.addView(imageButtonRow, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, root, 8);

        name = Ui.input(this, "Tên sản phẩm");
        price = Ui.input(this, "Giá gốc");
        salePrice = Ui.input(this, "Giá khuyến mãi (để trống hoặc 0 nếu không sale)");
        image = Ui.input(this, "Icon hoặc đường dẫn ảnh");
        des = Ui.input(this, "Mô tả");
        brand = Ui.input(this, "Thương hiệu");
        spec = Ui.input(this, "Thông số");
        stock = Ui.input(this, "Tồn kho");

        price.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        salePrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        stock.setInputType(InputType.TYPE_CLASS_NUMBER);

        addInput(root, name);
        addInput(root, price);
        addInput(root, salePrice);
        addInput(root, image);
        addInput(root, des);
        addCategoryPicker(root, p == null ? 0 : p.categoryId);
        addInput(root, brand);
        addInput(root, spec);
        addInput(root, stock);

        Button save = Ui.button(this, "Lưu sản phẩm");
        root.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));

        ScrollView scrollView = Ui.scroll(this, root);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        scrollView.setPadding(0, 0, 0, Ui.dp(this, 150));
        setContentView(scrollView);

        if (p != null) {
            name.setText(p.name);
            price.setText(String.valueOf((long) p.price));
            salePrice.setText(p.salePrice > 0 ? String.valueOf((long) p.salePrice) : "");
            image.setText(p.image);
            selectedImageUri = p.image == null ? "" : p.image;
            selectedImages.clear();
            if (selectedImageUri != null && !selectedImageUri.trim().isEmpty()) {
                for (String part : selectedImageUri.split("\\|\\|")) selectedImages.add(part);
            }
            des.setText(p.description);
            brand.setText(p.brand);
            spec.setText(p.spec);
            stock.setText(String.valueOf(p.stock));
            renderPreview(selectedImageUri);
        }

        chooseImage.setOnClickListener(v -> pickImage());
        chooseAssetImage.setOnClickListener(v -> showAssetImagePicker());
        save.setOnClickListener(v -> save());
    }

    private void addInput(LinearLayout root, EditText editText) {
        root.addView(editText, new LinearLayout.LayoutParams(-1, Ui.dp(this, 50)));
        Ui.gap(this, root, 8);
    }

    private void addCategoryPicker(LinearLayout root, int currentCategoryId) {
        TextView label = Ui.text(this, "Danh mục sản phẩm", 14);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextColor(Ui.textColor(this));
        root.addView(label);

        categoryList.clear();
        categoryList.add(new Category(0, "Chọn danh mục sản phẩm", ""));
        try {
            categoryList.addAll(pc.categories());
        } catch (Exception ignored) {}

        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, categoryList) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                styleSpinnerText(view);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setText(categoryList.get(position).id == 0 ? categoryList.get(position).name : categoryList.get(position).name + "  •  ID " + categoryList.get(position).id);
                view.setTextColor(Color.parseColor("#111827"));
                view.setTextSize(15);
                view.setPadding(Ui.dp(AdminProductFormActivity.this, 14), Ui.dp(AdminProductFormActivity.this, 12), Ui.dp(AdminProductFormActivity.this, 14), Ui.dp(AdminProductFormActivity.this, 12));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cateSpinner = new Spinner(this);
        cateSpinner.setAdapter(adapter);
        cateSpinner.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
        cateSpinner.setBackground(Ui.round(this, Ui.cardBg(this), 14, Ui.strokeColor(this), 1));

        int selectedPosition = 0;
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).id == currentCategoryId) {
                selectedPosition = i;
                break;
            }
        }
        cateSpinner.setSelection(selectedPosition);
        selectedCategoryId = categoryList.get(selectedPosition).id;

        cateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long itemId) {
                selectedCategoryId = categoryList.get(position).id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = 0;
            }
        });

        root.addView(cateSpinner, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        Ui.gap(this, root, 8);
    }

    private void styleSpinnerText(TextView view) {
        if (view == null) return;
        int index = cateSpinner == null ? 0 : cateSpinner.getSelectedItemPosition();
        if (index >= 0 && index < categoryList.size()) {
            Category c = categoryList.get(index);
            view.setText(c.id == 0 ? c.name : c.name + "  •  ID " + c.id);
        }
        view.setTextColor(Ui.textColor(this));
        view.setTextSize(15);
        view.setSingleLine(true);
        view.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Chọn một hoặc nhiều ảnh sản phẩm"), PICK_PRODUCT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PRODUCT_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImages.clear();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(uri.toString());
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
                }
            } else if (data.getData() != null) {
                Uri uri = data.getData();
                selectedImages.add(uri.toString());
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
            }
            selectedImageUri = joinImages();
            image.setText(selectedImageUri);
            renderPreview(firstImage(selectedImageUri));
        }
    }

    private void showAssetImagePicker() {
        try {
            String[] files = getAssets().list("product_images");
            if (files == null || files.length == 0) {
                Ui.toast(this, "Chưa có ảnh trong app/src/main/assets/product_images");
                return;
            }

            final String[] imagePaths = new String[files.length];
            for (int i = 0; i < files.length; i++) imagePaths[i] = "asset://product_images/" + files[i];

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 8));

            TextView title = Ui.text(this, "Chọn ảnh có sẵn trong pool", 18);
            title.setTextColor(Color.parseColor("#111827"));
            content.addView(title);
            Ui.gap(this, content, 10);

            ScrollView scrollView = new ScrollView(this);
            LinearLayout grid = new LinearLayout(this);
            grid.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(grid);

            AlertDialog dialog = new AlertDialog.Builder(this).create();
            LinearLayout currentRow = null;
            int countInRow = 0;

            for (String path : imagePaths) {
                if (currentRow == null || countInRow == 2) {
                    currentRow = new LinearLayout(this);
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    grid.addView(currentRow, new LinearLayout.LayoutParams(-1, -2));
                    countInRow = 0;
                }

                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6), Ui.dp(this, 6));
                item.setBackground(Ui.round(this, Color.parseColor("#F3F4F6"), 16, Color.parseColor("#E5E7EB"), 1));

                ImageView assetPreview = new ImageView(this);
                assetPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                assetPreview.setAdjustViewBounds(true);
                loadAssetInto(assetPreview, path);
                item.addView(assetPreview, new LinearLayout.LayoutParams(-1, Ui.dp(this, 105)));

                TextView fileName = Ui.text(this, path.substring(path.lastIndexOf('/') + 1), 12);
                fileName.setSingleLine(true);
                fileName.setPadding(0, Ui.dp(this, 5), 0, 0);
                item.addView(fileName);

                item.setOnClickListener(v -> {
                    if (!selectedImages.contains(path)) selectedImages.add(path);
                    selectedImageUri = joinImages();
                    image.setText(selectedImageUri);
                    renderPreview(firstImage(selectedImageUri));
                    Ui.toast(this, "Đã thêm ảnh vào sản phẩm");
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
                params.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 8));
                currentRow.addView(item, params);
                countInRow++;
            }

            content.addView(scrollView, new LinearLayout.LayoutParams(-1, Ui.dp(this, 360)));
            Ui.gap(this, content, 8);
            Button cancel = Ui.darkButton(this, "Xong");
            cancel.setOnClickListener(v -> dialog.dismiss());
            content.addView(cancel, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

            dialog.setView(content);
            dialog.show();
        } catch (Exception e) {
            Ui.toast(this, "Không đọc được thư mục assets/product_images");
        }
    }

    private void renderPreview(String value) {
        String first = firstImage(value);
        if (first != null && first.startsWith("asset://")) {
            loadAssetInto(preview, first);
        } else if (first != null && (first.startsWith("content://") || first.startsWith("file://"))) {
            preview.setImageURI(Uri.parse(first));
        } else {
            preview.setImageDrawable(null);
        }
        renderPreviewStrip(value);
    }

    private void renderPreviewStrip(String value) {
        if (previewStrip == null) return;
        previewStrip.removeAllViews();
        ArrayList<String> images = splitImages(value);
        if (images.isEmpty()) {
            TextView empty = Ui.text(this, "Chưa chọn ảnh", 13);
            previewStrip.addView(empty, new LinearLayout.LayoutParams(-2, Ui.dp(this, 78)));
            return;
        }
        for (String img : images) {
            ImageView thumb = new ImageView(this);
            thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            thumb.setAdjustViewBounds(true);
            thumb.setBackground(Ui.round(this, Color.parseColor("#EEF2F7"), 14, Color.parseColor("#E5E7EB"), 1));
            if (img.startsWith("asset://")) {
                loadAssetInto(thumb, img);
            } else if (img.startsWith("content://") || img.startsWith("file://")) {
                thumb.setImageURI(Uri.parse(img));
            } else {
                thumb.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Ui.dp(this, 86), Ui.dp(this, 78));
            params.setMargins(0, 0, Ui.dp(this, 8), 0);
            previewStrip.addView(thumb, params);
        }
    }

    private String joinImages() {
        StringBuilder b = new StringBuilder();
        for (String img : selectedImages) {
            if (img != null && !img.trim().isEmpty()) {
                if (b.length() > 0) b.append("||");
                b.append(img);
            }
        }
        return b.toString();
    }

    private String firstImage(String value) {
        if (value == null) return "";
        String[] parts = value.split("\\|\\|");
        return parts.length > 0 ? parts[0] : value;
    }

    private ArrayList<String> splitImages(String value) {
        ArrayList<String> list = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) return list;
        for (String part : value.split("\\|\\|")) {
            String img = part == null ? "" : part.trim();
            if (!img.isEmpty()) list.add(img);
        }
        return list;
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

    private void save() {
        try {
            if (selectedCategoryId <= 0) {
                Ui.toast(this, "Vui lòng chọn danh mục sản phẩm");
                return;
            }

            String img = image.getText().toString().trim();
            double originalPrice = Double.parseDouble(price.getText().toString().trim());
            String saleRaw = salePrice.getText().toString().trim();
            double sale = saleRaw.isEmpty() ? 0 : Double.parseDouble(saleRaw);
            if (sale >= originalPrice) sale = 0;

            Product p = new Product(
                    id,
                    name.getText().toString().trim(),
                    originalPrice,
                    sale,
                    img,
                    des.getText().toString().trim(),
                    selectedCategoryId,
                    brand.getText().toString().trim(),
                    spec.getText().toString().trim(),
                    Integer.parseInt(stock.getText().toString().trim()),
                    sale > 0 ? 1 : 0,
                    1
            );
            pc.save(p);
            Ui.toast(this, "Đã lưu sản phẩm");
            finish();
        } catch (Exception e) {
            Ui.toast(this, "Kiểm tra lại dữ liệu nhập");
        }
    }
}
