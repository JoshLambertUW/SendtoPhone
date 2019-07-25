# Send to Phone | Android application

An Android application that can efficiently receive text data from a Chrome browser. Useful for saving links, text excerpts from web pages, and notes.

* Device will display a notification with preview when data is recieved
* The maximum size of a FCM message is 4KB, so the Firebase Messaging service may deploy a worker thread for larger messages
* This thread will download and then delete any pending messages on a Firestore database
* Received data is saved on device
* Device is added to database upon log in
* If device is off or the app is uninstalled, small messages will timeout in 4 weeks and large messages will timeout in 2 weeks
* Uses Google Authentication

Used with the [Chrome extension](https://github.com/JoshLambertUW/SendtoPhoneExtension)

## Getting Started

### Prerequisites

* Google Chrome with the Send to Phone extension installed
* Android device with Google Play services installed
* Google account

### Installing

```
To install, first import into Android Studio. Then run the app on an Android device or the emulator. Alternately, you can build the APK under the build menu.

```

## Deployment

If you would like to host the app yourself, you will need to:

1. Follow the same directions used for the [Chrome extension](https://github.com/JoshLambertUW/SendtoPhoneExtension) to create a Firebase project in the Firebase console.
2. Add the Android app to your Firebase project. You will need to use your own SHA-1 fingerprint from the Android project.
