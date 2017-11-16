package com.bytetobyte.xwallet.ui.send.view;

import com.bytetobyte.xwallet.ui.ViewsContract;

import java.util.Map;

/**
 * Created by bruno on 16/11/2017.
 */

public interface ExtraOptionsViewContract extends ViewsContract {
    Map<Integer, Object> getExtraInputOptions();
}
