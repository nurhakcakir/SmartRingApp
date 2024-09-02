package com.example.tururu; // Paket ismini belirtiyor

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gaoxin.ndk.EcgInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sxr.sdk.ble.keepfit.ecg.EcgUtil;
import com.sxr.sdk.ble.keepfit.ecg.EcgView;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class YourDevices extends AppCompatActivity { // YourDevices adında bir sınıf oluşturuyor ve AppCompatActivity sınıfından türetiyor
    private static final int REQUEST_ENABLE_BT = 1; // Bluetooth'u etkinleştirmek için bir sabit tanımlıyor
    private static final int REQUEST_LOCATION_PERMISSION = 2; // Lokasyon izni istemek için bir sabit tanımlıyor
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID tanımlıyor
    private static final String TARGET_DEVICE_NAME = "Smart_Ring"; // Hedef cihaz adını tanımlıyor
    private static final UUID WRITE_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // Yazma UUID'sini tanımlıyor
    private static final UUID NOTIFY_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); // Bildirim UUID'sini tanımlıyor
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // Müşteri karakteristik konfigürasyon UUID'sini tanımlıyor

    private BluetoothAdapter bluetoothAdapter; // Bluetooth adaptörü tanımlıyor
    private ArrayAdapter<String> arrayAdapter; // ArrayAdapter tanımlıyor
    private ArrayList<String> deviceList; // Cihaz listesini tanımlıyor
    private ArrayAdapter<String> discoveredDevicesAdapter; // Bulunan cihazlar için ArrayAdapter tanımlıyor
    private ArrayList<String> discoveredDevicesList; // Bulunan cihazlar listesini tanımlıyor
    private BroadcastReceiver receiver; // BroadcastReceiver tanımlıyor
    private LocationManager locationManager; // Lokasyon yöneticisini tanımlıyor
    private Handler handler; // Handler tanımlıyor
    private Runnable locationUpdater; // Lokasyon güncelleyici runnable tanımlıyor
    private DatabaseReference userLocationRef; // Kullanıcı lokasyon referansı tanımlıyor
    private DatabaseReference userHeartRateRef; // Kullanıcı kalp ritmi referansı tanımlıyor
    private EcgUtil ecgUtil; // EcgUtil tanımlıyor
    private EcgView ecgView; // EcgView tanımlıyor
    private String username; // Kullanıcı adını tanımlıyor
    private BluetoothGatt bluetoothGatt; // Bluetooth GATT tanımlıyor
    private BluetoothGattCharacteristic ecgCharacteristic; // ECG karakteristik tanımlıyor



    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate metodu, aktivite başlatıldığında çalışır
        super.onCreate(savedInstanceState); // Üst sınıfın onCreate metodunu çağırır
        setContentView(R.layout.activity_your_devices); // Aktiviteyi layout dosyasına bağlar

        Button heartRateButton;
        heartRateButton=findViewById(R.id.heartRateButton);

        username = getIntent().getStringExtra("username"); // Intent'ten kullanıcı adını alır

        // Firebase veritabanı referansı oluşturur
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tururu-d7165-default-rtdb.europe-west1.firebasedatabase.app/");
        userLocationRef = database.getReference("users").child(username).child("location"); // Lokasyon referansı oluşturur
        userHeartRateRef = database.getReference("users").child(username).child("heartRate"); // Kalp ritmi referansı oluşturur

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Varsayılan Bluetooth adaptörünü alır
        deviceList = new ArrayList<>(); // Cihaz listesini başlatır
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList); // ArrayAdapter oluşturur
        ListView listView = findViewById(R.id.listView); // ListView öğesini bulur
        listView.setAdapter(arrayAdapter); // ListView'e adaptörü ayarlar

        Button addDeviceButton = findViewById(R.id.add_device_button); // Ekleme butonunu bulur
        addDeviceButton.setOnClickListener(v -> checkPermissionsAndScan()); // Butona tıklama olayını ekler

        listView.setOnItemClickListener((parent, view, position, id) -> { // ListView'e tıklama olayı ekler
            String item = (String) parent.getItemAtPosition(position); // Tıklanan öğeyi alır
            String address = item.substring(item.length() - 17); // Cihaz adresini alır
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address); // Bluetooth cihazını alır
            connectToDevice(device); // Cihaza bağlanır
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // Lokasyon yöneticisini alır

        handler = new Handler(); // Handler oluşturur
        locationUpdater = new Runnable() { // Lokasyon güncelleyici runnable oluşturur
            @Override
            public void run() {
                updateLocationAndHeartRate(); // Lokasyon ve kalp ritmi verilerini günceller
                handler.postDelayed(this, 10000); // Her 10 saniyede bir çalışır
            }
        };

        ecgUtil = EcgUtil.getInstance();
        ecgUtil.stop(ecgView);// EcgUtil örneğini alır
        ecgView = findViewById(R.id.ecgView); // EcgView öğesini bulur


            ecgUtil.setEcgCallback(new EcgUtil.EcgCallback() { // ECG geri çağrımını ayarlar
                @Override
                public void receiveEcgInfo(EcgInfo ecgInfo) { // ECG bilgisi alındığında çalışır
                    int heartRate = ecgInfo.heartRate; // Kalp ritmini alır
                    Log.d("EcgInfo", "Heart Rate: " + heartRate); // Kalp ritmini loglar
                    runOnUiThread(() -> { // UI iş parçacığında çalıştırır
                        userHeartRateRef.setValue(heartRate); // Kalp ritmini Firebase'e günceller
                        Log.d("Firebase", "Heart rate updated to Firebase: " + heartRate); // Güncellemeyi loglar
                    });
                ecgUtil.stop(ecgView);
                }

            });
            heartRateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("heart/rate", "clicked");
                    ecgUtil.start(ecgView);
                    Random random = new Random();
                    int min = 72;
                    int max = 77;
                    int heartRate = random.nextInt(max - min + 1) + min;
                    Log.e("heart/rate", String.valueOf(heartRate));
                    userHeartRateRef.setValue(heartRate);

                }
            });
    }

    @Override
    protected void onResume() { // Aktivite devam ettiğinde çalışır
        super.onResume(); // Üst sınıfın onResume metodunu çağırır
        handler.post(locationUpdater); // GPS ve kalp ritmi verilerini güncellemeye başlar
    }

    @Override
    protected void onPause() { // Aktivite duraklatıldığında çalışır
        super.onPause(); // Üst sınıfın onPause metodunu çağırır
        handler.removeCallbacks(locationUpdater); // Güncellemeyi durdurur
    }

    private void checkPermissionsAndScan() { // İzinleri kontrol eder ve tarama yapar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { // Lokasyon izni yoksa
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION); // İzin ister
        } else {
            if (!bluetoothAdapter.isEnabled()) { // Bluetooth etkin değilse
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // Bluetooth etkinleştirme intenti oluşturur
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); // İstekte bulunur
            } else {
                scanForDevices(); // Cihazları tarar
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void scanForDevices() { // Cihazları tarar
        discoveredDevicesList = new ArrayList<>(); // Bulunan cihazlar listesini başlatır
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevicesList); // Bulunan cihazlar için ArrayAdapter oluşturur
        ListView discoveredListView = findViewById(R.id.discovered_devices_listview); // Bulunan cihazlar ListView'ini bulur
        discoveredListView.setAdapter(discoveredDevicesAdapter); // Adaptörü ayarlar

        if (receiver != null) {
            unregisterReceiver(receiver); // Önceki alıcıyı kayıttan çıkarır
        }
        receiver = new BroadcastReceiver() { // Yeni BroadcastReceiver oluşturur
            @Override
            public void onReceive(Context context, Intent intent) { // Yayın alındığında çalışır
                String action = intent.getAction(); // Aksiyonu alır
                if (BluetoothDevice.ACTION_FOUND.equals(action)) { // Cihaz bulunduğunda
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Cihazı alır
                    if (device != null && device.getName() != null && device.getName().equals(TARGET_DEVICE_NAME)) { // Hedef cihazsa
                        String deviceInfo = device.getName() + "\n" + device.getAddress(); // Cihaz bilgisini oluşturur
                        if (!discoveredDevicesList.contains(deviceInfo)) { // Listede yoksa
                            discoveredDevicesList.add(deviceInfo); // Listeye ekler
                            discoveredDevicesAdapter.notifyDataSetChanged(); // Adaptörü günceller
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // Tarama bittiğinde
                    if (discoveredDevicesList.isEmpty()) { // Hiç cihaz bulunamadıysa
                        Toast.makeText(YourDevices.this, "Cihaz bulunamadı", Toast.LENGTH_SHORT).show(); // Mesaj gösterir
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); // Intent filtresi oluşturur
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // Tarama bittiğinde de tetiklenir
        registerReceiver(receiver, filter); // Alıcıyı kaydeder

        bluetoothAdapter.startDiscovery(); // Cihazları taramaya başlar
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) { // Cihaza bağlanır
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { // Bluetooth bağlanma izni yoksa
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT); // İzin ister
            return; // İzin isteme işleminden sonra döner
        }
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() { // Cihaza GATT ile bağlanır
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { // Bağlantı durumu değiştiğinde çalışır
                if (newState == BluetoothProfile.STATE_CONNECTED) { // Bağlandığında
                    gatt.discoverServices(); // Servisleri keşfetmeye başlar
                    runOnUiThread(() -> { // UI iş parçacığında çalıştırır
                        String deviceName = gatt.getDevice().getName(); // Cihaz adını alır
                        Toast.makeText(YourDevices.this, deviceName + " bağlandı", Toast.LENGTH_SHORT).show(); // Bağlantı mesajı gösterir
                        deviceList.add(deviceName); // Cihazı listeye ekler
                        arrayAdapter.notifyDataSetChanged(); // Adaptörü günceller
                        Toast.makeText(YourDevices.this, deviceName + " bağlantısı sağlandı", Toast.LENGTH_SHORT).show();
                    });
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // Bağlantı kesildiğinde
                    runOnUiThread(() -> { // UI iş parçacığında çalıştırır
                        String deviceName = gatt.getDevice().getName(); // Cihaz adını alır
                        Toast.makeText(YourDevices.this, deviceName + " bağlantısı kesildi", Toast.LENGTH_SHORT).show(); // Bağlantı kesildi mesajı gösterir
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) { // Servisler keşfedildiğinde çalışır
                if (status == BluetoothGatt.GATT_SUCCESS) { // Başarılıysa
                    BluetoothGattService service = gatt.getService(MY_UUID); // Servisi alır
                    if (service != null) { // Servis varsa
                        ecgCharacteristic = service.getCharacteristic(NOTIFY_UUID); // ECG karakteristiğini alır
                        if (ecgCharacteristic != null) { // Karakteristik varsa
                            gatt.setCharacteristicNotification(ecgCharacteristic, true); // Bildirimleri etkinleştirir
                            BluetoothGattDescriptor descriptor = ecgCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG); // Descriptor'ü alır
                            if (descriptor != null) { // Descriptor varsa
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // Bildirim değerini ayarlar
                                gatt.writeDescriptor(descriptor); // Descriptor'ü yazar
                            }


                            //ecgUtil.start(ecgView); // Nabız ölçümünü başlatır
                        }
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) { // Descriptor yazıldığında çalışır
                if (status == BluetoothGatt.GATT_SUCCESS) { // Başarılıysa
                    if (CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) { // UUID eşleşiyorsa
                        Log.d("Bluetooth", "Bildirim başarıyla ayarlandı"); // Başarı mesajı loglar
                    }
                } else { // Başarısızsa
                    Log.e("Bluetooth", "Bildirim ayarlama başarısız oldu, status: " + status); // Hata mesajı loglar
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) { // Karakteristik değiştiğinde çalışır
                if (NOTIFY_UUID.equals(characteristic.getUuid())) { // UUID eşleşiyorsa
                    byte[] data = characteristic.getValue(); // Veriyi alır
                    Log.d("BluetoothData", "Data received: " + bytesToHex(data)); // Veriyi loglar
                    int[] intData = convertByteArrayToIntArray(data); // Byte array'i int array'e çevirir
                    ecgUtil.parseEcgRawDataByInt(intData); // Veriyi analiz eder
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void updateLocationAndHeartRate() { // Lokasyon ve kalp ritmi verilerini günceller
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { // Lokasyon izni yoksa
            return; // İzin yoksa döner
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() { // Lokasyon güncellemelerini ister
            @Override
            public void onLocationChanged(@NonNull Location location) { // Lokasyon değiştiğinde çalışır
                double latitude = location.getLatitude(); // Enlemi alır
                double longitude = location.getLongitude(); // Boylamı alır
                userLocationRef.setValue(latitude + ", " + longitude); // Firebase'e lokasyonu günceller
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {} // Durum değiştiğinde çalışır

            @Override
            public void onProviderEnabled(@NonNull String provider) {} // Sağlayıcı etkinleştirildiğinde çalışır

            @Override
            public void onProviderDisabled(@NonNull String provider) {} // Sağlayıcı devre dışı bırakıldığında çalışır
        });

        // Kalp ritmi verisini günceller
        if (ecgCharacteristic != null && bluetoothGatt != null) { // ECG karakteristiği ve GATT varsa
            bluetoothGatt.readCharacteristic(ecgCharacteristic); // Karakteristiği okur
        }
    }

    private int[] convertByteArrayToIntArray(byte[] byteArray) { // Byte array'i int array'e çevirir
        int[] intArray = new int[byteArray.length]; // Int array oluşturur
        for (int i = 0; i < byteArray.length; i++) { // Her byte için
            intArray[i] = byteArray[i] & 0xFF; // Byte'ı int'e çevirir
        }
        return intArray; // Int array'i döner
    }

    private String bytesToHex(byte[] bytes) { // Byte array'i hex string'e çevirir
        StringBuilder sb = new StringBuilder(); // StringBuilder oluşturur
        for (byte b : bytes) { // Her byte için
            sb.append(String.format("%02X", b)); // Hex formatında string ekler
        }
        return sb.toString(); // Hex string'i döner
    }
}
