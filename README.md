<img align="right" height="256" src="https://lut.im/3IqLwsAZWH/piFLRMOgNLWmiqB8.png">

# Red Moon [![Translation status](https://hosted.weblate.org/widgets/red-moon/-/svg-badge.svg)](https://hosted.weblate.org/engage/red-moon/?utm_source=widget)

Red Moon is a simple screen filter app for night time phone use. It helps you sleep
healthier by filtering out blue light, which can disrupt your sleep cycle
(circadian rhythm). And when your phone's lowest brightness isn't low enough,
Red Moon protects your eyes by making your screen even darker. 

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/repository/browse/?fdid=com.jmstudios.redmoon)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" 
      alt="Download from Google Play" 
      height="80">](https://play.google.com/store/apps/details?id=com.jmstudios.redmoon)
[<img src="art/direct-download.png" 
      alt="Direct download" 
      height="80">](https://github.com/raatmarien/red-moon/releases)

## Features

* Use the default filters or create your own, with separate color, intensity and dim settings.
* Automatically turn Red Moon on while the sun is down, or set custom times.
* Switch between filters right in the notification, without leaving the app you're using.
* Turn Red Moon on and off without opening the app using:
  * A widget or a shortcut on your home screen.
  * A quick settings tile, like WiFi or Airplane mode (Android 7.0+). 
  * The notification (if Red Moon is on, or was recently on).

## Screenshots

<img src="https://lut.im/uMUMujZSZU/rfHRfhIrDnDznetz.png" width="180" height="320" /> <img src="https://lut.im/nZBsmMs4KI/RutzvgfCCPSR2vDd.png" width="180" height="320" />
<img src="https://lut.im/MxfTcNiz5b/xhJDuKvyxEOJlc39.png" width="180" height="320" />
<img src="https://lut.im/t3Ll6xBLle/XrhZCJmIcggRHeHf.png" width="180" height="320" />
<img src="https://lut.im/O5bUIZVPDR/ddcBY3akDK6Sq1zU.png" width="180" height="320" />
<img src="https://lut.im/0YrVNYZbj2/lr0SDpIqy7jlpg13.png" width="180" height="320" />
<img src="https://lut.im/LPf77AuSRG/MsJbEeHXHxyQ7XSf.png" width="180" height="320" />
<img src="https://lut.im/7eRSVHlsoS/2OJNIqCG3NQTExZI.png" width="180" height="320" />
<img src="https://lut.im/CU0bQLa8RN/3o90N3QNCuNrOKnu.png" width="180" height="320" />
<img src="https://lut.im/7QAYwUXjqi/pWiBEF8fP8D3fpAp.png" width="180" height="320" />
<img src="https://lut.im/d9a4QT8Nek/9jgcvxedO1pnAfXl.png" width="180" height="320" />
<img src="https://lut.im/BQODkgLLGg/VGFnSMufAVBQoOSt.png" width="180" height="320" />

---
---

**All help is welcome!** Code is great and saves us work, but sometimes a
comment that clarifies a design issue or a link to the relevant part of some
documentation is just as helpful (and often more efficient, since you don't need
to familiarize yourself with Red Moon's code base).

## Bugs, feedback, and ideas

Issues are tagged with [`feedback wanted`], [`needs design`],
[`can't replicate`], or [`needs information`] (this one's usually technical)
based on what needs to be done next. There are also tags about priorities:  
<a href="https://hosted.weblate.org/engage/red-moon/?utm_source=widget">
<img align="right" src="https://hosted.weblate.org/widgets/red-moon/-/multi-auto.svg" alt="Translation status" />
</a>
- [`planned`]: The most important issues.
- [`help wanted`]: These would be `planned`, but there's a question or problem that we're stuck on. We'd love help getting un-stuck!
- [`someday/maybe`]: Worth adding, but not that important.

## Translations

**[Weblate] is the easiest way for most people.** You can work in your browser
instead of crawling through text files, and your translations will be
automatically be merged into this repository. If you'd prefer to make a pull
request, that's fine too, but if you're also changing code, please keep that
in a separate PR.

**Trust your judgement!** If there is a loose translation that you think is
simpler and clearer than the literal translation, go for it. We'll try not to
use strings in multiple places, even if the English word could be reused.

## Pull requests

[Bugfixes] are very safe PRs (likely to be merged), and a good way to learn the
code base, but often not as exciting as adding something new. Issues tagged
[`patches welcome`] are also safe, since they have the design worked out. If
you have something else in mind, [open a new issue] about it first, especially
if it requires increasing Red Moon's complexity (ie, adding options).

### Coding Style

**Prioritize legibility over dogmatism.** That said, consistency is nice, so here's a short list of what I've been doing. 

- Try very hard to stay under 100 characters per line. Try less hard to stay under 80.
- Indent 4 spaces, (exception: 8 spaces for the second line of variable assignments that have to wrap)
- constants are `ALL_CAPS`, functions, vals, and short-lived vars are `camelCase`, persistent vars are `mCamelCase`.
- Form suggests function: Group and align similar actions, and *don't* do that for dissimmilar ones, even if you could.
- Good code is (mostly) self-documenting. If you're commenting frequently, consider:
    - Refactoring into smaller functions with descriptive names
    - Converting comments to logs. Code confusing enough to require comments will probably require good logs to debug, and debug output that includes variable contents forces you to keep it up to date.
- Always use brackets with `if`, with one exception: one-liners with an `else` branch (`x = if (p) q else r`)
    - note: I (@smichel17) used to prefer omitting parenthesis from one-liners; some code still uses that style.

Again, **prioritize legibility over dogmatism.**

---
---

## Building

To build the app on GNU+Linux, clone the repository and run

`./gradlew build`

in the root directory.

Use

`./gradlew installDebug`

to install the app on a connected device or running emulator.

## License

"Red Moon" is a derivative of
"[Shades](https://github.com/cngu/shades)" by
[Chris Nguyen](https://github.com/cngu) used under the
[MIT License](https://github.com/cngu/shades/blob/e240edc1df3e6dd319cd475a739570ff8367d7f8/LICENSE). "Red
Moon" is licensed under the
[GNU General Public License version 3](https://www.gnu.org/licenses/gpl-3.0.html),
or (at your option) any later version by [the contributors](https://github.com/raatmarien/red-moon/graphs/contributors).

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

All used artwork is released into the public domain. Some of the icons
use cliparts from [openclipart.org](https://openclipart.org/), which
are all released in the public domain, namely:

* https://openclipart.org/detail/121903/full-moon
* https://openclipart.org/detail/219211/option-button-symbol-minimal-svg-markup
* https://openclipart.org/detail/20806/wolf-head-howl-1
* https://openclipart.org/detail/213998/nexus-5-flat
* https://openclipart.org/detail/192689/press-button

## Footnotes

\* Google Play and the Google Play logo are trademarks of Google Inc.

[`can't replicate`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22can%27t+replicate%22
[`needs design`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22needs+design%22
[`needs information`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22needs+information%22
[`feedback wanted`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22feedback+wanted%22
[`planned`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3Aplanned
[`help wanted`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22
[`someday/maybe`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3Asomeday%2Fmaybe
[`patches welcome`]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3A%22patches+welcome%22
[open a new issue]: https://github.com/raatmarien/red-moon/issues/new
[Bugfixes]: https://github.com/raatmarien/red-moon/issues?q=is%3Aissue+is%3Aopen+label%3Abug
[Weblate]: https://hosted.weblate.org/projects/red-moon/strings/
