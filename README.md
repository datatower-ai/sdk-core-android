## 工程结构

- roiquery-core/: SDK 核心代码
- demo-app/: SDK 接口使用范例，用于开发和测试 SDK

## 核心代码

核心代码位于 `com.roiquery.analytics` 包下:

- DT、DTAnalytics 、DTAnalyticsUtils: 对外接口类，开发者直接调用类中静态方法
- api 包:对外接口的实现，提供给 DT、DTAnalytics 等对外接口类直接调用
- PropertyManager: 收集、更新、过滤事件属性，管理属性的相关操作
- PresetEventManager: 预置事件采集管理，包括首次安装、打开标记等
- EventTrackManager: 事件采集管理，包括事件名称、属性检测，事件拼装
- EventUploadManager: 事件入库、上报管理
- data 包: 数据库、sp 封装，数据库用户存储事件，sp则保存配置 quality:质量监控，对 SDK 内部
  可能产生bug的地方进行打点监控
- ad、iap、ias 包: 对track接口的封装，预置一广告、内购、订阅相关的事件

## 构建工程

在项目根目录下的文件 `build.gradle.kts` 里找到 `dtsdkCoreVersionName` 更新它右边引号内
的值并记录下来, 然后在工程根目录下运行以下命令:

```shell
gradle :roiquery-core:clean
gradle :roiquery-core:assemblePublicRelease
gradle :roiquery-core:copyProguardMappingFiles
# 上述3个命令执行完成并且都没有错误后, 执行如下命令
git add roiquery-core/build.gradle.kts
git add roiquery-core/proguard-mapping
git commit -m "Bump version to '$dtsdkCoreVersionName'." # 'dtsdkCoreVersionName' 替换掉之前记录的文本, 下同。
git tag "core/$dtsdkCoreVersionName"
git push --tags
```

当前版本的 Proguard Mapping 文件将被一同保存在 `/roiquery-core/proguard-mapping/`
目录下。

## 版本发布

### 发布至 Maven Central

向工程根目录下文件 `local.properties` 添加如下内容:

```properties
ossrhEmail=develop@nodetower.com
ossrhUsername=xxxxxxxx
ossrhPassword=xxxxxxxx
```

然后在工程目根目录下运行如下命令:

```shell
gradle :roiquery-core:publishReleasePublicationToMavenCentralRepository
```

### Proguard Mapping 文件

从版本 `core/2.0.0-beta1`(commit#`976b70609a2efd17b8c86dc99caa9f900fc11904`) 起
proguard mapping 文件将放在工程根目录下的 `/roiquery-core/proguard-mapping` 目录内,
每一次公开发布 SDK 版本都**必须**更新该目录内的文件。

旧版的 proguard mapping 文件存放于深圳办公室的 NAS 服务器, 以下是服务器地址和登录凭证:

```
smb://192.168.50.100/LovinJoy/roiquery/
roiquery
B6SyMJj8OVivOFQe
```
