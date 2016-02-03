package org.beeender.virtualspacelimit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private AtomicBoolean finished = new AtomicBoolean(false);
    private AtomicLong mmapSize = new AtomicLong(0);
    private long addr = -1;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        handler = new Handler(Looper.myLooper());
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Running...");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finished.get()) {
                    doMmap();
                    try {
                        Thread.sleep(16L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void doMmap() {
        long size = mmapSize.get() + 100 * 1024 * 1024;
        long ret;
        if (addr == -1) {
            ret = MMap.mmap(size);
        } else {
            ret = MMap.mremap(addr, mmapSize.get(), size);
        }
        if (ret != -1) {
            mmapSize.set(size);
        } else {
            finished.set(true);
            MMap.munmap(addr, mmapSize.get());
            addr = -1;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void update() {
        String text = "Current mmap size is " + mmapSize.get() / 1024 / 1024 + "MB";
        if (finished.get()) {
            text += " - DONE";
        }
        textView.setText(text);
    }
}
