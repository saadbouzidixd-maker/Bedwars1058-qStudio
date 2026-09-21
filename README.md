# BedWars1058-qStudio

**By : qSa3ed**

BedWars1058-qStudio is an enhanced fork of [BedWars1058](https://github.com/andrei1058/BedWars1058),
the open source bed wars mini-game by Andrei Dascălu (GPL 3.0). The original
gameplay, shop, arena and multi-version architecture are kept intact; qStudio
adds a configurable enhancement layer on top: modern gameplay items that work
on legacy versions, deeper combat tuning, more traps and upgrades, progression
and better administration tooling.

- Requires Java 17+ (runtime) and Maven (build)
- Supported servers: Spigot/Paper 1.8.8 through 1.20.4 (same matrix as BedWars1058)
- Original project and credits: Andrei Dascălu and the BedWars1058 contributors

## Features

### Modern item compatibility layer
Modern mechanics are represented on old versions through configurable legacy
materials. The material is only the visual representation; behaviour comes from
qStudio. Each item has display item, cooldown, particles, sounds and per-group
toggles in `qstudio.yml` (`modern-items.<id>`):

| Item | Legacy representation | Highlights |
|---|---|---|
| Mace | Iron Shovel | smash attack, falling damage scaling, area knockback |
| Wind Charge | Snowball | self launch, vertical/horizontal knockback, strength |
| Firework Boost | Firework | directional boost, charges |
| Dash | Feather | short dash, multi-use with stored charges |
| Grappling Hook | Fishing Rod | pull to blocks, max range, cooldown |
| Temporary Bridge | Wool block | auto-expiring team bridge, max length/blocks |
| Bridge Builder | Wool block | continuous bridging with void safety checks |
| Teleport Beacon | Beacon block | activation delay, enemy interruption, uses limit |
| Emergency Teleport | Ender Pearl | channel back to base, cancel on damage/move |
| Base Shield | Nether Star | temporary team damage reduction |
| EMP Pulse | Blaze Powder | disables enemy shields, beacons and temp blocks |

Items are purchased through the regular shop (config key
`modern-items.<id>` inside a shop content `receive` list or via the
`qstudio.yml` item settings) and state such as remaining charges is stored
on the item itself, so it survives drops and pickup without duplication.

### Combat
- knockback profiles per arena group: ground, air, sprint, projectile and
  explosion multipliers (`combat.knockback.*`)
- hit feedback: sounds, action bar, optional attack cooldown
- combat tagging with configurable duration
- projectile tuning and explosive tuning (fireball/TNT radius, TNT jump)

### Traps
Extended trap actions (blindness, slowness, weakness, alarm, knockback,
marking, exhaustion) driven through the existing trap queue, plus the
`QSTrapTriggerEvent` fired when a base trap activates.

### Team upgrades
New upgrade actions usable in `upgrades2` tiers via the `receive` list:
`forge`, `trap-capacity`, `regen-aura`, `defense-aura`,
`anti-invisibility`, `gen-boost`.

### Shop
- purchase limits: per life, per game, cooldown (`shop.purchase-limits.*`)
- modern items integrated with the vanilla shop format

### Progression
- quests (daily/weekly/once) configured in `progression.quest-list`,
  action bar progress, `/bw quests`-style GUI through the quests command
- achievements evaluated from persistent stats
- async persistence of quest state through the configured database

### Administration
- `/qstudio diagnostics` — arena, team and generator overview
- `/qstudio validate` — config validator for materials, slots and references
- `/qstudio debug` — toggle verbose qStudio logging (off by default)

## Installation
1. Build or download the plugin jar.
2. Drop it into your server `plugins/` folder (it replaces BedWars1058 —
   do not run both).
3. Start the server: `BedWars1058` config is migrated as usual and
   `qstudio.yml` plus the `profiles/` folder are generated.
4. Optional: install PlaceholderAPI before first start to enable the
   placeholder extension.

## PlaceholderAPI
Registered only when PlaceholderAPI is present:

`%bw1058_qstudio_level%`, `%bw1058_qstudio_quests_done%`,
`%bw1058_qstudio_achievements%`, `%bw1058_qstudio_arena%`,
`%bw1058_qstudio_team%` (prefix matches the plugin placeholder prefix).

## Configuration
- `qstudio.yml` — every feature toggle and value, organised by section
  (`modern-items`, `combat`, `explosives`, `shop`, `progression`)
- `profiles/<group>.yml` — per arena group overrides; assign profiles in
  `qstudio.yml` under `profile-groups`
- the validator reports misconfigured materials, slots and references

## Building
```
mvn clean package
```
The shaded plugin jar is produced in `bedwars-plugin/target/`.

## Developer API
Add the api module and use `com.andrei1058.bedwars.api.qstudio`:

- `QStudioApi` — modern item availability, generator status, forge level
- `ModernItems` — constants of the built-in modern items
- events: `QSTrapTriggerEvent`, `QSPlayerPurchaseEvent`, `QSItemUseEvent`,
  `QSQuestCompleteEvent`, `QSAchievementUnlockEvent`

```xml
<repository>
    <id>codemc-releases</id>
    <url>https://repo.codemc.io/repository/maven-releases/</url>
</repository>
<dependency>
    <groupId>com.andrei1058.bedwars</groupId>
    <artifactId>bedwars-api</artifactId>
    <version>25.2</version>
    <scope>provided</scope>
</dependency>
```

## Credits & License
- BedWars1058 was created by Andrei Dascălu and is licensed under
  [GNU GPL 3.0](LICENSE). This fork keeps the original copyright notices
  and is distributed under the same license.
- qStudio enhancements: **qSa3ed**

See [CHANGELOG.md](CHANGELOG.md) for version history and
[CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.
