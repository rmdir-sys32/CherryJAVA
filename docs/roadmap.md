# Redis-in-Java Build Plan & Roadmap

## 🏗️ Architecture Overview

Before coding, it helps to understand the layers of our database:

```
+-------------------------------------------------+
|                  TCP Clients                    |
+-------------------------------------------------+
                        |
                        | RESP Commands (Raw Bytes)
                        v
+-------------------------------------------------+
|               Network I/O Layer                 |
|       (Accepts connections, reads/writes)        |
+-------------------------------------------------+
                        |
                        | Raw Bytes Stream
                        v
+-------------------------------------------------+
|                  RESP Parser                    |
|   (Parses raw bytes into structured commands)   |
+-------------------------------------------------+
                        |
                        | Command Objects (e.g. GET key)
                        v
+-------------------------------------------------+
|                Command Router                   |
|       (Routes command to the handler)           |
+-------------------------------------------------+
       /                                   \
      /                                     \
     v                                       v
+-----------------------+             +-----------------------+
|   In-Memory Store     |             |  Persistence Engine   |
| (ConcurrentHashMap &  |             |      (AOF / RDB)      |
|  TTL Eviction Engine) |             +-----------------------+
+-----------------------+
```

---

## 📅 The Step-by-Step Milestones

Here is the sequential plan we will follow. We will complete one feature at a time, providing exact, copy-pasteable Java code for that specific step without skipping details or requiring you to rewrite core parts from scratch.

### Milestone 1: The Basic TCP Socket Server (Single Client)
* **Goal**: Establish a TCP server on port `6379` that can accept a connection, read data from a client, and return a simple hardcoded string response (e.g., `+PONG\r\n`).
* **Topics Covered**: `ServerSocket`, `Socket`, Input/Output Streams, standard Redis port conventions.
* **Outcome**: A server that can be pinged using `redis-cli` or `nc localhost 6379` and successfully responds.

### Milestone 2: Handling Concurrency (Multi-Client Support)
* **Goal**: Enable the server to accept connections from multiple clients concurrently without blocking.
* **Topics Covered**: Multithreading, Thread Pools (`ExecutorService`), client connection lifecycle, thread safety introduction.
* **Outcome**: Multiple terminal clients or instances of `redis-cli` interacting with the server at the same time.

### Milestone 3: RESP (Redis Serialization Protocol) Parser
* **Goal**: Implement a complete and robust parser for RESP2.
* **Topics Covered**: Stream parsing, state machines, and handling RESP types:
  * Simple Strings (e.g., `+OK\r\n`)
  * Errors (e.g., `-ERR unknown command\r\n`)
  * Integers (e.g., `:1000\r\n`)
  * Bulk Strings (e.g., `$5\r\nhello\r\n`)
  * Arrays (e.g., `*2\r\n$4\r\nPING\r\n$5\r\nworld\r\n`)
* **Outcome**: A clean parser that takes raw bytes/streams and converts them into structured command representation objects.

### Milestone 4: The Core Key-Value Engine & Basic Commands
* **Goal**: Implement the in-memory hash table storage and execute core Redis commands.
* **Topics Covered**: Thread-safe collections (`ConcurrentHashMap`), command routing (Command Pattern), execution.
* **Commands**:
  * `PING [message]`
  * `SET key value`
  * `GET key`
  * `EXISTS key`
  * `DEL key`
* **Outcome**: Ability to store and retrieve string values via `redis-cli`.

### Milestone 5: Key Expiry (TTL & Eviction)
* **Goal**: Add time-to-live (TTL) support for keys and clean up expired keys to prevent memory leaks.
* **Topics Covered**: Scheduling, passive (lazy) eviction, and active eviction (random sampling in a background thread).
* **Commands**:
  * `SET key value [EX seconds] [PX milliseconds]`
  * `EXPIRE key seconds`
  * `TTL key`
* **Outcome**: Automatic deletion of keys after their expiry time, simulating Redis's double-eviction strategy.

### Milestone 6: Complex Data Structures
* **Goal**: Expand the database to support more than just raw strings.
* **Topics Covered**: Java Data Structures (Lists, Sets, Sorted Sets/Hashes), encapsulation.
* **Commands**:
  * **Hashes**: `HSET`, `HGET`, `HGETALL`
  * **Lists**: `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LRANGE`
  * **Sets**: `SADD`, `SREM`, `SMEMBERS`
* **Outcome**: Supporting fully functional collections just like real Redis.

### Milestone 7: Persistence (AOF and RDB)
* **Goal**: Persist database state to disk so data is not lost when the server restarts.
* **Topics Covered**: File I/O, serialization, background threads.
* **Features**:
  * **Append-Only File (AOF)**: Log every write command to disk and replay the file on startup.
  * **RDB Snapshots**: Periodically save the database snapshot (in RESP or custom binary format) to a background file.
* **Outcome**: Data durability across server restarts.

### Milestone 8: Transactions (Atomicity & Isolation)
* **Goal**: Allow clients to execute a group of commands atomically.
* **Topics Covered**: Transaction states, command queuing, atomic blocks.
* **Commands**:
  * `MULTI`
  * `EXEC`
  * `DISCARD`
* **Outcome**: Commands inside a transaction are queued per connection and executed sequentially and atomically when `EXEC` is called.

### Milestone 9: Replication (Master-Slave Sync)
* **Goal**: Support database replication where secondary nodes sync state with a primary master.
* **Topics Covered**: Network synchronization, handshake protocol, master-replica stream replication.
* **Commands / Options**:
  * `--replicaof <master-host> <master-port>`
  * `PSYNC` / `REPLCONF`
* **Outcome**: Multi-instance database replication.

---

## 🎯 Placement & Interview Highlights

To make this project stand out on resumes, we will follow these production-grade principles:
1. **Clean Code & Design Patterns**: We will use patterns like the **Command Pattern** for command routing, **Strategy Pattern** for eviction policies, and clean abstraction for protocol parsing.
2. **Concurrency & Thread Safety**: Using lock-free concurrent collections, understanding synchronization vs. concurrent utilities, and structuring thread tasks correctly.
3. **No External Dependencies**: We will write this project using pure, standard Java SE libraries (no Spring, no Netty, no third-party libraries). This shows interviewers you understand low-level socket programming and protocol design.
4. **Structured Testing**: Showing how to write clean, unit-testable components (especially the RESP parser and storage engine).

---

## 🚀 Ready to Begin?

We will tackle this step-by-step.
* **Rule 1**: I will guide you on how to write the code, explaining every concept thoroughly.
* **Rule 2**: I will provide the complete, copy-pasteable blocks of code for the files we create or edit, so there's no confusion or partial code rewriting.
* **Rule 3**: We do not touch any code files outside the `docs` folder. You will perform the edits in the source code files yourself based on my guides!
