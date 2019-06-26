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
    public static final String EMP_CODE = "EmpCode";
    public static final String EMP_NAME = "EmpName";
    public static final String IS_LOGIN = "IsLoggedIn";
    public static final String CHECKIN_ID = "CheckInId";


    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }


    public void logoutEmp() {
        editor = pref.edit();

        editor.putBoolean(IS_LOGIN, false);
        editor.remove(EMP_CODE);
        editor.remove(EMP_NAME);
        editor.remove(CHECKIN_ID);

        boolean isDataRemoved = editor.commit();

        if (isDataRemoved) {
            Log.e(TAG, "Emp Data removed");
        } else {
            Log.e(TAG, "Emp Data not removed");
        }
    }


    public void createEmpLoginSession(int empCode, String location, String empName) {
        editor = pref.edit();

        editor.putBoolean(IS_LOGIN, true);
        editor.putInt(EMP_CODE, empCode);
        editor.putString(EMP_NAME, empName);
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
        user.put(EMP_NAME, pref.getString(EMP_NAME, "default"));
        return user;
    }

    public void saveCheckInData(String mChecckInId) {
        editor = pref.edit();

        editor.putString(CHECKIN_ID, mChecckInId);
        boolean isDataInserted = editor.commit();

        if (isDataInserted) {
            Log.e(TAG, "CheckIn Data inserted");
        } else {
            Log.e(TAG, "CheckIn Data not inserted");
        }
    }

    public HashMap<String, String> getCheckInDetails() {
        HashMap<String, String> checkInData = new HashMap<>();
        checkInData.put(CHECKIN_ID, pref.getString(CHECKIN_ID, "default"));
        return checkInData;
    }
}
