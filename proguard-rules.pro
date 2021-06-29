# Add any project specific keep options here:

#-dontobfuscate
#-dontshrink

-keep class java8.** { *; }
-keep interface java8.** { *; }
-keep enum  java8.** { *; }

-dontwarn java8.**
-dontwarn org.openjdk.**

-dontnote java.util.Arrays*
-dontnote java.util.HashMap*
-dontnote java.util.concurrent.LinkedBlockingQueue*
-dontnote java.util.concurrent.LinkedBlockingDeque*

