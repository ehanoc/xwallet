package com.bytetobyte.xwallet.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.ui.ViewContract;
import com.bytetobyte.xwallet.ui.fragment.view.BackupFragmentView;
import com.bytetobyte.xwallet.ui.fragment.view.RecoverFragmentView;

/**
 * Created by bruno on 27.04.17.
 */
public class BackupFragment extends BaseDialogFragment {

    private BackupFragmentView _backupView;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_backup, container, false);
        _backupView = new BackupFragmentView(this);

        return rootView;
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _backupView.initViews();
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        getBaseActivity().requestMnemonic(CoinManagerFactory.BITCOIN);
    }

    /**
     *
     * @param seedBackup
     */
    @Override
    public void onMnemonicSeedBackup(MnemonicSeedBackup seedBackup) {
        _backupView.displayBackup(seedBackup);
    }

    /**
     *
     * @param text
     */
    public void copyToClipboard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getBaseActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData addData = ClipData.newPlainText("mnemonic_copied", text);
        clipboard.setPrimaryClip(addData);

        Toast.makeText(getBaseActivity(), "Added to clipboard!", Toast.LENGTH_SHORT).show();
    }
}
