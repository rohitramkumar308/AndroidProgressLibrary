package com.sample.progress;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.progress.library.WaveProgressView;

public class MainActivity extends AppCompatActivity {
    private boolean startLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.animate);
        final WaveProgressView loadingView = (WaveProgressView) findViewById(R.id.loadingView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoading = !startLoading;
                if (startLoading) {
                    loadingView.start();
                } else {
                    loadingView.stop();
                }
            }
        });
    }
}
