# Hawker Rush: Deep Gameplay & Strategy Analysis

## 1. Core Gameplay Mechanics
Hawker Rush is a hexagonal-grid tower defense game where players defend "Goal Tables" from waves of hungry customers (enemies). The game uses an **axial coordinate system** and features a vertical map (8x16 grid).

### Maze Construction
Unlike fixed-path tower defense games, Hawker Rush allows for **dynamic mazing**.
- **Pathing:** Enemies follow the shortest path from the Start to the Goal Table.
- **Blocking:** Stalls act as obstacles. The game prevents placement if it would completely block all paths for any enemy currently on the board or for future spawns.
- **Strategy:** The primary winning strategy is to force enemies through a "winding" path (S-curve) to maximize their time within stall ranges.

---

## 2. Math & Efficiency Analysis (DPS & ROI)

Using the base stats and upgrade logic from the source, we can calculate the true efficiency of each stall.

### 2.1 Base Efficiency (Level 0)
| Stall Type | Cost | DPS (Raw) | DPS per 100g | Targeting |
| :--- | :--- | :--- | :--- | :--- |
| **Chicken Rice** | 100 | **21.43** | **21.43** | Homing |
| **Satay** | 200 | 20.00 | 10.00 | Fixed AoE |
| **Durian** | 300 | 12.50 | 4.17 | Homing AoE |
| **Ice Kachang** | 250 | 1.33 | 0.53 | Homing Freeze |
| **Teh Tarik** | 150 | 0.00 | 0.00 | Puddle Slow |

**Critical Insight:** **Chicken Rice** is over **2x more cost-efficient** than Satay and **5x more efficient** than Durian for single-target damage. Build these for bosses.

### 2.2 Upgrade ROI (Return on Investment)
When you spend gold to upgrade, the game randomly chooses between Damage/Range or Fire Rate. The ROI on damage is significantly higher:

| Stall Type | Base DPS | Dmg Up DPS | Rate Up DPS | Dmg ROI% | Rate ROI% |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Chicken Rice | 21.43 | 27.14 | 23.81 | **5.71%** | 2.38% |
| Satay | 20.00 | 24.67 | 22.22 | 2.33% | 1.11% |
| Durian | 12.50 | 15.50 | 13.89 | 1.00% | 0.46% |

**Key Takeaway:** Damage upgrades are **~2.4x more efficient** than rate upgrades. Because upgrades are random, it is statistically better to **expand your board with Level 0 stalls** before fishing for upgrades.

---

## 3. Item Analysis (Stalls)
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

## 4. Enemy Behavior & Scaling
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

## 5. Scoring & Economy
- **Gold/Score:** Earned by defeating enemies. Standard enemies grant 20, Delivery Riders grant 100.
- **Refunds:** Selling a stall returns 50% of the total investment (Base cost + Upgrades).
- **Upgrades:** Upgrading a stall costs its base price and provides a random boost to Damage, Range, Fire Rate, or Special Effects (AoE/Duration).

---

## 6. Optimal Board Layouts (Mazing)

The goal is to maximize the time enemies spend in "Kill Zones." On an 8-column map, the "Snake" or "S-Curve" is dominant.

### 5.1 The "Wide Snake" (8-Width Map)
Legend: `[S]` = Start, `[G]` = Goal, `[T]` = Tower, `.` = Path

```text
Row 0:  .  .  .  .  .  .  .  [G]  (Goal Table)
Row 1:  . [T][T][T][T][T][T]  .
Row 2:  .  .  .  .  .  .  .  .
Row 3: [T][T][T][T][T][T] .  .
Row 4:  .  .  .  .  .  .  .  .
Row 5:  . [T][T][T][T][T][T]  .
Row 6:  .  .  .  .  .  .  .  .
Row 7: [S] .  .  .  .  .  .  .  (Start)
```
*Note: This layout forces enemies to walk 7 tiles horizontally for every 2 tiles vertically.*

### 5.2 The "Kill Zone" Hub
Position your most expensive towers (Durian/Ice Kachang) at the "U-turns" of your maze. This allows the tower to hit the enemy as they approach, as they turn, and as they leave.

```text
    [Path] -> [Path] -> [P]
                         |
    [T] <- [T] <- [T]    v
     |             ^    [P]
     v             |     |
    [P] -> [P] -> [K] -> [P]
```
`[K]` = Kill Zone Hub. A tower at `[K]` covers 4-5 different path segments in an S-curve.

---

## 7. Financial Strategy: The "Wave Budget"

### 6.1 Early Game (Waves 1-6)
- **Spend Aggressively:** You start with 500 gold. Place 3 Chicken Rice and 1 Teh Tarik immediately.
- **Save for Wave 10:** Boss waves provide a 44% budget jump for enemies. Ensure you have at least one **Ice Kachang** by wave 10.

### 6.2 Mid-Late Game (Waves 7+)
- **HP Scaling:** Enemies gain 10% HP per wave. If your DPS doesn't grow by 10% per wave, you will leak.
- **The "Sell & Rebuild" Strategy:** In emergencies, sell a stall at the back of the maze (50% refund) to place an **Ice Kachang** at the front. The time gained from the freeze is often worth more than the lost raw damage.

## 8. Strategic Conclusions

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
