# Hawker Rush: 100 Ideas for Extension

This document outlines 100 ideas to extend Hawker Rush. All ideas follow the **Singapore Hawker** theme.

## Structure
Each idea includes:
- **Concept**: Description of the idea.
- **Implementation**: How to add it to the code conceptually.
- **Difficulty**: Easy/Medium/Hard + Reason.
- **Risks**: Potential issues or regressions.

---

## Hawker Stalls (Towers)

### Laksa Splash
- **Concept**: A stall that shoots spicy coconut gravy, dealing splash damage in a small radius around the target.
- **Implementation**: Create a new `StallType.LAKSA`. In `handleStallFiring`, generate a projectile with an `aoeRadius`.
- **Difficulty**: Easy. Similar to `DURIAN` but with a smaller radius and higher fire rate.
- **Risks**: Visual clutter if many Laksa stalls are placed.

### Bak Kut Teh Steam
- **Concept**: A support stall that emits "herbal steam," buffing the attack speed of all adjacent stalls.
- **Implementation**: In `handleStallFiring`, check for nearby stalls and temporarily reduce their `fireRateMs`.
- **Difficulty**: Medium. Requires a new "buff" system that isn't currently in the game.
- **Risks**: Stacking buffs might break game balance and make towers too fast.

### Chilli Crab Claw
- **Concept**: A high-damage, single-target stall that "pinches" enemies, slowing them significantly for a short duration.
- **Implementation**: Add `StallType.CHILLI_CRAB`. High `damage` and a slow effect in `handleProjectiles`.
- **Difficulty**: Easy. Combination of `CHICKEN_RICE` damage and `TEH_TARIK` slow.
- **Risks**: Overlapping slows might stop enemies completely.

### Kaya Toast Launcher
- **Concept**: Rapid-fire stall that shoots slices of crispy toast. Low damage but extremely high fire rate.
- **Implementation**: `StallType.KAYA_TOAST` with `fireRateMs = 100L` and low `damage`.
- **Difficulty**: Easy. Just a tuning change on existing projectile logic.
- **Risks**: Performance issues with too many projectiles on screen.

### Rojak Mixer
- **Concept**: Every shot has a random effect: extra damage, slow, or a small gold bonus.
- **Implementation**: In `handleProjectiles`, use `Random` to apply different logic based on the hit.
- **Difficulty**: Medium. Needs to handle gold generation from projectiles.
- **Risks**: RNG can make it frustrating for players if they get "bad" rolls.

### Oyster Omelette Crit
- **Concept**: Low base damage but has a 20% chance to deal 10x damage (a "Big Oyster" shot).
- **Implementation**: In `handleProjectiles`, add a crit multiplier check.
- **Difficulty**: Easy. Simple math modification in the damage calculation.
- **Risks**: Bosses could be trivialized by a lucky string of crits.

### Nasi Lemak Ikan Bilis
- **Concept**: Shoots a swarm of small ikan bilis (anchovies) that track different targets.
- **Implementation**: A single fire event generates multiple `Projectile` objects with slightly different target logic.
- **Difficulty**: Medium. Requires changing the 1-to-1 firing logic.
- **Risks**: Tracking many small projectiles can be CPU intensive on older phones.

### Otah Grill
- **Concept**: Deals damage in a straight line, piercing through all enemies in its path.
- **Implementation**: A projectile that doesn't disappear on first hit, but checks for all enemies along its vector.
- **Difficulty**: Medium. Needs a line-collision check rather than a point/radius check.
- **Risks**: Positioning becomes too critical, potentially making some map layouts too easy.

### Bandung Mist
- **Concept**: A pink mist stall that confuses enemies, causing them to walk backwards for 1 second.
- **Implementation**: In `handleEnemyMovement`, check for a "confused" state and reverse the `targetIndex` logic.
- **Difficulty**: Hard. Reversing pathfinding logic while maintaining movement state is tricky.
- **Risks**: Enemies could get stuck in an infinite loop if placed correctly.

