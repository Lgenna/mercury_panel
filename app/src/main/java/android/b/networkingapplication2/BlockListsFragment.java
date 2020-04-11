package android.b.networkingapplication2;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.List;

import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;
import database.MasterBlockListDbSchema;

public class BlockListsFragment extends Fragment {

    private TextView editDomain;
    private BlockListBaseHelper myBloDb;
    private RecyclerView mBlockListRecyclerView;
    public static View view;

//    public static ArrayList<String> masterBlockList = new ArrayList<>();

    Thread buildMasterList;

    ArrayList<BlockList> mBlockLists;

    private static final String TAG = "BlockListsFragment";

    private List<MasterBlocklist> domainEntrys;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        AlertDialog.Builder builder;
        ImageButton addBlocklist;

        view = inflater.inflate(R.layout.activity_blocklists, container, false);

//        myBloDb = new BlockListBaseHelper(getContext());
        myBloDb = OverviewActivity.getMyBloDb();

        addBlocklist = view.findViewById(R.id.blocklist_urls_add);
        editDomain = view.findViewById(R.id.custom_blocklists_textfield);
        mBlockListRecyclerView = view.findViewById(R.id.blocklist_recycler_view);

        LinearLayoutManager blocklistlinearLayoutManager = new LinearLayoutManager(getContext());
        mBlockListRecyclerView.setLayoutManager(blocklistlinearLayoutManager);

        builder = new AlertDialog.Builder(getContext());

        try {

            updateUI();

            addBlocklist.setOnClickListener(v -> {
                if (!editDomain.getText().toString().equals("")) {

                    String enteredText = editDomain.getText().toString();

                    if (Patterns.WEB_URL.matcher(enteredText).matches()) {

                        boolean domainPresent = false;

                        Cursor cursor = myBloDb.selectData(enteredText);

                        if (cursor == null || cursor.getCount() > 0) {
                            domainPresent = true;
                        }

                        if (!domainPresent) {
                            myBloDb.insertData(enteredText);

                            Log.i(TAG, "Added: " + enteredText + " to blocklist Database");

                            updateUI();
                            editDomain.setText("");

                            builder.setMessage(getResources().getString(R.string.gathering) + enteredText)
                                    .setCancelable(false);

                            AlertDialog alert = builder.create();
                            alert.setTitle(R.string.please_wait);
                            alert.show();

                            buildMasterList = new Thread() {
                                @Override
                                public void run() {
                                    buildMasterList(enteredText);
                                    alert.dismiss();
//                                    alert.cancel();
                                }
                            };

                            buildMasterList.start();

                        } else {
                            Toast.makeText(getContext(), "\"" + enteredText + "\" has all ready been added", Toast.LENGTH_SHORT).show();
                        }

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

        myMasDb = OverviewActivity.getMyMasDb();
//        Cursor myMasDbRes = myMasDb.getAllData();
        boolean domainsPresent = false;

        Cursor cursor = myMasDb.selectData(sBlockList);

        if (cursor == null || cursor.getCount() > 0) {
            Log.i(TAG, "Atleast one domain from " + sBlockList + ", skipping.");
            domainsPresent = true;

        }

        if (!domainsPresent) {
            Log.i(TAG, "No domains found from " + sBlockList + ", adding blocked domains...");

            try {

                URL url;

                if(!sBlockList.startsWith("https://")) {
                    Log.i(TAG, "Adding \"https://\" to the blocklist input");
                   url = new URL("https://" + sBlockList);
                } else {
                    url = new URL(sBlockList);
                }

                try {
                    Log.i(TAG, "Looking for domains in " + sBlockList);

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String domainFromBlockList;

                    long sizeBefore = myMasDb.countData();

                    domainEntrys = new ArrayList<>();

                    while ((domainFromBlockList = in.readLine()) != null) {
                        if (!domainFromBlockList.equals("") && !domainFromBlockList.equals(" ") && !domainFromBlockList.startsWith("#")) {

                            MasterBlocklist domain = new MasterBlocklist();
                            domain.setDomain(domainFromBlockList);
                            domain.setBlockList(sBlockList);
                            domain.setStatus(1);

                            domainEntrys.add(domain);
                        }
                    }

                    myMasDb.insertData(domainEntrys);

                    Log.i(TAG, "Added " + (myMasDb.countData() - sizeBefore) + " domains to blocklist from " + sBlockList);

                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Added " + (myMasDb.getAllData().getCount() - sizeBefore) + " domains to the blocklist", Toast.LENGTH_LONG).show());

                    in.close();

                } catch (IOException e) {
                    Log.i(TAG, "[IOException]");
                }

            } catch (MalformedURLException e) {
                Toast.makeText(getContext(), "[MalformedURLException]", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static BlockListsFragment newInstance() {
        return new BlockListsFragment();
    }
}
