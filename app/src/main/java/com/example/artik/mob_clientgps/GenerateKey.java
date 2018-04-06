package com.example.artik.mob_clientgps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;



/**
 * Created by artik on 06.04.2018.
 */

public class GenerateKey {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int MODE_PRIVATE = 0;
    private static final String PREFS_CHECK = "check";
    private static final String PREFS_NAME= "checker";

    private static final String PREFS_KEY = "key";

    private static SecretKeySpec sks;


    public GenerateKey(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(PREFS_CHECK, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(PREFS_CHECK, true);
    }
    public String getKey()
    {
        return pref.getString(PREFS_KEY,"");
    }

    @SuppressLint("NewApi")        //Функция генерации ключа
    public void GenKey() {

        sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");  //генерация  криптографических случайных чисел с расширяемым входом  SHA1PRNG
            sr.setSeed("data used as random seed".getBytes()); //дополянем существющее семя для увеличение случайности
            KeyGenerator kg = KeyGenerator.getInstance("AES");    //указываем  алгориnм  шифрования   AES
            kg.init(192, sr); //указываем размер
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES"); // создаем ключ
        } catch (Exception e) {
            Log.e("KEY GEN", "AES secret key spec error");
        }

        editor.putString(PREFS_KEY, Base64.encodeToString(sks.getEncoded(), Base64.NO_WRAP));
        setFirstTimeLaunch(false);
    }
}
