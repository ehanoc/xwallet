package com.bytetobyte.xwallet.service.listeners;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.PeerDiscoveredEventListener;

import java.util.Set;

/**
 * Created by bruno on 20.03.17.
 */
public class XWalletPeerListener implements PeerConnectedEventListener,
        PeerDisconnectedEventListener, PeerDiscoveredEventListener {

    /**
     *
     * @param peer
     * @param i
     */
    @Override
    public void onPeerConnected(Peer peer, int i) {
        System.out.println("onPeerConnected(), peer: " + peer + ", i: " + i);
    }

    /**
     *
     * @param peer
     * @param i
     */
    @Override
    public void onPeerDisconnected(Peer peer, int i) {
        System.out.println("onPeerDisconnected(), peer: " + peer + ", i: " + i);
    }

    /**
     *
     * @param set
     */
    @Override
    public void onPeersDiscovered(Set<PeerAddress> set) {
        System.out.println("onPeersDiscovered(), peers: " + set);
    }
}
