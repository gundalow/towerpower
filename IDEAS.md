# Hawker Rush: 100 Ideas for Extension

This document outlines 100 ideas to extend Hawker Rush. All ideas follow the **Singapore Hawker** theme.

## Structure
Each idea includes:
- **Concept**: Description of the idea.
- **Vibe Coding Suitability**: High/Medium/Low. How effectively can an LLM/Agent implement this with minimal guidance?
- **Vibe Implementation**: How we'd "vibe code" this (e.g., "Ask Jules to add a new enum and update the fire loop").
- **Asset Requirement**: Code only vs New Sprites/Sound.
- **Difficulty**: Easy/Medium/Hard + Reason.
- **Risks**: Potential issues or regressions.

---

## Hawker Stalls (Towers)

### Laksa Splash
- **Concept**: A stall that shoots spicy coconut gravy, dealing splash damage in a small radius around the target.
- **Vibe Coding Suitability**: High. Uses existing AOE logic from Durian.
- **Vibe Implementation**: Ask the agent to clone the Durian logic but reduce the radius and increase the fire rate.
- **Asset Requirement**: New "Laksa Bowl" sprite and orange gravy projectile.
- **Difficulty**: Easy.
- **Risks**: Overlapping AOE circles can cause visual lag.

### Bak Kut Teh Steam
- **Concept**: A support stall that emits "herbal steam," buffing the attack speed of all adjacent stalls.
- **Vibe Coding Suitability**: Medium. Requires a new "buff" collection logic.
- **Vibe Implementation**: "Hey Jules, make a stall that looks for nearby hexes and decreases their fireRateMs by 20% while active."
- **Asset Requirement**: Code only (uses existing steam/smoke particles).
- **Difficulty**: Medium. Requires a clean way to apply/remove buffs without permanent stat corruption.
- **Risks**: Stacking buffs from multiple stalls could lead to infinite fire rates.

### Chilli Crab Claw
- **Concept**: A high-damage, single-target stall that "pinches" enemies, slowing them significantly for a short duration.
- **Vibe Coding Suitability**: High. Combination of existing damage and slow logic.
- **Vibe Implementation**: "Add a Chilli Crab stall that uses SATAY damage but adds a 50% slow effect on hit."
- **Asset Requirement**: New sprite.
- **Difficulty**: Easy.
- **Risks**: Slowing bosses too much makes the game trivial.

### Kaya Toast Launcher
- **Concept**: Rapid-fire stall that shoots slices of crispy toast. Low damage but extremely high fire rate.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Create a stall with 100ms fire rate and 2 damage. Call it Kaya Toast."
- **Asset Requirement**: Toast projectile sprite.
- **Difficulty**: Easy.
- **Risks**: Projectile count could exceed 100+ quickly.

### Rojak Mixer
- **Concept**: Every shot has a random effect: extra damage, slow, or a small gold bonus.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "In the projectile hit logic, add a `when(Random.nextInt(3))` block for Rojak stalls."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium. RNG needs to be seeded correctly to feel fair.
- **Risks**: Getting a "Gold" roll on a boss instead of "Damage" might lose the game.

### Oyster Omelette Crit
- **Concept**: Low base damage but has a 20% chance to deal 10x damage (a "Big Oyster" shot).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Add a crit chance check to the damage calculation for this stall type."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Spiky damage makes wave outcome unpredictable.

### Nasi Lemak Ikan Bilis
- **Concept**: Shoots a swarm of small ikan bilis (anchovies) that track different targets.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Modify the fire method to spawn 5 projectiles instead of 1, each with a random nearby target."
- **Asset Requirement**: Small fish sprite.
- **Difficulty**: Medium. Targeting logic needs to be robust.
- **Risks**: Swarm AI is more expensive to calculate.

### Otah Grill
- **Concept**: Deals damage in a straight line, piercing through all enemies in its path.
- **Vibe Coding Suitability**: Medium. Requires line-segment collision.
- **Vibe Implementation**: "Instead of a point check, check all enemies within a width of 0.2 along the projectile's forward vector."
- **Asset Requirement**: Leaf-wrapped projectile.
- **Difficulty**: Medium.
- **Risks**: Pierce can be overpowered on straight-line paths.

### Bandung Mist
- **Concept**: A pink mist stall that confuses enemies, causing them to walk backwards for 1 second.
- **Vibe Coding Suitability**: Low. Reversing pathfinding state is complex.
- **Vibe Implementation**: "If hit by Bandung, decrement the enemy's path index instead of incrementing it for 1000ms."
- **Asset Requirement**: Pink cloud particles.
- **Difficulty**: Hard. Requires careful state management in the movement loop.
- **Risks**: Infinite loops if enemies walk back into the start.

