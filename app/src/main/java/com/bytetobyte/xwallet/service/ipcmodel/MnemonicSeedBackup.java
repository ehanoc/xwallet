package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;
import java.util.Map;

/**
 * Created by bruno on 27.04.17.
 */
public class MnemonicSeedBackup {
    private final Map<String, String> _addrsAndKeys;
    private Date _creationDate;
    private String _mnemonicSeed;
    private int _coinId;

    /**
     *  @param seed
     * @param creationDate
     * @param addrsKeys
     */
    public MnemonicSeedBackup(int coinId, String seed, Date creationDate, Map<String, String> addrsKeys) {
        this._coinId = coinId;
        this._creationDate = creationDate;
        this._mnemonicSeed = seed;
        this._addrsAndKeys = addrsKeys;
    }

    public int getCoindId() {
        return _coinId;
    }

    public Map<String, String> getAddrsKeys() {
        return _addrsAndKeys;
    }

    /**
     *
     * @return
     */
    public Date getCreationDate() {
        return _creationDate;
    }

    /**
     *
     * @return
     */
    public String getMnemonicSeed() {
        return _mnemonicSeed;
    }
}
