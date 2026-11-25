package com.fastlauncher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private AppAdapter adapter;
    private EditText search;
    private Button allButton;
    private Button favButton;
    private TextView clock;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat f = new SimpleDateFormat("HH:mm • d MMMM", Locale.getDefault());
            clock.setText(f.format(new Date()));
            handler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        clock = (TextView) findViewById(R.id.clock);
        search = (EditText) findViewById(R.id.search);
        allButton = (Button) findViewById(R.id.all);
        favButton = (Button) findViewById(R.id.fav);
        GridView grid = (GridView) findViewById(R.id.grid);

        adapter = new AppAdapter(this);
        grid.setAdapter(adapter);
        grid.requestFocus();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppAdapter.AppInfo app = adapter.getItem(position);
                Intent i = getPackageManager().getLaunchIntentForPackage(app.pkg);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }
        });

        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleFavorite(position);
                Toast.makeText(MainActivity.this, "Избранное обновлено", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.apply(s.toString(), adapter.isFavoritesOnly());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(false);
            }
        });
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(true);
            }
        });

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Настройки недоступны", Toast.LENGTH_SHORT).show();
                }
            }
        });

        handler.post(tick);
    }

    private void setMode(boolean favorites) {
        allButton.setSelected(!favorites);
        favButton.setSelected(favorites);
        adapter.apply(search.getText().toString(), favorites);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMode(adapter.isFavoritesOnly());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (search.getText().length() > 0) {
            search.setText("");
            return;
        }
        if (adapter.isFavoritesOnly()) {
            setMode(false);
            return;
        }
        // Лаунчер не закрывается кнопкой «назад»
    }
}