### Sugar Cane Juicer
- **Concept**: A "battery" stall. It doesn't attack but increases the gold earned from enemies killed within its range.
- **Implementation**: Add a range check in `handleProjectiles` when an enemy dies to see if a Sugar Cane stall is nearby.
- **Difficulty**: Medium. Requires connecting death events to stall proximity.
- **Risks**: Economy inflation making the late game too easy.

### Satay Bee Hoon Slow
- **Concept**: Covers a large area in thick peanut sauce, slowing all enemies significantly more than Teh Tarik.
- **Implementation**: A larger `puddle` with a higher speed reduction multiplier.
- **Difficulty**: Easy. Just a variant of `TEH_TARIK`.
- **Risks**: Visual similarity to Satay stall might confuse players.

### Fried Carrot Cake (Black vs White)
- **Concept**: A stall with a toggle. "Black" mode deals AOE fire damage, "White" mode deals high single-target physical damage.
- **Implementation**: Add a toggle button in the `StallConsole`. Update the `Stall` model with a `mode` property.
- **Difficulty**: Medium. Requires UI updates and conditional firing logic.
- **Risks**: UI complexity for a mobile screen.

### Popiah Wrap
- **Concept**: Traps an enemy in a "wrap" for 3 seconds, stopping them and preventing them from being hit by other towers.
- **Implementation**: A freeze effect that also sets an `isInvulnerable` flag on the enemy.
- **Difficulty**: Medium. Needs to update `handleProjectiles` to skip invulnerable targets.
- **Risks**: Can be used to "troll" the game by saving an enemy from a killing blow.

### Char Kway Teow Char
- **Concept**: Adds a "burn" effect that deals damage over time (DOT).
- **Implementation**: Add a `dotDuration` to `Enemy` and a loop in `updateGame` to subtract HP.
- **Difficulty**: Medium. Requires persistent state tracking on enemies.
- **Risks**: Calculating DOT for 100+ enemies every tick might lag.

### Hainanese Chicken Rice (Elite)
- **Concept**: A more expensive version of the base stall that spawns a "Waiter" unit to block the path.
- **Implementation**: Stalls that can spawn temporary `TileType` changes or "blocker" entities.
- **Difficulty**: Hard. Requires pathfinding to be recalculated frequently.
- **Risks**: Blocking the path completely would break the AI.

### Durian King
- **Concept**: Massive AOE that also leaves a "stink" cloud (Gas Cloud) that lingers.
- **Implementation**: Combine `DURIAN` damage with a long-lasting `VisualEffectType.GAS_CLOUD` that deals tick damage.
- **Difficulty**: Medium. Uses existing systems but needs a "damage in cloud" check.
- **Risks**: Overlapping gas clouds could be visually overwhelming.

### Ice Kachang Brain Freeze
- **Concept**: Shards of ice that shatter on impact, freezing the target and chilling nearby enemies (slowing them).
- **Implementation**: `Projectile` hits a target, applies freeze, and then checks `aoeRadius` for a slow effect.
- **Difficulty**: Medium. Nested status effects.
- **Risks**: Chain-freezing enemies indefinitely.

### Putu Piring Pulse
- **Concept**: Releases a radial pulse of steam every 5 seconds, pushing enemies back slightly.
- **Implementation**: A stall with a very slow `fireRateMs` that modifies the `position` of enemies in a radius.
- **Difficulty**: Hard. Modifying coordinates directly can mess up pathfinding and A* logic.
- **Risks**: Enemies being pushed into walls or outside the grid.

### Murtabak Shield
- **Concept**: A stall that can be placed on the path to block enemies, but has its own HP and can be "eaten" (destroyed).
- **Implementation**: Allow stalls to be placed on `FLOOR` tiles that are part of the path, and give enemies an "attack" state.
- **Difficulty**: Hard. Major change to how enemies and stalls interact.
- **Risks**: Changes the game from TD to a hybrid lane-defense game.

### Teh C Special
- **Concept**: Three layers of effects: First hit slows, second hit burns, third hit deals massive damage.
- **Implementation**: Track "HitCount" on enemies from specific stall IDs.
- **Difficulty**: Medium. Requires state tracking on the enemy for specific stall interactions.
- **Risks**: Hard to communicate the mechanic to the player visually.

