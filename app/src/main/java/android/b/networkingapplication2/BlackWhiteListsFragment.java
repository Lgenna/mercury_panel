package android.b.networkingapplication2;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

import database.BlackListBaseHelper;
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
                Log.w(TAG, "Blacklist DB empty, adding test data");
                myBlaDb.insertData("1111");
                myBlaDb.insertData("2222");
                myBlaDb.insertData("3333");
                myBlaDb.insertData("4444");
                myBlaDb.insertData("5555");
            }

            if (myWhiDb.getAllData().getCount() == 0) {
                Log.w(TAG, "Whitelist DB empty, adding test data");
                myWhiDb.insertData("1111");
                myWhiDb.insertData("2222");
                myWhiDb.insertData("3333");
                myWhiDb.insertData("4444");
                myWhiDb.insertData("5555");
            }

            updateUI();

            addBlacklist.setOnClickListener(v -> {
                if (!editBlackListDomain.getText().toString().equals("")) {
                    String enteredText = editBlackListDomain.getText().toString();

                    myBlaDb.insertData(enteredText);
                    Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Added: " + enteredText + " to blacklist Database");

                    updateUI();
                    editBlackListDomain.setText("");
                } else {
                    Toast.makeText(getContext(), "Black list can't be blank", Toast.LENGTH_SHORT).show();
                }
            });

            addWhitelist.setOnClickListener(v -> {
                if (!editWhiteListDomain.getText().toString().equals("")) {
                    String enteredText = editWhiteListDomain.getText().toString();

                    myWhiDb.insertData(enteredText);
                    Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Added: " + enteredText + " to whitelist Database");

                    updateUI();
                    editWhiteListDomain.setText("");
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

    public void updateUI() {

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

//        System.out.println("You know where ya' oughta' hide next time? Back in France.");

    }

    public static BlackWhiteListsFragment newInstance() {
        return new BlackWhiteListsFragment();
    }
}
