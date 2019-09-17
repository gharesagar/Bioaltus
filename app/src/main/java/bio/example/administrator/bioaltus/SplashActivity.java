package bio.example.administrator.bioaltus;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.bio.bioaltus.R;

import services.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "Splash_screen";
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(getApplicationContext());

        splashTime();

    }

    private void splashTime() {
        Thread timerTread = new Thread() {
            public void run() {
                try {
                    sleep(3500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    // Log.e(TAG, String.valueOf("Client is"+sessionManager.isLoggedIn()+""+"Emp is"+sessionManager.isEmpLoggedIn()));
                    if (sessionManager.isLoggedIn()) {
                        Intent intent = new Intent(getApplicationContext(), CheckInOutActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity2.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                }
            }
        };
        timerTread.start();

    }
}
