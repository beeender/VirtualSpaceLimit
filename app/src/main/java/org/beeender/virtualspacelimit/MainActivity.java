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
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private TextView threadCountsTextView;
    private Button startButton;
    private AtomicBoolean finished;
    private AtomicLong mmapSize;
    private long[] addrs;
    private long[] sizes;
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
        startButton = (Button) findViewById(R.id.button);
        threadCountsTextView = (TextView) findViewById(R.id.threadsCount);
        textView = (TextView) findViewById(R.id.textView);
    }

    private void doMmap() {
        long size = mmapSize.get() + 100 * 1024 * 1024;
        long ret = -1;
        for (int i = 0; i < addrs.length; i++) {
            long addr = addrs[i];
            if (addr == -1) {
                ret = MMap.mmap(size);
            } else {
                ret = MMap.mremap(addr, mmapSize.get(), size);
            }
            if (ret != -1) {
                addrs[i] = ret;
                sizes[i] = size;
            } else {
                break;
            }
        }
        if (ret != -1) {
            mmapSize.set(size);
        } else {
            finished.set(true);
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
            startButton.setEnabled(true);
        }
        textView.setText(text);
    }

    public void onClick(View view) {
        startButton.setEnabled(false);

        if (addrs != null) {
            for (int i = 0; i < addrs.length; i++) {
                MMap.munmap(addrs[i], sizes[i]);
            }
        }

        int counts = Integer.valueOf(threadCountsTextView.getText().toString());
        addrs = new long[counts];
        sizes = new long[counts];
        for (int i = 0; i < addrs.length; i++) {
            addrs[i] = -1;
            sizes[i] = 0;
        }

        finished = new AtomicBoolean(false);
        mmapSize = new AtomicLong(0);

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
}
