# Add project specific ProGuard rules here.
# You can control the filterSet of applied configuration files using the
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

# 保持配置类 config 不被混淆
-keep class com.lzf.easyfloat.data.FloatConfig {*;}

# 保持自定义控件、ContentProvider 不被混淆
-keep public class * extends android.view.View
-keep public class * extends android.content.ContentProvider

# 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持反射不被混淆
-keepattributes EnclosingMethod