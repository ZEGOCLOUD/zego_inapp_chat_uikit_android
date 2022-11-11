# Run the sample code

## Overview

The following describes how to run the sample code of the In-app Chat UIKit.


## Prerequisites

  1.  Create a project, and get the `AppID` and `AppSign` of your project. 
  2.  Subscribe to the **In-app Chat** service (Contact technical support if the subscript doesn't go well).
  <img src="https://storage.zego.im/sdk-doc/Pics/InappChat/ActivateZIMinConsole2.png">

- Platform-specific requirements:
    - Android Studio Arctic Fox (2020.3.1) or later.
    - Android SDK Packages: Android SDK 30, Android SDK Platform - Tools 30.x.x or later
    - An Android device or Simulator that is running on Android 5.0 or later. We recommend you use a real device. And please enable "USB Debugging".
    - The Android device and your computer are connected to the internet.


## Run the sample code

1. Open Android Studio on your computer, and select **Open an Existing Project**.
<img src="https://storage.zego.im/sdk-doc/Pics/zimkit_android/open_existing_project.png" width="60%">

2. Open the `ZIMKitDemo` under the `zimkit_android ` folder.
<img src="https://storage.zego.im/sdk-doc/Pics/zimkit_android/zimkit_demo.png" width="60%">

3. Navigate to the `KeyCenter.java` file under the `ZIMKitDemo/app/src/main/java/im/zego/zimkit/keycenter/` directory, and modify it with the `APP_ID` and `APP_SIGN` you get from ZEGOCLOUD Admin Console. 

    (Note: Append a letter `L` to the value of the `APP_ID`)

```java
    public class AppConfig {

    /**
     * appID. To get this, go to ZEGOCLOUD Admin Console (https://console.zegocloud.com/).
     */
    public static final Long APP_ID = 0L;
    /**
     * appSign. To get this, go to ZEGOCLOUD Admin Console (https://console.zegocloud.com/).
     */
    public static final String APP_SIGN = "";

}
```


5. Select a real Android device (recommended) that has been connected to the internet. When the device has successfully turned on the **developer mode** and **USB debugging** function, you can see the Android Studio change to the following figure:
<img src="https://storage.zego.im/sdk-doc/Pics/zimkit_android/before_link_devices_Android.png">
This means the Android Studio software has successfully connected to the Android device, and the sample code can be run on the device.

6. Click the **build and run** button on Android Studio to compile and run the sample code. 
<img src="https://storage.zego.im/sdk-doc/Pics/zimkit_android/demo_build_Andriod.png">

Congratulations! So far, you have finished all the steps, and this is what you gonna see when the sample code is run successfully:

<img src="https://storage.zego.im/sdk-doc/Pics/zimkit_android/zimkit_android_login.jpeg"  width="30%">


## More to explore

* To get started swiftly, follow the steps in this doc: [Integrate the SDK](https://docs.zegocloud.com/article/14664)
* To explore more customizable components, check this out: [Component overview](https://docs.zegocloud.com/article/14661)



## Get support

If you have any questions regarding bugs and feature requests, visit the [ZEGOCLOUD community](https://discord.gg/EtNRATttyp).
