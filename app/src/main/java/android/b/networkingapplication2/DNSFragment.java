package android.b.networkingapplication2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DNSFragment extends Fragment {

    Context context;
    RecyclerView mDNSRecyclerView;
    Thread uiUpdater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dns, container, false);

        this.context = getContext();

        mDNSRecyclerView = view.findViewById(R.id.dns_recycler_view);

        LinearLayoutManager dnslinearLayoutManager = new LinearLayoutManager(context);
        mDNSRecyclerView.setLayoutManager(dnslinearLayoutManager);

        uiUpdater = new Thread() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(() -> updateUI());
                } catch (NullPointerException ignore) { }
            }
        };
        uiUpdater.start();

        return view;
    }


    public void updateUI() {

        Log.i(TAG, "updateUI");

        ArrayList<DNS> mDNSEntries = new ArrayList<>();

        String[] DNSNames = {"Custom 1 (IPV4)", "Custom 2 (IPV4)", "Custom 3 (IPV6)", "Custom 4 (IPV6)"};


        for (String element : DNSNames) {

            DNS DNSItem = new DNS();

            DNSItem.setTitle(element);
            DNSItem.setTextbox("");
            DNSItem.setStatus(false);

            mDNSEntries.add(DNSItem);
        }

        DNSCustomAdapter dnscustomAdapter = new DNSCustomAdapter(context, mDNSEntries);
        mDNSRecyclerView.setAdapter(dnscustomAdapter);

    }


    public static DNSFragment newInstance() {
        return new DNSFragment();
    }
}