---

## Customers (Enemies)

### Hungry Ghost
- **Concept**: Transparent and invisible to most stalls unless they are within a "light" stall's range.
- **Implementation**: Add `isInvisible` to `Enemy`. Stalls skip them unless a "Detector" stall is nearby.
- **Difficulty**: Medium. Modification to the targeting loop.
- **Risks**: If the player doesn't build a detector, they lose instantly.

### Merlion Boss
- **Concept**: A giant boss with high HP that periodically sprays water, clearing all `puddles` in an area.
- **Implementation**: A boss-tier enemy with a `lastActionTime` that removes `StickyPuddle` objects from the state.
- **Difficulty**: Medium. Interaction between enemies and transient objects.
- **Risks**: Makes `TEH_TARIK` useless against the most important enemy.

### Office Lady (OL)
- **Concept**: Moves fast and carries a "First Aid" kit (bubble tea), healing nearby customers.
- **Implementation**: In `updateGame`, OL enemies check for nearby `Enemy` objects and increment their `health`.
- **Difficulty**: Medium. Requires a proximity check for every OL enemy.
- **Risks**: A pack of OLs could become an immortal death ball.

### Food Critic
- **Concept**: Very slow, but if they reach the goal, you lose 5 health (tables) instead of 1.
- **Implementation**: Add a `healthPenalty` property to the `EnemyType`.
- **Difficulty**: Easy. Simple change in the `handleEnemyMovement` goal logic.
- **Risks**: High frustration if one slips through due to a random freeze.

### Secondary School Kids
- **Concept**: Move in a tight "canteen" pack. When one is hit, they all gain a temporary speed boost.
- **Implementation**: Grouped spawning logic and a "rage" state triggered by damage.
- **Difficulty**: Medium. Needs to track "group" IDs.
- **Risks**: Sudden speed bursts can feel unfair.

### Uncle with Newspaper
- **Concept**: Stops moving for 2 seconds every few steps to "read the news," blocking other customers behind him.
- **Implementation**: Already similar to `TOURIST` logic but with a "blocker" collision box.
- **Difficulty**: Medium. Needs enemy-on-enemy collision which currently doesn't exist.
- **Risks**: Massive traffic jams and performance drops.

### Safe Entry Ambassador
- **Concept**: A "mid-boss" that slows down all other enemies behind him for "checks," effectively grouping them up for AOE.
- **Implementation**: An enemy that provides a speed debuff to other enemies in a trailing radius.
- **Difficulty**: Medium. Reverse-buff logic.
- **Risks**: Actually helps the player by grouping enemies for Durian/Satay.

### PMD Rider
- **Concept**: Extremely fast and immune to puddles, but takes extra damage from "cooling" stalls (Ice Kachang).
- **Implementation**: Flag `isPuddleImmune` and add a damage multiplier in `handleProjectiles`.
- **Difficulty**: Easy. Extension of existing status/modifier logic.
- **Risks**: Too fast for single-target stalls to hit.

### Otter Family
- **Concept**: Small, very fast enemies that move in a zig-zag pattern instead of following the path perfectly.
- **Implementation**: Add a `sineWave` offset to the `toScreenPrecise` calculation or the actual coordinate logic.
- **Difficulty**: Medium. Makes targeting much harder.
- **Risks**: Projectiles might miss constantly.

### Durian Lover
- **Concept**: An enemy that is healed by Durian stall projectiles.
- **Implementation**: Check `sourceStallType` in `handleProjectiles` and add HP instead of subtracting.
- **Difficulty**: Easy.
- **Risks**: Players might not realize why their strongest tower is helping the enemy.

### The "Chope" Auntie
- **Concept**: Leaves a "Tissue Packet" on a random stall, disabling it for 5 seconds.
- **Implementation**: Enemy-to-Stall interaction. Auntie targets the nearest `AxialCoordinate` with a stall.
- **Difficulty**: Hard. Requires enemies to have "attack" AI targeting stalls.
- **Risks**: Disabling the only defense can be frustrating.

