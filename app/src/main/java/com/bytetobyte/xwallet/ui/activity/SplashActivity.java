package com.bytetobyte.xwallet.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bytetobyte.xwallet.R;

/**
 * Created by bruno on 05.04.17.
 */
public class SplashActivity extends XWalletBaseActivity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // resume instead of restarting
        // check : http://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toLock();
            }
        }, 1000);
    }

    /**
     *
     */
    private void toLock() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String pinSet = prefs.getString(LockScreenActivity.PREFS_KEY_PIN, null);

        String lockAction = LockScreenActivity.UNLOCK_PIN_ACTION;
        if (pinSet == null) {
            lockAction = LockScreenActivity.SET_PIN_ACTION;
        }

        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.setAction(lockAction);
        startActivityForResult(intent, LockScreenActivity.REQUEST_CODE_LOCK);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                toHome();
                break;

            default:
                break;
        }
    }

    /**
     *
     */
    private void toHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}