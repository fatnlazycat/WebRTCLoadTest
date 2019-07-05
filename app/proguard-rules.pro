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

 -dontwarn org.webrtc.*
 -keep class org.webrtc.WebRtcClassLoader { *;}
 -keep class org.webrtc.JniHelper { *;}
 -keep class org.webrtc.IceCandidate { *;}
 -keepclassmembers class org.webrtc.PeerConnectionFactory { static <methods>; }
 -keep class org.webrtc.PeerConnection { *;}
 -keep class org.webrtc.PeerConnection$RTCConfiguration { *;}
 -keep class org.webrtc.PeerConnection$SignalingState { *;}
 -keep class org.webrtc.PeerConnection$Observer { *;}
 -keep class org.webrtc.PeerConnection$IceConnectionState { *;}
 -keepclassmembers class org.webrtc.PeerConnection$IceConnectionState { static <methods>; }
 -keep class org.webrtc.PeerConnection$IceGatheringState { *;}
 -keep class org.webrtc.DataChannel { *;}
 -keep class org.webrtc.DataChannel** { *;}
 #-keep class org.webrtc.DataChannel$Init { *;}
 -keep class org.webrtc.voiceengine.* { *;}
 -keep class org.webrtc.SessionDescription { *;}
 -keep class org.webrtc.SdpObserver { *;}
 -keep class org.webrtc.MediaConstraints { *;}
 -keep class org.webrtc.NetworkMonitor { *;}
 -keep class org.webrtc.NetworkMonitorAutoDetect** { *;}
