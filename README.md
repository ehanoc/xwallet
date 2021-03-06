# **XWallet**

<a href='https://play.google.com/store/apps/details?id=com.bytetobyte.xwallet&hl=en&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width="200"/></a>

A pet project, a bitcoin wallet.

- Thin Client (Partial chain sync)
- No third-party servers
- Open source


<img src="https://raw.githubusercontent.com/ehanoc/xwallet/master/promos/tx_screenshot.png" width="100">

<img src="https://raw.githubusercontent.com/ehanoc/xwallet/master/promos/lock_screenshot.png" width="100">

<img src="https://raw.githubusercontent.com/ehanoc/xwallet/master/promos/color_scheme.png" width="400">

## **Techy Info**

_**Service**_ (All wallet management; dedicated thread) **< ----- IPC  ----- >** _**Main Thread**_ (UI / Flow ) 

## Contributions

 - Feel free
 - Just keep clean dependencies of specific coin (only btc atm) on the client side.
 
### Adding other currencies ? 

- Service side
- Pay attention to service structure.

├── **service**

│   ├── BlockchainService.java

│   ├── **coin**

│   │   ├── **bitcoin**

│   │   │   ├── **actions**

│   │   │   │   ├── BitcoinSendAction.java

│   │   │   │   └── BitcoinSetupAction.java

│   │   │   ├── Bitcoin.java

│   │   │   ├── BitcoinManager.java

│   │   │   ├── DownloadProgressListener.java

│   │   │   └── WalletUtils.java

│   │   ├── CoinAction.java (Interface)

│   │   ├── CoinManagerFactory.java

│   │   ├── CoinManager.java (Interface)

│   │   └── CurrencyCoin.java (Interface)


# License

- GPL v3.0

## Dependencies Credits 

- https://github.com/bitcoinj
- https://github.com/lzyzsd/CircleProgress
- https://github.com/Nightonke/BoomMenu
- https://github.com/hdodenhof/CircleImageView
- https://github.com/PhilJay/MPAndroidChart
- https://github.com/bumptech/glide
- https://github.com/pedant/sweet-alert-dialog
- https://github.com/zxing/zxing
- https://github.com/nisrulz/qreader
