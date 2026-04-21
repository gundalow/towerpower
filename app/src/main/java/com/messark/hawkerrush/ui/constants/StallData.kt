package com.messark.hawkerrush.ui.constants

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntRect
import com.messark.hawkerrush.model.*

// Constants related to stall upgrades and economics
object StallConstants {
    const val SELL_REFUND_PERCENTAGE = 0.5f
    const val LEGENDARY_NAME_LEVEL_THRESHOLD = 10
    const val MIN_FIRE_RATE_MS = 50L // Minimum fire rate for stalls
}

// Enum for upgrade categories to improve clarity
enum class StallUpgradeCategory {
    DAMAGE, RATE, SPECIAL
}

// Data class to hold the static configuration for a stall type
data class StallConfig(
    val type: StallType,
    val name: String,
    val cost: Int,
    val color: Color,
    val range: Float,
    val damage: Int,
    val fireRateMs: Long,
    val description: String,
    val tutorialTitle: String,
    val signatureMove: String,
    val tutorialDescription: String,
    val spriteRect: IntRect,
    val aoeRadius: Float = 0f,
    val effectDurationMs: Long = 0L,
    val freezeDurationMs: Long = 0L,
    val projectileSpeed: Float = 0.2f,
    val isArc: Boolean = false,
    val projectileColor: Color = color,
    val visualEffectType: VisualEffectType = VisualEffectType.EXPANDING_CIRCLE,
    val visualEffectColor: Color? = null,
    val visualEffectDuration: Long = 150L
)

object StallData {
    val configs = mapOf(
        StallType.TEH_TARIK to StallConfig(
            type = StallType.TEH_TARIK,
            name = "Teh Tarik",
            cost = 150,
            color = Color.Blue,
            range = 3f,
            damage = 10,
            fireRateMs = 1000L,
            description = "Creates slowing puddles",
            tutorialTitle = "Teh Tarik Maestro (Movement Slow)",
            signatureMove = "The Perpetual Tarik Puddle",
            tutorialDescription = "Welcome to the Teh Tarik Maestro, where the art of 'pulling' tea is a high-level tactical maneuver. This Maestro doesn't just make your enemies slower; he makes the very ground they walk on sticky. Utilizing a massive pair of custom cups, he performs a continuous, mesmerizing 'tarik' high in the air. Each 'pull' perfectly places a wide, frothy Perpetual Tarik Puddle of viscous, sweet milk tea. The tea is so thick and syrupy that enemies stepping into it are immediately bogged down, their speed cut in half as they struggle through the delicious, sticky mess. A crowd favorite for slowing the rush.",
            spriteRect = IntRect(22, 41, 330, 451),
            effectDurationMs = 3000L
        ),
        StallType.SATAY to StallConfig(
            type = StallType.SATAY,
            name = "Satay",
            cost = 200,
            color = Color.Red,
            range = 2.5f,
            damage = 20,
            fireRateMs = 1500,
            description = "Area chili sauce damage",
            tutorialTitle = "Uncle's Satay Stall (AoE Damage)",
            signatureMove = "The Chili Conflagration",
            tutorialDescription = "Wah, smells so shiok! Behind this unassuming grill, the Satay Uncle is fanning a fiery revolution. Watch out for his signature Chili Conflagration—the chili isn't just spicy; it's explosive. He loads up a massive spoon and, with a precision usually reserved for satay-counting, launches a gigantic splash of his secret, explosive chili sauce. When it hits, it covers a wide circle, dousing groups of enemies in a sticky, burning chili storm that eats away at their health (and their willpower). If you need a crowd-control burn, this Uncle is the OG.",
            spriteRect = IntRect(358, 41, 666, 451),
            aoeRadius = 1.0f,
            projectileSpeed = 0.3f,
            isArc = true,
            projectileColor = Color.White,
            visualEffectType = VisualEffectType.GAS_CLOUD,
            visualEffectColor = Color.Red.copy(alpha = 0.3f),
            visualEffectDuration = 500L
        ),
        StallType.CHICKEN_RICE to StallConfig(
            type = StallType.CHICKEN_RICE,
            name = "Chicken Rice",
            cost = 100,
            color = Color.Yellow,
            range = 4f,
            damage = 15,
            fireRateMs = 700,
            description = "High single-target damage",
            tutorialTitle = "Ah Hock’s Chicken Rice Stand (Single-Target DPS)",
            signatureMove = "The Garlic-Ginger Gatling Gun",
            tutorialDescription = "Ah Hock’s Chicken Rice is famous for two things: the tenderest steamed chicken and the single-minded focus of his attacks. Don’t be fooled by the simple setup; this stand is your base single-target workhorse. When an enemy is targeted, Ah Hock deploys his Garlic-Ginger Gatling Gun. Instead of bullets, he’s launching high-velocity, precision-aimed balls of marinated meat, dousing targets in flavor-infused damage. It’s consistent, it’s powerful, and it never runs out of stock. A classic choice that never fails.",
            spriteRect = IntRect(22, 500, 330, 930)
        ),
        StallType.DURIAN to StallConfig(
            type = StallType.DURIAN,
            name = "Durian",
            cost = 300,
            color = Color(0xFF4CAF50),
            range = 3f,
            damage = 120,
            fireRateMs = 2000,
            description = "Massive damage, slow fire",
            tutorialTitle = "The King Durian Bunker (High Damage/Slight AoE)",
            signatureMove = "The Spiky Cataclysm",
            tutorialDescription = "They call the Durian the King of Fruits, and this stall is the King of Damage. The King Durian Bunker is fortified with armor-plating and smells… well, like a durian. When the King’s crew makes a sale, they aren't selling just fruit; they are deploying a localized explosive. Using a heavy-duty pneumatic launcher, they fire an overripe, spikey Durian bomb into the largest cluster of enemies. Upon impact, it delivers a high-damage, single-target blow, followed immediately by a Spiky Cataclysm AoE explosion as the potent, heavy aroma bursts outward. It’s high-cost and slow-reloading, but the raw damage (and the scent) is devastating.",
            spriteRect = IntRect(33, 961, 341, 1318),
            aoeRadius = 1.0f,
            visualEffectColor = Color(0xFFCDDC39).copy(alpha = 0.5f)
        ),
        StallType.ICE_KACHANG to StallConfig(
            type = StallType.ICE_KACHANG,
            name = "Ice Kachang",
            cost = 250,
            color = Color.Cyan,
            range = 3.5f,
            damage = 2,
            fireRateMs = 1500,
            description = "Freezes enemies in place",
            tutorialTitle = "Auntie's Ice Kachang Cart (Stun/Freezer)",
            signatureMove = "The Absolute Zero Brain Freeze",
            tutorialDescription = "Want something to really chill out the enemies? Then you need the Auntie at the Ice Kachang Cart! She’s taken traditional dessert techniques to the cryo-level. Her specialized ice shaver can launch a massive, compacted ball of shaved ice, syrup, and cold, cold, red beans, aimed precisely at the lead enemy. Upon impact, it doesn't just damage; it delivers an Absolute Zero Brain Freeze. The target is frozen solid, encased in a giant colorful ice cube, completely immobilized for several precious seconds. A perfect stall for controlling boss units.",
            spriteRect = IntRect(358, 500, 666, 930),
            freezeDurationMs = 500L
        )
    )
}

