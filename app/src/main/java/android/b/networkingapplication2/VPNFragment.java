package android.b.networkingapplication2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.util.zip.Inflater;

public class VPNFragment extends Fragment {

    public static final String TAG = "VPNFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.i(TAG, "onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_vpn, container, false);
    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.activity_vpn, container, false);
//
//        Log.i(TAG, "onCreateView");
//
//        return view;
//    }

    public static VPNFragment newInstance() {
        VPNFragment fragment = new VPNFragment();

        Log.i(TAG, "newInstance");

        return fragment;
    }
}
