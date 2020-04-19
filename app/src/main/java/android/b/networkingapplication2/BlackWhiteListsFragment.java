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
import java.util.ArrayList;
import java.util.List;

import database.BlackListBaseHelper;
import database.MasterBlockListBaseHelper;
import database.WhiteListBaseHelper;

public class BlackWhiteListsFragment extends Fragment {

    private TextView editWhiteListDomain;
    private TextView editBlackListDomain;
    private BlackListBaseHelper myBlaDb;
    private WhiteListBaseHelper myWhiDb;
    private RecyclerView mWhiteListRecyclerView;
    private RecyclerView mBlackListRecyclerView;
    public View view;

    private static final String TAG = "BlackWhiteListsFragment";
    private static final String BLTAG = "BlackWhiteListsFragment - BlackList";
    private static final String WLTAG = "BlackWhiteListsFragment - WhiteList";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ImageButton addBlacklist;
        ImageButton addWhitelist;

        view = inflater.inflate(R.layout.activity_black_white_lists, container, false);

        myBlaDb = new BlackListBaseHelper(getContext());
        myWhiDb = new WhiteListBaseHelper(getContext());

        addBlacklist = view.findViewById(R.id.addBlacklist);
        addWhitelist = view.findViewById(R.id.addWhitelist);

        editBlackListDomain = view.findViewById(R.id.blacklist_textbox);
        editWhiteListDomain = view.findViewById(R.id.whitelist_textbox);
        mBlackListRecyclerView = view.findViewById(R.id.blacklist_domains_recycler_view);
        mWhiteListRecyclerView = view.findViewById(R.id.whitelist_domains_recycler_view);

        LinearLayoutManager blacklistlinearLayoutManager = new LinearLayoutManager(getContext());
        mBlackListRecyclerView.setLayoutManager(blacklistlinearLayoutManager);

        LinearLayoutManager whitelistlinearLayoutManager = new LinearLayoutManager(getContext());
        mWhiteListRecyclerView.setLayoutManager(whitelistlinearLayoutManager);

