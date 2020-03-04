package android.b.networkingapplication2;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;

public class BlockListsFragment extends Fragment {

    private TextView editDomain;
    private BlockListBaseHelper myBloDb;
    private RecyclerView mBlockListRecyclerView;
    public static View view;

//    public static ArrayList<String> masterBlockList = new ArrayList<>();

    Thread buildMasterList;

    ArrayList<BlockList> mBlockLists;

    private static final String TAG = "BlockListsFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ImageButton addBlocklist;

        view = inflater.inflate(R.layout.activity_blocklists, container, false);

        myBloDb = new BlockListBaseHelper(getContext());

        addBlocklist = view.findViewById(R.id.blocklist_urls_add);
        editDomain = view.findViewById(R.id.custom_blocklists_textfield);
        mBlockListRecyclerView = view.findViewById(R.id.blocklist_recycler_view);

        LinearLayoutManager blocklistlinearLayoutManager = new LinearLayoutManager(getContext());
        mBlockListRecyclerView.setLayoutManager(blocklistlinearLayoutManager);

        try {

//            if (myBloDb.getAllData().getCount() == 0) {
//                Log.w(TAG, "Blocklist DB empty, adding test data");
//                myBloDb.insertData("https://blocklist.site/app/dl/ransomware");
//                myBloDb.insertData("https://blocklist.site/app/dl/ransomware");
//            }

            updateUI();

            addBlocklist.setOnClickListener(v -> {
                if (!editDomain.getText().toString().equals("")) {

                    String enteredText = editDomain.getText().toString();

                    if (Patterns.WEB_URL.matcher(enteredText).matches()) {

                        myBloDb.insertData(enteredText);

                        Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Added: " + enteredText + " to blocklist Database");

                        updateUI();
                        editDomain.setText("");

                        Toast.makeText(getContext(), "Please Wait, we're gathering URL's from : " + enteredText, Toast.LENGTH_LONG).show();

                        buildMasterList = new Thread() {
                            @Override
                            public void run() {
                                buildMasterList(enteredText);
                            }
                        };

                        buildMasterList.start();

                    } else {
                        Toast.makeText(getContext(), "\"" + enteredText + "\" is not a valid block list", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "\"" + enteredText + "\" is not a valid block list");

                    }

                } else {
                    Toast.makeText(getContext(), "Block list can't be blank", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }

        return view;
    }

    public void updateUI() {

        mBlockLists = new ArrayList<>();

        Cursor blocklistRes = myBloDb.getAllData();

        if (blocklistRes.getCount() != 0) {
            while (blocklistRes.moveToNext()) {
                BlockList BlockListItem = new BlockList();
                BlockListItem.setDomain(blocklistRes.getString(1));

                mBlockLists.add(BlockListItem);
            }
        }

        BlockListCustomAdapter blocklistcustomAdapter = new BlockListCustomAdapter(getContext(), mBlockLists);
        mBlockListRecyclerView.setAdapter(blocklistcustomAdapter);
    }

    private void buildMasterList(String sBlockList) {

        MasterBlockListBaseHelper myMasDb;

        myMasDb = DomainBlockerFragment.getMyMasDb();
        Cursor myMasDbRes = myMasDb.getAllData();
        boolean domainsPresent = false;

        if (myMasDbRes.getCount() != 0) {
            while (myMasDbRes.moveToNext()) {
                if (sBlockList.equals(myMasDbRes.getString(1))) {
                    Log.i(TAG, "Atleast one domain from " + sBlockList + ", skipping.");
                    domainsPresent = true;
                }
            }
        }

        if (!domainsPresent) {
            Log.i(TAG, "No domains found from " + sBlockList + ", adding blocked domains...");

            try {
                URL url = new URL(sBlockList);

                try {
                    Log.i(TAG, "Looking for domains in " + sBlockList);

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String domainFromBlockList;

                    int sizeBefore = myMasDbRes.getCount();

                    while ((domainFromBlockList = in.readLine()) != null) {
                        if (!domainFromBlockList.equals("") && !domainFromBlockList.equals(" ")) {
                            myMasDb.insertData(
                                    sBlockList,
                                    domainFromBlockList,
                                    1);

                            DomainBlockerFragment.setMyMasDb(myMasDb);
                        }
                    }
                    Log.i(TAG, "Added " + (myMasDb.getAllData().getCount() - sizeBefore) + " domains to blocklist from " + sBlockList);

                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Added " + (myMasDb.getAllData().getCount() - sizeBefore) + " domains to the blocklist", Toast.LENGTH_LONG).show());

                    in.close();

                } catch (IOException e) {
                    Log.i(TAG, "[IOException]");
                }

            } catch (MalformedURLException e) {
                Log.i(TAG, "[MalformedURLException]");
            }
        }
    }

    public static BlockListsFragment newInstance() {
        return new BlockListsFragment();
    }
}
