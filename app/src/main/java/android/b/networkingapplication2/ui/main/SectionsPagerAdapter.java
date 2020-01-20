package android.b.networkingapplication2.ui.main;

import android.b.networkingapplication2.BlackWhiteListsFragment;
import android.b.networkingapplication2.BlockListsFragment;
import android.b.networkingapplication2.DomainBlockerActivity;
import android.b.networkingapplication2.DomainBlockerFragment;
import android.b.networkingapplication2.QueryLogFragment;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.b.networkingapplication2.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[] {
            R.string.domain_blocker,
            R.string.block_lists,
            R.string.black_white_lists,
            R.string.query_log};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 1: return BlockListsFragment.newInstance();
            case 2: return BlackWhiteListsFragment.newInstance();
            case 3: return QueryLogFragment.newInstance();
            default: return DomainBlockerFragment.newInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}