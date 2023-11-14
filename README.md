## 工程结构

- datatowerai-core/: SDK 核心代码
- demo_analytics/: SDK 接口使用范例，用于开发和测试 SDK

## 核心代码

核心代码位于 `ai.datatower.analytics` 包下:

- DT、DTAnalytics 、DTAnalyticsUtils: 对外接口类，开发者直接调用类中静态方法
- api 包:对外接口的实现，提供给 DT、DTAnalytics 等对外接口类直接调用
- PropertyManager: 收集、更新、过滤事件属性，管理属性的相关操作
- PresetEventManager: 预置事件采集管理，包括首次安装、打开标记等
- EventTrackManager: 事件采集管理，包括事件名称、属性检测，事件拼装
- EventUploadManager: 事件入库、上报管理
- data 包: 数据库、sp 封装，数据库用户存储事件，sp则保存配置 quality:质量监控，对 SDK 内部
  可能产生bug的地方进行打点监控
- ad、iap、ias 包: 对track接口的封装，预置一广告、内购、订阅相关的事件