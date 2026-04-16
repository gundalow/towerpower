# Hawker Rush: Deep Gameplay & Strategy Analysis

## 1. Core Gameplay Mechanics
Hawker Rush is a hexagonal-grid tower defense game where players defend "Goal Tables" from waves of hungry customers (enemies). The game uses an **axial coordinate system** and features a vertical map (8x16 grid).

### Maze Construction
Unlike fixed-path tower defense games, Hawker Rush allows for **dynamic mazing**.
- **Pathing:** Enemies follow the shortest path from the Start to the Goal Table.
- **Blocking:** Stalls act as obstacles. The game prevents placement if it would completely block all paths for any enemy currently on the board or for future spawns.
- **Strategy:** The primary winning strategy is to force enemies through a "winding" path (S-curve) to maximize their time within stall ranges.

---

## 2. Item Analysis (Stalls)
There are five types of stalls, each with distinct roles:

| Stall Type | Cost | DMG | Rate | Range | Targeting | Special Effect |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Chicken Rice** | 100 | 15 | 700ms | 4.0 | Homing | High single-target DPS. Best for bosses. |
| **Teh Tarik** | 150 | 0 | 1000ms | 3.0 | Fixed | Creates **Sticky Puddles** (3s) slowing enemies to 60% speed. |
| **Satay** | 200 | 30 | 1500ms | 2.5 | **Fixed** | **AoE Arc Damage**. Fast projectile but doesn't track targets. |
| **Ice Kachang** | 250 | 2 | 1500ms | 3.5 | Homing | **Freezes** enemies for 1.5s. |
| **Durian** | 300 | 25 | 2000ms | 3.0 | **Homing** | Heavy AoE damage. Tracks targets to ensure impact. |

**Balance Insight:** While *Satay* has higher raw DPS than *Durian*, its "Fixed" targeting means it can miss fast enemies (Salaryman). *Durian* is more reliable for mid-maze defense.

---

## 3. Enemy Behavior & Scaling
Enemies have varied attributes and behaviors:

- **Salaryman:** The baseline enemy. Fast (0.08 speed) but low health.
- **Tourist:** Slow (0.04 speed). **Unique Behavior:** Stops for 2 seconds every 8 seconds to "take photos," making them easy targets.
- **Auntie:** Very slow (0.03 speed) but tanky.
- **Delivery Rider:** High health and relatively fast (0.06 speed). **Crucial:** They are **immune to Teh Tarik puddles**, requiring Ice Kachang or raw DPS to stop.

### Scaling (Wave 7+)
The game uses exponential scaling:
- **HP:** Increases by **10% per wave** (`1.1^(wave-1)`).
- **Budget:** Increases by **20% per wave**, with a **44% jump** on Boss Waves (every 10 waves).

---

## 4. Scoring & Economy
- **Gold/Score:** Earned by defeating enemies. Standard enemies grant 20, Delivery Riders grant 100.
- **Refunds:** Selling a stall returns 50% of the total investment (Base cost + Upgrades).
- **Upgrades:** Upgrading a stall costs its base price and provides a random boost to Damage, Range, Fire Rate, or Special Effects (AoE/Duration).

---

## 5. Winning Strategies

### A. The "S-Curve" Maze
Don't just place towers randomly. Build a corridor that forces enemies to walk back and forth across the width of the map. This multiplies the effectiveness of every stall you place.

### B. The Slow-Burn Synergy
Place **Teh Tarik** stalls at the beginning of "kill zones." The slowing effect stacks with the high damage of **Satay** or **Chicken Rice**. Since puddles last 3 seconds, a single Teh Tarik can maintain 100% slow uptime on a specific tile.

### C. Countering the Delivery Rider
Since Delivery Riders ignore puddles, they are the most common cause of "leaks."
- **Solution:** Place **Ice Kachang** near the end of your maze to freeze them, and ensure you have high-damage **Chicken Rice** towers with "Strongest" or "First" targeting modes.

### D. Target Management
Use the **Target Mode** toggle (First, Closest, Strongest, Weakest):
- Set **Chicken Rice** to *Strongest* to chip away at Aunties and Delivery Riders.
- Set **Satay/Durian** to *First* to clear the lead pack of Salarymen.

### E. Strategic Selling & Upgrading
- **Selling:** If an enemy is about to reach the Goal Table, sell a tower at the start of the maze and quickly place an **Ice Kachang** at the end to buy time.
- **Upgrading:** Upgrades are **randomly assigned** to either Damage/Range, Fire Rate, or Special Effects. It is often more cost-effective to build multiple level 1 stalls before gambling on upgrades, unless you have a perfectly positioned stall in a central "hub."

### F. The Tourist Advantage
Since **Tourists** stop every 8 seconds, place high-fire-rate stalls (**Chicken Rice**) in areas where they are likely to pause. Their 2-second photography break effectively doubles the damage they take in that zone.

### G. Advanced Synergies
1.  **The "Choke Point" Freeze:** Place an **Ice Kachang** at a corner where the maze turns. When the lead enemy freezes, they block the path for those behind them, effectively "clumping" the wave for a **Satay** or **Durian** blast.
2.  **Teh Tarik Overlap:** While puddles don't stack their slow effect (fixed at 60% speed), overlapping the range of two Teh Tarik stalls ensures that even if one is on cooldown, a puddle is always present, creating a permanent slow zone.
3.  **Economy Management:** Avoid over-investing in a single tower early. Since scaling is exponential, having a broad coverage of Level 1 towers is safer than one Level 3 tower that might miss fast Salarymen.
