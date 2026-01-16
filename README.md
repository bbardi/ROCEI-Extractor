# ROCEI-Extractor

A little tool cobbled together after decompiling the official Romanain CEI reader application for Android. This should be considered experimental. May only work if the card is connected to an ISO 7816 compliant reader as the Android app initiates PACE which requires the CAN number on the front while the desktop app only requires the 4 digit PIN.

TODO(maybe, probably if I won't forget I made this):
- Add PACE support
- Extract photo