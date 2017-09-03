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

    /**
     *  @param seed
     * @param creationDate
     * @param addrsKeys
     */
    public MnemonicSeedBackup(String seed, Date creationDate, Map<String, String> addrsKeys) {
        this._creationDate = creationDate;
        this._mnemonicSeed = seed;
        this._addrsAndKeys = addrsKeys;
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
