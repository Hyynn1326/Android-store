package com.example.hyynnstore.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "hyynn_session";
    private final SharedPreferences sp;
    public SessionManager(Context c){ sp=c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }
    public void save(int userId, String role){ sp.edit().putInt("userId",userId).putString("role",role).apply(); }
    public int userId(){ return sp.getInt("userId",-1); }
    public String role(){ return sp.getString("role",""); }
    public boolean logged(){ return userId()>0; }
    public void logout(){ sp.edit().clear().apply(); }
}
