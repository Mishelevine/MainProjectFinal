package org.hse.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity
        implements PermissionManager.PermissionCallback, SensorHelper.SensorCallback {

    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int CAMERA_PERMISSION_CODE = 1001;

    private ImageView imageView;
    private EditText editTextName;
    private TextView lightData;
    private TextView sensorsListLabel;
    private Button btnTakePhoto;
    private Button btnSave;

    private PermissionManager permissionManager;
    private SensorHelper sensorHelper;

    private String currentPhotoPath;

    private Uri tempPhotoUri;
    private boolean hasUnsavedPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize managers
        permissionManager = new PermissionManager(this, this);
        sensorHelper = new SensorHelper(this, this);

        // Initialize UI components
        initUI();
        setupButtons();
        loadSavedData();
        setupSensorsList();
    }

    private void initUI() {
        imageView = findViewById(R.id.imageView);
        editTextName = findViewById(R.id.editTextName);
        lightData = findViewById(R.id.lightData);
        sensorsListLabel = findViewById(R.id.textViewSensors);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupButtons() {
        btnTakePhoto.setOnClickListener(v ->
                permissionManager.checkPermission(CAMERA_PERMISSION, CAMERA_PERMISSION_CODE));

        btnSave.setOnClickListener(v -> {
            saveUserName();
            savePhoto();
        });
    }

    private void loadSavedData() {
        // Load saved username
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editTextName.setText(prefs.getString("username", ""));

        // Load saved photo
        currentPhotoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/avatar.jpg";
        File photoFile = new File(currentPhotoPath);
        if (photoFile.exists()) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
        }
    }

    private void setupSensorsList() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorsListLabel.setText(SensorHelper.getSensorList(sensorManager));
    }

    private void saveUserName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("username", editTextName.getText().toString())
                .apply();

        Toast.makeText(this, "Имя сохранено", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionGranted() {
        dispatchTakePictureIntent();
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, "Для съемки фото требуется доступ к камере", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLightSensorChanged(float lux) {
        runOnUiThread(() -> lightData.setText(lux + " lux"));
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createTempImageFile();
            currentPhotoPath = photoFile.getAbsolutePath();

            // Явно сохраняем URI в переменную класса
            tempPhotoUri = FileProvider.getUriForFile(
                    this,
                    "org.hse.android.provider",
                    photoFile
            );

            Log.d(TAG, "Generated URI: " + tempPhotoUri);

            if (tempPhotoUri == null) {
                throw new IOException("Failed to generate photo URI");
            }

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Предоставляем временные разрешения
            List<ResolveInfo> resolvedIntentActivities = getPackageManager()
                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                grantUriPermission(
                        resolvedIntentInfo.activityInfo.packageName,
                        tempPhotoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            }

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        } catch (Exception e) {
            Log.e(TAG, "Camera initialization failed: " + e.getMessage());
            Toast.makeText(this, "Ошибка инициализации камеры: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private File createTempImageFile() throws IOException {
        File storageDir = new File(getFilesDir(), "temp_photos");

        // Гарантированное создание директории
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Log.e(TAG, "Directory creation failed: " + storageDir.getAbsolutePath());
            throw new IOException("Cannot create temp directory");
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "TEMP_" + timeStamp + ".jpg";

        File outputFile = new File(storageDir, imageFileName);

        // Явное создание пустого файла
        if (!outputFile.createNewFile()) {
            Log.e(TAG, "File creation failed: " + outputFile.getAbsolutePath());
            throw new IOException("Temp file creation failed");
        }

        Log.d(TAG, "Temp file created: " + outputFile.getAbsolutePath());
        return outputFile;
    }

    private void savePhoto() {
        if (tempPhotoUri == null || !hasUnsavedPhoto) return;

        try {
            // Постоянное хранилище
            File permanentFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "avatar.jpg");

            // Копируем из временного в постоянное
            try (InputStream in = getContentResolver().openInputStream(tempPhotoUri);
                 OutputStream out = new FileOutputStream(permanentFile)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            // Обновляем текущий путь
            currentPhotoPath = permanentFile.getAbsolutePath();
            hasUnsavedPhoto = false;
            Toast.makeText(this, "Фото сохранено", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error saving photo", e);
            Toast.makeText(this, "Ошибка сохранения фото", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (tempPhotoUri == null) {
                Toast.makeText(this, "Фото не было сохранено", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                // Загрузка через FileDescriptor
                ParcelFileDescriptor parcelFileDescriptor =
                        getContentResolver().openFileDescriptor(tempPhotoUri, "r");

                assert parcelFileDescriptor != null;
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    hasUnsavedPhoto = true;
                } else {
                    throw new IOException("Failed to decode bitmap");
                }

            } catch (Exception e) {
                Log.e(TAG, "Photo loading error: " + e.getMessage());
                Toast.makeText(this, "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    // endregion

    // region Lifecycle
    @Override
    protected void onResume() {
        super.onResume();
        sensorHelper.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorHelper.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.handleResult(requestCode, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tempPhotoUri != null) {
            File tempFile = new File(Objects.requireNonNull(tempPhotoUri.getPath()));
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}