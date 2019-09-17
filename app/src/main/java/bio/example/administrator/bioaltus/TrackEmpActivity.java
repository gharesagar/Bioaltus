package bio.example.administrator.bioaltus;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bio.bioaltus.R;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.CustomerListAdapter;
import model.FetchCustomerByEMP;
import model.FetchEmp;
import services.ApiConstants;
import services.AppController;
import services.SessionManager;

public class TrackEmpActivity extends AppCompatActivity {
    public static final String TAG = "TrackEmpActivity";
    private MaterialSearchView searchView;
    private SessionManager sessionManager;
    private HashMap<String,String> empData;
    private String empName;
    String[] empNames;
    private ArrayList<FetchEmp> empsList;
    private ArrayList<String> empNamesList;
    private int empId;

    CustomerListAdapter customerListAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    List<FetchCustomerByEMP> customerByEMPList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_emp);

        recyclerView=findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Customer data");

        sessionManager=new SessionManager(TrackEmpActivity.this);
        empData=new HashMap<>();
        empData=sessionManager.getEmpDetails();
        empName=empData.get(SessionManager.EMP_NAME);

        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        fetchEmp();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String selectedEmpName) {

                for (int i = 0; i < empsList.size(); i++) {
                    FetchEmp emp=empsList.get(i);
                    String empName=emp.getEmpName();

                    if(empName.equalsIgnoreCase(selectedEmpName)){
                        empId=emp.getEmpId();
                        fetchCustomerData(empId);
                    }

                }
                Toast.makeText(TrackEmpActivity.this, " " + selectedEmpName, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });
    }

    private void fetchCustomerData(final int empId) {
        customerByEMPList=new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.FETCH_CUSTOMER_BY_EMP, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);

                    JSONArray jsonArray=new JSONArray(response);

                    for (int i = 0; i <jsonArray.length() ; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String cardName = jsonObject.getString("CardName");
                        String chkInDate = jsonObject.getString("CheckINDate");
                        String chkInTime=jsonObject.getString("CheckINTime");
                        String chkOutTime=jsonObject.getString("CheckOutTIme");
                        String location=jsonObject.getString("location");

                        FetchCustomerByEMP fetchCustomerByEMP=new FetchCustomerByEMP(cardName,chkInDate,chkInTime,chkOutTime,location);
                        customerByEMPList.add(fetchCustomerByEMP);
                    }
                    customerListAdapter=new CustomerListAdapter(customerByEMPList);
                    recyclerView.setAdapter(customerListAdapter);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, error.getMessage());

                AlertDialog.Builder al = new AlertDialog.Builder(TrackEmpActivity.this);
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
                } else {
                    mesaage = "Something went wrong. Please try again after some time!!";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("empID", String.valueOf(empId));
                return params;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }


    private void fetchEmp() {
        empsList=new ArrayList<>();
        empNamesList=new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConstants.FETCH_EMPLOYEE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.e(TAG, response);

                    JSONArray jsonArray=new JSONArray(response);

                    for (int i = 0; i <jsonArray.length() ; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int empId = jsonObject.getInt("EmpId");
                        String empName = jsonObject.getString("EmpName");

                        FetchEmp fetchEmp=new FetchEmp(empId,empName);
                        empsList.add(fetchEmp);
                        empNamesList.add(empName);
                    }

                    if(empNamesList.size()>0){
                        String [] empNameStringArray = empNamesList.toArray(new String[empNamesList.size()]);
                        searchView.setSuggestions(empNameStringArray);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, error.getMessage());

                AlertDialog.Builder al = new AlertDialog.Builder(TrackEmpActivity.this);
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
                } else {
                    mesaage = "Something went wrong. Please try again after some time!!";
                }
                al.setTitle("Error");
                al.setMessage(mesaage);
                al.show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("EmpName", empName);
                return params;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 30000;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppController.getInstance().addToRequestQueue(stringRequest);

    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
