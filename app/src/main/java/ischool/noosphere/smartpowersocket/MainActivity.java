package ischool.noosphere.smartpowersocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ImageView isClientConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        isClientConnected = (ImageView) toolbar.findViewById(R.id.device_connected);
        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(getTitle());
        setSupportActionBar(toolbar);
    }

    public void setConnected(int id) {
        isClientConnected.setImageDrawable(getResources().getDrawable(id));
    }

}
