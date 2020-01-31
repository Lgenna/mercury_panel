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

import database.BlockListBaseHelper;

public class BlockListsFragment extends Fragment {

    private TextView editDomain;
    private BlockListBaseHelper myBloDb;
    private RecyclerView mBlockListRecyclerView;
    public static View view;

    private static final String TAG = "BlockListsFragment";

//    Context context;

//    public BlockListsFragment(Context context) {
//        this.context = context;
//    }

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

            if (myBloDb.getAllData().getCount() == 0) {
                Log.w(TAG, "Blocklist DB empty, adding test data");
                myBloDb.insertData("1111", "true");
                myBloDb.insertData("2222", "false");
                myBloDb.insertData("3333", "true");
                myBloDb.insertData("4444", "false");
                myBloDb.insertData("5555", "true");
            }

            updateUI();

            addBlocklist.setOnClickListener(v -> {
                if (!editDomain.getText().toString().equals("")) {
                    String enteredText = editDomain.getText().toString();

                    myBloDb.insertData(
                            enteredText,
                            "true");
                    Toast.makeText(getContext(), "Added: " + enteredText, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Added: " + enteredText + " to blocklist Database");

                    updateUI();
                    editDomain.setText("");
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

        ArrayList<BlockList> mBlockLists = new ArrayList<>();

        Cursor blocklistRes = myBloDb.getAllData();

        if (blocklistRes.getCount() != 0) {
            while (blocklistRes.moveToNext()) {
                BlockList BlockListItem = new BlockList();
                BlockListItem.setDomain(blocklistRes.getString(1));
                BlockListItem.setStatus(Boolean.parseBoolean(blocklistRes.getString(2)));

                mBlockLists.add(BlockListItem);
            }
        }

        BlockListCustomAdapter blocklistcustomAdapter = new BlockListCustomAdapter(getContext(), mBlockLists);
        mBlockListRecyclerView.setAdapter(blocklistcustomAdapter);

//        System.out.println("You know where ya' oughta' hide next time? Back in France.");

    }

    public static BlockListsFragment newInstance() {
        return new BlockListsFragment();
    }
}