### Sugar Cane Juicer
- **Concept**: A "battery" stall. It doesn't attack but increases the gold earned from enemies killed within its range.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add a check in the enemy death logic: if a Sugar Cane stall is in range, reward += 10."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: Economy snowballing.

### Satay Bee Hoon Slow
- **Concept**: Covers a large area in thick peanut sauce, slowing all enemies significantly more than Teh Tarik.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Clone Teh Tarik but change the color to brown and the slow multiplier to 0.3."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Visual confusion with Satay stall projectiles.

### Fried Carrot Cake (Black vs White)
- **Concept**: A stall with a toggle. "Black" mode deals AOE fire damage, "White" mode deals high single-target physical damage.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add a button to the Stall UI that toggles a boolean. Use that boolean in the fire loop."
- **Asset Requirement**: Two sprite variants (or just a tint).
- **Difficulty**: Medium.
- **Risks**: UI bloat.

### Popiah Wrap
- **Concept**: Traps an enemy in a "wrap" for 3 seconds, stopping them and preventing them from being hit by other towers.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Apply a freeze but also set `isTargetable = false` for the duration."
- **Asset Requirement**: Wrap overlay for the enemy.
- **Difficulty**: Medium.
- **Risks**: Can prevent players from killing a dangerous enemy.

### Char Kway Teow Char
- **Concept**: Adds a "burn" effect that deals damage over time (DOT).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add a list of active DOTs to the Enemy model. Tick them in the ViewModel."
- **Asset Requirement**: Fire/smoke particles.
- **Difficulty**: Medium. Requires a list of active effects.
- **Risks**: DOT calculations for many enemies.

### Hainanese Chicken Rice (Elite)
- **Concept**: A more expensive version of the base stall that spawns a "Waiter" unit to block the path.
- **Vibe Coding Suitability**: Low. Spawning blockers on the path is dangerous for AI.
- **Vibe Implementation**: "Spawn a temporary Pillar on the floor tile when a special ability is used."
- **Asset Requirement**: Waiter sprite.
- **Difficulty**: Hard.
- **Risks**: Blocking the path completely breaks the game.

### Durian King
- **Concept**: Massive AOE that also leaves a "stink" cloud (Gas Cloud) that lingers.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "After the Durian projectile hits, spawn a long-lived VisualEffect that also applies damage."
- **Asset Requirement**: Large green cloud.
- **Difficulty**: Medium.
- **Risks**: Visual clutter.

### Ice Kachang Brain Freeze
- **Concept**: Shards of ice that shatter on impact, freezing the target and chilling nearby enemies (slowing them).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "In the hit logic, apply freeze to the target and a slow debuff to all enemies in range 1.0."
- **Asset Requirement**: Shattering ice effect.
- **Difficulty**: Medium.
- **Risks**: Overlapping CC (Crowd Control) is hard to balance.

### Putu Piring Pulse
- **Concept**: Releases a radial pulse of steam every 5 seconds, pushing enemies back slightly.
- **Vibe Coding Suitability**: Low. Direct position manipulation messes with pathing.
- **Vibe Implementation**: "Every 5 seconds, iterate through enemies in range and move their PreciseAxialCoordinate away from the center."
- **Asset Requirement**: Steam pulse effect.
- **Difficulty**: Hard.
- **Risks**: Pushing enemies into walls or off-grid.

### Murtabak Shield
- **Concept**: A stall that can be placed on the path to block enemies, but has its own HP and can be "eaten" (destroyed).
- **Vibe Coding Suitability**: Low. Requires major changes to enemy AI (adding an attack state).
- **Vibe Implementation**: "Allow stalls on path. If enemy hits stall, stop enemy and reduce stall health."
- **Asset Requirement**: Code only.
- **Difficulty**: Hard.
- **Risks**: Complete genre shift from TD to Lane Defense.

### Teh C Special
- **Concept**: Three layers of effects: First hit slows, second hit burns, third hit deals massive damage.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add a `Map<String, Int>` to the enemy to track hits from specific towers."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: Hard to communicate "hit count" to player.

---

## Customers (Enemies)

### Hungry Ghost
- **Concept**: Transparent and invisible to most stalls unless they are within a "light" stall's range.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add `isVisible` flag. Stalls skip if false. A 'Lantern' stall sets it to true in range."
- **Asset Requirement**: Semi-transparent sprite.
- **Difficulty**: Medium.
- **Risks**: Frustrating if player lacks detectors.

