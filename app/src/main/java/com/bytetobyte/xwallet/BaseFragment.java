package com.bytetobyte.xwallet;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by bruno on 12.04.17.
 */
public abstract class BaseFragment extends Fragment {

    private XWalletBaseActivity _baseActivity;

    /**
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        _baseActivity = (XWalletBaseActivity) context;
    }

    /**
     *
     * @return
     */
    public XWalletBaseActivity getBaseActivity() {
        return _baseActivity;
    }

    protected abstract void onServiceReady();
}
