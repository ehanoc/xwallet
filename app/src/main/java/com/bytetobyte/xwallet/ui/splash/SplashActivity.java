package com.bytetobyte.xwallet.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.XWalletBaseActivity;
import com.bytetobyte.xwallet.ui.lock.LockScreenActivity;
import com.bytetobyte.xwallet.ui.main.MainActivity;

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
//        if (!isTaskRoot()
//                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
//                && getIntent().getAction() != null
//                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
//
//            finish();
//            return;
//        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toLock(LockScreenActivity.REQUEST_CODE_LOCK);
            }
        }, 1000);
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

    @Override
    protected void onLockPinResult(int requestCode, int resultCode) {
        super.onLockPinResult(requestCode, resultCode);

        switch (resultCode) {
            case RESULT_OK:
                toHome();
                break;
        }
    }
}