### Merlion Boss
- **Concept**: A giant boss with high HP that periodically sprays water, clearing all `puddles` in an area.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Every 3 seconds, the Merlion calls `gameState.puddles.clear()` for nearby objects."
- **Asset Requirement**: Large Merlion sprite.
- **Difficulty**: Medium.
- **Risks**: Makes Teh Tarik completely useless.

### Office Lady (OL)
- **Concept**: Moves fast and carries a "First Aid" kit (bubble tea), healing nearby customers.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Every tick, find enemies within 1.0 distance and add 5 HP."
- **Asset Requirement**: New sprite.
- **Difficulty**: Medium.
- **Risks**: Healing "death balls" that never die.

### Food Critic
- **Concept**: Very slow, but if they reach the goal, you lose 5 health (tables) instead of 1.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Add `healthPenalty` to Enemy. If reached goal, `lives -= enemy.healthPenalty`."
- **Asset Requirement**: Grumpy critic sprite.
- **Difficulty**: Easy.
- **Risks**: Sudden death from one leak.

### Secondary School Kids
- **Concept**: Move in a tight "canteen" pack. When one is hit, they all gain a temporary speed boost.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "On damage, send a broadcast to all enemies with the same 'GroupID' to increase speed."
- **Asset Requirement**: Small student sprites.
- **Difficulty**: Medium.
- **Risks**: Chain reactions make the whole pack move at light speed.

### Uncle with Newspaper
- **Concept**: Stops moving for 2 seconds every few steps to "read the news," blocking other customers behind him.
- **Animation Details**: Needs a walking sprite with a newspaper tucked under the arm, and a stationary sprite where he is holding the paper open in front of his face.
- **Gameplay Balance**: To prevent unfair deadlocks, the Uncle has a "clumsy" flag—if he blocks 3+ enemies for more than 4 seconds, he gets "shoved," instantly ending his reading session and moving at 1.5x speed for a short burst.
- **Vibe Coding Suitability**: Medium. Extension of Tourist logic.
- **Vibe Implementation**: "Make a variant of Tourist. When stopped, play the 'Reading' animation. Add a 'Shove' counter to prevent infinite blocking."
- **Asset Requirement**: Walking vs Reading sprites.
- **Difficulty**: Medium. Requires coordination between movement and animation state.
- **Risks**: Traffic jams can cause performance drops if many path-recalculations are triggered.

### Safe Entry Ambassador
- **Concept**: A "mid-boss" that slows down all other enemies behind him for "checks," effectively grouping them up for AOE.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Apply a trailing slow aura that only affects other enemies."
- **Asset Requirement**: High-vis vest sprite.
- **Difficulty**: Medium.
- **Risks**: Unintentionally helping the player by grouping enemies for Durian/Satay.

### PMD Rider
- **Concept**: Extremely fast and immune to puddles, but takes extra damage from "cooling" stalls (Ice Kachang).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Check for `isPuddleImmune` in movement. Add damage multiplier for Ice Kachang in hit logic."
- **Asset Requirement**: Electric scooter sprite.
- **Difficulty**: Easy.
- **Risks**: Too fast for slow-firing towers.

### Otter Family
- **Concept**: Small, very fast enemies that move in a zig-zag pattern instead of following the path perfectly.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Apply a `sin(time)` offset to the rendered X/Y position relative to the grid path."
- **Asset Requirement**: Otter sprites.
- **Difficulty**: Medium. Targeting logic might miss if it doesn't account for the offset.
- **Risks**: Hard to click.

### Durian Lover
- **Concept**: An enemy that is healed by Durian stall projectiles.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "If hit by DURIAN, `hp += damage`."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Counter-intuitive for players.

### The "Chope" Auntie
- **Concept**: Leaves a "Tissue Packet" on a random stall, disabling it for 5 seconds.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "If Auntie is near a stall, add a `DisabledEffect` to the stall's state."
- **Asset Requirement**: Tissue packet overlay.
- **Difficulty**: Hard. Enemies usually don't target towers.
- **Risks**: Disabling the only defense.

### Giant Tropical Cockroach
- **Concept**: When "killed," it flies! Becomes a fast aerial unit that ignores pathfinding (moves straight to goal).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "On death, spawn a 'Flying Roach' at current pos with a simple direct vector to the goal."
- **Asset Requirement**: Flying variant sprite.
- **Difficulty**: Medium.
- **Risks**: Aerial units need clear visual feedback that they are flying.

### GrabFood Cyclist
- **Concept**: High speed, but crashes (stops) for 3 seconds if it takes more than 50% damage in one hit.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "In damage logic, if `damage > maxHp/2`, apply 3s freeze."
- **Asset Requirement**: Crash animation/sprite.
- **Difficulty**: Easy.
- **Risks**: Encourages only high-alpha towers.

