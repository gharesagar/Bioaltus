package com.example.administrator.bioaltus;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fragment.ExistingCustomerFragment;
import fragment.NewCustomerFragment;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import model.Customer;
import model.Product;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    TextView tvCustomerName, tvProductName;
    ImageView imgDrop1, imgDrop2;
    SpinnerDialog spinnerDialog,spinnerDialog2;
    private ArrayList<Customer> customerArrayList;
    private ArrayList<String> customerNamesList,productsNameList;
    private ArrayList<Product> productArrayList;
    private Customer customer;
    private Product product;

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

  /*      tvCustomerName = findViewById(R.id.tvCustomerName);
        imgDrop1 = findViewById(R.id.imgDrop1);
        imgDrop2 = findViewById(R.id.imgDrop2);*/
        fragmentContainer=findViewById(R.id.fragmentContainer);
        linearLayout1=findViewById(R.id.linearLayout1);
        linearLayout2=findViewById(R.id.linearLayout2);

        linearLayout1.setBackgroundColor(getResources().getColor(R.color.layout_bg));


        //loadCustomers();
        //loadProducts();

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
        fragmentTransaction.replace(R.id.fragmentContainer,existingCustomerFragment);
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

/*    private void loadProducts() {
        productsNameList=new ArrayList<>();
        productArrayList=new ArrayList<>();


        final StringRequest stringRequest = new StringRequest(Request.Method.GET, ApiConstants.PRODUCT_MASTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    Log.e(TAG, response);

                    JSONArray jsonArray = new JSONArray(response);

                    //json response
                    if (productArrayList.size() > 0) {
                        productArrayList.clear();
                    }
                    if(productsNameList.size()>0){
                        productsNameList.clear();
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String productId = jsonObject1.getString("ProductId");
                        String productName = jsonObject1.getString("ProductName");
                        product = new Product(productId, productName);
                        productArrayList.add(product);
                        productsNameList.add(productName);
                    }

                    spinnerDialog2 = new SpinnerDialog(MainActivity.this, productsNameList, "Select product");
                    spinnerDialog2.bindOnSpinerListener(new OnSpinerItemClick() {
                        @Override
                        public void onClick(String item, int pos) {
                            tvProductName.setText(item);
                        }
                    });

                    tvProductName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinnerDialog2.showSpinerDialog();
                        }
                    });

                    imgDrop2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            spinnerDialog2.showSpinerDialog();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                AlertDialog.Builder al = new AlertDialog.Builder(MainActivity.this);
                String mesaage = null;
                if (error instanceof NetworkError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof ServerError) {
                    mesaage = "The server could not be found. Please try again after some time!!";
                } else if (error instanceof AuthFailureError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof NoConnectionError) {
                    mesaage = "Cannot connect to Internet...Please check your connection!";
                } else if (error instanceof TimeoutError) {
                    mesaage = "Connection TimeOut! Please check your internet connection.";
                } else if (error instanceof ParseError) {
                    mesaage = "Indicates that the server response could not be parsed.";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();

            }
        });

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        AppController.getInstance().addToRequestQueue(stringRequest);
    }

   */
}
