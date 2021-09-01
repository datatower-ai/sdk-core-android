# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

####################################################################################################
# 系统通用混淆配置，参见 android-sdk/tools/proguard-rules/proguard-rules-android-optimize.txt
####################################################################################################
# ProGuard的介绍以及参数说明，参阅 https://blog.csdn.net/hibit521/article/details/79709659
# -keep 指定需要被保留的类和成员。
# -keepclassmembers 指定需要被保留的类成员，如果他们的类也有被保留。如保留一个序列化类中的所有成员和方法
# -keepclasseswithmembers 指定保留那些含有指定类成员的类，如保留所有包含main方法的类
# -keepnames 指定那些需要被保留名字的类和类成员，如保留那些实现了Serializable接口的类的名字
# -keepclassmembernames 指定那些希望被保留的类成员的名字
# -keepclasseswithmembernames 保留含有指定名字的类和类成员。
# class可以指向任何接口或类。interface只能指向接口，enum只能指向枚举
# 接口或者枚举前面的!表示相对应的非枚举或者接口
# 每个类都必须是全路径指定或者由?、*、**这三个通配符指定
# 类通配符：?任意匹配类名中的一个字符，但是不匹配包名分隔符
# 类通配符：*匹配类名的任何部分除了包名分隔符
# 类通配符：**匹配所有类名的所有部分，包括包名分隔符
# <init>匹配任何构造函数，<fields>匹配任何域，<methods>匹配任何方法，*匹配任何方法和域
# 方法和域通配符：?任意匹配方法名中的单个字符，*匹配方法命中的任意部分
# 数据类型通配符：%匹配任何原生类型，?任意匹配单个字符，*匹配类名的任何部分除了包名分隔符
# 数据类型通配符：**匹配所有类名的所有部分，包括报名分隔符，***匹配任何类型，…匹配任意参数个数
####################################################################################################

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/* # 谷歌推荐的混淆算法及过滤器，一般不做更改
-allowaccessmodification # 混淆时是否允许改变作用域
-optimizationpasses 5 # 指定代码的压缩级别
-ignorewarnings # 忽略警告
-verbose # 混淆时是否记录日志（混淆后生产映射文件，包含有类名->混淆后类名的映射关系)
-dontpreverify # 不做预校验，preverify是混淆步骤之一，Android不需要preverify，去掉以便加快混淆速度。
#-dontoptimize # 不优化输入的类文件，需要优化才能过滤代码中的日志
-dontshrink # 不启用压缩，需要压缩才能移除未用到的资源

-dontusemixedcaseclassnames # 混淆时不使用大小写混合，混淆后的类名为小写
-dontskipnonpubliclibraryclasses # 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclassmembers # 指定不去忽略非公共库的类成员


-keepattributes Exceptions # 保留异常类
-keepattributes InnerClasses # 保留匿名内部类
-keepattributes SourceFile,LineNumberTable # 保留代码行号
-renamesourcefileattribute SourceFile
-keepattributes Signature,EnclosingMethod #保留泛型与反射


 # 不混淆注解
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

# 不混淆明确通过`@Keep`注解标记的类、方法或属性
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

# 不混淆JS于Java的交互桥梁
-keepattributes *JavascriptInterface*


# XML映射问题，一些控件无法Inflate
-keep class org.xmlpull.v1.** {*;}

# 删除控制台日志打印
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
-assumenosideeffects class java.io.PrintStream {
    public *** println(...);
    public *** print(...);
}

-keep public class **.*Listener{*;}
