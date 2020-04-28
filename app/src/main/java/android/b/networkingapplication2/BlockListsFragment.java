package android.b.networkingapplication2;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;

public class BlockListsFragment extends Fragment {

    private TextView editDomain;
    private BlockListBaseHelper myBloDb;
    private RecyclerView mBlockListRecyclerView;
    private Context context;

    private static final String TAG = "BlockListsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_blocklists, container, false);

        ImageButton addBlocklist;
        context = getContext();


        myBloDb = OverviewActivity.getMyBloDb();

        addBlocklist = view.findViewById(R.id.blocklist_urls_add);
        editDomain = view.findViewById(R.id.custom_blocklists_textfield);
        mBlockListRecyclerView = view.findViewById(R.id.blocklist_recycler_view);

        LinearLayoutManager blocklistlinearLayoutManager = new LinearLayoutManager(getContext());
        mBlockListRecyclerView.setLayoutManager(blocklistlinearLayoutManager);

        try {

            updateUI();

            editDomain.setOnEditorActionListener((v, actionId, event) -> {

                if ((actionId == EditorInfo.IME_ACTION_DONE)) {

                    BlockListsFragment.this.addBlocklist();

                    InputMethodManager imm = (InputMethodManager) BlockListsFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
                return false;
            });

            addBlocklist.setOnClickListener(v -> {
                addBlocklist();
            });

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }

        return view;
    }

    private void addBlocklist() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

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

                    alertBuilder.setMessage(getResources().getString(R.string.gathering) + enteredText)
                            .setCancelable(false);

                    AlertDialog alert = alertBuilder.create();
                    alert.setTitle(R.string.please_wait);
                    alert.show();

                    Thread buildMasterList = new Thread() {
                        @Override
                        public void run() {
                            buildMasterList(enteredText);
                            alert.dismiss();
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
    }

    private void updateUI() {

        ArrayList<BlockList> mBlockLists = new ArrayList<>();

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
        MasterBlockListBaseHelper myMasDb = OverviewActivity.getMyMasDb();

        Cursor cursor = myMasDb.selectData(sBlockList);

        if (cursor == null || cursor.getCount() > 0) {
            Log.i(TAG, "Atleast one domain from " + sBlockList + ", skipping.");

        } else {
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

                    List<MasterBlocklist> domainEntries = new ArrayList<>();

                    while ((domainFromBlockList = in.readLine()) != null) {
                        if (!domainFromBlockList.equals("") && !domainFromBlockList.equals(" ") && !domainFromBlockList.startsWith("#")) {

                            MasterBlocklist domain = new MasterBlocklist();
                            domain.setDomain(domainFromBlockList);
                            domain.setBlockList(sBlockList);
                            domain.setStatus(1);

                            domainEntries.add(domain);
                        }
                    }

                    myMasDb.insertData(domainEntries);

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
