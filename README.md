# Lavalink Client [![Release](https://img.shields.io/github/tag/KittyBot-Org/Lavalink-Client.svg)](https://jitpack.io/#KittyBot-Org/Lavalink-Client)
This is a modified version of <https://github.com/freyacodes/Lavalink-Client> which does not depend on [lavaplayer](https://github.com/sedmelluq/lavaplayer). Therefore it's not compatible with lavaplayer in one codebase
This specific Fork supports JDA 5, and will be updated for each alpha.

## Installation
Lavalink-Client does not have a maven repository and instead uses Jitpack.
You can add the following to your POM if you're using Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.KittyBot-Org</groupId>
        <artifactId>Lavalink-Client</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Or Gradle:

```groovy
    repositories {
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        compile group: 'com.github.KittyBot-Org', name: 'Lavalink-Client', version: 'x.y.z'
    }
```

### Jitpack versions
Jitpack versioning is based on git branches and commit hashes, or tags. Eg:

```
ab123c4d
master-SNAPSHOT
dev-SNAPSHOT
3.2
```

***Note:*** The above versions are for example purposes only.

Version tags of this client are expected to roughly follow lavalink server versioning.

## Usage
This guide assumes you have JDA in your classpath, and your bot is written with JDA.

### Configuring Lavalink
All your shards should share a single Lavalink instance. Here is how to construct an instance:

```java
JdaLavalink lavalink = new JdaLavalink(
                myDiscordUserId,
                fixedNumberOfShards,
                shardId -> getJdaInstanceFromId(shardId)
        );
```

The interesting part is the third parameter, which is a `Function<Integer, JDA>`.
You must define this `Function` so that Lavalink can get your current JDA instance for that shardId.

You can now register remote nodes to your Lavalink instance:
```java
lavalink.addNode("ws://example.com", "my-secret-password");
```

If a node is down Lavalink will continue trying to connect until you remove the node.
When a node dies Lavalink will attempt to balance the load unto other nodes if they are available.

Next when you are building a shard, you must register Lavalink as an event listener to bind your shard.
You may not register more than one Lavalink instance per shard.

```java
JDABuilder.createDefault("token")
        .addEventListener(myJdaLavalinkInstance)
        .setVoiceDispatchInterceptor(myJdaLavalinkInstance.getVoiceInterceptor())
        ...
```

### The Link class
The `JdaLink` class is the state of one of your guilds in relation to Lavalink.
A `JdaLink` object is instantiated if it doesn't exist already when invoking `JdaLavalink#getLink(Guild/String)`.

```java
JdaLink someLink = myLavalink.getLink(someGuild);
someLink = myLavalink.getLink(someGuildId);
```

Here are a few important methods:
* `connect(VoiceChannel channel)` connects you to a VoiceChannel.
  * Note: This also works for moving to a new channel, in which case we will disconnect first.
* `disconnect()` disconnects from the VoiceChannel.
* `destroy()` resets the state of the `Link` and removes Lavalink's internal reference to this Link. This `Link` should be discarded.
* `getPlayer()` returns an `IPlayer` you can use to play music with.

The `IPlayer` more or less works like a drop-in replacement for Lavaplayer's `AudioPlayer`. Which leads me to...

**Warning:** You should not use JDA's `AudioManager#openAudioConnection()` or `AudioManager#closeAudioConnection()` when Lavalink is being used. Use `Link` instead.

### Using Lavalink and Lavaplayer in the same codebase
This not possible with this client anymore look at https://github.com/freyacodes/Lavalink-Client if you need this

### Node statistics
Lavalink-Client allows access to the client WebSockets with `Lavalink#getNodes()`.
This is useful if you want to read the statistics and state of a node connection.

Useful methods:
* `isAvailable()` Whether or not we are connected and can play music.
* `getRemoteUri()` Returns a `URI` of the remote address.
* `getStats()` Returns a nullable `RemoteStats` object, with statistics from the Lavalink Server. Updated every minute.

# Common pitfalls
If you are experiencing problems with playing audio or joining a voice channel with Lavalink please check to see if all these apply to you:

1. You are adding the Lavalink instance to your JDABuilder *before* building it. Lavalink must be able to receive the ready event.
2. You don't have multiple Lavalink instances.
3. You don't attempt to join a voice channel via JDA directly.

# Using this client without JDA
Since this client has been rewritten to be generic, it is possible to write custom implementations of the abstract 
`Lavalink` and `Link` classes. 

The client was made generic with FredBoat in mind, but if you need generics for your own purposes, you can open an issue 
and I can elaborate on this brief documentation.
