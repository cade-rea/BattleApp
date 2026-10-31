package com.caderea.battleapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.caderea.battleapp.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        padContentForSystemBars()

        findViewById<Button>(R.id.button_battle).setOnClickListener {
            startActivity(Intent(this, BattleActivity::class.java))
        }
        findViewById<Button>(R.id.button_map).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
