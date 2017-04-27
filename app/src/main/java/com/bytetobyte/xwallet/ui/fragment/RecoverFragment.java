package com.bytetobyte.xwallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.fragment.view.RecoverFragmentView;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;

import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by bruno on 26.04.17.
 */
public class RecoverFragment extends BaseDialogFragment implements CalendarDatePickerDialogFragment.OnDateSetListener {
    private static final String FRAG_TAG_DATE_PICKER = "FRAG_TAG_DATE_PICKER";

    private RecoverFragmentView _rView;
    private Date _lastDateSet;

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
        Calendar dateStart = Calendar.getInstance();
        dateStart.set(2010, 01, 01);

        MonthAdapter.CalendarDay calendarStartDay = new MonthAdapter.CalendarDay(dateStart);

        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(this)
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setPreselectedDate(2017, 01, 01)
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
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);

        _lastDateSet = cal.getTime();
        _rView.setDate(_lastDateSet);
    }

    /**
     *
     * @param seed
     */
    public void promptWalletRecovery(final String seed) {
        new SweetAlertDialog(getBaseActivity(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Recovery")
                .setContentText("You sure you want to recover wallet from the seed : " + seed)
                .setConfirmText("Yes, Recover!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        getBaseActivity().recoverWallet(CoinManagerFactory.BITCOIN, seed, _lastDateSet);
                        getBaseActivity().showMenuSelection(0);
                    }
                })
                .setCancelText("Cancel")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                    }
                })
                .show();
    }

}
