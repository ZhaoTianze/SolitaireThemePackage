# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/yfc/android/studio_SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn android.net.**
-keep class android.net.**{*;}

-dontwarn org.apache.http.**
-keep class org.apache.http.** {*;}

-dontwarn com.google.**
-keep class com.google.**{*;}

-dontwarn android.support.**
-keep class android.support.**{*;}


# 保留facebook下的所有类及其内部类
-keep class com.facebook.** {*;}
-dontwarn com.facebook.**