### Giant Tropical Cockroach
- **Concept**: When "killed," it flies! Becomes a fast aerial unit that ignores pathfinding (moves straight to goal).
- **Implementation**: On death, spawn a new enemy type with `path = listOf(currentPos, goalPos)`.
- **Difficulty**: Medium. Transitions between path-following and direct-flying.
- **Risks**: Aerial units need new visual indicators so players know they can't be slowed by puddles.

### GrabFood Cyclist
- **Concept**: High speed, but crashes (stops) for 3 seconds if it takes more than 50% damage in one hit.
- **Implementation**: Track `damageTakenInTick` and apply a stop duration.
- **Difficulty**: Easy.
- **Risks**: Encourages only using high-damage towers like Durian.

### Kiasu Parent
- **Concept**: Drags a "Student" enemy. If the parent dies, the student runs at 3x speed.
- **Implementation**: "Carrier" enemy logic.
- **Difficulty**: Medium. Spawning new entities on death with velocity inheritance.
- **Risks**: Student might be too small to see/click.

### Tour Group
- **Concept**: 10+ Tourists following a "Flag Bearer." If the bearer dies, the tourists scatter (random paths).
- **Implementation**: Complex pathfinding. Tourists follow a leader's coordinate with an offset.
- **Difficulty**: Hard. Group AI and dynamic repathing.
- **Risks**: High CPU usage for 20+ tourists repathing simultaneously.

### National Serviceman (Full Battle Order)
- **Concept**: High armor. Reduces all incoming damage by 5.
- **Implementation**: `damage = Math.max(1, incomingDamage - armor)`.
- **Difficulty**: Easy.
- **Risks**: Fast-firing, low-damage stalls (Kaya Toast) become useless.

### Influencer
- **Concept**: Stops to take a "selfie," stunning all stalls in a small radius with the "flash."
- **Implementation**: Stall `isStunned` state.
- **Difficulty**: Medium.
- **Risks**: Visual flash effect might be annoying to players.

### Stray Cat
- **Concept**: Not an enemy, but walks on the path. If a stall hits it, you lose gold (fine for animal cruelty!).
- **Implementation**: A "neutral" entity in the `enemies` list with a negative reward.
- **Difficulty**: Easy.
- **Risks**: Players might get angry at the game for "tricking" them.

### Canteen Cleaner Uncle
- **Concept**: Moves around the map (not the path) and "collects" dropped gold or power-ups.
- **Implementation**: Separate AI loop for non-path entities.
- **Difficulty**: Medium.
- **Risks**: Adds more visual noise.

### Bird (Mynah)
- **Concept**: Flies over stalls and occasionally "steals" an upgrade level.
- **Implementation**: Aerial unit that targets stalls.
- **Difficulty**: Medium.
- **Risks**: Extremely high frustration factor.

---

## Map & Environment

### Sudden Rainstorm
- **Concept**: Periodically, the whole map slows down, and "puddles" form randomly on the floor.
- **Implementation**: A global `speedMultiplier` in `MainViewModel` and random `StickyPuddle` spawning.
- **Difficulty**: Easy.
- **Risks**: Can make the game feel sluggish.

### Lunch Hour Rush
- **Concept**: A "Fast Forward" mode that is forced upon the player for 30 seconds, with 2x spawn rate.
- **Implementation**: Modify `tickRate` and `spawnTimer`.
- **Difficulty**: Easy.
- **Risks**: Players might lose control of the game state.

### Table Cleaning Robot Obstacle
- **Concept**: A robot that moves along the paths, pushing enemies back if it hits them.
- **Implementation**: A "friendly" enemy that follows the path in reverse.
- **Difficulty**: Medium.
- **Risks**: Can create "infinite" stalls where enemies never progress.

### Tissue Packet "Chope" Zones
- **Concept**: Certain hexes are blocked by tissue packets at the start of the wave. You can't place stalls there.
- **Implementation**: Add a `isReserved` flag to `HexTile`.
- **Difficulty**: Easy.
- **Risks**: Limits player freedom on already tight maps.

