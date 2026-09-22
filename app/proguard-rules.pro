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

# ============================
# Gson rules
# ============================
# Keep all data model classes used with Gson
-keep class com.example.catmusic.bean.** { *; }

# Keep generic signature of Gson classes
-keepattributes Signature

# Keep Gson specific classes
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }

# Prevent stripping of names from annotations
-keepattributes *Annotation*

# Keep custom serializers/deserializers
-keepclassmembers class * implements com.google.gson.JsonSerializer {
    <methods>;
}
-keepclassmembers class * implements com.google.gson.JsonDeserializer {
    <methods>;
}

# ============================
# OkHttp rules
# ============================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote okhttp3.internal.Platform

# ============================
# Glide rules
# ============================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# ============================
# AndroidX and Support Library
# ============================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ============================
# Keep all service and activity classes
# ============================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ============================
# Keep custom views
# ============================
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ============================
# Parcelable implementations
# ============================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================
# Serializable implementations
# ============================
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============================
# Keep native methods
# ============================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================
# Keep enum classes
# ============================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
