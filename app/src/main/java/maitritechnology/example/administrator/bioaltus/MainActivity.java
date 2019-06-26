package maitritechnology.example.administrator.bioaltus;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.maitritechnology.bioaltus.R;
import fragment.ExistingCustomerFragment;
import fragment.NewCustomerFragment;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    RelativeLayout fragmentContainer;
    ExistingCustomerFragment existingCustomerFragment;
    NewCustomerFragment newCustomerFragment;
    LinearLayout linearLayout1,linearLayout2;
    FragmentManager fragmentManage;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cust_product);

        fragmentContainer=findViewById(R.id.fragmentContainer);
        linearLayout1=findViewById(R.id.linearLayout1);
        linearLayout2=findViewById(R.id.linearLayout2);

        linearLayout1.setBackgroundColor(getResources().getColor(R.color.layout_bg));


        loadExistingCustomerFragment();

        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadExistingCustomerFragment();
            }
        });

        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewCustomerFragment();
            }
        });
    }

    private void loadExistingCustomerFragment() {
        linearLayout1.setBackgroundColor(getResources().getColor(R.color.layout_bg));
        linearLayout2.setBackgroundColor(getResources().getColor(R.color.white));

        fragmentManage=getSupportFragmentManager();
        fragmentTransaction=fragmentManage.beginTransaction();
        existingCustomerFragment=new ExistingCustomerFragment();
        fragmentTransaction.replace(R.id.fragmentContainer,existingCustomerFragment,"ExistingCustomerTag");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    private void loadNewCustomerFragment() {

        linearLayout1.setBackgroundColor(getResources().getColor(R.color.white));
        linearLayout2.setBackgroundColor(getResources().getColor(R.color.layout_bg));

        fragmentManage=getSupportFragmentManager();
        fragmentTransaction=fragmentManage.beginTransaction();
        newCustomerFragment=new NewCustomerFragment();
        fragmentTransaction.replace(R.id.fragmentContainer,newCustomerFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment frg = getSupportFragmentManager().findFragmentByTag("ExistingCustomerTag");
        if (frg != null) {
            frg.onActivityResult(requestCode, resultCode, data);
        }
    }

}
