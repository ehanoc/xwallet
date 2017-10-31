package com.bytetobyte.xwallet.service;

import com.google.common.io.BaseEncoding;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.MonetaryFormat;

/**
 * Created by bruno on 23.03.17.
 */
public class Constants {
    public static final org.bitcoinj.core.NetworkParameters NETWORK_PARAMETERS = TestNet3Params.get();
  //  public static final org.bitcoinj.core.NetworkParameters NETWORK_PARAMETERS = MainNetParams.get();

    public static final char CHAR_HAIR_SPACE = '\u200a';
    public static final char CHAR_THIN_SPACE = '\u2009';
    public static final char CHAR_ALMOST_EQUAL_TO = '\u2248';
    public static final char CHAR_CHECKMARK = '\u2713';
    public static final char CURRENCY_PLUS_SIGN = '\uff0b';
    public static final char CURRENCY_MINUS_SIGN = '\uff0d';
    public static final String PREFIX_ALMOST_EQUAL_TO = Character.toString(CHAR_ALMOST_EQUAL_TO) + CHAR_THIN_SPACE;
    public static final int ADDRESS_FORMAT_GROUP_SIZE = 36;
    public static final int ADDRESS_FORMAT_LINE_SIZE = 36;

    public static final MonetaryFormat LOCAL_FORMAT = new MonetaryFormat().noCode().minDecimals(2).optionalDecimals();

    public static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();

    /** Maximum size of backups. Files larger will be rejected. */
    public static final long BACKUP_MAX_CHARS = 10000000;

    /** Currency code for the wallet name resolver. */
    public static final String WALLET_NAME_CURRENCY_CODE = NETWORK_PARAMETERS.getId().equals(NetworkParameters.ID_MAINNET) ? "btc" : "tbtc";

    /**
     * The earliest possible HD wallet.
     * This is taken as the date of the last edit of the BIP39 word list: Oct 16 2014:
     * https://github.com/bitcoin/bips/commits/master/bip-0039/bip-0039-wordlists.md
     */
    public static final String EARLIEST_HD_WALLET_DATE = "2014-10-16";
}