        try {

            if (myBlaDb.getAllData().getCount() == 0) {
                Log.w(BLTAG, "DB empty, adding initial data");
                myBlaDb.insertData(getResources().getString(R.string.no_data));
            }

            if (myWhiDb.getAllData().getCount() == 0) {
                Log.w(WLTAG, "DB empty, adding initial data");
                myWhiDb.insertData(getResources().getString(R.string.no_data));
            }

            updateUI();

            addBlacklist.setOnClickListener(v -> {
                if (!editBlackListDomain.getText().toString().equals("")) {
                    String enteredText = editBlackListDomain.getText().toString();

                    if (Patterns.WEB_URL.matcher(enteredText).matches()) {

                        // just a precautionary measure because there might somehow be nothing in the list
                        try {
                            Cursor checkInitial = myBlaDb.getAllData();

                            if (checkInitial.getCount() == 1) {
                                checkInitial.moveToFirst();
                                if (checkInitial.getString(1).equals(getResources().getString(R.string.no_data))) {
                                    myBlaDb.deleteData("1");
                                    Log.w(BLTAG, "Removing initial row");
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Log.e(TAG, "Phew, we were just grazed by an apocalypse...");
                        }

                        myBlaDb.insertData(enteredText);
                        Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                        Log.i(BLTAG, "Added: " + enteredText);

                        addEntryToDatabase(enteredText, true);

                        updateUI();
                        editBlackListDomain.setText("");

                    } else {
                        Toast.makeText(getContext(), "\"" + enteredText + "\" is not a valid black list", Toast.LENGTH_SHORT).show();
                        Log.i(BLTAG, "\"" + enteredText + "\" is not a valid black list");
                    }
                } else {
                    Toast.makeText(getContext(), "Black list can't be blank", Toast.LENGTH_SHORT).show();
                }
            });

            addWhitelist.setOnClickListener(v -> {
                if (!editWhiteListDomain.getText().toString().equals("")) {
                    String enteredText = editWhiteListDomain.getText().toString();

                    if (Patterns.WEB_URL.matcher(enteredText).matches()) {

                        // just a precautionary measure because there might somehow be nothing in the list
                        try {
                            Cursor checkInitial = myWhiDb.getAllData();

                            if (checkInitial.getCount() == 1) {
                                checkInitial.moveToFirst();
                                if (checkInitial.getString(1).equals(getResources().getString(R.string.no_data))) {
                                    myWhiDb.deleteData("1");
                                    Log.w(WLTAG, "Removing initial row");
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Log.e(TAG, "Phew, we were just grazed by an apocalypse...");
                        }

                        myWhiDb.insertData(enteredText);
                        Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                        Log.i(WLTAG, "Added: " + enteredText);

                        addEntryToDatabase(enteredText, false);

                        updateUI();
                        editWhiteListDomain.setText("");
                    } else {
                        Toast.makeText(getContext(), "\"" + enteredText + "\" is not a valid white list", Toast.LENGTH_SHORT).show();
                        Log.i(WLTAG, "\"" + enteredText + "\" is not a valid white list");
                    }
                } else {
                    Toast.makeText(getContext(), "White list can't be blank", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }
        return view;
    }

    /**
     *
     * @param input
     * @param isBlackListItem true = block it, or false = allow it
     */

    private void addEntryToDatabase(String input, boolean isBlackListItem) {
        // Lets now add it where it to where it can do something
        MasterBlockListBaseHelper myMasDb = OverviewActivity.getMyMasDb();

        Cursor cursor = myMasDb.selectData(input);

        if (isBlackListItem) {
            if (cursor.getCount() > 0) {
                Log.i(TAG, "Found " + input + " at-least once, skipping.");

            } else {
                // lets make a list, add one item, and return that list
                Log.i(TAG, "Adding " + input + " to the blacklist.");
                List<MasterBlocklist> domainEntries = new ArrayList<>();

                MasterBlocklist domain = new MasterBlocklist();
                domain.setDomain(input);
                domain.setBlockList(input);
                domain.setStatus(1);

                domainEntries.add(domain);

                myMasDb.insertData(domainEntries);
            }
        } else {
            if (cursor.getCount() > 0) {
                Log.i(TAG, "Found " + input + " in the database, updating its status");
                // move to the "first" value, otherwise it will be -1
                while (cursor.moveToNext()) {
                    // get the value in the first column of that first value and change that
                    myMasDb.updateData(0, cursor.getString(0));

                }
            } else {
                Log.i(TAG, "Didn't find " + input + " in the database, ignoring");
            }
        }
    }

    private void updateUI() {

        ArrayList<BlackWhiteList> mBlackLists = new ArrayList<>();
        ArrayList<BlackWhiteList> mWhiteLists = new ArrayList<>();

        Cursor blacklistRes = myBlaDb.getAllData();
        Cursor whitelistRes = myWhiDb.getAllData();

        if (blacklistRes.getCount() != 0) {
            while (blacklistRes.moveToNext()) {
                BlackWhiteList BlackListItem = new BlackWhiteList();
                BlackListItem.setDomain(blacklistRes.getString(1));

                mBlackLists.add(BlackListItem);
            }
        }

        if (whitelistRes.getCount() != 0) {
            while (whitelistRes.moveToNext()) {
                BlackWhiteList WhiteListItem = new BlackWhiteList();
                WhiteListItem.setDomain(whitelistRes.getString(1));

                mWhiteLists.add(WhiteListItem);
            }
        }

        BlackListCustomAdapter blacklistcustomAdapter = new BlackListCustomAdapter(getContext(), mBlackLists);
        mBlackListRecyclerView.setAdapter(blacklistcustomAdapter);

        WhiteListCustomAdapter whitelistcustomAdapter = new WhiteListCustomAdapter(getContext(), mWhiteLists);
        mWhiteListRecyclerView.setAdapter(whitelistcustomAdapter);
    }

    public static BlackWhiteListsFragment newInstance() {
        return new BlackWhiteListsFragment();
    }
}
