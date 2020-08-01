## Wiiload for Android

**Wiiload for Android** is an Android port of [JWiiload](https://github.com/vgmoose/jwiiload) and is used for sending homebrew apps to the Wii or Wii U. It accepts .dol, .elf, .rpx, and supports sending .zip to 1.0.8 for installation. This can be useful when there is no computer around, yet an app needs to be quickly installed. The application is simple to use, and includes full functionality of the computer version, as well as more faith in the scanning functions, PLUS the option to have the Wii use a static IP address, although not as useful as scanning.

As of August 2020 (yes really!) it now supports RPX files as well for the Wii U's HBL.

### Requirements
- Android Device Jelly Bean or higher with WiFi capabilities
- Be on the same network as your Wii or Wii U

### Usage
The application can launch itself from intents (from .zip, .elf, .rpx, or .dol from other file browser's open with... dialogs) or through the own built in filebrowser which isn't as simple as it appears. It displays images and contains filetype filtering, and support of a home directory and new file support. The application checks if the user's wifi is on, and if it's not, requests it to be enabled. The scan option should locate the Wii if it is on the same Wi-Fi network as the phone, usually returning the IP address of the Wii in seconds, assuming the Wii has been put on the Homebrew Channel. Arguments and port can be specified from the menu on the main screen. There is an ad at the bottom of the screen, but it can be disabled by entering "I love you" under the "Make New Folder" dialog in the file browser. Please report any errors!

### Download
From the [Releases](https://github.com/vgmoose/Wiiload-for-Android/releases) page. This app was on Google play for 7 years before being removed for "Violation of Device and Network Abuse policy and sections 4.8 and 4.9 of the Developer Distribution Agreement", specifically:

> We don’t allow apps that interfere with, disrupt, damage, or access in an unauthorized manner the user’s device, other devices or computers, servers, networks, application programming interfaces (APIs), or services, including but not limited to other apps on the device, any Google service, or an authorized carrier’s network.

This app helps run [Homebrew software](https://en.wikipedia.org/wiki/Homebrew_(video_games)) easier on a modded Wii console. Not only does it not perform any modding, but even if it did, Google's terms are using a very loose definition of "unauthorized manner"– In this context it seems to just mean something that Nintendo doesn't like. The user's use of this app to interact with their own Wii device is obviously a very authorized action!

### License
This software is licensed under the GPLv3.

> Free software is software that gives you the user the freedom to share, study and modify it. We call this free software because the user is free. - [Free Software Foundation](https://www.fsf.org/about/what-is-free-software)

### Screen shots
<img src="https://i.imgur.com/NL0EWfW.png" width=300 /> <img src="https://i.imgur.com/zvkTQ4n.png" width=300 /> <img src="https://i.imgur.com/bVzz38a.png" width=300 />
