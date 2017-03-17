package com.bytetobyte.xwallet.service.listeners;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.listeners.DownloadProgressTracker;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Created by bruno on 19.03.17.
 */
public class DownloadProgressListener extends DownloadProgressTracker {

    @Override
    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
        super.onChainDownloadStarted(peer, blocksLeft);

        System.out.println("onChainDownloadStarted, peer : " + peer + " blocksLeft : " + blocksLeft);
    }

    @Override
    public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);

        System.out.println("onBlocksDownloaded, peer : " + peer + " block : " + block + " blocksLeft : " + blocksLeft);
    }

    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        super.progress(pct, blocksSoFar, date);

        System.out.println("pct : " + pct + " blocksSoFar : " + blocksSoFar + " date : " + date);
    }

    @Override
    protected void startDownload(int blocks) {
        super.startDownload(blocks);

        System.out.println("blocks : " + blocks);
    }

    @Override
    protected void doneDownload() {
        super.doneDownload();

        System.out.println("doneDownload()");
    }

    @Override
    public void await() throws InterruptedException {
        super.await();

        System.out.println("await()");
    }
}
