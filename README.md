[![Maven Central](https://img.shields.io/maven-central/v/net.sourceforge.streamsupport/streamsupport.svg)](http://mvnrepository.com/artifact/net.sourceforge.streamsupport/streamsupport)
[![javadoc.io](https://javadocio-badges.herokuapp.com/net.sourceforge.streamsupport/streamsupport/badge.svg)](http://www.javadoc.io/doc/net.sourceforge.streamsupport/streamsupport/)
[![Download](https://api.bintray.com/packages/stefan-zobel/streamsupport/streamsupport/images/download.svg) ](https://bintray.com/stefan-zobel/streamsupport/streamsupport/_latestVersion)

# streamsupport

![](art/streamsupport-sf.png)

streamsupport is a backport of the Java 8 `java.util.function` (functional interfaces) and `java.util.stream`
(streams) API for Android and users of Java 6 or 7 supplemented with selected additions from `java.util.concurrent`
which didn't exist back in Java 6.

Due to the lack of default interface methods and static interface methods in pre-Java 8 the API had to be slightly
adjusted in these areas but still covers the full functionality scope of Java 8. In detail, static and default
interface methods have been moved to companion classes in the same package that bear the identical name as the
interface but with an "s" appended (e.g. `Comparator` -> `Comparators`).

For ease of use, the default methods for most of the functional interfaces were NOT retained as abstract methods
in the redefined interfaces (keeping them single method interfaces) - the missing default (and static) methods can
always be found in the corresponding companion class.

The current stable release is `streamsupport-1.6.2`.

Want also lambdas? https://github.com/orfjackal/retrolambda

Note that the [streamsupport sourceforge](https://sourceforge.net/projects/streamsupport/) site has been discontinued.
New developments, if any, will take place here.

Please give feedback [here](https://github.com/stefan-zobel/streamsupport/issues) if you experience any problems.


## Features

* Java 8 / Java 9 Streams library backport
* Java 8 / Java 9 CompletableFuture backport
* Java 8 Parallel array operations backport
* Java 8 Functional interfaces backport
* Further java.util.concurrent enhancements from Java 7/8 backported to Java 6
* Includes miscellaneous Java 8 / Java 9 goodies (Optional, StringJoiner, ...)
* Supports Android - all versions, starting from Ice Cream Sandwich


### build.gradle:

```gradle
dependencies {
    compile 'net.sourceforge.streamsupport:streamsupport:1.6.2'
}
```


### Maven:

```xml
<dependency>
    <groupId>net.sourceforge.streamsupport</groupId>
    <artifactId>streamsupport</artifactId>
    <version>1.6.2</version>
</dependency>
```


### Forks

Android developers using the Android Studio 3.x toolchain should have a look at the `streamsupport` forks:

* [android-retrostreams](https://github.com/retrostreams/android-retrostreams)
* [android-retrofuture](https://github.com/retrostreams/android-retrofuture)
* [android-retroflow](https://github.com/retrostreams/android-retroflow)
* [android-retroatomic](https://github.com/retrostreams/android-retroatomic)


## Release Notes

[Release Notes](Readme.txt)


## LICENSE

GNU General Public License, version 2, with the Classpath Exception
