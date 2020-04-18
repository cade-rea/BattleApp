package com.caderea.battleapp.activity;

import com.caderea.battleapp.core.Map;
import com.caderea.battleapp.view.MapView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
        uiHandler = new Handler();

        map = new Map(this);
        (new Thread(map)).start();
    }

    public void moveCircle() {
        mapView.setX(mapView.getX() + 50);
    }

    public void notifyDone() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
