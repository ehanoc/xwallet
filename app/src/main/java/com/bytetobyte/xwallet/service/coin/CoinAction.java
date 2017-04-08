package com.bytetobyte.xwallet.service.coin;

/**
 * Created by bruno on 22.03.17.
 */
public interface CoinAction <T> {
    public void execute(T... callbacks);

    public interface CoinActionCallback<R> {
        public void onResult(R result);
        public void onError(R result);

        void onChainSynced(R coin);
        void onCoinsReceived(String addressStr, long value, R coin);
    }
}