### Ceiling Fan Blowback
- **Concept**: Large fans on the ceiling that affect projectile trajectory (wind effect).
- **Implementation**: Add a `windVector` to `Projectile` movement logic.
- **Difficulty**: Medium. Physics-based projectiles.
- **Risks**: Satay or Chicken Rice shots might miss targets they should have hit.

### Wet Floor Sign
- **Concept**: A placeable item (not a stall) that creates a permanent slow zone but can be destroyed by enemies.
- **Implementation**: New entity type: `EnvironmentObject`.
- **Difficulty**: Medium.
- **Risks**: Pathfinding needs to treat them as obstacles or ignore them.

### Day/Night Cycle (Supper Club)
- **Concept**: Night mode reduces stall range but increases gold earned (supper prices!).
- **Implementation**: Visual overlay and global stat modifiers.
- **Difficulty**: Easy.
- **Risks**: Harder to see the sprites on dark backgrounds.

### Multi-Level Hawker Center
- **Concept**: Teleporters or stairs that move enemies between two different grid maps.
- **Implementation**: `HexTile` type `STAIRS` that links to another `AxialCoordinate`.
- **Difficulty**: Hard. Pathfinding across disconnected graphs.
- **Risks**: Confusing for the player to track enemies across screens.

### Tray Return Station
- **Concept**: If enemies pass near this, they speed up (done eating!).
- **Implementation**: A static map feature that applies a speed buff.
- **Difficulty**: Easy.
- **Risks**: Encourages building away from the goal, which is counter-intuitive.

### The "Drain" Hazard
- **Concept**: Holes in the floor. If an enemy is pushed into one (by a Putu Piring pulse), they die instantly.
- **Implementation**: `TileType.DRAIN` with a collision check.
- **Difficulty**: Easy.
- **Risks**: Makes "push" mechanics too overpowered.

### Dynamic Construction
- **Concept**: Every 5 waves, a "Pillar" is built or removed, changing the path.
- **Implementation**: Modify `hexes` map and call `recalculateEnemyPaths`.
- **Difficulty**: Medium. Already partially implemented in stall placement logic.
- **Risks**: Could accidentally block the path entirely if not careful.

### Festive Decorations (CNY/Deepavali/Hari Raya)
- **Concept**: Seasonal map skins that change floor tiles and add decorative pillars.
- **Implementation**: Asset swapping based on system date.
- **Difficulty**: Easy.
- **Risks**: Bloats the APK size with extra assets.

### Air-Con Zone vs Non Air-Con
- **Concept**: Air-con tiles increase stall fire rate (workers are cool!) but stalls cost 2x more to place.
- **Implementation**: `TileType.AIR_CON` with cost and stat modifiers.
- **Difficulty**: Easy.
- **Risks**: Players will only build in the "optimal" zone.

### Moving Walkway (Like Changi, but Hawker)
- **Concept**: Specific tiles that move enemies in a fixed direction regardless of their path.
- **Implementation**: Add a `conveyorVector` to the `Enemy` movement calculation.
- **Difficulty**: Medium.
- **Risks**: Can break A* logic if the conveyor moves against the path.

### Bird Droppings
- **Concept**: Randomly disables a tile for 10 seconds.
- **Implementation**: Visual effect + `isDisabled` flag on a tile.
- **Difficulty**: Easy.
- **Risks**: Purely annoying RNG.

---

## Graphics & Sound

### 8-Bit "Chiptune" Remix
- **Concept**: A retro sound mode that changes the music to a lo-fi version of Singaporean folk songs.
- **Implementation**: Add an audio engine and toggle in options.
- **Difficulty**: Easy (if assets exist).
- **Risks**: Audio syncing issues on Android.

### Particle Effects for "Wok Hei"
- **Concept**: Adding smoke and spark particles when a stall fires to simulate high-heat cooking.
- **Implementation**: A simple particle system in the `Canvas` draw loop.
- **Difficulty**: Medium. Performance is the main concern.
- **Risks**: Slowing down the frame rate on mid-range devices.

