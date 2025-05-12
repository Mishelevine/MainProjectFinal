package org.hse.android;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TimeViewModel extends AndroidViewModel {

    private static final String TAG = "TimeViewModel";
    private static final String URL = "http://api.ipgeolocation.io/ipgeo?apiKey=b03018f75ed94023a005637878ec0977";

    private final MutableLiveData<Date> currentTime = new MutableLiveData<>();
    private final OkHttpClient client = new OkHttpClient();

    public TimeViewModel(@NonNull Application application) {
        super(application);
        fetchTimeFromServer();
    }

    public LiveData<Date> getTime() {
        return currentTime;
    }

    public void fetchTimeFromServer() {
        Request request = new Request.Builder().url(URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        Log.e(TAG, "Response body is null");
                        currentTime.postValue(new Date());
                        return;
                    }

                    String json = body.string();
                    TimeResponse timeResponse = new Gson().fromJson(json, TimeResponse.class);

                    if (timeResponse == null || timeResponse.timeZone == null || timeResponse.timeZone.currentTime == null) {
                        Log.e(TAG, "Time parse failed");
                        currentTime.postValue(new Date());
                        return;
                    }

                    String raw = timeResponse.timeZone.currentTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                    Date parsedDate = sdf.parse(raw);

                    currentTime.postValue(parsedDate != null ? parsedDate : new Date());
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                    currentTime.postValue(new Date());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network error", e);
                currentTime.postValue(new Date());
            }
        });
    }
}