### Kiasu Parent
- **Concept**: Drags a "Student" enemy. If the parent dies, the student runs at 3x speed.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Spawning logic on death."
- **Asset Requirement**: Two sprites.
- **Difficulty**: Medium.
- **Risks**: Spawning enemies in the middle of a path can be glitchy.

### Tour Group
- **Concept**: 10+ Tourists following a "Flag Bearer." If the bearer dies, the tourists scatter (random paths).
- **Vibe Coding Suitability**: Low. Requires group behavior logic.
- **Vibe Implementation**: "Leader/Follower pattern in movement."
- **Asset Requirement**: Multiple tourist sprites.
- **Difficulty**: Hard.
- **Risks**: CPU spikes from multiple repaths.

### National Serviceman (Full Battle Order)
- **Concept**: High armor. Reduces all incoming damage by a flat amount (e.g., -5 per hit).
- **Design Decisions**: Flat reduction is chosen over percentage to create a hard counter for fast-firing, low-damage towers (like Kaya Toast). This forces the player to diversify into high-damage "tank busters" (like Durian).
- **Vibe Coding Suitability**: High. Simple math modification.
- **Vibe Implementation**: "Modify the damage calculation: `actualDamage = Math.max(1, damage - 5)`."
- **Asset Requirement**: Camouflage uniform sprite.
- **Difficulty**: Easy. Simple arithmetic change.
- **Risks**: If the flat reduction is too high, early-game towers become literally useless.

### Influencer
- **Concept**: Stops to take a "selfie," stunning all stalls in a small radius with the "flash."
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Periodic radial stun effect."
- **Asset Requirement**: Selfie stick/Flash effect.
- **Difficulty**: Medium.
- **Risks**: Annoying visual flash.

### Stray Cat
- **Concept**: Not an enemy, but walks on the path. If a stall hits it, you lose gold (fine for animal cruelty!).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Neutral target. Check target ID in hit logic; if cat, `gold -= 100`."
- **Asset Requirement**: Cat sprite.
- **Difficulty**: Easy.
- **Risks**: Players hate accidental penalties.

### Canteen Cleaner Uncle
- **Concept**: Moves around the map (not the path) and "collects" dropped gold or power-ups.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "New entity type that wanders to nearest gold drop."
- **Asset Requirement**: Cleaning cart sprite.
- **Difficulty**: Medium.
- **Risks**: More visual noise.

### Bird (Mynah)
- **Concept**: Flies over stalls and occasionally "steals" an upgrade level.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Aerial unit that interacts with `Stall.upgradeCount` on collision."
- **Asset Requirement**: Bird sprite.
- **Difficulty**: Medium.
- **Risks**: High frustration; losing hard-earned upgrades feels bad.

---

## Map & Environment

### Sudden Rainstorm
- **Concept**: Periodically, the whole map slows down, and "puddles" form randomly on the floor.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Global speed multiplier tied to a 'isRaining' flag."
- **Asset Requirement**: Rain particle overlay.
- **Difficulty**: Easy.
- **Risks**: Makes game feel sluggish.

### Lunch Hour Rush
- **Concept**: A "Fast Forward" mode that is forced upon the player for 30 seconds, with 2x spawn rate.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Temporary tick rate increase."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Loss of control.

### Table Cleaning Robot Obstacle
- **Concept**: A robot that moves along the paths, pushing enemies back if it hits them.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Friendly 'enemy' with a reverse path."
- **Asset Requirement**: Robot sprite.
- **Difficulty**: Medium.
- **Risks**: Infinite loops.

### Tissue Packet "Chope" Zones
- **Concept**: Certain hexes are blocked by tissue packets at the start of the wave. You can't place stalls there.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Randomly mark some FLOOR tiles as `isBlocked` during map gen."
- **Asset Requirement**: Tissue packet sprite.
- **Difficulty**: Easy.
- **Risks**: Frustrating map layouts.

### Ceiling Fan Blowback
- **Concept**: Large fans on the ceiling that affect projectile trajectory (wind effect).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Apply a constant vector to projectiles every frame."
- **Asset Requirement**: Ceiling fan sprite (top layer).
- **Difficulty**: Medium.
- **Risks**: Projectiles missing when they shouldn't.

### Wet Floor Sign
- **Concept**: A placeable item (not a stall) that creates a permanent slow zone but can be destroyed by enemies.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "New object type with HP and a slow aura."
- **Asset Requirement**: Wet floor sign sprite.
- **Difficulty**: Medium.
- **Risks**: Pathing complexity.

