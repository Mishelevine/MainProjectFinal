package org.hse.android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class SettingsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int CAMERA_PERMISSION_CODE = 1001;

    private static final String PREF_USERNAME = "username";
    private static final String PREF_AVATAR_PATH = "avatar_path";

    private ImageView imageView;
    private EditText editTextName;
    private TextView lightData;
    private TextView sensorsListLabel;
    private Button btnTakePhoto;
    private Button btnSave;

    private SensorManager sensorManager;
    private Sensor lightSensor;

    private String currentPhotoPath;
    private Uri tempPhotoUri;
    private String tempPhotoPath;
    private boolean hasUnsavedPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (lightSensor == null) {
                Log.w(TAG, "Light sensor not available.");
            }
        } else {
            Log.e(TAG, "SensorManager is not available.");
        }

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
                checkCameraPermission());

        btnSave.setOnClickListener(v -> {
            saveUserName();
            savePhoto();
        });
    }

    private void loadSavedData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        editTextName.setText(prefs.getString(PREF_USERNAME, ""));

        currentPhotoPath = prefs.getString(PREF_AVATAR_PATH, null);
        if (currentPhotoPath != null) {
            File photoFile = new File(currentPhotoPath);
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e(TAG, "Failed to decode saved avatar: " + currentPhotoPath);
                    prefs.edit().remove(PREF_AVATAR_PATH).apply();
                    currentPhotoPath = null;
                }
            } else {
                Log.w(TAG, "Saved avatar file not found: " + currentPhotoPath);
                prefs.edit().remove(PREF_AVATAR_PATH).apply();
                currentPhotoPath = null;
            }
        }
    }

    private void setupSensorsList() {
        if (sensorManager != null) {
            sensorsListLabel.setText(getSensorListString(sensorManager));
        } else {
            sensorsListLabel.setText(getString(R.string.sensor_manager_inaccessibility));
        }
    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION)) {
                showPermissionExplanationDialog();
            } else {
                requestCameraPermission();
            }
        } else {
            onPermissionGranted();
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permission_dialog_title))
                .setMessage(getString(R.string.camera_permission_message))
                .setPositiveButton(getString(R.string.request_accept_button), (dialog, which) ->
                        requestCameraPermission())
                .setNegativeButton(getString(R.string.request_deny_button), (dialog, which) ->
                        onPermissionDenied())
                .show();
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{CAMERA_PERMISSION},
                CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted();
            } else {
                onPermissionDenied();
            }
        }
    }

    private void onPermissionGranted() {
        Log.d(TAG, "Camera permission granted.");
        dispatchTakePictureIntent();
    }

    private void onPermissionDenied() {
        Log.w(TAG, "Camera permission denied.");
        Toast.makeText(this, getText(R.string.camera_request_denied_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            runOnUiThread(() -> lightData.setText(String.format(Locale.getDefault(), "%.1f lux", lux)));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for sensor " + sensor.getName() + ": " + accuracy);
    }

    private String getSensorListString(SensorManager manager) {
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder builder = new StringBuilder(getString(R.string.found_sensors_header) + "\n");
        if (sensors.isEmpty()) {
            builder.append(getString(R.string.sensors_not_found_message));
        } else {
            for (Sensor s : sensors) {
                builder.append(" • ").append(s.getName()).append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null && sensorManager != null) {
            sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
            Log.d(TAG, "Light sensor listener registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Sensor listener unregistered.");
        }
    }

    private void saveUserName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString(PREF_USERNAME, editTextName.getText().toString())
                .apply();

        Toast.makeText(this, getText(R.string.name_saved_message), Toast.LENGTH_SHORT).show();
    }

    private void savePhoto() {
        if (tempPhotoUri == null || !hasUnsavedPhoto) {
            if (hasUnsavedPhoto) {
                Log.e(TAG, "Cannot save photo, temp URI is null but hasUnsavedPhoto is true.");
            } else {
                Log.d(TAG, "No new photo to save.");
            }
            return;
        }

        File permanentStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (permanentStorageDir == null) {
            Log.e(TAG, "External picture directory not available.");
            return;
        }
        File permanentFile = new File(permanentStorageDir, "avatar_" + System.currentTimeMillis() + ".jpg");

        try (InputStream in = getContentResolver().openInputStream(tempPhotoUri);
             OutputStream out = new FileOutputStream(permanentFile)) {

            if (in == null) {
                throw new IOException("Cannot open input stream for temporary photo URI: " + tempPhotoUri);
            }

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            if (currentPhotoPath != null) {
                File oldFile = new File(currentPhotoPath);
                if (oldFile.exists() && !oldFile.delete()) {
                    Log.w(TAG, "Could not delete old avatar: " + currentPhotoPath);
                }
            }

            currentPhotoPath = permanentFile.getAbsolutePath();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_AVATAR_PATH, currentPhotoPath).apply();

            hasUnsavedPhoto = false;
            Log.d(TAG, "Photo saved successfully to: " + currentPhotoPath);
            Toast.makeText(this, getText(R.string.photo_saved_message), Toast.LENGTH_SHORT).show();

            deleteTempPhotoFile();


        } catch (IOException e) {
            Log.e(TAG, "Error saving photo from " + tempPhotoUri + " to " + permanentFile.getAbsolutePath(), e);
            Toast.makeText(this, getText(R.string.saving_photo_error), Toast.LENGTH_SHORT).show();
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createTempImageFile();
                tempPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating temporary image file", ex);
                return;
            }

            tempPhotoUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile
            );

            Log.d(TAG, "Temporary photo URI generated: " + tempPhotoUri);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhotoUri);

            List<ResolveInfo> resolvedIntentActivities = getPackageManager()
                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                grantUriPermission(
                        packageName,
                        tempPhotoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                Log.d(TAG, "Granted URI permission to: " + packageName);
            }

            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start camera activity", e);
                tempPhotoUri = null;
                tempPhotoPath = null;
                deleteTempPhotoFile();
            }
        } else {
            Log.e(TAG, "No camera app found to handle intent.");
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "TEMP_JPEG_" + timeStamp + "_";

        File storageDir = getCacheDir();

        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        Log.d(TAG, "Temporary image file created at: " + image.getAbsolutePath());
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (tempPhotoUri != null) {
                    Log.d(TAG, "Camera returned RESULT_OK. Temp URI: " + tempPhotoUri);
                    try {
                        ParcelFileDescriptor parcelFileDescriptor =
                                getContentResolver().openFileDescriptor(tempPhotoUri, "r");
                        if (parcelFileDescriptor != null) {
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            Bitmap imageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                            parcelFileDescriptor.close();

                            if (imageBitmap != null) {
                                imageView.setImageBitmap(imageBitmap);
                                hasUnsavedPhoto = true;
                                Log.d(TAG, "Bitmap loaded from temp URI and set to ImageView.");
                            } else {
                                throw new IOException("Failed to decode bitmap from FileDescriptor.");
                            }
                        } else {
                            throw new IOException("Could not open FileDescriptor for URI: " + tempPhotoUri);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading bitmap from temporary URI: " + tempPhotoUri, e);
                        tempPhotoUri = null;
                        tempPhotoPath = null;
                        hasUnsavedPhoto = false;
                        deleteTempPhotoFile();
                    }
                } else {
                    Log.e(TAG, "Camera returned RESULT_OK, but tempPhotoUri is null!");
                    hasUnsavedPhoto = false;
                    tempPhotoPath = null;
                    deleteTempPhotoFile();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Camera action canceled by user.");
                tempPhotoUri = null;
                tempPhotoPath = null;
                hasUnsavedPhoto = false;
                deleteTempPhotoFile();
            } else {
                Log.w(TAG, "Camera returned unexpected result code: " + resultCode);
                tempPhotoUri = null;
                tempPhotoPath = null;
                hasUnsavedPhoto = false;
                deleteTempPhotoFile();
            }
        }
    }

    private void deleteTempPhotoFile() {
        if (tempPhotoPath != null) {
            File tempFile = new File(tempPhotoPath);
            if (tempFile.exists()) {
                if (tempFile.delete()) {
                    Log.d(TAG, "Temporary photo file deleted: " + tempPhotoPath);
                } else {
                    Log.w(TAG, "Failed to delete temporary photo file: " + tempPhotoPath);
                }
            }
            tempPhotoPath = null;
        }
        if (tempPhotoUri != null) {
            revokeUriPermission(tempPhotoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            tempPhotoUri = null;
            Log.d(TAG,"Temp photo URI revoked and nulled");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called. Cleaning up temporary photo file.");
        deleteTempPhotoFile();
    }
}