package com.bytetobyte.xwallet.ui.activity.view;

import android.app.Activity;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.MainViewContract;
import com.bytetobyte.xwallet.ui.activity.MainActivity;
import com.bytetobyte.xwallet.ui.listeners.MainBoomListener;
import com.bytetobyte.xwallet.ui.listeners.SettingsBoomListener;
import com.bytetobyte.xwallet.views.CircleLayout;
import com.bytetobyte.xwallet.views.WheelMenuLayout;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Util;

import de.hdodenhof.circleimageview.CircleImageView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by bruno on 24.04.17.
 */
public class MainActivityView implements MainViewContract {

    // main wheel boom
//    public static final int SYNC_BOOM_INDEX = 0;
//    public static final int STOP_BOOM_INDEX = 3;
    public static final int BACKUP_BOOM_INDEX = 0;
    public static final int RECOVER_BOOM_INDEX = 1;
    public static final int SEND_BOOM_INDEX = 2;
    public static final int RECEIVE_BOOM_INDEX = 3;

    private final MainActivity _act;
    private final TutorialAnim _tutorialHandler;

    private ArcProgress _btcChainArcProgress;
    private ArcProgress _xmrChainArcProgress;

    private CircleImageView _lense;
    private FrameLayout _content;
    private BadgeView _badgeView;
    private BoomMenuButton _bmb;
    private BoomMenuButton _settingsBmb;
    private WheelMenuLayout _wheelMenuLayout;

    /**
     *
     * @param activity
     */
    public MainActivityView(MainActivity activity) {
        this._act = activity;
        this._tutorialHandler = new TutorialAnim(this);
    }

    /**
     *
     */
    @Override
    public void initViews() {
        initCircleMenuBoom();
        initSettingsBoom();

        _btcChainArcProgress = (ArcProgress) _act.findViewById(R.id.main_arc_progress);
        _xmrChainArcProgress = (ArcProgress) _act.findViewById(R.id.xmr_arc_progress);
        _lense = (CircleImageView) _act.findViewById(R.id.lense_middle_image);
        _content = (FrameLayout) _act.findViewById(R.id.xwallet_main_content_layout);

         _wheelMenuLayout = (WheelMenuLayout) _act.findViewById(R.id.wheelMenu);
        _badgeView = (BadgeView) _act.findViewById(R.id.lense_badgeview);

        CircleLayout mCircleLayout = (CircleLayout) _act.findViewById(R.id.circle_layout_id);
        ImageView mWheelBackgroundMenu = (ImageView) _act.findViewById(R.id.wheelmenu_background_menu);

        if (_wheelMenuLayout != null) {
            _wheelMenuLayout.prepareWheelUIElements(mCircleLayout, mWheelBackgroundMenu);
            _wheelMenuLayout.setWheelChangeListener(new WheelMenuLayout.WheelChangeListener() {
                @Override
                public void onSelectionChange(int selectedPosition) {
                    if (_badgeView != null) {

                        int contentIndex = selectedPosition + 1;

                        _act.showMenuSelection(contentIndex);
                        _badgeView.setValue(contentIndex);
                    }
                }
            });
        }

        _lense.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                _bmb.boom();
                return false;
            }
        });
    }

    /**
     *
     */
    private void initSettingsBoom() {
        int[] settingsBtns = { R.drawable.ic_info, android.R.drawable.ic_menu_preferences};
        String[] settingsStrs = { "Info & Credits",  "Settings" };


        _settingsBmb = (BoomMenuButton) _act.findViewById(R.id.main_settings_boom);
        for (int i = 0; i < _settingsBmb.getButtonPlaceEnum().buttonNumber(); i++) {
            _settingsBmb.addBuilder(new HamButton.Builder()
                    .normalImageRes(settingsBtns[i])
                    .imagePadding(new Rect(10, 10, 10, 10))
                    .normalText(settingsStrs[i])
            );
        }

        _settingsBmb.setOnBoomListener(new SettingsBoomListener(_act));
    }

    /**
     *
     */
    private void initCircleMenuBoom() {
        int[] boomsButtons = { R.drawable.ic_backup, R.drawable.ic_recover_wallet , R.drawable.ic_send, R.drawable.ic_receive};
        String [] btnsText = { "Backup", "Recover", "Send", "Receive" };

        _bmb = (BoomMenuButton) _act.findViewById(R.id.bmb);
        for (int i = 0; i < _bmb.getButtonPlaceEnum().buttonNumber(); i++) {
            _bmb.addBuilder(new TextInsideCircleButton.Builder()
                    .normalImageRes(boomsButtons[i])
                    .normalText(btnsText[i])
                    .imageRect(new Rect(Util.dp2px(25), Util.dp2px(20), Util.dp2px(55), Util.dp2px(55)))
            );
        }

        _bmb.setOnBoomListener(new MainBoomListener(_act));
    }

    /**
     *
     */
    @Override
    public void startTutorial() {
        //this._tutorialHandler.tutorialFocus();
    }

    /**
     *
     * @param progress
     */
    @Override
    public void setSyncProgress(int coinId, int progress) {
        if (progress == 0) return;

        if (coinId == CoinManagerFactory.BITCOIN)
            _btcChainArcProgress.setProgress(progress);
        else if (coinId == CoinManagerFactory.MONERO)
            _xmrChainArcProgress.setProgress(progress);
    }

    public View getWheelMiddleLense() {
        return _bmb;
    }

    public Activity getActivity() {
        return _act;
    }

    public View getSettingsMenu() {
        return _settingsBmb;
    }

    public View getBtcSyncMeter() {
        return _btcChainArcProgress;
    }

    public View getWheelMenuLayout() {
        return _wheelMenuLayout;
    }
}