### Dynamic Background Chatter
- **Concept**: Ambient sound that gets louder and more crowded as the wave size increases.
- **Implementation**: Layered audio files with volume tied to `enemies.size`.
- **Difficulty**: Easy.
- **Risks**: Can become "noise" rather than "ambiance."

### Stall "Level Up" Animations
- **Concept**: Stalls physically grow or get more "bling" (gold plates) as they are upgraded.
- **Implementation**: Multiple sprite variants for each `StallType`.
- **Difficulty**: Easy (Art-heavy).
- **Risks**: Needs many more assets.

### Screen Shake on Boss Spawn
- **Concept**: The whole board shakes when a Merlion or large enemy appears.
- **Implementation**: Offset the `Canvas` draw calls by a random small amount for a few frames.
- **Difficulty**: Easy.
- **Risks**: Motion sickness for some players.

### Customer Emotes
- **Concept**: Enemies show a "sweat" drop when low health or a "heart" when healed.
- **Implementation**: Small overlay icons above the enemy sprite.
- **Difficulty**: Easy.
- **Risks**: Visual clutter.

### Damage Numbers (Pop-ups)
- **Concept**: Numbers flying off enemies when they get hit.
- **Implementation**: A list of `TransientText` objects updated in the game loop.
- **Difficulty**: Medium. Needs a separate animation system.
- **Risks**: Can obscure the actual gameplay.

### Food "Glow"
- **Concept**: Projectiles glow based on their element (Red for spicy, Blue for cold).
- **Implementation**: `Paint` with `ShadowLayer` or `BlurMaskFilter` in the `Canvas`.
- **Difficulty**: Medium. `Canvas` performance.
- **Risks**: Looks "cheap" if not done with good art.

### Custom UI Themes
- **Concept**: Change the UI from "Modern App" to "Old School Chalkboard" style.
- **Implementation**: Jetpack Compose theme switching.
- **Difficulty**: Medium.
- **Risks**: Maintaining two sets of UI code.

### Hand-Drawn Sprite Mode
- **Concept**: Replace pixel/clean art with messy, hand-drawn "napkin" sketches.
- **Implementation**: Asset swap.
- **Difficulty**: Easy.
- **Risks**: Style might not fit the "Rush" vibe.

---

## Meta & Economy

### Michelin Guide Progression
- **Concept**: Unlock new stalls and upgrades by earning "stars" from a separate objective system.
- **Implementation**: A `StarRepository` and a "Missions" UI.
- **Difficulty**: Medium.
- **Risks**: Adds "grind" to the game.

### Daily Special Stall
- **Concept**: Every day, one random stall type has +50% damage.
- **Implementation**: Seeded random based on `LocalDate`.
- **Difficulty**: Easy.
- **Risks**: None.

### Hawker Center Customization
- **Concept**: Spend gold earned across games to buy permanent cosmetic upgrades for your center.
- **Implementation**: Meta-currency system.
- **Difficulty**: Medium.
- **Risks**: Needs a "Shop" UI.

### Skill Tree (The "Recipe Book")
- **Concept**: Permanent upgrades like "Cheaper Satay" or "Faster Teh Tarik."
- **Implementation**: A persistent `PlayerStats` model.
- **Difficulty**: Medium.
- **Risks**: Power creep makes early levels too easy.

### Global Leaderboards
- **Concept**: Compare your highest wave with other players in Singapore.
- **Implementation**: Firebase or a simple custom backend.
- **Difficulty**: Hard. Needs server-side validation to prevent cheating.
- **Risks**: Cheaters ruining the fun.

### Achievements (The "Foodie Badges")
- **Concept**: "Win a wave using only Teh Tarik" or "Kill 1000 Salarymen."
- **Implementation**: Achievement tracker in `MainViewModel`.
- **Difficulty**: Easy.
- **Risks**: None.

