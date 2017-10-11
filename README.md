<!-- TODO: fix url -->
[![header](./header.png)](https://ramotion.com?utm_source=gthb&utm_medium=special&utm_campaign=cardslider-android-logo)

# CircleMenu for Android
[![Twitter](https://img.shields.io/badge/Twitter-@Ramotion-blue.svg?style=flat)](http://twitter.com/Ramotion)

<!-- TODO: fix urls -->
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/42eb7b00b93645c0812c045ab26cb3b7)](https://www.codacy.com/app/andreylos/cardslider-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Ramotion/cardslider-android&amp;utm_campaign=Badge_Grade)
[![CircleCI](https://circleci.com/gh/Ramotion/cardslider-android/tree/master.svg?style=svg)](https://circleci.com/gh/Ramotion/cardslider-android/tree/master)

## About
This project is maintained by Ramotion, Inc.<br>
We specialize in the designing and coding of custom UI for Mobile Apps and Websites.<br><br>**Looking for developers for your project?** 

<!-- TODO: fix url -->
<a href="https://ramotion.com?utm_source=gthb&utm_medium=special&utm_campaign=cardslider-android-contact-us/#Get_in_Touch" > <img src="https://github.com/Ramotion/navigation-stack/raw/master/contact_our_team@2x.png" width="150" height="30"></a>

<!-- TODO: add preview.gif -->
![Animation](./preview.gif)

<!-- TODO: fix url -->
The [Android mockup](https://store.ramotion.com?utm_source=gthb&utm_medium=special&utm_campaign=cardslider-android) available [here](https://store.ramotion.com/product/htc-one-a9-mockups?utm_source=gthb&utm_medium=special&utm_campaign=cardslider-android).

## Requirements
​
- Android 4.4 KitKat (API lvl 19) or greater
- Your favorite IDE

## Installation
​
<!-- TODO: add url -->
Just download the package from [here]() and add it to your project classpath, or just use the maven repo:

<!-- TODO: add package -->
Gradle:
```groovy
'com.ramotion.?'
```
SBT:
```scala
libraryDependencies += "?"
```
Maven:
```xml
<dependency>
	<groupId>com.ramotion.?</groupId>
	<artifactId>?</artifactId>
	<version>?</version>
</dependency>
```
​

## Basic usage

Place the `CircleMenuView` in your layout and set the icons and colors of the buttons, as shown below.
```xml
app:button_colors="@array/colors"
app:button_icons="@array/icons"
```

Example of arrays `colors` and `icons` in `res\values\buttons.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="icons">
        <item>@drawable/ic_home_white_24dp</item>
        <item>@drawable/ic_search_white_24dp</item>
        <item>@drawable/ic_notifications_white_24dp</item>
        <item>@drawable/ic_settings_white_24dp</item>
        <item>@drawable/ic_place_white_24dp</item>
    </array>
    <array name="colors">
        <item>@android:color/holo_blue_light</item>
        <item>@android:color/holo_green_dark</item>
        <item>@android:color/holo_red_light</item>
        <item>@android:color/holo_purple</item>
        <item>@android:color/holo_orange_light</item>
    </array>
</resources>
```

Or use the constructor
`CircleMenuView(@NonNull Context context, @NonNull List<Integer> icons, @NonNull List<Integer> colors)`,
to add `CircleMenuView` and configure the buttons programmatically (in the code).

Next, connect the event handler `CircleMenuView.EventListener` as shown below,
and override the methods you need.

```java
final CircleMenuView menu = (CircleMenuView) findViewById(R.id.circle_menu);
menu.setEventListener(new CircleMenuView.EventListener() {
    @Override
    public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
        Log.d("D", "onMenuOpenAnimationStart");
    }

    @Override
    public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
        Log.d("D", "onMenuOpenAnimationEnd");
    }

    @Override
    public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
        Log.d("D", "onMenuCloseAnimationStart");
    }

    @Override
    public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
        Log.d("D", "onMenuCloseAnimationEnd");
    }

    @Override
    public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
        Log.d("D", "onButtonClickAnimationStart| index: " + index);
    }

    @Override
    public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
        Log.d("D", "onButtonClickAnimationEnd| index: " + index);
    }
});
```

Here are the attributes you can specify through XML or related setters:
*`button_icons` - Array of buttons icons.
*`button_colors` - Array of buttons colors.
*`icon_menu` - Menu default icon.
*`icon_close` - Menu closed icon.
*`icon_color` - Menu icon color.
*`duration_ring` - Ring effect duration.
*`duration_open` - Menu opening animation duration.
*`duration_close` - Menu closing animation duration.
*`distance` - Distance between center button and buttons

## License
​
CircleMenu for Android is released under the MIT license.
See [LICENSE](./LICENSE.md) for details.

## Follow us

[![Twitter URL](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=https://github.com/Ramotion/circle-menu-android)
[![Twitter Follow](https://img.shields.io/twitter/follow/ramotion.svg?style=social)](https://twitter.com/ramotion)
