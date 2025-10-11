# Mini QuickFIX

A lightweight FIX (Financial Information eXchange) protocol library for Java.

## Overview

Mini QuickFIX is a simple, easy-to-use Java library for working with the FIX protocol. It provides a clean API for creating, parsing, and managing FIX messages without the complexity of full-featured FIX engines.

## Features

### Core Features
- **Message Building**: Fluent API for constructing FIX messages
- **Message Parsing**: Parse FIX messages from both raw and readable formats
- **Session Management**: Built-in session handling with sequence numbers
- **Checksum Validation**: Automatic checksum calculation and validation
- **Type-Safe Fields**: Enumerated field types for common FIX tags
- **Multiple Message Types**: Support for Logon, Heartbeat, NewOrderSingle, ExecutionReport, and more

### Extension Features
- **Message Validation**: Automatic validation of FIX messages with detailed error reporting
- **Repeating Groups**: Support for complex FIX repeating groups (market data, legs, etc.)
- **Message Persistence**: Store and retrieve messages with sequence number management
- **Event Listeners**: Event-driven architecture for monitoring and business logic
- **TCP Connector**: Network connectivity for sending/receiving FIX messages over TCP

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Installation

### Install to Local Maven Repository

To use this library in your own projects, first install it to your local Maven repository:

```bash
# Clone and install
git clone https://github.com/alaneel/mini-quickfix.git
cd mini-quickfix
mvn clean install
```

This will:
- Compile the source code
- Run tests
- Generate JAR files (main, sources, and javadoc)
- Install to your local Maven repository at `~/.m2/repository`

### Add as Dependency

After installation, add this dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.example.fix</groupId>
    <artifactId>mini-quickfix</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Manual JAR Installation

Alternatively, you can build and manually reference the JAR:

```bash
mvn clean package
# The JAR will be at target/mini-quickfix-1.0.0.jar
```

Then add the JAR to your project's classpath.

## Quick Start

### Run the Demo

```bash
# Using Maven exec plugin
mvn exec:java

# Or build and run the JAR
mvn package
java -jar target/mini-quickfix-1.0.0.jar
```

### Run Tests

```bash
mvn test
```

## Usage Examples

### Creating a Logon Message

```java
FixMessage logon = FixBuilder.logon()
    .sender("CLIENT1")
    .target("BROKER1")
    .msgSeqNum(1)
    .sendingTime()
    .field(FixFieldType.HEARTBT_INT, 30)
    .build();

System.out.println(logon);
```

### Creating a New Order

```java
FixMessage order = FixBuilder.newOrderSingle()
    .sender("CLIENT1")
    .target("BROKER1")
    .msgSeqNum(5)
    .sendingTime()
    .field(FixFieldType.CL_ORD_ID, "ORDER123")
    .field(FixFieldType.SYMBOL, "AAPL")
    .field(FixFieldType.SIDE, "1")  // 1=Buy
    .field(FixFieldType.ORDER_QTY, 100)
    .field(FixFieldType.ORD_TYPE, "2")  // 2=Limit
    .field(FixFieldType.PRICE, 150.50)
    .build();
```

### Parsing a FIX Message

```java
String fixString = "8=FIX.4.4|9=100|35=D|49=CLIENT1|56=BROKER1|...";
FixMessage message = FixParser.parseReadable(fixString);

String msgType = message.getMsgType();
String sender = message.getFieldValue(FixFieldType.SENDER_COMP_ID);
```

### Session Management

```java
FixSession session = new FixSession("CLIENT1", "BROKER1", 30);

// Create logon
FixMessage logon = session.createLogon();

// Create heartbeat
FixMessage heartbeat = session.createHeartbeat();

// Create logout
FixMessage logout = session.createLogout("Session terminated");
```

## Extension Features

### Message Validation

Validate FIX messages with detailed error and warning reporting:

```java
import com.example.fix.validation.FixValidator;
import com.example.fix.message.*;

// Validate any message automatically
FixMessage order = FixBuilder.newOrderSingle()
    .sender("CLIENT1")
    .target("BROKER1")
    // ... fields ...
    .build();

FixValidator.ValidationResult result = FixValidator.validate(order);
if (!result.isValid()) {
    System.out.println("Validation errors:");
    for (String error : result.getErrors()) {
        System.out.println("  - " + error);
    }
}
```

