# GeofenceAppOSS

Welcome to my Geofence project. This was a project that started in 2017 with a group of four as part of the Bachelors of Science in Computer Science Thesis program. After a successful demo, we've decided to open source the project for anyone to use for their own projects or existing application.

## Setup

**Obtain your Google Maps API key**

To enable Maps functionality, you must obtain an API key. Please follow instructions in this guide on how to get your own API key. https://developers.google.com/maps/documentation/javascript/get-api-key

Once you get your API key, copy and paste it in the AndroidManifest.xml file

**(Optional) Download a GPS mock app for testing**

I recommend you download the GPS JoySTick Fake GPS app from the Play Store: https://play.google.com/store/apps/details?id=com.theappninjas.gpsjoystick&hl=en

## Important Notes

**Developer training guide for the Geofence API**

If you wish to know more information regarding the Geofence API on Android, please read this guide: https://developer.android.com/training/location/geofencing

Please keep in mind that Geofencing on Android has certain limitations. For example, you can only load up to 100 geofences on a single device. Starting with Android 8.0, Google has made some slight changes to how apps behave in the background. Geofences are affected and may result in response delay. 

If you encounter unexpected Geofence triggers with the application even if you cleared all geofences within the app or manually stopping the application, just restart your device.

**Sample Geofence in app**

The sample Geofence is located in Tokyo Tower. Coordinates as follows `35.6586545, 139.7454603`.

**Sample APK Download**

If you don't want to build the app from source, but want to test it on your own device. Go to `Releases` then select the latest version and download the `GeofenceOSS.apk` file. Be sure that you allow third-party sources on your device. 

## MIT License

MIT License

Copyright © 2017-2019 Dasutein

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated  documentation  files  (the  "Software"),  to  deal  in  the  Software  without restriction,  including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial 
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
