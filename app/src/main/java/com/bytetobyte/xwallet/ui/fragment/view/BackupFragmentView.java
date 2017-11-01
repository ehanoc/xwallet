package com.bytetobyte.xwallet.ui.fragment.view;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.ui.BackupFragmentViewContract;
import com.bytetobyte.xwallet.ui.fragment.BackupFragment;

import java.util.Map;
import java.util.Set;

/**
 * Created by bruno on 27.04.17.
 */
public class BackupFragmentView implements BackupFragmentViewContract, View.OnClickListener {

    private final BackupFragment _backupFrag;

    //
    private TextView _backupText;
    private ImageView _copyImg;

    /**
     *
     * @param backupFragment
     */
    public BackupFragmentView(BackupFragment backupFragment) {
        this._backupFrag = backupFragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View v = _backupFrag.getView();
        if (v == null) return;

        _backupText = (TextView) v.findViewById(R.id.backup_seed_ouput);
        _copyImg = (ImageView) v.findViewById(R.id.backup_copy_seed);

        _copyImg.setOnClickListener(this);

        _backupText.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backup_copy_seed:
                _backupFrag.copyToClipboard(_backupText.getText());
                break;

            default:
                break;
        }
    }

    /**
     *
     * @param seedBackup
     */
    public void displayBackup(MnemonicSeedBackup seedBackup) {
        //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//        String listaddp = "";
//        Set<Map.Entry<String, String>> addrKeyEntrySet = seedBackup.getAddrsKeys().entrySet();
//        for (Map.Entry<String, String> entry : addrKeyEntrySet) {
//            listaddp = listaddp + "\n Addr : " + entry.getKey() + ", Key :" + entry.getValue();
//        }

        String textToDisplay = String.format("%s\n \n Creation Date : %s",
                seedBackup.getMnemonicSeed(),
                seedBackup.getCreationDate()
        );

        if (seedBackup.getCoindId() == CoinManagerFactory.MONERO) {
            textToDisplay += "\n\n View key : " + seedBackup.getAddrsKeys().get("view");
            textToDisplay += "\n\n Spend key : " + seedBackup.getAddrsKeys().get("spend");
        }

        _backupText.setText(textToDisplay);
    }
}
