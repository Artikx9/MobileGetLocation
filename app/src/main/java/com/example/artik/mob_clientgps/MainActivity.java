package com.example.artik.mob_clientgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.Manifest;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    static public final int REQUEST_LOCATION = 1;  // идентификации локации запроса на разрешение
    EditText txtLongitude, txtLatitude;
    EditText txtId, txtkey;
    TextView txtResponse;
    Button button;
    LocationListener listener;

    static Double Latitude, Longitude;  //переменные хранение координат
    static String HASH; // переменная для хранения кэша
    static String idDevice;  //переменная для хранения ИД устройства
    static SecretKeySpec sks;  // Переменная для хранения сгенерированого секретного ключа
    static String key = "qwertyuiopasdfghjklzxcvbnmqwerty"; // Здесь можно задать статический ключ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtLatitude = findViewById(R.id.txtLatitude);    // Связываем поля с объектами активити
        txtLongitude = findViewById(R.id.txtLongitude);
        txtId = findViewById(R.id.idAndroid);
        txtkey = findViewById(R.id.keyAndroid);
        txtResponse = findViewById(R.id.txtResponse);
        button = findViewById(R.id.btnGet);

       //Проверка, предоставлено ли разрешение или запрашивается ли оно с использованием ранее определенной константы
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Check permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);

            } else {

            }
            return;
        }


        //Получение ключа и ИД устроства и вывод их
        idDevice = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        txtId.setText(idDevice);
        GenKey();
        txtkey.setText(Base64.encodeToString(sks.getEncoded(), Base64.NO_WRAP));

        //Слушатель нажатия кнопки
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                txtLatitude.setText("Широта= " + Latitude);        //вывод широты
                txtLongitude.setText("Долгота= " + Longitude);   //вывод долготы
                String str = "http://192.168.43.174:3000/api/stuff?id=" + idDevice + "&encdata=" + Encryption();  //формирование строки запроса
                URL url = null;
                try {
                    url = new URL(str);   //создание URL для отправки GET запроса
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) url.openConnection(); // Отправка GET запроса
                    txtResponse.setText(con.getRequestMethod());   //вывод метода запроса
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        button.setOnClickListener(onClick);
    }

    @SuppressLint("NewApi")        //Функция генерации ключа
    static void GenKey() {
        // Set up secret key spec for 128-bit AES encryption and decryption
        sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");  //генерация  криптографических случайных чисел с расширяемым входом  SHA1PRNG
            sr.setSeed("any data used as random seed".getBytes()); //дополянем существющее семя для увеличение случайности
            KeyGenerator kg = KeyGenerator.getInstance("AES");    //указываем  алгориnм  шифрования   AES
            kg.init(256, sr); //указываем размер 256 бит
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES"); // создаем ключ
        } catch (Exception e) {
            Log.e("KEY GEN", "AES secret key spec error");
        }

    }

    @SuppressLint("NewApi") //функция получения кэша
    static String GetHash() {
        Calendar dating = Calendar.getInstance(); ///инцилизируем календарь
        SimpleDateFormat formating = new SimpleDateFormat("HH:mm");  //получаем строку
        String str = "time:" + formating.format(dating.getTime())
                + ",lat:" + Latitude
                + ",lng:" + Longitude;

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256"); // создает объетк хэш функции используя, устанавливаем алгорит SHA-256
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        md.update(str.getBytes(StandardCharsets.UTF_8)); // Change this to UTF-16 if needed
        byte[] digest = md.digest();  //завершаем вычисление кэша

        String hex = String.format("%064x", new BigInteger(1, digest)).toUpperCase(); //строку хэша переводим в верхний регистр
        HASH = hex;
        return hex;
    }
    //функция шифрования
    static String Encryption() {
        Calendar dating = Calendar.getInstance();
        SimpleDateFormat formating = new SimpleDateFormat("HH:mm");
        String txtEncod = "time:" + formating.format(dating.getTime())
                + ",lat:" + Latitude + ",lng:" + Longitude + ",hash:" + GetHash();  //получаем новую строку вызывая метод хэширования
        byte[] newIv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //создаем массив 0 для вектора инцилизации
        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");  //получаем статический ключ используя Алгоритм AES
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); //создаем класс шифрования, устанавливаем AES - это блок-шифр,CBC - это режим блочного шифрования,PKCS5Padding обработка неполного блолка
            IvParameterSpec iv = new IvParameterSpec(newIv); //создаем вектор инициализации
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv); //иницилизируем этот шифр ключем
            encodedBytes = c.doFinal(txtEncod.getBytes()); //Завершает операцию шифрования
        } catch (Exception e) {
            Log.e("AES CRYPT", "AES encryption error");
        }

        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);  //переводи в Base64
    }

  //функция получения текущих координат
    private void GetLocation() {
        final LocationManager locationManager = (LocationManager)   //создаем менджер локаций
                getSystemService(Context.LOCATION_SERVICE);
        //Проверяем доступ на запрос к местопложению
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //получаем долготу и широту
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        //проверка разрешений на использовании локации
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener); //привязывай слушатель и менджер локации
    }

    @Override    //перегружаем метод запроса разрешения
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GetLocation();  //если пользователь резрешил вызываем метод получения координат
                Intent intent =  new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent); //перезапускаем активити
            }

        }

    }
}
