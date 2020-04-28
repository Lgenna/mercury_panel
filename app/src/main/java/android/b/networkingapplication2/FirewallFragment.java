package android.b.networkingapplication2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class FirewallFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_firewall, container, false);
    }

    public static FirewallFragment newInstance() {
        return new FirewallFragment();
    }
}
