# Lemon
An extensive punishment, moderation, security and rank suite.

## Features:
- Network-specific color scheme and server information.
- JS scripts through the use of [helper](https://github.com/lucko/helper).
- Synchronized user & sub-server data through the use of Redis.
- Player data persistence through MongoDB.
  - Functionality to easily grab profiles & select specific ones in commands if mutliple were found.
- Realistic user disguise functionality.
  - Disguise rank selection & disguise synchronization across the network.
- Global chat channels, and additional staff functionality.
  - An advanced filter including the following checks:
    - Similarity
    - Profanity
    - Spam
  - An advanced builder-style API to dynamically create global/local chat channels.
  - Player-specific chat cooldowns as well as global ones.
- A dynamic player list command, and channel commands.
- Management commands & functionality.
  - Support for offline player/non-existent profiles to be supplied through commands.
    - Redis-based UUID cache to support this.
- Advanced alternate-account tracking system 
