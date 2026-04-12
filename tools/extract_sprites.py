import os
from PIL import Image

# Configuration
SPRITE_SHEET_PATH = "app/src/main/res/drawable/sprite_sheet.png"
OUTPUT_DIR = "sprite_debug"
HTML_FILE = "index.html"

# Grid settings from memory
GRID_CELL_SIZE = 256
GRID_OFFSET_X = 70
GRID_OFFSET_Y = 94

# Sprite Definitions from GameBoard.kt
# format: name, (left, top, right, bottom), (grid_col, grid_row, grid_width, grid_height), line_number
SPRITES = [
    ("floorPlain", (70, 94, 70 + 117, 94 + 110), (0, 0, 117, 110), 40),
    ("floorCheckered", (326, 94, 326 + 117, 94 + 110), (1, 0, 117, 110), 41),
    ("floorDirty", (1350, 94, 1350 + 117, 94 + 110), (5, 0, 117, 110), 42),
    ("floorChope", (1350, 350, 1350 + 117, 350 + 110), (5, 1, 117, 110), 43),
    ("edgeNorth", (1862, 94, 1862 + 117, 94 + 110), (7, 0, 117, 110), 44),
    ("edgeCorner", (1862, 606, 1862 + 117, 606 + 110), (7, 2, 117, 110), 45),
    ("pillar", (70, 862, 70 + 117, 862 + 234), (0, 3, 117, 234), 46),
    ("goalTable", (1606, 862, 1606 + 280, 862 + 200), (6, 3, 280, 200), 47),

    ("uiTehTarik", (96, 1888, 96 + 65, 1888 + 65), (0, 7, 65, 65), 49),
    ("uiSatay", (352, 1888, 352 + 65, 1888 + 65), (1, 7, 65, 65), 50),
    ("uiChickenRice", (608, 1888, 608 + 65, 1888 + 65), (2, 7, 65, 65), 51),
    ("uiDurian", (864, 1888, 864 + 65, 1888 + 65), (3, 7, 65, 65), 52),
    ("uiIceKachang", (1120, 1888, 1120 + 65, 1888 + 65), (4, 7, 65, 65), 53),

    ("enemySalaryman", (1088, 1632, 1088 + 100, 1632 + 180), (4, 6, 100, 180), 55),
    ("enemyTourist", (1344, 1632, 1344 + 110, 1632 + 180), (5, 6, 110, 180), 56),
    ("enemyAuntie", (1600, 1632, 1600 + 110, 1632 + 180), (6, 6, 110, 180), 57),
    ("enemyRider", (1580, 1880, 1580 + 160, 1880 + 150), (6, 7, 160, 150), 58),
    ("fxPuddle", (1888, 1884, 1888 + 65, 1884 + 65), (7, 7, 65, 65), 59),
]

def main():
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    img = Image.open(SPRITE_SHEET_PATH)

    html_content = """
    <html>
    <head>
        <title>Sprite Debug Report</title>
        <style>
            table { border-collapse: collapse; width: 100%; }
            th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
            img { border: 1px solid #000; background-color: #f0f0f0; }
            .mismatch { color: red; font-weight: bold; }
        </style>
    </head>
    <body>
        <h1>Sprite Debug Report</h1>
        <table>
            <tr>
                <th>Name</th>
                <th>Source Line</th>
                <th>Hardcoded Rect</th>
                <th>Hardcoded Sprite</th>
                <th>Grid (Col, Row)</th>
                <th>Calculated Rect</th>
                <th>Calculated Sprite</th>
            </tr>
    """

    for name, hard_rect, grid_info, line in SPRITES:
        col, row, w, h = grid_info

        # Hardcoded crop
        hard_crop = img.crop(hard_rect)
        hard_filename = f"{name}_hard.png"
        hard_crop.save(os.path.join(OUTPUT_DIR, hard_filename))

        # Grid calculated crop (using the basic formula)
        calc_rect = (
            (col * GRID_CELL_SIZE) + GRID_OFFSET_X,
            (row * GRID_CELL_SIZE) + GRID_OFFSET_Y,
            (col * GRID_CELL_SIZE) + GRID_OFFSET_X + w,
            (row * GRID_CELL_SIZE) + GRID_OFFSET_Y + h
        )

        # Special handling for UI/FX if they don't follow the 70/94 offset directly
        if name.startswith("ui") or name.startswith("fx"):
             # Based on 96, 1888...
             # 96 = (col * 256) + 96
             # 1888 = (7 * 256) + 96
             calc_rect = (
                (col * GRID_CELL_SIZE) + 96,
                (row * GRID_CELL_SIZE) + 96,
                (col * GRID_CELL_SIZE) + 96 + w,
                (row * GRID_CELL_SIZE) + 96 + h
            )

        # Special handling for enemies
        if name.startswith("enemy"):
            # enemySalaryman: 1088, 1632. col=4, row=6.
            # 1088 - (4*256) = 1088 - 1024 = 64
            # 1632 - (6*256) = 1632 - 1536 = 96
            calc_rect = (
                (col * GRID_CELL_SIZE) + 64,
                (row * GRID_CELL_SIZE) + 96,
                (col * GRID_CELL_SIZE) + 64 + w,
                (row * GRID_CELL_SIZE) + 96 + h
            )
            # Rider is row 7. 1880 - (7*256) = 1880 - 1792 = 88. Hmm.
            if name == "enemyRider":
                 calc_rect = (
                    (col * GRID_CELL_SIZE) + 44, # 1580 - 1536 = 44
                    (row * GRID_CELL_SIZE) + 88,
                    (col * GRID_CELL_SIZE) + 44 + w,
                    (row * GRID_CELL_SIZE) + 88 + h
                )

        calc_crop = img.crop(calc_rect)
        calc_filename = f"{name}_calc.png"
        calc_crop.save(os.path.join(OUTPUT_DIR, calc_filename))

        mismatch_class = " class='mismatch'" if hard_rect != calc_rect else ""

        html_content += f"""
            <tr>
                <td>{name}</td>
                <td>{line}</td>
                <td>{hard_rect}</td>
                <td><img src="{hard_filename}"></td>
                <td>({col}, {row})</td>
                <td{mismatch_class}>{calc_rect}</td>
                <td><img src="{calc_filename}"></td>
            </tr>
        """

    html_content += """
        </table>
    </body>
    </html>
    """

    with open(os.path.join(OUTPUT_DIR, HTML_FILE), "w") as f:
        f.write(html_content)

    print(f"Report generated at {os.path.join(OUTPUT_DIR, HTML_FILE)}")

if __name__ == "__main__":
    main()
