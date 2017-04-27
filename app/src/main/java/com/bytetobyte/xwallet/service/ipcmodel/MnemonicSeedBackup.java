package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;

/**
 * Created by bruno on 27.04.17.
 */
public class MnemonicSeedBackup {
    private Date _creationDate;
    private String _mnemonicSeed;

    /**
     *
     * @param seed
     * @param creationDate
     */
    public MnemonicSeedBackup(String seed, Date creationDate) {
        this._creationDate = creationDate;
        this._mnemonicSeed = seed;
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
