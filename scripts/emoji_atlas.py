import pathlib
import json
from PIL import Image
from tqdm import tqdm


def main():
    emojis = pathlib.Path("./emojis")
    columns = 64
    rows = []
    current_column = 0
    column_data = []
    for i, emoji in enumerate(emojis.glob("**/*.png")):
        if (i + 1) % 64 == 0:
            rows.append(column_data)
            current_column += 1
            column_data = []
        uni = emoji.stem
        column_data.append(uni)
    total_rows = len(rows)

    image_sheet = Image.new("RGBA", (columns * 72, total_rows * 72))

    for row in tqdm(range(len(rows))):
        for column in range(columns):
            if column >= len(rows[row]) - 1:
                break
            emoji_file = pathlib.Path("./emojis/" + rows[row][column] + ".png")
            emoji_image = Image.open(str(emoji_file))
            image_sheet.paste(emoji_image, (column * 72, row * 72))

    image_sheet.save("./emoji_atlas.png")

    with open("emoji_positions.json", "w+") as f:
        json.dump({"length": 72, "positions": rows}, f, indent=4)


if __name__ == '__main__':
    main()
