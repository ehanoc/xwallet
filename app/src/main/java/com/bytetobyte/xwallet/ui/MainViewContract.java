package com.bytetobyte.xwallet.ui;

/**
 * Created by bruno on 24.04.17.
 */
public interface MainViewContract extends ViewContract {

    void setSyncProgress(int coinId, int progress);

    void startTutorial();
}
