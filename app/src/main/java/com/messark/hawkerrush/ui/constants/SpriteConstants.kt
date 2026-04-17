package com.messark.hawkerrush.ui.constants

import androidx.compose.ui.unit.IntRect
import com.messark.hawkerrush.model.StallType

object SpriteConstants {
    val FLOOR_RECTS = listOf(
        IntRect(8, 33, 109, 124),   // floor01
        IntRect(126, 33, 227, 124), // floor02
        IntRect(244, 33, 345, 124), // floor03
        IntRect(361, 33, 462, 124), // floor04
        IntRect(478, 33, 579, 124), // floor05
        IntRect(595, 33, 696, 124), // floor06
        IntRect(713, 33, 814, 124), // floor07
        IntRect(595, 149, 696, 240) // floor10
    )
    val EDGE_NW_RECT = IntRect(831, 33, 932, 124)
    val EDGE_NE_RECT = IntRect(1064, 33, 1165, 124)
    val EDGE_SW_RECT = IntRect(948, 149, 1049, 240)
    val EDGE_SE_RECT = IntRect(1064, 149, 1165, 240)
    val EDGE_TOP_RECT = IntRect(830, 267, 931, 360)
    val PILLAR_RECT = IntRect(31, 501, 101, 627)
    val GOAL_TABLE_RECT = IntRect(1100, 430, 1363, 628)
    val TABLE_BASE_RECT = IntRect(1100, 430, 1363, 628) // Placeholder for table surface only

    // Placeholder rectangles for chairs and seated customers (using dummy coords for now)
    // N = Facing South (at top), S = Facing North (at bottom), E = Facing West (at right), W = Facing East (at left)
    val CHAIR_EMPTY_N = IntRect(0, 0, 32, 32)
    val CHAIR_EMPTY_S = IntRect(0, 32, 32, 64)
    val CHAIR_EMPTY_E = IntRect(0, 64, 32, 96)
    val CHAIR_EMPTY_W = IntRect(0, 96, 32, 128)

    val CHAIR_OCCUPIED_GENERIC_N = IntRect(32, 0, 64, 48)
    val CHAIR_OCCUPIED_GENERIC_S = IntRect(32, 48, 64, 96)
    val CHAIR_OCCUPIED_GENERIC_E = IntRect(32, 96, 64, 144)
    val CHAIR_OCCUPIED_GENERIC_W = IntRect(32, 144, 64, 192)

    val FOOD_SATAY_RECT = IntRect(64, 0, 88, 24)
    val FOOD_RICE_RECT = IntRect(64, 24, 88, 48)
    val FOOD_DRINK_RECT = IntRect(64, 48, 88, 72)

    val STALL_RECTS = mapOf(
        StallType.TEH_TARIK to IntRect(22, 41, 330, 451),
        StallType.SATAY to IntRect(358, 41, 666, 451),
        StallType.CHICKEN_RICE to IntRect(22, 500, 330, 930),
        StallType.ICE_KACHANG to IntRect(358, 500, 666, 930),
        StallType.DURIAN to IntRect(33, 961, 341, 1318)
    )

    val ENEMY_SALARYMAN_RECT = IntRect(615, 638, 665, 742)
    val ENEMY_TOURIST_RECT = IntRect(679, 638, 729, 742)
    val ENEMY_AUNTIE_RECT = IntRect(745, 638, 795, 742)
    val ENEMY_RIDER_RECT = IntRect(990, 677, 1044, 744)
    val FX_PUDDLE_RECT = IntRect(1078, 679, 1142, 741)

    // Main Menu Buttons (from drawable-nodpi/board.png)
    val BTN_RESUME_RECT = IntRect(170, 403, 517, 494)
    val BTN_RESUME_CLICK_RECT = IntRect(170, 505, 517, 596)
    val BTN_NEWGAME_RECT = IntRect(170, 635, 517, 726)
    val BTN_OPTIONS_RECT = IntRect(170, 765, 517, 856)
    val BTN_OPTIONS_CLICK_RECT = IntRect(170, 875, 517, 966)
}
