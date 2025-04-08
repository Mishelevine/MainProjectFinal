package org.hse.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

public class SensorHelper implements SensorEventListener {
    public interface SensorCallback {
        void onLightSensorChanged(float lux);
    }

    private final SensorManager sensorManager;
    private final SensorCallback callback;

    public SensorHelper(Context context, SensorCallback callback) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.callback = callback;
    }

    public void startListening() {
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            callback.onLightSensorChanged(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static String getSensorList(SensorManager manager) {
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder builder = new StringBuilder("Все датчики:\n");
        for (Sensor s : sensors) {
            builder.append(s.getName()).append("\n");
        }
        return builder.toString();
    }
}
