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


# This example uses Picasso, see: https://github.com/square/picasso/blob/master/README.md
-dontwarn com.squareup.okhttp.**

-dontwarn org.apache.http.**
-dontwarn android.net.http.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn java.nio.**

# Required by IndoorAtlas SDK
-keep public class com.indooratlas.algorithm.ClientProcessingManager { *; }
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
