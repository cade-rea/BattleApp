package com.caderea.battleapp.activity;

import com.caderea.battleapp.core.Map;
import com.caderea.battleapp.view.map.MapView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

public class MapActivity extends Activity {

    public Handler uiHandler;
    private MapView mapView;
    private Map map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView = new MapView(this);
        setContentView(mapView);

        //this handler can be passed to other threads to refer to the UI thread
        uiHandler = new Handler(Looper.getMainLooper());

        map = new Map(this);
        (new Thread(map)).start();
    }

    @Override
    protected void onDestroy() {
        //otherwise the map thread keeps running after the screen is gone
        map.stop();
        super.onDestroy();
    }

    public void moveCircle() {
        mapView.setX(mapView.getX() + 50);
    }

    public void notifyDone() {
        if (!isFinishing()) {
            finish();
        }
    }
}
