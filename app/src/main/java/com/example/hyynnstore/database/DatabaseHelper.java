package com.example.hyynnstore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hyynnstore.model.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "HyynnStore.db";
    private static final int DB_VERSION = 8;

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, phone TEXT, password TEXT NOT NULL, avatar TEXT, address TEXT, role TEXT DEFAULT 'user', status TEXT DEFAULT 'active', lockReason TEXT)");
        db.execSQL("CREATE TABLE Categories(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, image TEXT)");
        db.execSQL("CREATE TABLE Products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, price REAL NOT NULL, image TEXT, description TEXT, categoryId INTEGER, brand TEXT, spec TEXT, stock INTEGER DEFAULT 0, isHot INTEGER DEFAULT 0, isNew INTEGER DEFAULT 0, salePrice REAL DEFAULT 0)");
        db.execSQL("CREATE TABLE Cart(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, quantity INTEGER DEFAULT 1, price REAL)");
        db.execSQL("CREATE TABLE Orders(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, date TEXT, totalAmount REAL, status TEXT, address TEXT, paymentMethod TEXT, adminDone INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE OrderDetails(id INTEGER PRIMARY KEY AUTOINCREMENT, orderId INTEGER, productId INTEGER, quantity INTEGER, price REAL)");
        db.execSQL("CREATE TABLE Reviews(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, rating INTEGER, comment TEXT, createdAt TEXT, adminReply TEXT, adminDone INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE ReviewImages(id INTEGER PRIMARY KEY AUTOINCREMENT, reviewId INTEGER, imageUri TEXT)");
        db.execSQL("CREATE TABLE Notifications(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT, type TEXT, createdAt TEXT, readAdmin INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE ProductReports(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, reason TEXT, createdAt TEXT, adminDone INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE Banners(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, imageUri TEXT NOT NULL, active INTEGER DEFAULT 1)");
        seed(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration an toàn: không DROP TABLE để tránh mất dữ liệu khi update giao diện/chức năng.
        safeExec(db, "CREATE TABLE IF NOT EXISTS Notifications(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT, type TEXT, createdAt TEXT, readAdmin INTEGER DEFAULT 0)");
        safeExec(db, "CREATE TABLE IF NOT EXISTS ProductReports(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, reason TEXT, createdAt TEXT, adminDone INTEGER DEFAULT 0)");
        safeExec(db, "CREATE TABLE IF NOT EXISTS ReviewImages(id INTEGER PRIMARY KEY AUTOINCREMENT, reviewId INTEGER, imageUri TEXT)");
        safeExec(db, "ALTER TABLE Users ADD COLUMN lockReason TEXT");
        safeExec(db, "ALTER TABLE Reviews ADD COLUMN adminReply TEXT");
        safeExec(db, "ALTER TABLE Orders ADD COLUMN adminDone INTEGER DEFAULT 0");
        safeExec(db, "ALTER TABLE Reviews ADD COLUMN adminDone INTEGER DEFAULT 0");
        safeExec(db, "ALTER TABLE ProductReports ADD COLUMN adminDone INTEGER DEFAULT 0");
        safeExec(db, "ALTER TABLE Notifications ADD COLUMN readAdmin INTEGER DEFAULT 0");
        safeExec(db, "ALTER TABLE Products ADD COLUMN salePrice REAL DEFAULT 0");
        safeExec(db, "CREATE TABLE IF NOT EXISTS Banners(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, imageUri TEXT NOT NULL, active INTEGER DEFAULT 1)");
    }

    private void safeExec(SQLiteDatabase db, String sql) {
        try { db.execSQL(sql); } catch (Exception ignored) { }
    }

    private void seed(SQLiteDatabase db) {
        db.execSQL("INSERT INTO Users(name,email,phone,password,address,role,status,lockReason) VALUES" +
                "('Admin Hyynn','admin@gmail.com','0900000000','123456','Hyynn Store','admin','active','')," +
                "('User Demo','user@gmail.com','0911111111','123456','TP.HCM','user','active','')");
        String[] cats = {"Laptop Gaming","PC Gaming","Màn hình","Bàn phím","Chuột","Tai nghe","Linh kiện PC","Phụ kiện"};
        for (String c : cats) { ContentValues v = new ContentValues(); v.put("name", c); v.put("image", ""); db.insert("Categories", null, v); }
        addProduct(db,"Laptop ASUS TUF Gaming F15",18990000,"💻","Laptop gaming hiệu năng cao cho học tập và chơi game.",1,"ASUS","Core i5 / RAM 16GB / SSD 512GB / RTX 4050",12,1,1);
        addProduct(db,"Laptop Lenovo Legion 5",25990000,"💻","Laptop gaming màn đẹp, tản nhiệt tốt.",1,"Lenovo","Ryzen 7 / RAM 16GB / SSD 1TB / RTX 4060",8,1,1);
        addProduct(db,"PC Gaming RTX 4060",22990000,"🖥️","Bộ PC chơi game 1080p/2K mượt.",2,"Hyynn Build","i5 13400F / RAM 16GB / RTX 4060 / SSD 1TB",5,1,0);
        addProduct(db,"PC Gaming RTX 4070 Super",38990000,"🖥️","Bộ PC cao cấp cho gaming và đồ họa.",2,"Hyynn Build","i7 / RAM 32GB / RTX 4070 Super / SSD 1TB",3,1,0);
        addProduct(db,"Màn hình ASUS TUF 27 inch 165Hz",5190000,"🖥️","Màn hình gaming tần số quét cao.",3,"ASUS","27 inch / IPS / 165Hz / 1ms",15,0,1);
        addProduct(db,"Màn hình LG UltraGear 24 inch",3590000,"🖥️","Màn hình gaming phổ thông giá tốt.",3,"LG","24 inch / IPS / 144Hz / Full HD",20,0,0);
        addProduct(db,"Bàn phím cơ AKKO 5075B",2190000,"⌨️","Bàn phím cơ layout gọn, gõ êm.",4,"AKKO","Bluetooth / Hotswap / RGB",10,1,0);
        addProduct(db,"Bàn phím Logitech G Pro",2490000,"⌨️","Bàn phím cơ gaming chuyên nghiệp.",4,"Logitech","Tenkeyless / GX Switch / RGB",9,0,0);
        addProduct(db,"Chuột Logitech G102",390000,"🖱️","Chuột gaming quốc dân giá rẻ.",5,"Logitech","8000 DPI / RGB / 6 nút",35,1,0);
        addProduct(db,"Chuột Razer DeathAdder V2",890000,"🖱️","Chuột gaming ergonomic cho FPS.",5,"Razer","20000 DPI / Optical Switch",17,0,1);
        addProduct(db,"Tai nghe HyperX Cloud II",1690000,"🎧","Tai nghe gaming âm thanh tốt, mic rõ.",6,"HyperX","7.1 Surround / USB Sound Card",14,1,0);
        addProduct(db,"SSD Samsung 980 500GB",1190000,"💾","SSD NVMe tốc độ cao.",7,"Samsung","NVMe PCIe 3.0 / 500GB",30,0,0);
        addProduct(db,"RAM Kingston Fury 16GB",990000,"🧩","RAM DDR4 hiệu năng ổn định.",7,"Kingston","16GB / DDR4 / 3200MHz",28,0,0);
        addProduct(db,"Ghế gaming E-Dra",2490000,"🪑","Ghế gaming ngồi học và chơi game thoải mái.",8,"E-Dra","Da PU / Ngả lưng / Gối cổ",6,0,1);
        addNotification(db,"Khuyến mãi Hyynn Store","Giảm giá laptop gaming và gear trong tuần này.","Khuyến mãi");
        addNotification(db,"Sản phẩm sale","Một số sản phẩm HOT đang có giá tốt, kiểm tra ngay trong trang chủ.","Sale");
        addBanner(db, "Sale công nghệ", "asset://banner_images/banner_sale_cong_nghe.png");
        addBanner(db, "Laptop Gaming", "asset://banner_images/banner_laptop_gaming.png");
    }

    private void addProduct(SQLiteDatabase db, String name, double price, String image, String des, int cate, String brand, String spec, int stock, int hot, int isNew) {
        ContentValues v = new ContentValues(); v.put("name",name); v.put("price",price); v.put("image",image); v.put("description",des); v.put("categoryId",cate); v.put("brand",brand); v.put("spec",spec); v.put("stock",stock); v.put("isHot",hot); v.put("isNew",isNew); db.insert("Products",null,v);
    }

    private void addNotification(SQLiteDatabase db, String title, String content, String type) {
        ContentValues v = new ContentValues(); v.put("title", title); v.put("content", content); v.put("type", type); v.put("createdAt", now()); v.put("readAdmin", 0); db.insert("Notifications", null, v);
    }

    private void addBanner(SQLiteDatabase db, String title, String imageUri) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("imageUri", imageUri);
        v.put("active", 1);
        db.insert("Banners", null, v);
    }

    public String now(){ return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()); }

    public User login(String email, String password) { Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Users WHERE email=? AND password=?", new String[]{email,password}); User u = null; if(c.moveToFirst()) u = readUser(c); c.close(); return u; }
    public boolean emailExists(String email){ Cursor c=getReadableDatabase().rawQuery("SELECT id FROM Users WHERE email=?",new String[]{email}); boolean b=c.moveToFirst(); c.close(); return b; }
    public long register(String name,String email,String phone,String pass){ ContentValues v=new ContentValues(); v.put("name",name);v.put("email",email);v.put("phone",phone);v.put("password",pass);v.put("address","");v.put("role","user");v.put("status","active");v.put("lockReason",""); return getWritableDatabase().insert("Users",null,v); }
    public boolean updatePassword(String email,String pass){ ContentValues v=new ContentValues(); v.put("password",pass); return getWritableDatabase().update("Users",v,"email=?",new String[]{email})>0; }
    public User getUser(int id){ Cursor c=getReadableDatabase().rawQuery("SELECT * FROM Users WHERE id=?",new String[]{String.valueOf(id)}); User u=null; if(c.moveToFirst()) u=readUser(c); c.close(); return u; }
    public boolean updateUser(User u){ ContentValues v=new ContentValues(); v.put("name",u.name);v.put("phone",u.phone);v.put("address",u.address); return getWritableDatabase().update("Users",v,"id=?",new String[]{String.valueOf(u.id)})>0; }
    public List<User> getAllUsers(){ List<User> list=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT * FROM Users ORDER BY id DESC",null); while(c.moveToNext()) list.add(readUser(c)); c.close(); return list; }
    public boolean toggleUserStatus(int id, String status){ ContentValues v=new ContentValues(); boolean lock=status.equals("active"); v.put("status",lock?"locked":"active"); if(!lock) v.put("lockReason",""); return getWritableDatabase().update("Users",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean lockUserWithReason(int id, String reason){ ContentValues v=new ContentValues(); v.put("status","locked"); v.put("lockReason",reason); return getWritableDatabase().update("Users",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean unlockUser(int id){ ContentValues v=new ContentValues(); v.put("status","active"); v.put("lockReason",""); return getWritableDatabase().update("Users",v,"id=?",new String[]{String.valueOf(id)})>0; }
    private User readUser(Cursor c){ String lock=""; int idx=c.getColumnIndex("lockReason"); if(idx>=0) lock=c.getString(idx); return new User(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("name")),c.getString(c.getColumnIndexOrThrow("email")),c.getString(c.getColumnIndexOrThrow("phone")),c.getString(c.getColumnIndexOrThrow("password")),c.getString(c.getColumnIndexOrThrow("avatar")),c.getString(c.getColumnIndexOrThrow("address")),c.getString(c.getColumnIndexOrThrow("role")),c.getString(c.getColumnIndexOrThrow("status")),lock==null?"":lock); }

    public List<Category> getCategories(){ List<Category> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT * FROM Categories",null); while(c.moveToNext()) l.add(new Category(c.getInt(0),c.getString(1),c.getString(2))); c.close(); return l; }
    public long addCategory(String name){ ContentValues v=new ContentValues(); v.put("name",name); v.put("image",""); return getWritableDatabase().insert("Categories",null,v); }
    public boolean updateCategory(int id,String name){ ContentValues v=new ContentValues(); v.put("name",name); return getWritableDatabase().update("Categories",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean deleteCategory(int id){ SQLiteDatabase db=getWritableDatabase(); ContentValues pv=new ContentValues(); pv.put("categoryId",0); db.update("Products",pv,"categoryId=?",new String[]{String.valueOf(id)}); return db.delete("Categories","id=?",new String[]{String.valueOf(id)})>0; }

    public List<Product> getProducts(String keyword, int categoryId){ List<Product> l=new ArrayList<>(); String sql="SELECT * FROM Products WHERE name LIKE ?"+(categoryId>0?" AND categoryId="+categoryId:"")+" ORDER BY id DESC"; Cursor c=getReadableDatabase().rawQuery(sql,new String[]{"%"+keyword+"%"}); while(c.moveToNext()) l.add(readProduct(c)); c.close(); return l; }
    public Product getProduct(int id){ Cursor c=getReadableDatabase().rawQuery("SELECT * FROM Products WHERE id=?",new String[]{String.valueOf(id)}); Product p=null; if(c.moveToFirst()) p=readProduct(c); c.close(); return p; }
    public long saveProduct(Product p){ ContentValues v=productValues(p); if(p.id>0) return getWritableDatabase().update("Products",v,"id=?",new String[]{String.valueOf(p.id)}); return getWritableDatabase().insert("Products",null,v); }
    public boolean deleteProduct(int id){ return getWritableDatabase().delete("Products","id=?",new String[]{String.valueOf(id)})>0; }
    private ContentValues productValues(Product p){ ContentValues v=new ContentValues(); v.put("name",p.name);v.put("price",p.price);v.put("image",p.image);v.put("description",p.description);v.put("categoryId",p.categoryId);v.put("brand",p.brand);v.put("spec",p.spec);v.put("stock",p.stock);v.put("isHot",p.isHot);v.put("isNew",p.isNew);v.put("salePrice",p.salePrice); return v; }
    private Product readProduct(Cursor c){ double salePrice = 0; int saleIndex = c.getColumnIndex("salePrice"); if (saleIndex >= 0 && !c.isNull(saleIndex)) salePrice = c.getDouble(saleIndex); return new Product(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("name")),c.getDouble(c.getColumnIndexOrThrow("price")),salePrice,c.getString(c.getColumnIndexOrThrow("image")),c.getString(c.getColumnIndexOrThrow("description")),c.getInt(c.getColumnIndexOrThrow("categoryId")),c.getString(c.getColumnIndexOrThrow("brand")),c.getString(c.getColumnIndexOrThrow("spec")),c.getInt(c.getColumnIndexOrThrow("stock")),c.getInt(c.getColumnIndexOrThrow("isHot")),c.getInt(c.getColumnIndexOrThrow("isNew"))); }

    public void addToCart(int userId,int productId,int qty,double price){ Cursor c=getReadableDatabase().rawQuery("SELECT id,quantity FROM Cart WHERE userId=? AND productId=?",new String[]{String.valueOf(userId),String.valueOf(productId)}); if(c.moveToFirst()){ int id=c.getInt(0); int old=c.getInt(1); ContentValues v=new ContentValues(); v.put("quantity",old+qty); v.put("price",price); getWritableDatabase().update("Cart",v,"id=?",new String[]{String.valueOf(id)}); } else { ContentValues v=new ContentValues(); v.put("userId",userId);v.put("productId",productId);v.put("quantity",qty);v.put("price",price); getWritableDatabase().insert("Cart",null,v); } c.close(); }
    public List<CartItem> getCart(int userId){ List<CartItem> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT c.id,c.userId,c.productId,c.quantity,c.price,p.name,p.image FROM Cart c JOIN Products p ON c.productId=p.id WHERE c.userId=?",new String[]{String.valueOf(userId)}); while(c.moveToNext()) l.add(new CartItem(c.getInt(0),c.getInt(1),c.getInt(2),c.getInt(3),c.getDouble(4),c.getString(5),c.getString(6))); c.close(); return l; }
    public boolean updateCartQty(int id,int qty){ if(qty<=0) return deleteCart(id); ContentValues v=new ContentValues(); v.put("quantity",qty); return getWritableDatabase().update("Cart",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean deleteCart(int id){ return getWritableDatabase().delete("Cart","id=?",new String[]{String.valueOf(id)})>0; }
    public double getCartTotal(int userId){ double t=0; for(CartItem i:getCart(userId)) t+=i.price*i.quantity; return t; }

    public long placeOrder(int userId,String address,String payment){ List<CartItem> cart=getCart(userId); if(cart.isEmpty()) return -1; SQLiteDatabase db=getWritableDatabase(); db.beginTransaction(); long orderId=-1; try{ double total=0; for(CartItem item:cart) total+=item.price*item.quantity; ContentValues o=new ContentValues(); o.put("userId",userId);o.put("date",now());o.put("totalAmount",total);o.put("status","Chờ xác nhận");o.put("address",address);o.put("paymentMethod",payment);o.put("adminDone",0); orderId=db.insert("Orders",null,o); if(orderId<=0) return -1; for(CartItem item:cart){ ContentValues d=new ContentValues(); d.put("orderId",orderId);d.put("productId",item.productId);d.put("quantity",item.quantity);d.put("price",item.price); db.insert("OrderDetails",null,d); } db.delete("Cart","userId=?",new String[]{String.valueOf(userId)}); db.setTransactionSuccessful(); } finally { db.endTransaction(); } return orderId; }
    public List<Order> getOrders(int userId, boolean all){ List<Order> l=new ArrayList<>(); String sql="SELECT o.*,u.name FROM Orders o JOIN Users u ON o.userId=u.id"+(all?"":" WHERE o.userId="+userId)+" ORDER BY o.id DESC"; Cursor c=getReadableDatabase().rawQuery(sql,null); while(c.moveToNext()) l.add(new Order(c.getInt(c.getColumnIndexOrThrow("id")),c.getInt(c.getColumnIndexOrThrow("userId")),c.getString(c.getColumnIndexOrThrow("date")),c.getDouble(c.getColumnIndexOrThrow("totalAmount")),c.getString(c.getColumnIndexOrThrow("status")),c.getString(c.getColumnIndexOrThrow("address")),c.getString(c.getColumnIndexOrThrow("paymentMethod")),c.getString(c.getColumnIndexOrThrow("name")))); c.close(); return l; }
    public boolean updateOrderStatus(int orderId,String status){ ContentValues v=new ContentValues(); v.put("status",status); return getWritableDatabase().update("Orders",v,"id=?",new String[]{String.valueOf(orderId)})>0; }

    public long addReview(int userId,int productId,int rating,String comment){ ContentValues v=new ContentValues(); v.put("userId",userId);v.put("productId",productId);v.put("rating",rating);v.put("comment",comment);v.put("createdAt",now()); v.put("adminReply",""); v.put("adminDone",0); return getWritableDatabase().insert("Reviews",null,v); }
    public void addReviewImage(long reviewId,String uri){ ContentValues v=new ContentValues(); v.put("reviewId",reviewId);v.put("imageUri",uri); getWritableDatabase().insert("ReviewImages",null,v); }
    public List<Review> getReviews(int productId){ List<Review> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT r.*,u.name AS userName,p.name AS productName FROM Reviews r JOIN Users u ON r.userId=u.id JOIN Products p ON r.productId=p.id WHERE r.productId=? ORDER BY r.id DESC",new String[]{String.valueOf(productId)}); while(c.moveToNext()) l.add(readReview(c)); c.close(); return l; }
    public List<Review> getAllReviews(){ List<Review> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT r.*,u.name AS userName,p.name AS productName FROM Reviews r JOIN Users u ON r.userId=u.id JOIN Products p ON r.productId=p.id ORDER BY r.id DESC",null); while(c.moveToNext()) l.add(readReview(c)); c.close(); return l; }
    private Review readReview(Cursor c){ String reply=""; int replyIdx=c.getColumnIndex("adminReply"); if(replyIdx>=0) reply=c.getString(replyIdx); return new Review(c.getInt(c.getColumnIndexOrThrow("id")),c.getInt(c.getColumnIndexOrThrow("userId")),c.getInt(c.getColumnIndexOrThrow("productId")),c.getInt(c.getColumnIndexOrThrow("rating")),c.getString(c.getColumnIndexOrThrow("comment")),c.getString(c.getColumnIndexOrThrow("createdAt")),c.getString(c.getColumnIndexOrThrow("userName")),c.getString(c.getColumnIndexOrThrow("productName")),reply==null?"":reply); }
    public boolean replyReview(int id, String reply){ ContentValues v=new ContentValues(); v.put("adminReply",reply); return getWritableDatabase().update("Reviews",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public List<ReviewImage> getReviewImages(int reviewId){ List<ReviewImage> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT * FROM ReviewImages WHERE reviewId=?",new String[]{String.valueOf(reviewId)}); while(c.moveToNext()) l.add(new ReviewImage(c.getInt(0),c.getInt(1),c.getString(2))); c.close(); return l; }
    public boolean deleteReview(int id){ SQLiteDatabase db=getWritableDatabase(); db.delete("ReviewImages","reviewId=?",new String[]{String.valueOf(id)}); return db.delete("Reviews","id=?",new String[]{String.valueOf(id)})>0; }
    public float avgRating(int productId){ Cursor c=getReadableDatabase().rawQuery("SELECT AVG(rating) FROM Reviews WHERE productId=?",new String[]{String.valueOf(productId)}); float a=0; if(c.moveToFirst()) a=c.isNull(0)?0:c.getFloat(0); c.close(); return a; }

    public long addNotification(String title,String content,String type){ ContentValues v=new ContentValues(); v.put("title",title); v.put("content",content); v.put("type",type); v.put("createdAt",now()); v.put("readAdmin",0); return getWritableDatabase().insert("Notifications",null,v); }
    public int unreadAdminNotifications(){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Notifications WHERE IFNULL(readAdmin,0)=0",null); int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n; }
    public void markAdminNotificationsRead(){ ContentValues v=new ContentValues(); v.put("readAdmin",1); getWritableDatabase().update("Notifications",v,null,null); }
    public List<NotificationItem> getNotifications(){ List<NotificationItem> l=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT * FROM Notifications ORDER BY id DESC",null); while(c.moveToNext()) l.add(new NotificationItem(c.getInt(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("title")),c.getString(c.getColumnIndexOrThrow("content")),c.getString(c.getColumnIndexOrThrow("type")),c.getString(c.getColumnIndexOrThrow("createdAt")))); c.close(); return l; }
    public boolean deleteNotification(int id){ return getWritableDatabase().delete("Notifications","id=?",new String[]{String.valueOf(id)})>0; }

    public long addProductReport(int userId, int productId, String reason){ ContentValues v=new ContentValues(); v.put("userId",userId); v.put("productId",productId); v.put("reason",reason); v.put("createdAt",now()); v.put("adminDone",0); return getWritableDatabase().insert("ProductReports",null,v); }

    public List<BannerItem> getBanners(boolean onlyActive){
        ensureSampleBanners();
        List<BannerItem> list = new ArrayList<>();
        String sql = "SELECT * FROM Banners" + (onlyActive ? " WHERE IFNULL(active,1)=1" : "") + " ORDER BY id DESC";
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        while(c.moveToNext()){
            list.add(new BannerItem(c.getInt(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("title")), c.getString(c.getColumnIndexOrThrow("imageUri")), c.getInt(c.getColumnIndexOrThrow("active"))));
        }
        c.close();
        return list;
    }

    private void ensureSampleBanners(){
        SQLiteDatabase db = getWritableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM Banners WHERE imageUri IS NOT NULL AND TRIM(imageUri)<>''", null);
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            if (count == 0) {
                db.delete("Banners", null, null);
                addBanner(db, "Sale công nghệ", "asset://banner_images/banner_sale_cong_nghe.png");
                addBanner(db, "Laptop Gaming", "asset://banner_images/banner_laptop_gaming.png");
            }
        } catch (Exception ignored) { }
    }

    public long saveBanner(BannerItem b){
        ContentValues v = new ContentValues();
        v.put("title", b.title == null ? "" : b.title);
        v.put("imageUri", b.imageUri == null ? "" : b.imageUri);
        v.put("active", b.active);
        if(b.id > 0) return getWritableDatabase().update("Banners", v, "id=?", new String[]{String.valueOf(b.id)});
        return getWritableDatabase().insert("Banners", null, v);
    }

    public boolean deleteBanner(int id){ return getWritableDatabase().delete("Banners", "id=?", new String[]{String.valueOf(id)}) > 0; }


    public int pendingOrdersCount(){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Orders WHERE IFNULL(adminDone,0)=0",null); int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n; }
    public int pendingReviewsCount(){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Reviews WHERE IFNULL(adminDone,0)=0",null); int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n; }
    public int pendingProductReportsCount(){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM ProductReports WHERE IFNULL(adminDone,0)=0",null); int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n; }

    public boolean markOrderAdminDone(int id){ ContentValues v=new ContentValues(); v.put("adminDone",1); return getWritableDatabase().update("Orders",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean markReviewAdminDone(int id){ ContentValues v=new ContentValues(); v.put("adminDone",1); return getWritableDatabase().update("Reviews",v,"id=?",new String[]{String.valueOf(id)})>0; }
    public boolean markProductReportAdminDone(int id){ ContentValues v=new ContentValues(); v.put("adminDone",1); return getWritableDatabase().update("ProductReports",v,"id=?",new String[]{String.valueOf(id)})>0; }

    public boolean isOrderAdminDone(int id){ Cursor c=getReadableDatabase().rawQuery("SELECT IFNULL(adminDone,0) FROM Orders WHERE id=?",new String[]{String.valueOf(id)}); boolean done=false; if(c.moveToFirst()) done=c.getInt(0)==1; c.close(); return done; }
    public boolean isReviewAdminDone(int id){ Cursor c=getReadableDatabase().rawQuery("SELECT IFNULL(adminDone,0) FROM Reviews WHERE id=?",new String[]{String.valueOf(id)}); boolean done=false; if(c.moveToFirst()) done=c.getInt(0)==1; c.close(); return done; }

    public int count(String table){ Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+table,null); int n=0; if(c.moveToFirst()) n=c.getInt(0); c.close(); return n; }
    public double revenue(){ Cursor c=getReadableDatabase().rawQuery("SELECT SUM(totalAmount) FROM Orders WHERE status='Hoàn thành'",null); double n=0; if(c.moveToFirst()) n=c.isNull(0)?0:c.getDouble(0); c.close(); return n; }
}
