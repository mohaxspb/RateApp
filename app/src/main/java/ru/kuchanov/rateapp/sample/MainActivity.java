package ru.kuchanov.rateapp.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ru.kuchanov.rate.PreRate;
import ru.kuchanov.rateapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showIfNeed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreRate.clearDialogIfOpen();
    }

    public void onClick(final View view) {
        PreRate.init(this, getString(R.string.feedback_email), getString(R.string.feedback_title)).showRateDialog();
    }
}