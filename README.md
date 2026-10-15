# Mini QuickFIX

**A small Java FIX protocol implementation for learning how messages and sessions fit together.**

Build a message, inspect its tag/value encoding, parse it back, and follow the session sequence state. The repository also includes validation, repeating-group, file-store, listener, and TCP connector examples. It is an independent educational project, not the QuickFIX/J engine or a certified FIX implementation.

[Message implementation](src/main/java/com/example/fix/message) · [Session state](src/main/java/com/example/fix/session/FixSession.java) · [Tests](src/test/java/com/example/fix) · [Project status](PROJECT_STATUS.md)

## Run the examples

Use Java 11+ and Maven 3.6+, from the repository root:

```bash
mvn compile exec:java
```

The configured main class is `com.example.fix.demo.FixExample`. It demonstrates message building, parsing, and session operations in the console. For validation, repeating groups, file storage, and listener examples:

```bash
mvn compile exec:java -Dexec.mainClass=com.example.fix.demo.FixExtensionsDemo
```

These are console demos. The inspected source does not contain a REST server or browser demo; the TCP connector is a separate library component, not an exchange connection supplied by this repository.

## Build a message

```java
import com.example.fix.message.FixBuilder;
import com.example.fix.message.FixMessage;
import com.example.fix.types.FixFieldType;

FixMessage logon = FixBuilder.logon()
    .sender("CLIENT1")
    .target("BROKER1")
    .msgSeqNum(1)
    .sendingTime()
    .field(FixFieldType.HEARTBT_INT, 30)
    .build();

System.out.println(logon.toFixString());
```

See [FixExample](src/main/java/com/example/fix/demo/FixExample.java) for complete executable usage, rather than placeholder wire messages with fabricated lengths or checksums.

## Read the implementation

| Concern | Code |
|---|---|
| Tag/value representation and serialization | [FixMessage](src/main/java/com/example/fix/message/FixMessage.java) |
| Raw and readable message parsing | [FixParser](src/main/java/com/example/fix/message/FixParser.java) |
| Message and field checks | [FixValidator](src/main/java/com/example/fix/validation/FixValidator.java) |
| Sequence state and session messages | [FixSession](src/main/java/com/example/fix/session/FixSession.java) |
| File-backed message storage | [FixMessageStore](src/main/java/com/example/fix/store/FixMessageStore.java) |
| Socket transport | [FixConnector](src/main/java/com/example/fix/transport/FixConnector.java) |

Class names and example coverage are not guarantees of protocol completeness, durable crash recovery, exchange interoperability, or production session resilience. Inspect the parser, store, and connector boundaries before adapting them.

## Test and use locally

```bash
mvn test
mvn clean install
```

The first command runs the checked-in JUnit tests for messages, builders, parsers, and sessions. The second builds and installs the artifact to your local Maven repository. Test configuration is not a claim that every extension has integration coverage.

```xml
<dependency>
    <groupId>com.example.fix</groupId>
    <artifactId>mini-quickfix</artifactId>
    <version>1.0.0</version>
</dependency>
```

These coordinates describe a **local installation**; they do not imply a Maven Central release. Maven executes the demo with its dependencies on the classpath; a standalone dependency-bundled JAR is not configured in the inspected POM.

Created by Alan Wang. See [CONTRIBUTING.md](CONTRIBUTING.md) and the [MIT license](LICENSE).
