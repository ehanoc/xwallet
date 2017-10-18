/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.WalletService;
import com.m2049r.xmrwallet.util.AsyncExchangeRate;
import com.m2049r.xmrwallet.util.BarcodeData;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.TxData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WalletActivity extends AppCompatActivity implements WalletFragment.Listener,
        WalletService.Observer, SendFragment.Listener, TxFragment.Listener,
        GenerateReviewFragment.ListenerWithWallet,
        ScannerFragment.Listener, ReceiveFragment.Listener {
    private static final String TAG = "WalletActivity";

    public static final String REQUEST_ID = "id";
    public static final String REQUEST_PW = "pw";

    Toolbar toolbar;

    private boolean synced = false;

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public boolean isWatchOnly() {
        return getWallet().isWatchOnly();
    }

    @Override
    public String getTxKey(String txId) {
        return getWallet().getTxKey(txId);
    }

    @Override
    public String getTxNotes(String txId) {
        return getWallet().getUserNote(txId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    private void startWalletService() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            acquireWakeLock();
            String walletId = extras.getString(REQUEST_ID);
            String walletPassword = extras.getString(REQUEST_PW);
            connectWalletService(walletId, walletPassword);
        } else {
            finish();
            //throw new IllegalStateException("No extras passed! Panic!");
        }
    }

    private void stopWalletService() {
        disconnectWalletService();
        releaseWakeLock();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        stopWalletService();
        super.onDestroy();
    }

    @Override
    public boolean hasWallet() {
        return haveWallet;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_info) {
            onWalletDetails();
            return true;
        } else if (i == R.id.action_receive) {
            onWalletReceive();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // activity restarted
            // we don't want that - finish it and fall back to previous activity
            finish();
            return;
        }

        setContentView(R.layout.wallet_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(R.string.app_name);

        boolean testnet = WalletManager.getInstance().isTestNet();
        if (testnet) {
            toolbar.setBackgroundResource(R.color.colorPrimaryDark);
        } else {
            toolbar.setBackgroundResource(R.color.moneroOrange);
        }

        Fragment walletFragment = new WalletFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, walletFragment).commit();
        Log.d(TAG, "fragment added");

        startWalletService();
        Log.d(TAG, "onCreate() done.");
    }

    public Wallet getWallet() {
        if (mBoundService == null) throw new IllegalStateException("WalletService not bound.");
        return mBoundService.getWallet();
    }

    private WalletService mBoundService = null;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((WalletService.WalletServiceBinder) service).getService();
            //Log.d(TAG, "setting observer of " + mBoundService);
            mBoundService.setObserver(WalletActivity.this);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String walletId = extras.getString(REQUEST_ID);
                if (walletId != null) {
                    setTitle(walletId);
                    setSubtitle("");
                }
            }
            updateProgress();
            //TODO show current pbProgress (eg. if the service is already busy saving last wallet)
            Log.d(TAG, "CONNECTED");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            setTitle(getString(R.string.wallet_activity_name));
            setSubtitle("");
            Log.d(TAG, "DISCONNECTED");
        }
    };

    void connectWalletService(String walletName, String walletPassword) {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        Intent intent = new Intent(getApplicationContext(), WalletService.class);
        intent.putExtra(WalletService.REQUEST_WALLET, walletName);
        intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_LOAD);
        intent.putExtra(WalletService.REQUEST_CMD_LOAD_PW, walletPassword);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d(TAG, "BOUND");
    }

    void disconnectWalletService() {
        if (mIsBound) {
            // Detach our existing connection.
            mBoundService.setObserver(null);
            unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "UNBOUND");
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    private PowerManager.WakeLock wl = null;

    void acquireWakeLock() {
        if ((wl != null) && wl.isHeld()) return;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getString(R.string.app_name));
        try {
            wl.acquire();
            Log.d(TAG, "WakeLock acquired");
        } catch (SecurityException ex) {
            Log.d(TAG, "WakeLock NOT acquired: " + ex.getLocalizedMessage());
            wl = null;
        }
    }

    public void releaseWakeLock() {
        if ((wl == null) || !wl.isHeld()) return;
        wl.release();
        wl = null;
        Log.d(TAG, "WakeLock released");
    }

    public void saveWallet() {
        if (mIsBound) { // no point in talking to unbound service
            Intent intent = new Intent(getApplicationContext(), WalletService.class);
            intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_STORE);
            startService(intent);
            Log.d(TAG, "STORE request sent");
        } else {
            Log.e(TAG, "Service not bound");
        }
    }

    //////////////////////////////////////////
    // WalletFragment.Listener
    //////////////////////////////////////////

    @Override
    public boolean hasBoundService() {
        return mBoundService != null;
    }

    @Override
    public Wallet.ConnectionStatus getConnectionStatus() {
        return mBoundService.getConnectionStatus();
    }

    @Override
    public long getDaemonHeight() {
        return mBoundService.getDaemonHeight();
    }

    @Override
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void setSubtitle(String subtitle) {
        toolbar.setSubtitle(subtitle);
    }

    @Override
    public void onSendRequest() {
        replaceFragment(new SendFragment(), null, null);
    }

    @Override
    public void onTxDetailsRequest(TransactionInfo info) {
        Bundle args = new Bundle();
        args.putParcelable(TxFragment.ARG_INFO, info);
        replaceFragment(new TxFragment(), null, args);
    }

    @Override
    public void forceUpdate() {
        try {
            onRefreshed(getWallet(), true);
        } catch (IllegalStateException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    ///////////////////////////
    // WalletService.Observer
    ///////////////////////////

    // refresh and return if successful
    @Override
    public boolean onRefreshed(final Wallet wallet, final boolean full) {
        Log.d(TAG, "onRefreshed()");
        try {
            final WalletFragment walletFragment = (WalletFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (wallet.isSynchronized()) {
                Log.d(TAG, "onRefreshed() synced");
                releaseWakeLock(); // the idea is to stay awake until synced
                if (!synced) {
                    onProgress(null);
                    saveWallet(); // save on first sync
                    synced = true;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            walletFragment.onSynced();
                        }
                    });
                }
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    walletFragment.onRefreshed(wallet, full);
                }
            });
            return true;
        } catch (ClassCastException ex) {
            // not in wallet fragment (probably send monero)
            Log.d(TAG, ex.getLocalizedMessage());
            // keep calm and carry on
        }
        return false;
    }

    @Override
    public void onWalletStored(final boolean success) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (success) {
                    Toast.makeText(WalletActivity.this, getString(R.string.status_wallet_unloaded), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WalletActivity.this, getString(R.string.status_wallet_unload_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    boolean haveWallet = false;

    @Override
    public void onWalletStarted(final boolean success) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (!success) {
                    Toast.makeText(WalletActivity.this, getString(R.string.status_wallet_connect_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
        if (!success) {
            finish();
        } else {
            haveWallet = true;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreatedTransaction(final PendingTransaction pendingTransaction) {
        try {
            final SendFragment sendFragment = (SendFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            runOnUiThread(new Runnable() {
                public void run() {
                    PendingTransaction.Status status = pendingTransaction.getStatus();
                    if (status != PendingTransaction.Status.Status_Ok) {
                        String errorText = pendingTransaction.getErrorString();
                        getWallet().disposePendingTransaction();
                        sendFragment.onCreatedTransactionFailed(errorText);
                    } else {
                        sendFragment.onCreatedTransaction(pendingTransaction);
                    }
                }
            });
        } catch (ClassCastException ex) {
            // not in spend fragment
            Log.d(TAG, ex.getLocalizedMessage());
            // don't need the transaction any more
            getWallet().disposePendingTransaction();
        }
    }

    @Override
    public void onSentTransaction(final boolean success) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (success) {
                    Toast.makeText(WalletActivity.this, getString(R.string.status_transaction_sent), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WalletActivity.this, getString(R.string.status_transaction_failed), Toast.LENGTH_SHORT).show();
                }
                popFragmentStack(null);
            }
        });
    }

    @Override
    public void onSetNotes(final boolean success) {
        try {
            final TxFragment txFragment = (TxFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!success) {
                        Toast.makeText(WalletActivity.this, getString(R.string.tx_notes_set_failed), Toast.LENGTH_LONG).show();
                    }
                    txFragment.onNotesSet(success);
                }
            });
        } catch (ClassCastException ex) {
            // not in tx fragment
            Log.d(TAG, ex.getLocalizedMessage());
            // never min
        }
    }

    @Override
    public void onProgress(final String text) {
        try {
            final WalletFragment walletFragment = (WalletFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            runOnUiThread(new Runnable() {
                public void run() {
                    walletFragment.onProgress(text);
                }
            });
        } catch (ClassCastException ex) {
            // not in wallet fragment (probably send monero)
            Log.d(TAG, ex.getLocalizedMessage());
            // keep calm and carry on
        }
    }

    @Override
    public void onProgress(final int n) {
        try {
            final WalletFragment walletFragment = (WalletFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            runOnUiThread(new Runnable() {
                public void run() {
                    walletFragment.onProgress(n);
                }
            });
        } catch (ClassCastException ex) {
            // not in wallet fragment (probably send monero)
            Log.d(TAG, ex.getLocalizedMessage());
            // keep calm and carry on
        }
    }

    private void updateProgress() {
        // TODO maybe show real state of WalletService (like "still closing previous wallet")
        if (hasBoundService()) {
            onProgress(mBoundService.getProgressText());
            onProgress(mBoundService.getProgressValue());
        }
    }

    ///////////////////////////
    // SendFragment.Listener
    ///////////////////////////

    @Override
    public void onSend(String notes) {
        if (mIsBound) { // no point in talking to unbound service
            Intent intent = new Intent(getApplicationContext(), WalletService.class);
            intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_SEND);
            intent.putExtra(WalletService.REQUEST_CMD_SEND_NOTES, notes);
            startService(intent);
            Log.d(TAG, "SEND TX request sent");
        } else {
            Log.e(TAG, "Service not bound");
        }

    }

    @Override
    public void onSetNote(String txId, String notes) {
        if (mIsBound) { // no point in talking to unbound service
            Intent intent = new Intent(getApplicationContext(), WalletService.class);
            intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_SETNOTE);
            intent.putExtra(WalletService.REQUEST_CMD_SETNOTE_TX, txId);
            intent.putExtra(WalletService.REQUEST_CMD_SETNOTE_NOTES, notes);
            startService(intent);
            Log.d(TAG, "SET NOTE request sent");
        } else {
            Log.e(TAG, "Service not bound");
        }

    }

    @Override
    public void onPrepareSend(TxData txData) {
        if (mIsBound) { // no point in talking to unbound service
            Intent intent = new Intent(getApplicationContext(), WalletService.class);
            intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_TX);
            intent.putExtra(WalletService.REQUEST_CMD_TX_DATA, txData);
            startService(intent);
            Log.d(TAG, "CREATE TX request sent");
        } else {
            Log.e(TAG, "Service not bound");
        }
    }

    @Override
    public void onPrepareSweep() {
        if (mIsBound) { // no point in talking to unbound service
            Intent intent = new Intent(getApplicationContext(), WalletService.class);
            intent.putExtra(WalletService.REQUEST, WalletService.REQUEST_CMD_SWEEP);
            startService(intent);
            Log.d(TAG, "SWEEP TX request sent");
        } else {
            Log.e(TAG, "Service not bound");
        }
    }

    @Override
    public String getWalletAddress() {
        return getWallet().getAddress();
    }

    void popFragmentStack(String name) {
        if (name == null) {
            getSupportFragmentManager().popBackStack();
        } else {
            getSupportFragmentManager().popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    void replaceFragment(Fragment newFragment, String stackName, Bundle extras) {
        if (extras != null) {
            newFragment.setArguments(extras);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(stackName);
        transaction.commit();
    }

    private void onWalletDetails() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Bundle extras = new Bundle();
                        extras.putString("type", GenerateReviewFragment.VIEW_TYPE_WALLET);
                        replaceFragment(new GenerateReviewFragment(), null, extras);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.details_alert_message))
                .setPositiveButton(getString(R.string.details_alert_yes), dialogClickListener)
                .setNegativeButton(getString(R.string.details_alert_no), dialogClickListener)
                .show();
    }

    @Override
    public void onDisposeRequest() {
        getWallet().disposePendingTransaction();
    }

    private boolean startScanFragment = false;

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (startScanFragment) {
            startScanFragment();
            startScanFragment = false;
        }
    }

    private void startScanFragment() {
        Bundle extras = new Bundle();
        replaceFragment(new ScannerFragment(), null, extras);
    }

    /// QR scanner callbacks
    @Override
    public void onScanAddress() {
        if (Helper.getCameraPermission(this)) {
            startScanFragment();
        } else {
            Log.i(TAG, "Waiting for permissions");
        }

    }

    private BarcodeData scannedData = null;

    @Override
    public boolean onAddressScanned(String uri) {
        BarcodeData bcData = parseMoneroUri(uri);
        if (bcData != null) {
            this.scannedData = bcData;
            popFragmentStack(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parse and decode a monero scheme string. It is here because it needs to validate the data.
     *
     * @param uri String containing a monero URL
     * @return BarcodeData object or null if uri not valid
     */
    public BarcodeData parseMoneroUri(String uri) {
        if (uri == null) return null;

        if (!uri.startsWith(ScannerFragment.QR_SCHEME)) return null;

        String noScheme = uri.substring(ScannerFragment.QR_SCHEME.length());
        Uri monero = Uri.parse(noScheme);
        Map<String, String> parms = new HashMap<>();
        String query = monero.getQuery();
        if (query != null) {
            String[] args = query.split("&");
            for (String arg : args) {
                String[] namevalue = arg.split("=");
                if (namevalue.length == 0) {
                    continue;
                }
                parms.put(Uri.decode(namevalue[0]).toLowerCase(),
                        namevalue.length > 1 ? Uri.decode(namevalue[1]) : null);
            }
        }
        String address = monero.getPath();
        String paymentId = parms.get(ScannerFragment.QR_PAYMENTID);
        String amountString = parms.get(ScannerFragment.QR_AMOUNT);
        long amount = -1;
        if (amountString != null) {
            amount = Wallet.getAmountFromString(amountString);
        }
        if ((paymentId != null) && !Wallet.isPaymentIdValid(paymentId)) {
            address = null;
        }

        if (Wallet.isAddressValid(address, WalletManager.getInstance().isTestNet())) {
            return new BarcodeData(address, paymentId, amount);
        }
        return null;
    }


    @Override
    public BarcodeData getScannedData() {
        BarcodeData data = scannedData;
        scannedData = null;
        return data;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case Helper.PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanFragment = true;
                } else {
                    String msg = getString(R.string.message_camera_not_permitted);
                    Log.e(TAG, msg);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    @Override
    public void onWalletReceive() {
        startReceive(getWalletAddress());
    }

    void startReceive(String address) {
        Log.d(TAG, "startReceive()");
        Bundle b = new Bundle();
        b.putString("address", address);
        startReceiveFragment(b);
    }

    void startReceiveFragment(Bundle extras) {
        replaceFragment(new ReceiveFragment(), null, extras);
        Log.d(TAG, "ReceiveFragment placed");
    }

    @Override
    public void onExchange(AsyncExchangeRate.Listener listener, String currencyA, String currencyB) {
        new AsyncExchangeRate(listener).execute(currencyA, currencyB);
    }
}