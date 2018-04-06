package com.example.artik.mob_clientgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    static public final int REQUEST_LOCATION = 1;  // идентификации локации запроса на разрешение
    EditText txtLongitude, txtLatitude, txtHost;
    EditText txtId, txtkey;
    TextView txtResponse;
    Button button;
    GenerateKey generateKey;

    private LocationListener  listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //получаем долготу и широту
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
            if(location != null)
            {
                txtLatitude.setText("Широта= " + Latitude);        //вывод широты
                txtLongitude.setText("Долгота= " + Longitude);   //вывод долготы
            }
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
    }; //слушаем полуение местоположения



    static Double Latitude, Longitude;  //переменные хранение координат
    static String HASH; // переменная для хранения кэша
    static String idDevice;  //переменная для хранения ИД устройства
    static String host="25.56.107.197:3000";
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
        txtHost = findViewById(R.id.idHost);
        button = findViewById(R.id.btnGet);

       //Проверка, предоставлено ли разрешение или запрашивается ли оно с использованием ранее определенной константы
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Check permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);

            }
            return;
        }
        final LocationManager locationManager = (LocationManager)   //создаем менджер локаций
                getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener); //привязывай слушатель и менджер локации

        generateKey = new GenerateKey(this);
        if (generateKey.isFirstTimeLaunch()) {
            generateKey.GenKey();
        }
        //Получение ключа и ИД устроства и вывод их

        idDevice = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        txtId.setText(idDevice);
        txtkey.setText(generateKey.getKey());
        txtHost.setText(host);

        //Слушатель нажатия кнопки
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new GetAsync().execute(); //создаем  и вызываем 2 поток
            }
        };

        button.setOnClickListener(onClick);
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
    private String Encryption() {
        Calendar dating = Calendar.getInstance();
        SimpleDateFormat formating = new SimpleDateFormat("HH:mm");
        String txtEncod = "time:" + formating.format(dating.getTime())
                + ",lat:" + Latitude + ",lng:" + Longitude + ",hash:" + GetHash();  //получаем новую строку вызывая метод хэширования
        byte[] newIv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //создаем массив 0 для вектора инцилизации
        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decode(generateKey.getKey().getBytes(),Base64.NO_WRAP), "AES");  //получаем статический ключ используя Алгоритм AES
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); //создаем класс шифрования, устанавливаем AES - это блок-шифр,CBC - это режим блочного шифрования,PKCS5Padding обработка неполного блолка
            IvParameterSpec iv = new IvParameterSpec(newIv); //создаем вектор инициализации
            c.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv); //иницилизируем этот шифр ключем
            encodedBytes = c.doFinal(txtEncod.getBytes()); //Завершает операцию шифрования
        } catch (Exception e) {
            Log.e("AES CRYPT", "AES encryption error");
        }

        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);  //переводи в Base64
    }



    @Override    //перегружаем метод запроса разрешения
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent =  new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent); //перезапускаем активити
            }

        }

    }


    private void SendGet() throws IOException {
        String str = "http://"+host+"/api/stuff?id=" + idDevice + "&encdata=" + Encryption();  //формирование строки запроса
        URL url = new URL(str);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(); // Отправка GET запроса
        if(con.getResponseCode()==HttpURLConnection.HTTP_OK)
        {

        }
    } //функция оптравки  Get запроса

    class GetAsync extends AsyncTask<Void,Void,Void> //класс 2 потока
    {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SendGet(); //вызываем фукнкцию отправки
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
