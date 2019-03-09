package services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import static android.content.Context.MODE_PRIVATE;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    private static final String TAG = "SessionManager";
    public static final String PREF_NAME = "BIOALTUS";
    public static final String EMP_CODE="EmpCode";
    public static final String IS_LOGIN = "IsLoggedIn";



    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }


    public void logoutEmp() {
        editor = pref.edit();

        editor.putBoolean(IS_LOGIN, false);
        editor.remove(EMP_CODE);
        boolean isDataRemoved = editor.commit();

        if (isDataRemoved) {
            Log.e(TAG, "Emp Data removed");
        } else {
            Log.e(TAG, "Emp Data not removed");
        }
    }


    public void createEmpLoginSession(int empCode) {
        editor = pref.edit();

        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(EMP_CODE, empCode);
        boolean isDataInserted = editor.commit();

        if (isDataInserted) {
            Log.e(TAG, "Data inserted");
        } else {
            Log.e(TAG, "Data not inserted");
        }
    }


    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }


    public HashMap<String, String> getEmpDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(EMP_CODE, String.valueOf(pref.getInt(EMP_CODE, 0)));
        return user;
    }
}
