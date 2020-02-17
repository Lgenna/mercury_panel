package android.b.networkingapplication2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import java.util.zip.Inflater;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class VPNFragment extends Fragment {

    public static final String TAG = "VPNFragment";


    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.i(TAG, "onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.activity_vpn, container, false);
        Log.i(TAG, "onCreateView");

        context = getContext();



        return view;
    }


    public static VPNFragment newInstance() {
        VPNFragment fragment = new VPNFragment();

        Log.i(TAG, "newInstance");

        return fragment;
    }
}
