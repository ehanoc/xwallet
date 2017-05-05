package com.bytetobyte.xwallet.ui.activity.view;

import android.graphics.Typeface;

import com.bytetobyte.xwallet.R;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

/**
 * Created by bruno on 28.04.17.
 */
public class TutorialAnim {

    private final MainActivityView _activityView;

    /**
     *
     * @param activityView
     */
    public TutorialAnim(MainActivityView activityView) {
        this._activityView = activityView;
    }

    /**
     *
     */
    public void tutorialFocus() {
        new TapTargetSequence(_activityView.getActivity())
                .targets(focusWheelLense(),
                        focusWheel(),
                        focusSyncMeter(),
                        focusSettingsMenu()).start();
    }

    /**
     *
     */
    private TapTarget focusWheelLense() {               // `this` is an Activity
        return TapTarget.forView(_activityView.getWheelMiddleLense(), "Wheel lense", "Wallet functions, such as SEND / RECEIVE")
                // All options below are optional
                .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                .outerCircleAlpha(0.5f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(android.R.color.white)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.colorSecondary)  // Specify the color of the description text
                .textColor(android.R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.colorContentBackgrounds)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)                   // Whether to tint the target view's color
                .transparentTarget(true)               // Specify a custom drawable to draw as the target
                .targetRadius(60);
    }

    /**
     *
     */
    private TapTarget focusSettingsMenu() {               // `this` is an Activity
        return  TapTarget.forView(_activityView.getSettingsMenu(), "Settings Menu", "Where you can restore and backup your wallet. Please BACKUP your wallet's seed right away!")
                // All options below are optional
                .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                .outerCircleAlpha(0.5f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(android.R.color.white)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.colorSecondary)  // Specify the color of the description text
                .textColor(android.R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.colorContentBackgrounds)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)                   // Whether to tint the target view's color
                .transparentTarget(true)               // Specify a custom drawable to draw as the target
                .targetRadius(60);
    }

    /**
     *
     */
    private TapTarget focusSyncMeter() {            // `this` is an Activity
        return TapTarget.forView(_activityView.getBtcSyncMeter(), "Sync Meter", "Syncing with the chain is in progress. This might take a few minutes, but you will only need to do it once")
                // All options below are optional
                .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                .outerCircleAlpha(0.5f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(android.R.color.white)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.colorSecondary)  // Specify the color of the description text
                .textColor(android.R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.colorContentBackgrounds)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)                   // Whether to tint the target view's color
                .transparentTarget(true)               // Specify a custom drawable to draw as the target
                .targetRadius(60);
    }

    /**
     *
     */
    private TapTarget focusWheel() {              // `this` is an Activity
        return TapTarget.forView(_activityView.getWheelMenuLayout(), "This is the menu wheel", "Rotate to select menu items")
                // All options below are optional
                .outerCircleColor(R.color.colorPrimary)      // Specify a color for the outer circle
                .outerCircleAlpha(0.5f)            // Specify the alpha amount for the outer circle
                .targetCircleColor(R.color.colorAccent)   // Specify a color for the target circle
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(android.R.color.white)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.colorSecondary)  // Specify the color of the description text
                .textColor(android.R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.colorContentBackgrounds)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)                   // Whether to tint the target view's color
                .transparentTarget(true)               // Specify a custom drawable to draw as the target
                .targetRadius(60);
    }
}
