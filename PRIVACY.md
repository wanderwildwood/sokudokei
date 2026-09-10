# Privacy

Speedometer asks for one thing — where you are — and never sends it anywhere, because it
has no way to send anything anywhere.

That is the whole policy. The rest of this page is the evidence for it, because a privacy
policy that cannot be checked is just a promise.

## One permission

`app/src/main/AndroidManifest.xml` declares exactly one:

```
android.permission.ACCESS_FINE_LOCATION
```

A speedometer needs a fix good enough to tell one second from the next. Coarse location
cannot produce a speed at all, so there is nothing to be gained by asking for less.

There is **no `INTERNET` permission**. Without it Android will not let the app open a
network connection, so nothing it knows can leave the phone even by accident, and no
promise from me is load-bearing.

There is **no `ACCESS_BACKGROUND_LOCATION`**, and no service of any kind. The GPS and the
barometer are switched on when the app comes to the front and switched off again the
moment it leaves — see `MainActivity.kt`, which ties both to `ON_RESUME` and `ON_PAUSE`.

## What happens to your position

It is turned into the numbers on the screen and then thrown away. The app keeps no
history, no track, no trip log, and no file of any kind holding a position.

The only thing written to storage at all is which units you chose and which location
provider to read from — four short strings in `SharedPreferences`, and you can see every
one of them in `meter/Preferences.kt`.

## The barometer

Air pressure is read from the phone's own sensor. It never leaves the phone either, and
on a phone with no barometer the app simply does not show those rows.

## No analytics

No crash reporting, no telemetry, no advertising identifier, no third-party SDK of any
kind. The dependency list in `app/build.gradle.kts` is AndroidX, Jetpack Compose and
Mudita's MMD component library, and nothing else.

## Checking any of this for yourself

The source is here in full. If you would rather not read it:

```
aapt2 dump badging app-release.apk | grep uses-permission
```

That prints every permission the built app actually carries, which is the claim above
without having to trust me for it.
