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

import database.BlackWhiteListBaseHelper;

public class BlackWhiteListsFragment extends Fragment {

    private TextView whitelist_textbox;
    private TextView editBlackListDomain;
    private BlackWhiteListBaseHelper myBlaDb, myWhiDb;
    private RecyclerView mWhiteListRecyclerView;
    private RecyclerView mBlackListRecyclerView;
    public static View view;

    private static final String TAG = "BlackWhiteListsFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ImageButton addBlacklist;
        ImageButton addWhitelist;

        view = inflater.inflate(R.layout.activity_black_white_lists, container, false);

        myBlaDb = new BlackWhiteListBaseHelper(getContext(), "blacklist.db");
        myWhiDb = new BlackWhiteListBaseHelper(getContext(), "whitelist.db");

        addBlacklist = view.findViewById(R.id.addBlacklist);
        editBlackListDomain = view.findViewById(R.id.blacklist_textbox);
        mBlackListRecyclerView = view.findViewById(R.id.blacklist_domains_recycler_view);

        LinearLayoutManager blacklistlinearLayoutManager = new LinearLayoutManager(getContext());
        mBlackListRecyclerView.setLayoutManager(blacklistlinearLayoutManager);

        try {

            if (myBlaDb.getAllData().getCount() == 0) {
                Log.w(TAG, "Blacklist DB empty, adding test data");
                myBlaDb.insertData("1111");
                myBlaDb.insertData("2222");
                myBlaDb.insertData("3333");
                myBlaDb.insertData("4444");
                myBlaDb.insertData("5555");
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

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }
        return view;
    }

    public void updateUI() {

        ArrayList<BlackList> mBlackLists = new ArrayList<>();

        Cursor blacklistRes = myBlaDb.getAllData();

        if (blacklistRes.getCount() != 0) {
            while (blacklistRes.moveToNext()) {
                BlackList BlackListItem = new BlackList();
                BlackListItem.setDomain(blacklistRes.getString(1));

                mBlackLists.add(BlackListItem);
            }
        }

        BlackListCustomAdapter blacklistcustomAdapter = new BlackListCustomAdapter(getContext(), mBlackLists);
        mBlackListRecyclerView.setAdapter(blacklistcustomAdapter);

//        System.out.println("You know where ya' oughta' hide next time? Back in France.");

    }

    public static BlackWhiteListsFragment newInstance() {
        return new BlackWhiteListsFragment();
    }
}
