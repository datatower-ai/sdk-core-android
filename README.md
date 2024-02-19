<p align="center">
    <a href="https://datatower.ai/" target="_blank">
        <picture>
            <source srcset="https://dash.datatower.ai/logo_v2.png" media="(prefers-color-scheme: dark)">
            <source srcset="https://dash.datatower.ai/logoWhite_v2.png" media="(prefers-color-scheme: light)" >
            <img src="https://dash.datatower.ai/logoWhite_v2.png" alt="DataTower.ai">
        </picture>
    </a>
</p>

<p align="center">
    <a title="Android" href="https://central.sonatype.com/artifact/ai.datatower/core" target="_blank"><img src="https://img.shields.io/maven-central/v/ai.datatower/core?logo=android&logoColor=70d68c&label=Android&labelColor=dbeffd" /></a>
    <a title="iOS" href="https://cocoapods.org/pods/datatower_ai_core" target="_blank"><img src="https://img.shields.io/cocoapods/v/datatower_ai_core?logo=ios&logoColor=000000&label=iOS&labelColor=f3f3f5" /></a>
    <a title="Unity" href="https://github.com/datatower-ai/core-unity/releases/latest" target="_blank"><img src="https://img.shields.io/github/v/release/datatower-ai/core-unity?logo=unity&logoColor=f7f7f7&label=Unity&labelColor=000000" /></a>
    <a title="Flutter" href="https://pub.dev/packages/datatower_ai_core" target="_blank"><img src="https://img.shields.io/pub/v/datatower_ai_core?logo=flutter&logoColor=2375c5f2&label=Flutter" /></a>
</p>

<p align="center">
  <span>Available in</span>
  <br />
  <a href="https://github.com/datatower-ai/sdk-core-android">Android</a>
  <span>-</span>
  <a href="https://github.com/datatower-ai/sdk-core-ios">iOS</a>
  <span>-</span>
  <a href="https://github.com/datatower-ai/sdk-core-unity">Unity</a>
  <span>-</span>
  <a href="https://github.com/datatower-ai/sdk-core-flutter">Flutter</a>
</p>

---

# DataTower.ai - Core - Android

## Getting Started

1. Register at [https://datatower.ai/][dt_url],
2. Log in and Create your Project,
3. Once created, you will get `appId` and `url` (*Reporting URL*) that will be used later.

## Usage

> **Please refer to our [API docs][api_doc_url]**

**Functionalities:**
- Event Tracking
- Timing Event Tracking
- User Property
- Ad Flow Tracking
- Attribution Integration/Linking

⚠ Remember to call _**DT.initSDK()**_ before any usage of other APIs:

```kotlin
import ai.datatower.analytics.DT

val appId = "xxx"
val reportingUrl = "xxx"

DT.initSDK(
    this,
    appId,
    reportingUrl,
    "",
    true,
    Log.DEBUG
)
```

## Other links

- **[DataTower.ai][dt_url]**
- **[DataTower.ai docs center][doc_url]**

[dt_url]: https://datatower.ai/
[api_doc_url]: https://docs.datatower.ai/docs/Android_SDK
[doc_url]: https://docs.datatower.ai/