### Day/Night Cycle (Supper Club)
- **Concept**: Night mode reduces stall range but increases gold earned (supper prices!).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Visual overlay + global range modifier."
- **Asset Requirement**: Night tint shader/filter.
- **Difficulty**: Easy.
- **Risks**: Hard to see.

### Multi-Level Hawker Center
- **Concept**: Teleporters or stairs that move enemies between two different grid maps.
- **Vibe Coding Suitability**: Low. Disconnected paths are hard for A*.
- **Vibe Implementation**: "Link two hexes so distance is treated as 1."
- **Asset Requirement**: Stairs sprite.
- **Difficulty**: Hard.
- **Risks**: AI pathfinding breakages.

### Tray Return Station
- **Concept**: If enemies pass near this, they speed up (done eating!).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Tile-based speed buff."
- **Asset Requirement**: Tray return rack sprite.
- **Difficulty**: Easy.
- **Risks**: Incentive to build away from goal.

### The "Drain" Hazard
- **Concept**: Holes in the floor. If an enemy is pushed into one (by a Putu Piring pulse), they die instantly.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "TileType.DRAIN. In movement, if on Drain, `isDead = true`."
- **Asset Requirement**: Drain tile sprite.
- **Difficulty**: Easy.
- **Risks**: OP with push mechanics.

### Dynamic Construction
- **Concept**: Every 5 waves, a "Pillar" is built or removed, changing the path.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Update map state and trigger re-pathing for all enemies."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium. Already partially implemented.
- **Risks**: Blocking the goal.

### Festive Decorations (CNY/Deepavali/Hari Raya)
- **Concept**: Seasonal map skins that change floor tiles and add decorative pillars.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Asset swap based on system clock."
- **Asset Requirement**: Multiple tile sets.
- **Difficulty**: Easy.
- **Risks**: App size.

### Air-Con Zone vs Non Air-Con
- **Concept**: Air-con tiles increase stall fire rate (workers are cool!) but stalls cost 2x more to place.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Fire rate multiplier on specific tiles."
- **Asset Requirement**: Frosty tile tint.
- **Difficulty**: Easy.
- **Risks**: Optimal building zones.

### Moving Walkway (Like Changi, but Hawker)
- **Concept**: Specific tiles that move enemies in a fixed direction regardless of their path.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Add a force vector to enemy position on specific hexes."
- **Asset Requirement**: Conveyor tile sprite.
- **Difficulty**: Medium.
- **Risks**: Fighting the A* pathing.

### Bird Droppings
- **Concept**: Randomly disables a tile for 10 seconds.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Visual effect + temporary flag."
- **Asset Requirement**: Dropping sprite.
- **Difficulty**: Easy.
- **Risks**: Pure annoyance.

---

## Graphics & Sound

### 8-Bit "Chiptune" Remix
- **Concept**: A retro sound mode that changes the music to a lo-fi version of Singaporean folk songs.
- **Vibe Coding Suitability**: High (if assets exist).
- **Vibe Implementation**: "Toggle audio track."
- **Asset Requirement**: New music.
- **Difficulty**: Easy.
- **Risks**: Syncing.

### Particle Effects for "Wok Hei"
- **Concept**: Adding smoke and spark particles when a stall fires to simulate high-heat cooking.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Spawn particles at stall coordinate on fire."
- **Asset Requirement**: Smoke particles.
- **Difficulty**: Medium.
- **Risks**: Performance.

### Dynamic Background Chatter
- **Concept**: Ambient sound that gets louder and more crowded as the wave size increases.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Adjust audio volume based on `enemies.size`."
- **Asset Requirement**: Crowd audio loops.
- **Difficulty**: Easy.
- **Risks**: Noise fatigue.

### Stall "Level Up" Animations
- **Concept**: Stalls physically grow or get more "bling" (gold plates) as they are upgraded.
- **Vibe Coding Suitability**: High (Art-heavy).
- **Vibe Implementation**: "Select sprite index based on `upgradeCount`."
- **Asset Requirement**: Multiple stall sprites.
- **Difficulty**: Easy.
- **Risks**: Asset bloat.

### Screen Shake on Boss Spawn
- **Concept**: The whole board shakes when a Merlion or large enemy appears.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Add a temporary random offset to the Canvas `drawTransform`."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Motion sickness.

### Customer Emotes
- **Concept**: Enemies show a "sweat" drop when low health or a "heart" when healed.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Render small icons at `enemy.position + offset`."
- **Asset Requirement**: Icon sprites.
- **Difficulty**: Easy.
- **Risks**: Clutter.

