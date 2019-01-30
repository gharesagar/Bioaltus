package com.example.administrator.bioaltus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CheckInOutActivity extends AppCompatActivity {

    TextView tvCheckIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_out);

        tvCheckIn=findViewById(R.id.tvCheckIn);

        tvCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckInOutActivity.this,CheckInActivity.class));
            }
        });
    }
}
