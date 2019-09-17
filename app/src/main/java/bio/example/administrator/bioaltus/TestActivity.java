package bio.example.administrator.bioaltus;

import android.Manifest;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.bio.bioaltus.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class TestActivity extends AppCompatActivity implements  EasyPermissions.PermissionCallbacks{

    public final int PERM_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 /*   if (checkPermission()) {

                    } else {
                        requestPermission();
                    }*/

                givePermission();
            } else {
                //below M
                //getLocation();
            }

        } catch (Exception e) {
            Toast.makeText(TestActivity.this, "Error Mesg : " + e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

    @AfterPermissionGranted(PERM_REQUEST_CODE)
    private void givePermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {

            //  getLocation();
        } else {
            EasyPermissions.requestPermissions(this, "We need permission of location",
                    PERM_REQUEST_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);

     /*   switch (requestCode) {

            case PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(LoginActivity.this, "Permission denied, Now you can't login", Toast.LENGTH_LONG).show();
                }
                break;
        }*/
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {

            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
