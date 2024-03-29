RELEASE NOTES

 - "1.7.4-stable" is the current stable release


GENERAL

 - To create a Stream from a java.util Collection use the static j8.u.s.StreamSupport
   methods j8.u.s.StreamSupport#stream(Collection), or (for a parallel Stream)
   j8.u.s.StreamSupport#parallelStream(Collection)

 - The static methods from interface j.u.s.Stream are located in j8.u.s.RefStreams
   which also contains the new Java 9 j.u.s.Stream default methods.

 - The provided Jar files are compiled for Java 6 (Bytecode version 50). You'll need
   Retrolambda (https://github.com/orfjackal/retrolambda) to build the core
   streamsupport.jar from its sources

 - It is possible to turn on an OpenJDK "compatibility mode" by setting the
   boolean system property

   java8.util.Spliterators.assume.oracle.collections.impl=false

   This switch is provided for users of non-OpenJDK based JREs (e.g. IBM Java 6/7)
   to increase the odds that streamsupport can be used on their platform.
   This switch is not needed (and has no effect) on Android.

 - Release 1.3 adds the new Java 9 Stream methods takeWhile() and dropWhile().
   If anyone longs for the default method implementations, they are available
   in j8.u.s.RefStreams (but that shouldn't be necessary as all Streams created
   via StreamSupport#stream()/#parallelStream() already implement these methods).

 - Release 1.4 completes the Java 9 "More Concurrency Updates" JEP 266.
   This covers, among other things, the introduction of the Java 9 Flow API
   (the preliminary implementation of reactive streams) and Java 9 enhancements
   to j.u.c.CompletableFuture and fork/join as well as improvements to their
   implementation.

 - As of release 1.4, the former single streamsupport.jar has been partitioned into
   a core streamsupport.jar and 3 additional optional components:

   * streamsupport-cfuture (CompletableFuture API)
   * streamsupport-atomic  (j8.u.c.atomic package)
   * streamsupport-flow    (Java 9 Flow API)

   All of them have a dependency on the core streamsupport.jar

 - As of release 1.5, a new optional component "streamsupport-literal"
   that contains the implementation for JEP 269 (Java 9) has been added.
   See JEP 269: "Convenience Factory Methods for Collections"
   http://openjdk.java.net/jeps/269

   * streamsupport-literal (Java 9 JEP 269 Collections factory methods)

 - As of release 1.5.3 streamsupport detects when it is running on a
   stream enabled Android 7+ device and automatically delegates to the
   Android Spliterators (contributed by Tobias Thierer, ticket#240).

   This feature can be disabled by setting the system property
   "java8.util.Spliterators.jre.delegation.enabled" to false.

 - Release 1.6.0 introduces some new methods from the Java 10 API
   (cf. version history for details)

 - As of release 1.6.0 "streamsupport-literal" has been merged into the
   core streamsupport.jar component in order to make the new Collectors
   for unmodifiable Lists, Sets, and Maps (JDK-8184690) possible.

   streamsupport-literal-1.6.0 is still available as a standalone component
   without any dependencies but has been discontinued in release 1.6.1

 - As of release 1.6.1, j8.u.c.Phaser has moved to the streamsupport-atomic
   component and j8.u.c.CompletionException has moved to streamsupport-cfuture
   (the only place where it is used).
   streamsupport-literal is no longer maintained but the 1.6.0 version can
   still be used since it doesn't have any dependencies on other artifacts.

 - Release 1.6.3 fixes some streamsupport issues with the Android P developer
   preview

 - Release 1.7.0 completes the Java 11 port and adds a couple of new methods
   from Java 12, especially a new merging Collector (JDK-8205461) and new
   exception handling methods for CompletableFuture (JDK-8210971).

 - Release 1.7.1 completes the Java 12 port and adds some improvements and
   bug fixes from Java 13.

 - Release 1.7.2 has a new Dual-pivot quicksort implementation, adds a few
   Java 9 methods to J8Arrays, is overall a bit smaller and adds some
   improvements and minor documentation fixes from Java 14.

 - Release 1.7.3 completes the Java 16 port and adds two new Stream methods
   (multiMap and toList) introduced in Java 16.

 - Release 1.7.4 adds some improvements from Java 17.



KNOWN PROBLEMS

 - Incorrect LinkedHashMap Spliterator ordering in Android N.

   The implementation of LinkedHashMap's collection views' spliterators in
   Android Nougat (API levels 24 and 25) uses the wrong order (inconsistent
   with the iterators, which use the correct order), despite reporting
   Spliterator#ORDERED (however, the unordered HashMap spliterators are used).

   Since streamsupport 1.5.3 and later will by default delegate to the Android 7.x
   spliterators you'd be affected by this unordered behavior on Android 7.x unless
   you disable spliterator delegation altogether or you work around this behavior
   on API level 24 and 25 as follows.

   You may use the following code to obtain a correctly ordered Spliterator:

   For a Collection view
   col = lhm.keySet(), col = lhm.entrySet() or col = lhm.values(), use

   Spliterator sp = java8.util.Spliterators.spliterator(col, c) where

   int c = Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED

   for keySet() and entrySet() and

   int c = Spliterator.ORDERED | Spliterator.SIZED for values()

   to obtain a correctly ordered spliterator. Then, instead of
   StreamSupport.stream(col) or StreamSupport.parallelStream(col), use

   java8.util.stream.StreamSupport.stream(sp, false)

   to construct a (nonparallel) java8.util.stream.Stream from the Spliterator sp.

   Note that these workarounds are only suggested where lhm is a LinkedHashMap.
   Note further that everything works perfectly fine on API level 23 and below (if
   you don't plan to deploy on Android 7.x devices, you can ignore this whole advice
   altogether).

   To mitigate the risk if you don't follow this advice, streamsupport 1.5.3 (and
   later) exercises a check whether the spliterator for a HashMap collection view
   reports ORDERED (a bug) and excepts these cases from the delegation mechanism
   (using the reflective implementation instead. So, in effect the same mechanism
   that is used on API level 23 and below gets employed in this case).

   But note that this check isn't 100% fool-proof as the LinkedHashMap (or its
   collection view) could be wrapped, for example in a j.u.Collections$UnmodifiableMap
   (whose UnmodifiableEntrySetSpliterator delegates back to the defective
   HashMap$EntrySpliterator).

   Since we can't know an arbitrary wrapper beforehand there is nothing streamsupport
   can do about this in such cases - you have been warned.

   The latest version of LinkedHashMap in AOSP implements its Spliterators via
   Spliterators.spliterator(), which means that this bug has already been fixed
   in Android Oreo.



VERSION HISTORY

1.7.4-stable (2021-09-19)
 - JDK-8265029: Preserve SIZED characteristics on slice operations (skip, limit)
 - JDK-8267452: Delegate forEachRemaining in Spliterators.iterator
 - JDK-8199318: add idempotent copy operation for Map.Entry
 - JDK-8267939: Clarify the specification of forEachRemaining
 - JDK-8271601: use x ^ y in the floorMod(int, int) test
 - JDK-8268113: Reuse Long.hashCode()
 - Implement Java 17 j.u.r.RandomGenerator methods
 - Add new CompensatedSums test for JDK-8214761
 - Completed Java 17 port

1.7.3-stable (2021-01-31)
 - JDK-8238286: Add new flatMap stream operation multiMap (Java 16)
 - JDK-8180352: Add Stream.toList() method (Java 16)
 - JDK-8234131: Misc. changes imported from jsr166 CVS 2021-01
 - JDK-8156071: List.of: reduce array copying during creation
 - JDK-8255150: Add utility methods to check long indexes and ranges
 - JDK-8238669: Long.divideUnsigned is slow for certain values
 - JDK-8254350: CompletableFuture.get may swallow InterruptedException
 - JDK-8259796: Timed CompletableFuture.get swallows InterruptedException
 - JDK-8254973: ThreadPerTaskExecutor does not throw NPE in #execute
 - JDK-8250240: Address use of default constructors in java.util.concurrent
 - JDK-8254146: Avoid unnecessary volatile write on new AtomicBoolean
 - JDK-8247402: Map::compute Javadoc confusing implementation requirements
 - JDK-8228615: Optional.empty doc should suggest using isEmpty
 - JDK-8245970: IntStream.reduce Javadoc should not mention average
 - JDK-8243655: Map.replace javadoc code snippet typo
 - JDK-8253066: Typo in Stream.mapMulti
 - JDK-8259816: Typo in java.util.stream package description
 - Add Loom's CompletableFuture.completed (Project Loom)
 - Completed Java 16 port

1.7.2-stable (2020-03-27)
 - JDK-8226297: Dual-pivot quicksort improvements
 - JDK-8033148: Comparators and mismatchers for arrays (partially)
 - JDK-8225490: Misc. changes imported from jsr166 CVS 2019-09
 - JDK-8236850: Consistent constant folding on List/​Set.of() instances
 - JDK-8236641: Improve Set.of(...).iterator() warmup characteristics
 - JDK-8227235: Rare failures in testForkHelpQuiesce tests
 - JDK-8231161: Wrong return type in Collector Javadoc code sample
 - JDK-8224716: SummaryStatistics Javadoc overflow of count
 - JDK-8238919: Spacing and punctuation in stream package doc
 - JDK-8241014: Misc. typos in doc comments
 - Test compatibility with Android 10 [#410, #411]
 - Test on Java 14, 15 (early access) [#409, #416]

1.7.1-stable (2019-05-26)
 - Completed Java 12 port, integrated improvements from Java 13
 - JDK-8215995: Add toArray() methods to immutable collections
 - JDK-8221921: Implement size() / isEmpty() in immutable collections
 - JDK-8214761: Bug in parallel Kahan summation
 - JDK-8221924: Unmodifiable Map with one entry returns null
 - JDK-8221981: Simplify Map/List/Set.of() implementation
 - JDK-8221980: Simplify Optional implementation
 - JDK-8215359: setContextClassLoader() needlessly throws
 - JDK-8211283: Misc. changes imported from jsr166 CVS 2018-11
 - JDK-8219138: Misc. changes imported from jsr166 CVS 2019-05
 - JDK-8223112: Clarify semantics of java.util.Objects.equals
 - JDK-8223379: Clarify ForkJoinPool.getStealCount() Javadoc
 - JDK-8213238: Erroneous Javadoc in MatchOps.MatchKind
 - Test on Java 13 (early access) [#396]

1.7.0-stable (2018-11-11)
 - Completed Java 11 port, new methods from Java 12
 - JDK-8205461: Create a merging Collector
 - JDK-8210971: CompletionStage / CompletableFuture exception methods
 - JDK-8060192: Add A[] Collection.toArray(IntFunction<A[]>)
 - JDK-8050818: Predicate::not
 - JDK-8203670: Don't use ListIterator for ImmutableCollections iterator()
 - JDK-8203184: List.copyOf() fails to copy sublists
 - JDK-8207003: Misc. changes imported from jsr166 CVS 2018-09
 - JDK-8203681: Misc. changes imported from jsr166 CVS 2018-06
 - JDK-8203864: Execution error in Timsort
 - JDK-8210347: Combine Set.contains() and Set.add() calls
 - JDK-8212899: SubmissionPublisherTest timed out waiting
 - JDK-8204172: Predicate::not should explicitly mention NPE
 - Test on Java 12 early-access [#379]
 - Test with latest Java 8 / Java 11 JRE public builds [#387]

1.6.3-stable (2018-05-14)
 - Compatibility with Android P preview rev. 3 [#365]
 - JDK-8184693: Add Optional.isEmpty
 - JDK-8201650: Move randomized iteration order to iterators

1.6.2-stable (2018-04-15)
 - JDK-8200520: ForkJoin tasks interrupted after shutdown
 - JDK-8193128: Reduce number of classes returned by List/Set/Map.of()
 - JDK-8195590: Misc. changes imported from jsr166 CVS 2018-02
 - JDK-8197531: Misc. changes imported from jsr166 CVS 2018-04
 - JDK-8191418: List.of().indexOf(null) doesn't throw NPE
 - JDK-8195649: Reorganize tests for Optional
 - Rename the j8.u.ImmutableCollections version of CollSer [#356]

1.6.1-stable (2018-01-28)
 - JDK-8075939: flatMap() breaks short-circuiting of terminal Ops
 - JDK-8193856: takeWhile produces incorrect result with flatMap
 - JDK-8140281: Add no-arg orElseThrow() as alternative to get()
 - JDK-8193300: Misc. changes imported from jsr166 CVS 2018-01
 - JDK-8134459: WhileOpTest.java timed out
 - Move j8.u.c.Phaser to atomic component [#349]
 - Move j8.u.c.CompletionException to cfuture component [#348]

1.6.0-stable (2017-12-10)
 - a first dive into Java 10 (in the sense of new API methods)
   * JDK-8184690: Add Collectors for unmodifiable List, Set, and Map
   * JDK-8177290: Add unmodifiable List, Set, Map copy factories
   * JDK-8178117: Public state constructors for Int/Long/DoubleSummaryStatistics
   * JDK-8188047: Add SplittableRandom.nextBytes
 - backported recent improvements from the Java 10 repo
   * JDK-8190974: Obey parallelism within custom ForkJoinPool
   * JDK-8187947: Race condition in SubmissionPublisher
   * JDK-8193174: SubmissionPublisher invokes the Subscriber's onComplete too early
   * JDK-8189764: Misc. changes imported from jsr166 CVS 2017-11
   * JDK-8192943: Optimize atomic accumulators using getAndSet
   * JDK-8186265: Make toString() methods of "task" objects more useful
   * JDK-8179314: CountedCompleterTest test failure
   * JDK-8181175: Stream.concat behaves like terminal operation
   * JDK-8015667: Stream.toArray(IntFunction) ArrayStoreException spec
   * JDK-8191429: List.sort should specify the sort is stable
   * JDK-8186466: Fix minor issues in java.base javadoc
   * JDK-8186684: Fix broken links in java.base API docs
 - streamsupport-specific changes and fixes
   * merge literal component into streamsupport (JDK-8184690) [#328]
   * Asynchronous tasks marker interface can't be loaded [#323]
   * exempt spliterator-delegated LBD / LBQ from Collection8Test [#327]

1.5.6-stable (2017-08-13)
 - JDK-8178409: Misc. changes imported from jsr166 CVS 2017-07
 - JDK-8185099: Misc. changes imported from jsr166 CVS 2017-08
 - reenable LHM Spliterator delegation on Android O [#314]
 - test on Android O preview [#315]
 - reduce method count and jar size [#303]
 - enforce Spliterator delegation on Java 9 [#299]

1.5.5-stable (2017-05-25)
 - JDK-8174267: findFirst() unnecessarily always allocates an Op
 - JDK-8176543: Misc. changes imported from jsr166 CVS 2017-04
 - JDK-8177653: Clarify restrictions on Iterator.forEachRemaining
 - JDK-8167981: Missing explanation of intended use of Optional
 - JDK-8178956: Misleading doc of LongAccumulator accumulator function
 - JDK-8023897: Rename executeAndCatch in various tests to assertThrow
 - update to retrolambda 2.5.1 to reduce method count for Android apps [#292]
 - cut down on Bytecode size [#293]
 - deduplicate spliterator traversing tests [#296]
 - ArrayDeque IteratorSpliterator should report NONNULL [#297]
 - add new JSR 166 TCK test ArrayDeque8Test [#298]
 - disable COWAL Java 6/7 RASpliterator sans native specialization [#300]
 - TLRandom: revert static initializer changes from commit 3e381f [#301]

1.5.4-stable (2017-03-21)
 - JDK-8172023: Concurrent spliterators fail to handle exhaustion properly
 - JDK-8172726: FJ common pool retains a reference to TCCL
 - JDK-8166365: Small immutable collections optimized implementations
 - JDK-8170484: Misc. changes imported from jsr166 CVS 2016-12
 - JDK-8171886: Misc. changes imported from jsr166 CVS 2017-02
 - JDK-8173909: Misc. changes imported from jsr166 CVS 2017-03
 - JDK-8170945: Collectors$Partition implement more Map methods
 - JDK-8176155: SubmissionPublisher closeExceptionally may override close
 - JDK-8176551: testCommonPoolThreadContextClassLoader() fails
 - JDK-8176303: Flow.Subscription.request(0) should be treated as error
 - JDK-8174950: Gracefully handle null Supplier in requireNonNull
 - JDK-8169903: Refactor spliterator traversing tests
 - JDK-8023898: Consolidate Map tests into general Map-based test
 - JDK-8175360: Error in Collectors.averaging... Javadoc
 - add new JSR 166 TCK test Collection8Test (#273)
 - IteratorSpliterator for j.u.Queue could often report ORDERED (#274)
 - TLRandom/FJWorkerThread: update createThreadGroup to JDK-8160710 (#272)
 - eliminate SinkConsumer compiler kludge (#266)
 - remove unused NullArgsTestCase [JDK-8173414] (#276)

1.5.3-stable (2016-12-17)
 - JDK-8166646: Misc. changes imported from jsr166 CVS 2016-11
 - JDK-8171051: LinkedBlockingQueue spliterator support node self-linking
 - JDK-8169739: LinkedBlockingDeque spliterator support node self-linking
 - JDK-8169222: Minor immutable collections optimizations
 - JDK-8156079: Make empty immutable collections instances singletons
 - JDK-8152617: Add wildcards to Optional or() and flatMap()
 - JDK-8170943: Collectors.partitioningBy spec change
 - JDK-8170560: Improve Collectors javadoc code samples
 - JDK-8168745: Iterator.forEachRemaining vs. Iterator.remove
 - JDK-8168841: Correct Collectors collectingAndThen() Javadoc
 - JDK-8164934: Optional.map() javadoc code example
 - JDK-8170573: Typo in Collectors javadoc examples
 - JDK-8170566: Incorrect phrase usage in javadocs
 - use delegating Spliterators on Android 7+ (#240)
 - remove dependency on streamsupport from literal component (#243)
 - VectorSpliterator: cleaner and faster forEachRemaining() (#259)
 - PQueueSpliterator: slightly faster implementation (#260)
 - enable Spliterator tests for PriorityQueue/WeakHashMap on Nougat (#244)
 - detect accidental use of API not present in Java 6 (#250)
 - fix usage of AssertionError(String, Throwable) constructor in tests (#249)
 - test on Android 7.1.1 (#265)

1.5.2-stable (2016-10-03)
 - JDK-8164189: Collectors.toSet() parallel performance improvement
 - JDK-8164691: Stream specification clarifications for iterate and collect
 - JDK-8166465: minimalCompletionStage.toCompletableFuture should be non-minimal
 - JDK-8162627: Misc. changes imported from jsr166 CVS 2016-08
 - JDK-8164169: Misc. changes imported from jsr166 CVS 2016-09
 - JDK-8165919: Misc. changes imported from jsr166 CVS 2016-09-21
 - JDK-8159404: Immutable collections should throw UOE unconditionally
 - JDK-8164983: CountedCompleter code samples and corresponding tests
 - JDK-8166059: JSR166TestCase can fail with NPE
 - JDK-8163210: Update JSR166TestCase to latest CVS revision
 - enable JDK-8158365 RandomAccess optimization (#217)
 - update ForkJoinTask(8)Tests to latest JSR 166 CVS revision (#220)
 - replace synthetic bridge constructors by package-private constructors (#235)
 - adapt HMSpliterators to latest Android 7.x changes (#222)
 - test on Android 7.0 final (#223)

1.5.1-stable (2016-07-30)
 - fixed: Android Harmony detection fails on JavaFXPorts (#210)
 - completed the great JSR 166 jdk9 integration "waves 7 & 8" (#209)
 - JDK-8160402: Garbage retention with CompletableFuture.anyOf
 - updated CompletableFuture to Java 9 JSR 166 CVS rev 1.207 (#207)
 - updated CompletableFutureTest to Java 9 JSR 166 CVS rev 1.170 (#207)
 - ART performance regression in CountLargeTest (#211)
 - test release 1.5.1 on Android N developer preview-5 (#215)

1.5-stable (2016-06-19)
 - JDK-8154049: DualPivot sorting incorrect for nearly sorted arrays
 - JDK-8155794: Remove Objects.checkIndex exception customization
 - JDK-8157523: Various improvements to ForkJoin/SubmissionPublisher
 - JDK-8157522: Performance improvements to CompletableFuture
 - JDK-8154387: Parallel unordered Stream.limit() performance if limit < 128
 - JDK-8158365: List.spliterator() should optimize for RandomAccess lists
 - JDK-8153768: Misc. changes imported from jsr166 CVS 2016-05
 - JDK-8048330: JEP 269 Convenience Factory Methods for Collections
 - JDK-8139233: Add compact immutable collection implementation
 - JDK-8130023: j.u.stream - explicitly specify guaranteed pipeline execution
 - JDK-8157437: Typos in Stream JavaDoc
 - JDK-8159821: PrimitiveStream's "iterateFinite" has incorrect code sample
 - test release 1.5 on Android N developer preview-3 (#194)
 - test release 1.5 on Android N developer preview-4 (#205)

1.4.3-stable (2016-04-17)
 - JDK-8072727: add variation of Stream.iterate() that's finite
 - JDK-8153293: preserve SORTED/DISTINCT for primitive stream ops
 - JDK-8152924: improve scalability of CompletableFuture
 - JDK-8151123: summingDouble/averagingDouble call mapper twice
 - JDK-8146458: better exception reports for index check methods
 - JDK-8152617: add wildcards to Optional flatMap()
 - JDK-8150417: make TLRandom robust against initialization cycles
 - perf. optimization for RandomAccess AbstractList subclasses (#176)
 - added support for the Android N developer preview (#149, #155, #191)
 - add tryAdvance() to the primitive AbstractSpliterator classes (#170)
 - optimize FJPool / Striped64 to avoid false sharing (#190)
 - updated CompletableFuture(Test) to latest jsr166 CVS revision (#185)
 - JDK-8151344: improve timeout factor handling in JSR166TestCase
 - JDK-8151511: one CollectionAndMapModifyStreamTest not executed
 - JDK-8151785: typo in j.u.stream.PipelineHelper
 - updated JSR166TestCase to latest jsr166 CVS revision (#181)
 - added subList() test to SpliteratorTraversingAndSplittingTest (#174)
 - added subList() test(s) for Java 9 test platform (#189)

1.4.2-stable (2016-02-12)
 - JDK-8148250: limit() optimization for ordered source
 - JDK-8148115: findFirst() optimization for unordered source
 - JDK-8148838: flatMap() splitting after partial traversal
 - JDK-8147505: onClose() behavior after stream is consumed
 - JDK-8146467: integrate JSR 166 TCK tests
 - JDK-8148638: TCK test failure
 - JDK-8148928: SequentialOpTest.java timeout
 - JDK-8076458: FlatMapOpTest.java timeout
 - updated TLRandom to JSR 166 CVS rev 1.39 (Ticket#152)
 - updated SplittableRandom to JSR 166 CVS rev 1.30 (Ticket#153)
 - updated CompletionStage to JSR 166 CVS rev. 1.38 (Ticket#160)
 - added sublist test to SpliteratorLateBindingFailFastTest (Ticket#168)
 - added preliminary support for Android N (Ticket#154)
 - removed the deprecated methods from j8.u.Maps (Ticket#148)

1.4.1-stable (2015-12-27)
 - JDK-8144675: add a filtering collector
 - edge case performance improvement for parallel distinct (Ticket#139)
 - moved the "concurrent" methods from j8.u.Maps to j8.u.c.ConcurrentMaps
   The "concurrent" methods in j8.u.Maps are deprecated now and will be
   removed in a future release (Ticket#140)
 - added compute() and computeIfPresent() to ConcurrentMaps (Ticket#144)
 - JDK-8145164: default impl of ConcurrentMap::compute can throw NPE
 - JSR166 jdk9 integration "wave 2" (Ticket#131) with sub-tasks:
   * JDK-8142441: improve jtreg tests for j.u.concurrent
   * JDK-8141031: j.u.c.Phaser Basic test fails intermittently
   * JDK-8143087: miscellaneous changes from JSR166 CVS
   * JDK-8139927: improve Javadoc for CompletableFuture composition
   * JDK-8143086: document that newThread() can return null
 - update FJPool to JSR166 CVS rev 1.298 (Ticket#136)
 - removed the deprecated methods from j8.u.s.StreamSupport (Ticket#145)

1.4-stable (2015-11-15)
 - JDK-8134852: Java 9 fork/join with API enhancements (JEP 266)
 - JDK-8134851: CompletableFuture with API enhancements (JEP 266)
 - JDK-8134850: integrate the Java 9 Flow API (JEP 266)
 - moved CompletableFuture to streamsupport-cfuture jar (Ticket#120)
 - moved j8.u.c.atomic package to streamsupport-atomic jar (Ticket#121)
 - moved the new Java 9 Flow API to streamsupport-flow jar (Ticket#119)
 - Optional.or() can use a covariant Supplier as parameter (Ticket#125)
 - JDK-8135248: add Objects utility methods to check indexes / ranges
 - JDK-8142493: check indexes/ranges behavior when oobe produces null
 - JDK-8138963: new Objects methods to default to non-null
 - JDK-8141652: rename methods nonNullElse* to requireNonNullElse*

1.3.2-stable (2015-10-04)
 - JDK-8080418: add Optional.or()
 - JDK-8136686: Collectors.counting reduce boxing
 - JDK-8134853: update j.u.concurrent and related (JEP 266)
 - JDK-8134854: update j.u.concurrent.atomic classes (JEP 266)
 - LinkedListSpliterator bugfix (Ticket#103)
 - The static methods from interface j.u.s.Stream that are located
   in j8.u.s.StreamSupport are now deprecated. Please use the
   equivalent methods in j8.u.s.RefStreams.

1.3.1-stable (2015-09-03)
 - this is primarily a maintenance and code cleansing release
 - on Android it is no longer required to set the
   "java8.util.Spliterators.assume.oracle.collections.impl"
   system property (Ticket#93)
 - reduced ConcurrentHashMap resizing for parallel Stream#distinct()
   operations and account for the known level of parallelism (#82)
 - backwards-compatible generalization of the signatures of the static
   #generate() and #iterate() methods in j8.u.s.RefStreams and
   j8.u.s.StreamSupport (JDK-8132097, Ticket#79)
 - cleaned up the source code (#84), got rid of the useless J8Builder
   classes in Double/Long/IntStreams (#87), removed the stupid static
   #add(Stream.Builder<T> builder, T t) method from j8.u.s.RefStreams
   and j8.u.s.StreamSupport (#88)
 - added the missing Builder#add() method to the nested Builder
   interfaces in j8.u.s.Double/Int/LongStream (#86), added the
   missing (former default) methods in the nested OfDouble/OfInt/OfLong
   interfaces in j8.u.PrimitiveIterator (#90)
 - a couple of Javadoc tweaks / clarifications from the OpenJDK
   bugtracking system

1.3-stable (2015-07-14)
 - JDK-8071597 : "Add Stream dropWhile and takeWhile operations"
   is the pre-eminent new (Java 9) feature
   https://bugs.openjdk.java.net/browse/JDK-8071597
   If anyone longs for the default method implementations, they
   are available in j8.u.s.RefStreams. But that should rarely be
   useful as all Streams created via StreamSupport#stream() or
   StreamSupport#parallelStream() already implement these methods.
 - added the missing Spliterator default method implementations
   to all AbstractSpliterator base classes in j8.u.Spliterators
   https://sourceforge.net/p/streamsupport/discussion/general/thread/b65852d5/

1.2.2-stable (2015-06-26)
 - JDK-8129120 is the outstanding new feature: the properties of
   terminal operations don't get back-propagated upstream anymore
   See https://bugs.openjdk.java.net/browse/JDK-8129120
 - backported other recent improvements from the Java 9 repo
   * JDK-8080623 (CPU overhead in FJ due to spinning in awaitWork)
   * JDK-8080945 (Improve performance of primitive Arrays.sort)
 - added #replaceAll() and #spliterator() to j8.util.Lists
   (Ticket#61)
 - added #removeIf() to j8.lang.Iterables and optimized
   its #spliterator() implementation (Ticket#62)
 - added #asIterator(Enumeration) to j8.util.Iterators
   (JDK-8072726, Ticket#44)
 - added new class j8.util.stream.RefStreams as a new home
   for the static (and upcoming default) methods of j.u.s.Stream

1.2.1-stable (2015-04-23)
 - this is a hotfix release for a serious ForkJoinPool bug
   introduced in OpenJDK 8u40 / 1.2-stable (JDK-8078490)
   * JDK-8078490 (Missed submissions in ForkJoinPool)
   See Ticket#58 and
   https://bugs.openjdk.java.net/browse/JDK-8078490
   http://cs.oswego.edu/pipermail/concurrency-interest/2015-April/014240.html
 - the only other appreciable change is a bugfix in
   CompletableFuture (Ticket#52)
   * JDK-8068432 (Inconsistent exception handling in thenCompose)

1.2-stable (2015-04-09)
 - a new ForkJoinPool implementation (Ticket#27), introduced
   in OpenJDK 8u40. See JDK-8056248 (improve ForkJoin thread
   throttling). This is a major change.
 - a first dive into Java 9 (in the sense of new API methods)
   * JDK-8071600 (Collectors: Add a flat-mapping collector)
   * JDK-8071670 (Optional  : Add method ifPresentOrElse())
   * JDK-8050820 (Optional  : Add method stream())
   * JDK-8050819 (Stream    : Add method ofNullable(T))
 - backported two recent improvements from the Java 9 repo
   * JDK-8067969 (Optimize Stream.count for SIZED Streams)
   * JDK-8075307 (Parallel stateful pipeline flags inconsistent)

1.1.5-stable (2015-03-12)
 - added new "native" specializations for
   * j.u.HashSet (Ticket#32) [NOT available on Android]
   * the Collections returned from the j.u.HashMap
     #entrySet(), #keySet() and #values() methods
     (Ticket#30) [also NOT available on Android]
 - on Android it therefore becomes now mandatory to set the
   "java8.util.Spliterators.assume.oracle.collections.impl"
   system property to false
 - the library now detects when it is running on a stream enabled
   JRE (>= Java 8) and automatically takes advantage of its
   Spliterators for all Collections (Ticket#11)
   This feature can be disabled by setting the system property
   "java8.util.Spliterators.jre.delegation.enabled" to false
   (which has no effect on a Java 6/7 JRE or Android)
 - backported most of the OpenJDK 8u40 changes (and some more
   that will supposedly appear in 8u60).  Notably:
   * JDK-8070099 (ForEachOps.ForEachOrderedTask improvement)
   * JDK-8040892 (Collectors.toMap incorrect message bugfix)
   * JDK-8072909 (TimSort AIOOBE exception bugfix)
   * JDK-8066397 (remove ThreadLocal/SplittableRandom network code)
   * JDK-8056249 (CompletableFuture resource usage improvement)
   In addition, lots of Javadoc tweaks / clarifications from the
   OpenJDK Jira (cf. tickets for more details)

1.1.4-stable (2015-02-09)
 - added a new "native" specialization for
   * j.u.LinkedList (Ticket#10)
 - added the missing static methods from j.u.Map.Entry
   to j8.u.Maps.Entry (Ticket#16)
 - consolidated the OpenJDK test suite and the Java 6 tests:
   The whole test suite can be run on Java 6 now (Ticket#17)
   It even works on Android with some caveats (Ticket#15/#17/#18)
   ART is a good environment for the suite (even on 4.4), Dalvik
   not so much
 - first release with dedicated support for Android (Ticket#15)
   All of the currently available "native" Spliterator implementations
   are also supported on Android
   The "java8.util.Spliterators.assume.oracle.collections.impl"
   property still has to be set to "false", but the library will
   detect that it is running on Android and enables the Android
   implementation automatically
   Parallel streams on Android are also supported since 1.1.4
   Tests on a Galaxy Nexus API 15 AVD seem to indicate that the
   library can even be used on Ice Cream Sandwich devices.
   Running the test suite on real Dalvik VM devices is hard because
   of heap memory constraints, but API 19 & 21 are quite well tested.

1.1.3-stable (2015-01-07)
 - added additional native specializations for
   * j.u.c.LinkedBlockingQueue (Ticket#9)
   * j.u.c.LinkedBlockingDeque (Ticket#9)
 - Ticket#12 & Ticket#13:
   It is now possible to turn off the automatic use of native
   specializations with the boolean system property
   java8.util.Spliterators.assume.oracle.collections.impl=false
   The property must have been set not later than the
   java8.util.Spliterators class is loaded
   This switch is provided for users on non-OpenJDK based
   JREs or for Android developers to increase the odds that
   streamsupport may be running on their platform. For all
   others nothing has changed.

1.1.2-stable (2014-09-22)
 - added j8.u.StreamSupport#parallelStream(Collection c) (Ticket#8)
 - added j8.u.c.ThreadLocalRandom (Ticket#7)
 - reenact fix for OpenJDK Bug ID: JDK-8037857
 - reenact fix for OpenJDK Bug ID: JDK-8042355

1.1.1-stable (2014-08-03)
 - fixed Ticket#6 (Bug in j8.u.Comparators)

1.1-stable (2014-08-01)
 - added the OpenJDK Map-defaults test to Java 8 tests
 - added minor test for COW collections to Java 6 tests
 - Javadoc: emphasized early-binding behavior of COW collections
 - no further changes since 1.1-rc3

1.1-rc3 (2014-07-27)
 - added additional specializations for
   * j.u.c.CopyOnWriteArrayList
   * j.u.c.CopyOnWriteArraySet
 - updated Javadoc for new/changed methods

1.1-rc2 (2014-07-20)
 - added additional specializations for
   * j.u.ArrayDeque
   * j.u.Vector
   * j.u.PriorityQueue
   * j.u.c.PriorityBlockingQueue
 - finished Ticket#4 (ArrayListSpliterator non-interference)
 - conducted the changes from Ticket#5
   * do the dispatch in a new method Spliterators#spliterator(Collection c)
   * use this method in j8.u.s.StreamSupport#stream(Collection c)
   * use this method in j8.u.s.Nodes.CollectionNode#spliterator()
   * make Spliterators.IteratorSpliterator<T> package-private again

1.1-rc1 (2014-07-13)
 - changes in j8.u.s.StreamSupport#stream(Collection<? extends T> c)
   * specialized for LinkedHashSet and ArrayBlockingQueue
   * natively specialized for ArrayList and Arrays$ArrayList
   * uses Java 8 interface defaults for List, Set and SortedSet
 - made Spliterators.IteratorSpliterator<T> public
 - minor Javadoc fixes

1.0-stable (2014-06-22)
 - first "stable release" (1.0)
 - added missing default methods from j.u.(c.Concurrent)Map to j8.u.Maps

rc-3.0 (2014-06-16)
 - fixed Ticket#3 (Stream.distinct() throws NoClassDefFoundError on Google App Engine)
 - added missing setAll() / parallelSetAll() methods to j8.u.J8Arrays
 - added missing parallelPrefix() methods to j8.u.J8Arrays
 - added parallelPrefix(), parallelSort() and (parallel)setAll() tests to Java 8 tests
 - added parallelSort() test to Java 6 tests

rc-2.0 (2014-06-09)
 - added missing j.u.Comparator methods to j8.u.Comparators
 - fixed Ticket#2 (Javadoc errors)
 - added Comparator tests to Java 8 tests

rc-1.5 (2014-06-01)
 - added j.u.c.RecursiveTask / j.u.c.RecursiveAction
 - visibility of Spliterators.OfPrimitive#forEachRemaining is now public
 - fixed most of the worst Javadoc errors (Ticket#2)

rc-1.0 (2014-05-29)
 - OpenJDK stream tests port completed (passes > 10000 tests now)
 - fixed SecurityException in j8.u.c.CompletableFuture
 - added j.u.c.Phaser
 - minor Striped64 performance improvement
 - added a couple of Java 6 tests

beta-1.5 (2014-05-24)
 - added missing OpenJDK tests from beta-1

beta-1 (2014-05-22)
 - Passing approximately 5700 OpenJDK stream tests
 - fixed Ticket#1: "A certain amount of boxing"
 - fixed ClassCastException in j8.u.s.AbstractPipeline#wrapSink
 - added missing method PrimitiveIterator#forEachRemaining
 - Spliterators.OfPrimitive's visibility is now public
 - more Javadoc

alpha-2 (2014-05-17)
 - added j.u.SplittableRandom
 - added j.u.c.atomic.Long/DoubleAdder
 - added j.u.c.atomic.Long/DoubleAccumulator
 - fixed stack overflow in j8.u.Objects#deepEquals
 - license + example code

alpha-1 (2014-05-10)
 - initial drop
