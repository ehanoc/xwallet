package com.bytetobyte.xwallet.service.coin.bitcoin;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

/**
 * Created by bruno on 10.04.17.
 */
public class DownloadProgressListener extends DownloadProgressTracker {
    private static final Logger log = LoggerFactory.getLogger(DownloadProgressListener.class);
    protected int originalBlocksLeft = -1;
    protected int lastPercent = 0;
    protected SettableFuture<Long> future = SettableFuture.create();
    protected boolean caughtUp = false;
    protected Date lastBlockDate;

    public DownloadProgressListener() {
    }

    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
        if(blocksLeft > 0 && this.originalBlocksLeft == -1) {
            this.startDownload(blocksLeft);
        }

        if(this.originalBlocksLeft == -1) {
            this.originalBlocksLeft = blocksLeft;
        } else {
            log.info("Chain download switched to {}", peer);
        }

        if(blocksLeft == 0) {
            this.doneDownload();
            this.future.set(Long.valueOf(peer.getBestHeight()));
        }

    }

    public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
        if(!this.caughtUp) {
            if(blocksLeft == 0) {
                this.caughtUp = true;
                this.doneDownload();
                this.future.set(Long.valueOf(peer.getBestHeight()));
            }

            if(blocksLeft >= 0 && this.originalBlocksLeft > 0) {
                double pct = 100.0D - 100.0D * ((double)blocksLeft / (double)this.originalBlocksLeft);
                if((int)pct != this.lastPercent) {

                    this.lastBlockDate = new Date(block.getTimeSeconds() * 1000L);
                    this.progress(pct, blocksLeft, this.lastBlockDate);
                    this.lastPercent = (int)pct;
                }

            }
        }
    }

    protected void progress(double pct, int blocksSoFar, Date date) {
        log.info(String.format(Locale.US, "Chain download %d%% done with %d blocks to go, block date %s", new Object[]{Integer.valueOf((int)pct), Integer.valueOf(blocksSoFar), Utils.dateTimeFormat(date)}));
    }

    protected void startDownload(int blocks) {
        log.info("Downloading block chain of size " + blocks + ". " + (blocks > 1000?"This may take a while.":""));
    }

    protected void doneDownload() {

    }

    public void await() throws InterruptedException {
        try {
            this.future.get();
        } catch (ExecutionException var2) {
            throw new RuntimeException(var2);
        }
    }

    public ListenableFuture<Long> getFuture() {
        return this.future;
    }
}