### Repeating Groups

Handle complex repeating groups for market data and other structures:

```java
import com.example.fix.message.FixRepeatingGroup;

// Create market data snapshot with repeating groups
// NoMDEntries=268, MDEntryType=269
FixRepeatingGroup mdEntries = new FixRepeatingGroup(268, 269);

mdEntries.addEntry()
    .setField(269, "0")      // MDEntryType = Bid
    .setField(270, "150.25")  // Price
    .setField(271, "1000");   // Size

mdEntries.addEntry()
    .setField(269, "1")      // MDEntryType = Offer
    .setField(270, "150.30")
    .setField(271, "800");

mdEntries.addToMessage(marketDataMessage);
```

### Message Persistence

Store and retrieve messages with automatic sequence number management:

```java
import com.example.fix.store.FixMessageStore;
import com.example.fix.session.FixSession;

// Create message store
FixMessageStore store = new FixMessageStore("./store", "CLIENT1-BROKER1");

// Load existing sequence numbers
FixMessageStore.SequenceNumbers seqNums = store.loadSequenceNumbers();

// Create session
FixSession session = new FixSession("CLIENT1", "BROKER1", 30);
FixMessage logon = session.createLogon();

// Store outgoing message
int seqNum = Integer.parseInt(logon.getFieldValue(FixFieldType.MSG_SEQ_NUM));
store.storeOutgoingMessage(seqNum, logon);

// Retrieve messages for resend
List<String> messages = store.getMessages(1, 10);
```

### Event Listeners

Monitor and react to FIX session events:

```java
import com.example.fix.event.FixEventListener;
import com.example.fix.session.FixSession;
import com.example.fix.message.FixMessage;

// Create custom event listener
FixEventListener listener = new FixEventListener() {
    @Override
    public void onLogon(FixSession session, FixMessage logonMessage) {
        System.out.println("Session logged on");
    }

    @Override
    public void onMessageReceived(FixSession session, FixMessage message) {
        System.out.println("Received: " + message.getMsgType());
    }

    @Override
    public void onError(FixSession session, Exception error) {
        System.err.println("Error: " + error.getMessage());
    }

    @Override
    public void onLogout(FixSession session, FixMessage logoutMessage) {
        System.out.println("Session logged out");
    }
};

// Use the listener to monitor session events
listener.onMessageReceived(session, message);
```

### TCP Network Connector

Send and receive FIX messages over TCP:

```java
import com.example.fix.transport.FixConnector;
import com.example.fix.message.FixMessage;

// Create connector
FixConnector connector = new FixConnector("localhost", 9878);

// Set message handler
connector.setMessageHandler(message -> {
    System.out.println("Received: " + message.getMsgType());
});

// Connect and send
connector.connect();
connector.send(logonMessage);

// Disconnect
connector.disconnect();
```

### Running Extension Features Demo

To see all extension features in action:

```bash
# Run extensions demo
mvn exec:java -Dexec.mainClass="com.example.fix.demo.FixExtensionsDemo"
```

## Project Structure

```
miniQuickFix/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── fix/
│   │                   ├── message/                    # Core message handling
│   │                   │   ├── FixMessage.java        # Core message class
│   │                   │   ├── FixField.java          # Field representation
│   │                   │   ├── FixBuilder.java        # Fluent API for building messages
│   │                   │   ├── FixParser.java         # Message parsing utilities
│   │                   │   └── FixRepeatingGroup.java # Repeating groups support
│   │                   ├── session/                   # Session management
│   │                   │   └── FixSession.java        # Session state and sequence numbers
│   │                   ├── validation/                # Message validation
│   │                   │   └── FixValidator.java      # Message validation with error reporting
│   │                   ├── store/                     # Persistence layer
│   │                   │   └── FixMessageStore.java   # Message storage and sequence management
│   │                   ├── event/                     # Event system
│   │                   │   └── FixEventListener.java  # Event listener interface and implementations
│   │                   ├── transport/                 # Network connectivity
│   │                   │   └── FixConnector.java      # TCP network connector
│   │                   ├── types/                     # Enumerations and constants
│   │                   │   ├── FixFieldType.java      # Field type enumerations
│   │                   │   └── FixMsgType.java        # Message type constants
│   │                   ├── exception/                 # Custom exceptions
│   │                   │   └── FixException.java      # FIX-related exceptions
│   │                   └── demo/                      # Demo applications
│   │                       ├── FixExample.java             # Basic usage demo
│   │                       └── FixExtensionsDemo.java      # Extension features demo
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── fix/
├── pom.xml
├── LICENSE
└── README.md
```