### Damage Numbers (Pop-ups)
- **Concept**: Numbers flying off enemies when they get hit.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Manage a list of short-lived text entities in the UI."
- **Asset Requirement**: Font/Text rendering.
- **Difficulty**: Medium.
- **Risks**: Overlapping text.

### Food "Glow"
- **Concept**: Projectiles glow based on their element (Red for spicy, Blue for cold).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Apply a `BlurMaskFilter` to the projectile paint."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: GPU lag.

### Custom UI Themes
- **Concept**: Change the UI from "Modern App" to "Old School Chalkboard" style.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Switch Compose Material colors and fonts."
- **Asset Requirement**: Full UI reskin.
- **Difficulty**: Medium.
- **Risks**: Maintenance.

### Hand-Drawn Sprite Mode
- **Concept**: Replace pixel/clean art with messy, hand-drawn "napkin" sketches.
- **Vibe Coding Suitability**: High (Art-heavy).
- **Vibe Implementation**: "Asset swap."
- **Asset Requirement**: Full sprite set reskin.
- **Difficulty**: Easy.
- **Risks**: Style clash.

---

## Meta & Economy

### Michelin Guide Progression
- **Concept**: Unlock new stalls and upgrades by earning "stars" from a separate objective system.
- **Purpose**: This system achieves long-term retention by giving players goals beyond just "survival." It is useful for gated complexity, ensuring new players aren't overwhelmed by 20+ stall types at once.
- **Vibe Coding Suitability**: Medium. Requires persistent storage and mission checks.
- **Vibe Implementation**: "Add a MissionRepository. Check for mission completion (e.g., 'Kill 50 Bosses') after each wave."
- **Asset Requirement**: Star/Badge icons.
- **Difficulty**: Medium. Requires a new UI screen and save data.
- **Risks**: Making the game too "grindy."

### Daily Special Stall
- **Concept**: Every day, one random stall type has +50% damage.
- **Definition of "Day"**: Defined as the 24-hour period (UTC) to ensure consistency across all players, or local device time for simplicity in "vibe" mode.
- **Balance Details**: To ensure fairness, the "Special" buff is capped at 50% damage and does not apply to AOE radius or CC effects. This prevents stalls like Durian from becoming "map-clearers" with one hit.
- **Design/Impl Questions**: Should we use a server? No, seeded random based on `currentDate.toEpochDay()` allows offline play with the same "Daily" for everyone.
- **Vibe Coding Suitability**: High. Seeded random logic is easy to prompt.
- **Vibe Implementation**: "In MainViewModel, set `dailyBonusStall = allStalls[Random(dateSeed).nextInt()]`."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: A "Durian Day" might make the leaderboard trivial for that day.

### Hawker Center Customization
- **Concept**: Spend gold earned across games to buy permanent cosmetic upgrades for your center.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Persistent meta-currency and a shop UI."
- **Asset Requirement**: Cosmetic assets.
- **Difficulty**: Medium.
- **Risks**: Inflation.

### Skill Tree (The "Recipe Book")
- **Concept**: Permanent upgrades like "Cheaper Satay" or "Faster Teh Tarik."
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Global multiplier object applied to all stall stats."
- **Asset Requirement**: Tree UI.
- **Difficulty**: Medium.
- **Risks**: Power creep.

### Global Leaderboards
- **Concept**: Compare your highest wave with other players in Singapore.
- **Vibe Coding Suitability**: Low. Requires backend integration.
- **Vibe Implementation**: "POST score to Firebase on GameOver."
- **Asset Requirement**: Backend setup.
- **Difficulty**: Hard.
- **Risks**: Cheating.

### Achievements (The "Foodie Badges")
- **Concept**: "Win a wave using only Teh Tarik" or "Kill 1000 Salarymen."
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Check conditions in the ViewModel and show a toast."
- **Asset Requirement**: Badge icons.
- **Difficulty**: Easy.
- **Risks**: None.

### GST Hike Event
- **Concept**: Every 20 waves, all costs increase by 9%, but rewards also increase.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Apply `1.09^waves/20` to all cost calculations."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Ugly decimals.

### Bulk Orders
- **Concept**: Bonus gold if you kill 10 enemies within 2 seconds.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Combo counter that resets after 2s."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Snowballing.

### Investor Funding
- **Concept**: Watch an ad (or just a button click in vibe mode) to get a 500 gold injection.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Button that adds 500 to gold."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Balance.

### Seasonal Battle Pass (The "Seasoning Pass")
- **Concept**: Earn XP to unlock Singapore-themed skins (e.g., National Day skin).
- **Vibe Coding Suitability**: Low. Content heavy.
- **Vibe Implementation**: "XP track in save data."
- **Asset Requirement**: Massive amounts of content.
- **Difficulty**: Hard.
- **Risks**: Player backlash.

