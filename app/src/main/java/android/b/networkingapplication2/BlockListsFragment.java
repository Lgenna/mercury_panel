package android.b.networkingapplication2;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import database.BlockListBaseHelper;

public class BlockListsFragment extends Fragment {

    ImageButton addBlocklist;
    TextView editDomain;
    BlockListBaseHelper myBloDb;
    RecyclerView mBlockListRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_blocklists, container, false);

        addBlocklist = view.findViewById(R.id.blocklist_urls_add);
        editDomain = view.findViewById(R.id.custom_blocklists_textfield);

        mBlockListRecyclerView = view.findViewById(R.id.blocklist_recycler_view);

//        final DomainBlockerActivity Refrigerated_Truck = new DomainBlockerActivity();

        try {
            myBloDb = new BlockListBaseHelper(getContext());

            addBlocklist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!editDomain.getText().toString().equals("")) {
                        myBloDb.insertData(
                                editDomain.getText().toString(),
                                "true");
                        Toast.makeText(getContext(), "Block list added", Toast.LENGTH_LONG).show();
                        updateUI();
                        editDomain.setText("");
                    } else {
                        Toast.makeText(getContext(), "Block list can't be blank", Toast.LENGTH_LONG).show();
                    }
                }
            });

            LinearLayoutManager blocklistlinearLayoutManager = new LinearLayoutManager(getContext());
            mBlockListRecyclerView.setLayoutManager(blocklistlinearLayoutManager);
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }

        return view;
    }

    public void updateUI() {

        Cursor blocklistRes = myBloDb.getAllData();
        ArrayList<BlockList> mBlockLists = new ArrayList<>();

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
    }

    public static BlockListsFragment newInstance() {
        return new BlockListsFragment();
    }
}
