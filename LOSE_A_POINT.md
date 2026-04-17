# LOSE_A_POINT.md - Implementation Options

This document outlines three options for making the event of losing a life (an enemy reaching the table) more visually interesting in Hawker Rush.

---

## Option 1: Directional Seated Occupancy (Square Table)
**Proposed Change:**
The `GOAL_TABLE` tile is transformed from a single static sprite into a dynamic assembly. We maintain 10 "slots" (chairs) around a square table, distributed across all four sides (North, South, East, West).

**Layout (10 Seats):**
*   **South (Bottom):** 3 chairs (facing North)
*   **North (Top):** 3 chairs (facing South)
*   **East (Right):** 2 chairs (facing West)
*   **West (Left):** 2 chairs (facing East)

As health decreases, generic "Seated Customer" sprites appear in these slots, oriented correctly for their side of the table.

### Detailed Code Changes
```diff
--- a/app/src/main/java/com/messark/hawkerrush/model/GameModels.kt
+++ b/app/src/main/java/com/messark/hawkerrush/model/GameModels.kt
@@ -107,6 +107,7 @@ data class GameState(
     val currentScreen: AppScreen = AppScreen.LOADING,
     val hexes: Map<AxialCoordinate, HexTile> = emptyMap(),
     val health: Int = 10, // 10 tables/chairs
+    val livesLost: Int = 0,
     val gold: Int = 500,
```

```diff
--- a/app/src/main/java/com/messark/hawkerrush/ui/components/GameBoard.kt
+++ b/app/src/main/java/com/messark/hawkerrush/ui/components/GameBoard.kt
@@ -140,7 +140,21 @@ fun GameBoard(...) {
                     srcRect?.let { rect ->
                         val isEdge = tile.type.name.startsWith("EDGE_")
                         drawables.add(DrawableEntity(
                             q = coord.q.toFloat(),
                             r = coord.r.toFloat(),
                             zOrder = if (isEdge) 1 else 2,
                             draw = {
                                 val scale = wPx / 101f
+                                if (tile.type == TileType.GOAL_TABLE) {
+                                    // Draw Table Base (no chairs)
+                                    drawSprite(SpriteConstants.TABLE_BASE_RECT, ...)
+                                    // Draw 10 chairs, some empty, some with seated customers
+                                    for (i in 0 until 10) {
+                                        val isOccupied = i < (10 - gameState.health)
+                                        val chairRect = if (isOccupied) SpriteConstants.CHAIR_OCCUPIED_RECT else SpriteConstants.CHAIR_EMPTY_RECT
+                                        val offset = getChairOffset(i)
+                                        drawSprite(chairRect, screenPos + offset, ...)
+                                    }
+                                } else {
                                     drawSprite(...)
+                                }
```

### Graphical Artifacts & Vibe Code
*   **TABLE_BASE**: The existing table surface but without chairs.
*   **CHAIR_EMPTY_[N/S/E/W]**: 4 directional versions of a hawker stool.
*   **CHAIR_OCCUPIED_[N/S/E/W]**: 4 directional versions of a generic seated customer.

**Vibe Code Prompts (DALL-E 3 / Midjourney):**
> `Pixel art sprite sheet of a red plastic Singaporean hawker stool from 4 angles (front, back, left side, right side), top-down 2D perspective, 32x32 pixels per frame, 16-bit Sega Genesis style, transparent background.`
>
> `Pixel art sprite sheet of a person sitting on a red stool from 4 angles (facing North, South, East, West), wearing a white t-shirt, 2D top-down perspective, 32x48 pixels per frame, matches existing game characters' pixel density and lighting.`

---

## Option 2: Character-Matched Occupancy
**Proposed Change:**
Similar to Option 1, but instead of a generic customer, we track which specific enemy type reached the goal and render that specific character sitting at the table.

### Detailed Code Changes
```diff
--- a/app/src/main/java/com/messark/hawkerrush/model/GameModels.kt
+++ b/app/src/main/java/com/messark/hawkerrush/model/GameModels.kt
@@ -107,6 +107,7 @@ data class GameState(
     val currentScreen: AppScreen = AppScreen.LOADING,
     val hexes: Map<AxialCoordinate, HexTile> = emptyMap(),
     val health: Int = 10,
+    val arrivedEnemies: List<EnemyType> = emptyList(),
```

### Graphical Artifacts & Vibe Code
*   Requires seated versions (4 directions each) of: **Salaryman, Tourist, Auntie, Delivery Rider**.

**Vibe Code Prompts:**
> `Pixel art sprite sheet of a tired office worker (Salaryman) sitting on a stool, 4 directional views (N, S, E, W), 2D top-down perspective, 16-bit style, 32x48 pixels per frame.`
>
> `Pixel art sprite sheet of an elderly woman (Auntie) in a floral blouse sitting on a stool, 4 directional views (N, S, E, W), 2D top-down perspective, 16-bit style, 32x48 pixels per frame.`

---

## Option 3: The Hawker Feast (Visual Food Accumulation)
**Proposed Change:**
As lives are lost, not only do people fill the chairs, but the table surface progressively fills with plates of food (Satay, Chicken Rice bowls, Teh Tarik glasses).

### Detailed Code Changes
```diff
--- a/app/src/main/java/com/messark/hawkerrush/ui/components/GameBoard.kt
+++ b/app/src/main/java/com/messark/hawkerrush/ui/components/GameBoard.kt
+ // Inside GOAL_TABLE draw block
+ if (gameState.health < 10) {
+     val foodItems = listOf(SpriteConstants.FOOD_SATAY_RECT, SpriteConstants.FOOD_RICE_RECT, ...)
+     for (i in 0 until (10 - gameState.health)) {
+         val foodRect = foodItems[i % foodItems.size]
+         val tableSurfaceOffset = getSurfaceOffset(i)
+         drawSprite(foodRect, screenPos + tableSurfaceOffset, ...)
+     }
+ }
```

### Graphical Artifacts & Vibe Code
*   **FOOD_SATAY**: A plate with 5 satay sticks and a bowl of sauce.
*   **FOOD_RICE**: A plate of chicken rice with cucumber slices.
*   **FOOD_TEH_TARIK**: A glass of frothy milk tea.

**Vibe Code Prompts:**
> `Pixel art icon of a plate of satay sticks with peanut sauce bowl, top-down perspective, 24x24 pixels, vibrant colors, Singaporean hawker food, 16-bit style.`
>
> `Pixel art icon of a bowl of hainanese chicken rice, top-down perspective, 24x24 pixels, clean pixel work, transparent background.`

---

## How to Match the "Look & Feel"
1.  **Palette Extraction:** Use the existing `sprite_sheet.png` to extract the primary color palette (the specific greens, reds, and wood tones used).
2.  **Resolution Scaling:** Ensure all new assets are generated/resized to match the pixel density of the current tiles (approx. 101x91 units per hex).
3.  **Outline Style:** The existing sprites use a soft dark outline (not pure black). New assets must follow this.
4.  **Lighting:** Light source is consistently top-left. Shadows should be cast to the bottom-right of the characters/objects.