### Recycling Gold
- **Concept**: Selling a stall gives 100% refund if it hasn't fired yet.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Flag on Stall, checked in sellStall()."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Placement exploits.

### Loan Shark (Oolong)
- **Concept**: Borrow gold now, but pay back 2x later or your stalls get "vandalized" (disabled).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Loan button + debt counter + periodic vandal event."
- **Asset Requirement**: Graffiti overlay for stalls.
- **Difficulty**: Medium.
- **Risks**: Dark tone.

### Franchise Mode
- **Concept**: Manage 3 hawker centers at once, jumping between maps.
- **Vibe Coding Suitability**: Low. Major architecture shift.
- **Vibe Implementation**: "Array of GameStates."
- **Asset Requirement**: Multi-map UI.
- **Difficulty**: Hard.
- **Risks**: Confusion.

### Halal Certification
- **Concept**: Buffs all stalls but removes the (theoretical) "Pork" stalls.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Stat modifier based on boolean flag."
- **Asset Requirement**: Logo icon.
- **Difficulty**: Easy.
- **Risks**: Sensitivity.

### Food Waste Penalty
- **Concept**: If an enemy reaches the goal with 90% health, you lose more "reputation" (extra life loss).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Scale lives loss based on `hp/maxHp`."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: High punishment.

---

## Architectural

### Entity Component System (ECS)
- **Concept**: Refactor the game loop to use ECS for better performance and easier mixing of behaviors.
- **Vibe Coding Suitability**: Low. LLMs struggle with massive architectural refactors across many files.
- **Vibe Implementation**: "Define Component and System interfaces. Move movement to MovementSystem."
- **Asset Requirement**: Code only.
- **Difficulty**: Hard.
- **Risks**: Massive regressions.

### Modding Support via JSON
- **Concept**: Allow new stalls and enemies to be defined in external JSON files.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Use GSON to load assets from the `assets/` folder instead of hardcoding objects."
- **Asset Requirement**: JSON files.
- **Difficulty**: Medium.
- **Risks**: Parsing errors.

### Multi-Threaded Collision Detection
- **Concept**: Run projectile/enemy collision checks on a separate background thread.
- **Vibe Coding Suitability**: Low. Concurrency is hard to prompt safely.
- **Vibe Implementation**: "Move the collision loop to `Dispatchers.Default`."
- **Asset Requirement**: Code only.
- **Difficulty**: Hard.
- **Risks**: Race conditions.

### Replay System
- **Concept**: Record every action (stall placement, wave start) and allow the player to watch it back.
- **Vibe Coding Suitability**: Low. Requires perfect determinism.
- **Vibe Implementation**: "Store list of (Time, Action) and play back on a fresh state."
- **Asset Requirement**: Code only.
- **Difficulty**: Hard.
- **Risks**: Replay desync.

### Level Editor
- **Concept**: An in-game tool to create custom hawker center layouts.
- **Vibe Coding Suitability**: Low. Complex UI and state logic.
- **Vibe Implementation**: "Tile-painting mode that saves to local storage."
- **Asset Requirement**: Editor UI icons.
- **Difficulty**: Hard.
- **Risks**: Unwinnable maps.

### Haptic Feedback Engine
- **Concept**: Advanced vibrations that feel like "sizzling" or "chopping."
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Custom `VibrationEffect` patterns for different stall types."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: Battery drain.

### Unit Testing for Game Logic
- **Concept**: Automated tests to ensure "Wave 10 is always winnable with 3 towers."
- **Vibe Coding Suitability**: Medium. LLMs are great at writing tests.
- **Vibe Implementation**: "Create a headless SimulationViewModel and run 1000 trials."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: Slow build times.

### Shader-Based Rendering
- **Concept**: Move visual effects (like heat haze or puddles) to AGSL (Android Graphics Shading Language).
- **Pixel 6A Compatibility**: This will work perfectly on a Pixel 6A. AGSL was introduced in Android 13, and the Tensor chip in the 6A handles fragment shaders very efficiently.
- **Vibe Coding Suitability**: Low. Shaders are hard to write via text prompts without visual feedback.
- **Vibe Implementation**: "Pass an AGSL string to `RuntimeShader` and apply it to a Modifier."
- **Asset Requirement**: Code only.
- **Difficulty**: Hard. Requires deep graphics knowledge.
- **Risks**: Incompatibility with very old devices (pre-Android 13).

### Save-State Serialization
- **Concept**: Allow "Undo" by keeping the last 3 game states in a stack.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Copy the GameState object into a circular buffer every action."
- **Asset Requirement**: Undo button icon.
- **Difficulty**: Medium.
- **Risks**: Memory usage.

