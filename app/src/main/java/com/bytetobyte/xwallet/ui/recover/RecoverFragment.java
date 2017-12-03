package com.bytetobyte.xwallet.ui.recover;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.recover.view.RecoverFragmentView;
import com.bytetobyte.xwallet.util.EncryptUtils;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by bruno on 26.04.17.
 */
public class RecoverFragment extends BaseDialogFragment implements CalendarDatePickerDialogFragment.OnDateSetListener, RadialTimePickerDialogFragment.OnTimeSetListener {
    private static final String FRAG_TAG_DATE_PICKER = "FRAG_TAG_DATE_PICKER";
    private static final String FRAG_TAG_TIME_PICKER = "FRAG_TAG_TIME_PICKER";

    private RecoverFragmentView _rView;
    private Date _lastDateSet;
    private Calendar _calendar;
    private boolean _isViewOnlyWallet;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet_recover, container, false);

        _rView = new RecoverFragmentView(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _rView.initViews();
    }

    /**
     *
     */
    public void launchDatePicker() {
        _calendar = Calendar.getInstance();

        Calendar dateStart = Calendar.getInstance();
        dateStart.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateStart.set(2010, 5, 1);

        MonthAdapter.CalendarDay calendarStartDay = new MonthAdapter.CalendarDay(dateStart);

        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(this)
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setPreselectedDate(2017, 1, 1)
                .setDateRange(calendarStartDay, null)
                .setDoneText("Yay")
                .setCancelText("Nop")
                .setThemeCustom(R.style.PickerDialogStyle);
        cdp.show(getBaseActivity().getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
    }

    /**
     *
     * @param dialog
     * @param year
     * @param monthOfYear
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, final int year, final int monthOfYear, final int dayOfMonth) {
        //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

       // _calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        _calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

        _lastDateSet = _calendar.getTime();
        System.out.println("Last date : " + _lastDateSet.getTime() / 1000 + " dt :" + _lastDateSet);
        _rView.setDate(_lastDateSet);

      //  long timeSinceEpoch = Date.UTC(year, monthOfYear, dayOfMonth, 0, 0, 0);

//        RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
//                .setOnTimeSetListener(this)
//                .setStartTime(10, 10)
//                .setDoneText("Yay")
//                .setCancelText("Nop")
//                .setThemeCustom(R.style.PickerDialogStyle);
//        rtpd.show(getBaseActivity().getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
    }

    /**
     *
     * @param dialog
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, final int hourOfDay, final int minute) {
        NumberPickerBuilder npb = new NumberPickerBuilder()
                .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
                    @Override
                    public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
                        _calendar.set(_calendar.get(Calendar.YEAR),
                                _calendar.get(Calendar.MONTH),
                                _calendar.get(Calendar.DAY_OF_MONTH),
                                hourOfDay,
                                minute,
                                number.intValue());

                        _lastDateSet = _calendar.getTime();
                        System.out.println("Last date : " + _lastDateSet.getTime() / 1000);
                        _rView.setDate(_lastDateSet);
                    }
                })
                .setFragmentManager(getBaseActivity().getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment)
                .setLabelText("Seconds!");
        npb.show();
    }

    /**
     *
     * @param seed
     */
    public void promptWalletRecovery(final String seed) {
        String blockHeightStr = _rView.getBlockHeight();
        long blockHeight = 800000;
        try {
            blockHeight = Long.valueOf(blockHeightStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // final temp to be used in dialog build
        final long finalBlockHeight = blockHeight;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
        builder.setTitle("Recovery")
                .setMessage("You sure you want to recover wallet from the seed : " + seed + " ? \n\n This might take some time, please keep your phone plugged in!")
                .setPositiveButton("Yes, Recover!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getBaseActivity().recoverWallet(getBaseActivity().getSelectedCoin(), seed, _lastDateSet, finalBlockHeight, _isViewOnlyWallet);
                        Toast.makeText(RecoverFragment.this.getBaseActivity(), "Initiating recovery... Please wait!", Toast.LENGTH_SHORT).show();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // +1 due to index starts at 0 and coin id start at 1
                                getBaseActivity().showMenuSelection(getBaseActivity().getSelectedCoin() - 1);
                                getBaseActivity().resetCoinProgress();
                            }
                        }, 500);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     *
     * @param isChecked
     */
    public void onViewOnlyWallet(boolean isChecked) {
        this._isViewOnlyWallet = isChecked;
    }

    /**
     *
     * @return
     */
    public boolean isViewOnlyWallet() {
        return _isViewOnlyWallet;
    }
}
