# Mini QuickFIX

A lightweight FIX (Financial Information eXchange) protocol library for Java.

## Overview

Mini QuickFIX is a simple, easy-to-use Java library for working with the FIX protocol. It provides a clean API for creating, parsing, and managing FIX messages without the complexity of full-featured FIX engines.

## Features

- **Message Building**: Fluent API for constructing FIX messages
- **Message Parsing**: Parse FIX messages from both raw and readable formats
- **Session Management**: Built-in session handling with sequence numbers
- **Checksum Validation**: Automatic checksum calculation and validation
- **Type-Safe Fields**: Enumerated field types for common FIX tags
- **Multiple Message Types**: Support for Logon, Heartbeat, NewOrderSingle, ExecutionReport, and more

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Quick Start

### Build the Project

```bash
mvn clean compile
```

### Run the Demo

```bash
mvn exec:java
```

Or run directly with Java after building:

```bash
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

## Project Structure

```
miniQuickFix/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── fix/
│   │                   ├── FixBuilder.java       # Fluent API for building messages
│   │                   ├── FixParser.java        # Message parsing utilities
│   │                   ├── FixMessage.java       # Core message class
│   │                   ├── FixField.java         # Field representation
│   │                   ├── FixFieldType.java     # Field type enumerations
│   │                   ├── FixMsgType.java       # Message type constants
│   │                   ├── FixSession.java       # Session management
│   │                   ├── FixException.java     # Custom exception
│   │                   └── FixExample.java       # Demo and examples
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── fix/
├── pom.xml
└── README.md
```

## Key Classes

- **FixMessage**: Represents a FIX message with fields
- **FixBuilder**: Fluent builder for creating FIX messages
- **FixParser**: Parses FIX messages from strings
- **FixSession**: Manages FIX session state and sequence numbers
- **FixFieldType**: Enumeration of common FIX field tags
- **FixMsgType**: Constants for FIX message types

## Maven Commands

| Command | Description |
|---------|-------------|
| `mvn clean` | Clean the project |
| `mvn compile` | Compile the source code |
| `mvn test` | Run tests |
| `mvn package` | Build JAR file |
| `mvn exec:java` | Run the demo application |
| `mvn clean install` | Full build with install to local repository |

## Contributing

This is a demonstration project. Feel free to extend it with additional features such as:
- More message types
- Enhanced validation
- Network transport layer
- Message store persistence
- Admin message handling

## License

This project is provided as-is for educational and demonstration purposes.

## Author

Created by alanwang