### Multi-Language Support (Singlish vs English)
- **Concept**: Toggle between standard English and full Singlish dialogue.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Localize strings and add a toggle in Settings."
- **Asset Requirement**: String files.
- **Difficulty**: Easy.
- **Risks**: None.

---

## Wacky Ideas

### VR Hawker Mode
- **Concept**: A 1st-person mini-game where you manually throw satay at customers.
- **Vibe Coding Suitability**: Low.
- **Vibe Implementation**: "Use a 3D engine like SceneView."
- **Asset Requirement**: 3D models.
- **Difficulty**: Hard.
- **Risks**: Genre clash.

### Crypto-Gold
- **Concept**: The value of your gold changes every minute based on a "Market" (random or real-world BTC price).
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Fetch price from API and multiply all gold gains."
- **Asset Requirement**: Market ticker UI.
- **Difficulty**: Medium.
- **Risks**: Market crash soft-locks the game.

### Kaiju Merlion Attack
- **Concept**: A 1-in-1000 chance that a giant Merlion stomps through the middle of the map, destroying everything.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Random event trigger that calls `clearStalls()` in a center radius."
- **Asset Requirement**: Giant Merlion foot sprite.
- **Difficulty**: Medium.
- **Risks**: Rage quitting.

### 1960s Street Hawker Mode
- **Concept**: A "Black and White" mode where you play as an illegal street hawker running from the "Mata" (police).
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Visual grayscale filter and re-skinning enemies."
- **Asset Requirement**: Retro assets.
- **Difficulty**: Easy.
- **Risks**: None.

### Voice Command Controls
- **Concept**: Shout "ONE TEH TARIK" to place a stall.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Use Android SpeechRecognizer."
- **Asset Requirement**: Code only.
- **Difficulty**: Medium.
- **Risks**: Mis-recognition.

### GPS-Based Buffs
- **Concept**: If you are physically at a real-world Hawker Center, you get a 2x gold buff.
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Check GPS location against a list of coordinates."
- **Asset Requirement**: Location access.
- **Difficulty**: Medium.
- **Risks**: Privacy concerns.

### Stall Romance Sim
- **Concept**: Stalls that are placed next to each other for a long time "fall in love" and get a permanent buff.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Increment a `bondLevel` on adjacent stalls every wave."
- **Asset Requirement**: Heart icons.
- **Difficulty**: Easy.
- **Risks**: Weird vibe.

### Durian Bomb (The "Nuke")
- **Concept**: A button that costs 5000 gold and instantly clears the entire screen with a massive green explosion.
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Button that kills all enemies in the list."
- **Asset Requirement**: Explosion animation.
- **Difficulty**: Easy.
- **Risks**: Game becomes too easy.

### Customer Conversation Logs
- **Concept**: A small text box showing what the customers are saying ("The Laksa is too spicy!", "Where is my tissue?").
- **Vibe Coding Suitability**: High.
- **Vibe Implementation**: "Pick random string based on `enemy.type` and `enemy.healthPercent`."
- **Asset Requirement**: Code only.
- **Difficulty**: Easy.
- **Risks**: Distracting.

### Ghost Map
- **Concept**: You play on a map that is perfectly normal, but the path changes randomly because "Ghosts are moving the tables."
- **Vibe Coding Suitability**: Medium.
- **Vibe Implementation**: "Randomly swap FLOOR and PILLAR tiles between waves."
- **Asset Requirement**: Spooky tile set.
- **Difficulty**: Medium.
- **Risks**: Unwinnable states.

---

## Out of Scope

The following ideas were considered but are designated as "Out of Scope" for the current version of the project:

### Asset Bundling / Hot Reload
- **Reasoning**: The current APK size is under 10MB, which is small enough for standard store updates. Furthermore, the internal API is highly unstable during the "vibe coding" phase. Attempting to hot-reload code or assets would likely lead to frequent crashes and massive technical debt without providing significant value to the player at this scale.

### Real-Time Multiplayer
- **Reasoning**: Synchronizing a physics-lite hexagonal grid in real-time requires a stable backend and sophisticated lag compensation (e.g., lockstep or rollback). This contradicts the "vibe coding" philosophy of rapid, high-level iteration and would require a total architectural overhaul.

### 3D Perspective Shift (Full 3D)
- **Reasoning**: The current engine is optimized for a 2.5D pseudo-3D perspective using Canvas sorting. Moving to full 3D would require switching to a different rendering engine (like Filament or Unity), making the current Compose-based UI and logic obsolete.