### GST Hike Event
- **Concept**: Every 20 waves, all costs increase by 9%, but rewards also increase.
- **Implementation**: A `globalInflation` multiplier.
- **Difficulty**: Easy.
- **Risks**: Can make the math "ugly" (non-integer costs).

### Bulk Orders
- **Concept**: Bonus gold if you kill 10 enemies within 2 seconds.
- **Implementation**: A "Combo" timer.
- **Difficulty**: Easy.
- **Risks**: None.

### Investor Funding
- **Concept**: Watch an ad (or just a button click in vibe mode) to get a 500 gold injection.
- **Implementation**: Simple button + state update.
- **Difficulty**: Easy.
- **Risks**: Breaks game balance.

### Seasonal Battle Pass (The "Seasoning Pass")
- **Concept**: Earn XP to unlock Singapore-themed skins (e.g., National Day skin).
- **Implementation**: XP system and reward track.
- **Difficulty**: Hard. Requires a lot of content.
- **Risks**: Players hate battle passes.

### Recycling Gold
- **Concept**: Selling a stall gives 100% refund if it hasn't fired yet.
- **Implementation**: Track `hasFired` on `Stall`.
- **Difficulty**: Easy.
- **Risks**: Allows "accidental placement" fixes.

### Loan Shark (Oolong)
- **Concept**: Borrow gold now, but pay back 2x later or your stalls get "vandalized" (disabled).
- **Implementation**: A "Debt" state that triggers a negative event.
- **Difficulty**: Medium.
- **Risks**: Might be too dark for a food game.

### Franchise Mode
- **Concept**: Manage 3 hawker centers at once, jumping between maps.
- **Implementation**: Multiple `GameState` objects in memory.
- **Difficulty**: Hard. UI/UX nightmare.
- **Risks**: Too complex for a casual game.

### Halal Certification
- **Concept**: Buffs all stalls but removes the (theoretical) "Pork" stalls.
- **Implementation**: Modifiers based on "Cert" status.
- **Difficulty**: Easy.
- **Risks**: Needs to be handled with cultural sensitivity.

### Food Waste Penalty
- **Concept**: If an enemy reaches the goal with 90% health, you lose more "reputation" (extra life loss).
- **Implementation**: Multiplier on health loss based on enemy remaining HP.
- **Difficulty**: Easy.
- **Risks**: Makes "leaking" enemies even more punishing.

---

## Architectural

### Entity Component System (ECS)
- **Concept**: Refactor the game loop to use ECS for better performance and easier mixing of behaviors.
- **Implementation**: Move logic from `MainViewModel` to dedicated `Systems` (MovementSystem, CombatSystem).
- **Difficulty**: Hard. Complete rewrite of the core engine.
- **Risks**: High chance of introducing regressions.

### Modding Support via JSON
- **Concept**: Allow new stalls and enemies to be defined in external JSON files.
- **Implementation**: Use GSON to load data into the `availableStalls` and `enemyTiers` lists.
- **Difficulty**: Medium. Moves hardcoded logic to data-driven.
- **Risks**: Need to validate JSON to prevent crashes.

### Multi-Threaded Collision Detection
- **Concept**: Run projectile/enemy collision checks on a separate background thread.
- **Implementation**: Use Kotlin Coroutines with a dedicated thread pool.
- **Difficulty**: Hard. Synchronizing state between threads without `ConcurrentModificationException` is tough.
- **Risks**: Race conditions.

### Replay System
- **Concept**: Record every action (stall placement, wave start) and allow the player to watch it back.
- **Implementation**: Store a list of `GameAction` objects with timestamps.
- **Difficulty**: Hard. Requires deterministic game logic.
- **Risks**: "Desync" where the replay doesn't match what actually happened.

### Asset Bundling / Hot Reload
- **Concept**: Update sprites and balance numbers without a full APK reinstall.
- **Implementation**: Download assets from a remote server on startup.
- **Difficulty**: Medium.
- **Risks**: Security and data usage.

### Level Editor
- **Concept**: An in-game tool to create custom hawker center layouts.
- **Implementation**: A new `AppScreen` where you can paint `TileType` on a grid.
- **Difficulty**: Hard. Requires a lot of UI work.
- **Risks**: Saving/Loading custom maps.