## Key Classes

### Core Message Handling (`message` package)
- **FixMessage**: Represents a FIX message with fields
- **FixField**: Represents a single FIX field (tag/value pair)
- **FixBuilder**: Fluent builder for creating FIX messages
- **FixParser**: Parses FIX messages from strings
- **FixRepeatingGroup**: Handles repeating groups in FIX messages

### Session Management (`session` package)
- **FixSession**: Manages FIX session state and sequence numbers

### Message Validation (`validation` package)
- **FixValidator**: Validates FIX messages with detailed error reporting

### Persistence Layer (`store` package)
- **FixMessageStore**: Persists messages and manages sequence numbers for session recovery

### Event System (`event` package)
- **FixEventListener**: Interface for implementing custom event listeners to monitor session events

### Network Connectivity (`transport` package)
- **FixConnector**: TCP network connector for sending/receiving messages
- **FixClientExample**: Example implementation of a FIX client

### Types and Constants (`types` package)
- **FixFieldType**: Enumeration of common FIX field tags
- **FixMsgType**: Constants for FIX message types

### Custom Exceptions (`exception` package)
- **FixException**: Custom exception for FIX-related errors

## Maven Commands

| Command | Description |
|---------|-------------|
| `mvn clean` | Clean the project |
| `mvn compile` | Compile the source code |
| `mvn test` | Run tests |
| `mvn package` | Build JAR file |
| `mvn exec:java` | Run the demo application |
| `mvn clean install` | Full build with install to local repository |
| `mvn source:jar` | Generate sources JAR |
| `mvn javadoc:jar` | Generate javadoc JAR |

## Using as a Library

Once installed to your local Maven repository or added as a JAR dependency, you can import and use the library in your projects:

```java
import com.example.fix.message.*;
import com.example.fix.session.FixSession;
import com.example.fix.exception.FixException;
import com.example.fix.types.*;

public class MyTradingApp {
    public static void main(String[] args) {
        try {
            // Create a FIX session
            FixSession session = new FixSession("MY_CLIENT", "BROKER", 30);

            // Build and send messages
            FixMessage logon = session.createLogon();
            System.out.println("Sending: " + logon.toFixString());

            // Parse incoming messages
            String incoming = "8=FIX.4.4|9=100|35=8|...";
            FixMessage executionReport = FixParser.parseReadable(incoming);

        } catch (FixException e) {
            e.printStackTrace();
        }
    }
}
```

## Distribution

### Installing to Local Repository

```bash
mvn clean install
```

The library will be installed to your local Maven repository at:
```
~/.m2/repository/com/example/fix/mini-quickfix/1.0.0/
```

The following artifacts will be generated:
- `mini-quickfix-1.0.0.jar` - Main library
- `mini-quickfix-1.0.0-sources.jar` - Source code
- `mini-quickfix-1.0.0-javadoc.jar` - API documentation

### For Enterprise Distribution

To deploy to a corporate Maven repository (Nexus, Artifactory, etc.), update the `distributionManagement` section in `pom.xml` with your repository details:

```xml
<distributionManagement>
    <repository>
        <id>your-releases</id>
        <url>https://your-repo.com/releases</url>
    </repository>
    <snapshotRepository>
        <id>your-snapshots</id>
        <url>https://your-repo.com/snapshots</url>
    </snapshotRepository>
</distributionManagement>
```

Then deploy with:
```bash
mvn clean deploy
```

## Contributing

This is a demonstration project. Feel free to extend it with additional features such as:
- More message types (Market Data, Order Cancel, etc.)
- Enhanced validation and error handling
- Network transport layer (TCP/IP socket support)
- Message store persistence
- Admin message handling
- FIX 5.0+ support
- Unit tests

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Created by Alan Wang
