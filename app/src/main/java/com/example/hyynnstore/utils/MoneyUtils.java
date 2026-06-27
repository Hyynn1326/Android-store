package com.example.hyynnstore.utils;
import java.text.NumberFormat;import java.util.Locale;
public class MoneyUtils { public static String vnd(double n){ return NumberFormat.getInstance(new Locale("vi","VN")).format(n)+" đ"; } public static String stars(int r){ String s=""; for(int i=1;i<=5;i++) s+=i<=r?"★":"☆"; return s; } }