### Haptic Feedback Engine
- **Concept**: Advanced vibrations that feel like "sizzling" or "chopping."
- **Implementation**: Use the Android `Vibrator` API with custom waveforms.
- **Difficulty**: Medium.
- **Risks**: Drains battery.

### Unit Testing for Game Logic
- **Concept**: Automated tests to ensure "Wave 10 is always winnable with 3 towers."
- **Implementation**: Headless game loop simulation in JUnit.
- **Difficulty**: Medium.
- **Risks**: Hard to simulate "player skill."

### Shader-Based Rendering
- **Concept**: Move some visual effects (like puddles or heat haze) to AGSL (Android Graphics Shading Language).
- **Implementation**: Custom `Modifier.drawWithCache` with shaders.
- **Difficulty**: Hard. Requires high-end Android versions and GPU knowledge.
- **Risks**: Incompatibility with older devices.

### Save-State Serialization
- **Concept**: Allow "Undo" by keeping the last 3 game states in a stack.
- **Implementation**: Deep-copy the `GameState` every 5 seconds.
- **Difficulty**: Medium. Performance/Memory impact.
- **Risks**: Memory leaks if the stack isn't managed.

---

## Wacky Ideas

### VR Hawker Mode
- **Concept**: A 1st-person mini-game where you manually throw satay at customers.
- **Implementation**: New 3D renderer or a very clever 2D perspective shift.
- **Difficulty**: Hard. Genre shift.
- **Risks**: Doesn't fit the rest of the app.

### Crypto-Gold
- **Concept**: The value of your gold changes every minute based on a "Market" (random or real-world BTC price).
- **Implementation**: An external API call + price multiplier.
- **Difficulty**: Medium.
- **Risks**: Frustrating if the "market" crashes when you need to buy a Durian stall.

### Kaiju Merlion Attack
- **Concept**: A 1-in-1000 chance that a giant Merlion stomps through the middle of the map, destroying everything.
- **Implementation**: A "Disaster" event.
- **Difficulty**: Medium.
- **Risks**: Players might hate losing their progress to a random event.

### 1960s Street Hawker Mode
- **Concept**: A "Black and White" mode where you play as an illegal street hawker running from the "Mata" (police).
- **Implementation**: Visual filter + new enemy type (Police).
- **Difficulty**: Easy.
- **Risks**: None.

### Voice Command Controls
- **Concept**: Shout "ONE TEH TARIK" to place a stall.
- **Implementation**: Android Speech-to-Text API.
- **Difficulty**: Medium.
- **Risks**: Accidental placements from background noise.

### GPS-Based Buffs
- **Concept**: If you are physically at a real-world Hawker Center, you get a 2x gold buff.
- **Implementation**: Location services + a database of SG hawker centers.
- **Difficulty**: Medium.
- **Risks**: Privacy concerns.

### Stall Romance Sim
- **Concept**: Stalls that are placed next to each other for a long time "fall in love" and get a permanent buff.
- **Implementation**: Track "ProximityTime" in the `Stall` model.
- **Difficulty**: Easy.
- **Risks**: Weird tonal shift.

### Durian Bomb (The "Nuke")
- **Concept**: A button that costs 5000 gold and instantly clears the entire screen with a massive green explosion.
- **Implementation**: Global `enemies.clear()` + visual effect.
- **Difficulty**: Easy.
- **Risks**: Trivializes the game.

### Customer Conversation Logs
- **Concept**: A small text box showing what the customers are saying ("The Laksa is too spicy!", "Where is my tissue?").
- **Implementation**: Random string selection based on enemy state.
- **Difficulty**: Easy.
- **Risks**: Distracting.

### Ghost Map
- **Concept**: You play on a map that is perfectly normal, but the path changes randomly because "Ghosts are moving the tables."
- **Implementation**: Randomly swap `FLOOR` and `PILLAR` tiles.
- **Difficulty**: Medium.
- **Risks**: Might create unsolvable maps.
