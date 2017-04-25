package com.bytetobyte.xwallet.ui.activity.view;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.MainViewContract;
import com.bytetobyte.xwallet.ui.activity.MainActivity;
import com.bytetobyte.xwallet.ui.listeners.MainBoomListener;
import com.bytetobyte.xwallet.views.CircleLayout;
import com.bytetobyte.xwallet.views.WheelMenuLayout;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import de.hdodenhof.circleimageview.CircleImageView;
import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by bruno on 24.04.17.
 */
public class MainActivityView implements MainViewContract {

    private final MainActivity _act;

    private ArcProgress _chainArcProgress;
    private CircleImageView _lense;
    private FrameLayout _content;
    private BadgeView _badgeView;
    private BoomMenuButton _bmb;

    /**
     *
     * @param activity
     */
    public MainActivityView(MainActivity activity) {
        this._act = activity;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        initMenuBoom();

        _chainArcProgress = (ArcProgress) _act.findViewById(R.id.main_arc_progress);
        _lense = (CircleImageView) _act.findViewById(R.id.lense_middle_image);
        _content = (FrameLayout) _act.findViewById(R.id.xwallet_content_layout);

        WheelMenuLayout wheelMenuLayout = (WheelMenuLayout) _act.findViewById(R.id.wheelMenu);
        _badgeView = (BadgeView) _act.findViewById(R.id.lense_badgeview);

        CircleLayout mCircleLayout = (CircleLayout) _act.findViewById(R.id.circle_layout_id);
        ImageView mWheelBackgroundMenu = (ImageView) _act.findViewById(R.id.wheelmenu_background_menu);

        if (wheelMenuLayout != null) {
            wheelMenuLayout.prepareWheelUIElements(mCircleLayout, mWheelBackgroundMenu);
            wheelMenuLayout.setWheelChangeListener(new WheelMenuLayout.WheelChangeListener() {
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
    private void initMenuBoom() {
        int[] boomsButtons = { R.drawable.ic_send, R.drawable.ic_receive};

        _bmb = (BoomMenuButton) _act.findViewById(R.id.bmb);
        for (int i = 0; i < _bmb.getButtonPlaceEnum().buttonNumber(); i++) {
            _bmb.addBuilder(new SimpleCircleButton.Builder()
                    .normalImageRes(boomsButtons[i])
            );
        }

        _bmb.setOnBoomListener(new MainBoomListener(_act));
    }

    /**
     *
     * @param progress
     */
    @Override
    public void setSyncProgress(int progress) {
        _chainArcProgress.setProgress(100);
    }
}
