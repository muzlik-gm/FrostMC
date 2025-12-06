import os
import re

root_dir = r"src\main\java\com\muzlik\fragment\ability\executors"

# Regex to find player.sendMessage("§...")
# We want to be careful not to remove error messages (which usually start with §c✗ or similar)
# Announcements usually start with a color code and maybe an icon.
# Common icons: 🔥, 💧, 💨, ⚡, 🧛, 🐲, 🌑, ⚔, 🛡, 🌪, 🌊, 🏔, 👊, 🐺, ☠
# Error messages usually have "Cannot", "Not enough", "Invalid", "cooldown".
# We want to remove lines that match `player.sendMessage("§[a-f0-9].*");` but NOT error messages.

# Let's inspect some positive examples:
# player.sendMessage("§c🔥 Blazing Step!");
# player.sendMessage("§b💧 Tsunami Wave!");
# player.sendMessage("§f💨 Wind Blade! §7(Rank " + rank + ")");
# player.sendMessage("§f§l💨 sᴛᴏʀᴍ sᴏᴠᴇʀᴇɪɢɴ! §7ʏᴏᴜ ᴄᴏᴍᴍᴀɴᴅ ᴛʜᴇ ᴡɪɴᴅs!");

# Negative examples (keep these):
# player.sendMessage("§c✗ Cannot dash there!");
# player.sendMessage("§c✗ Not enough mana: ...");

# Strategy: Remove lines with `player.sendMessage` that do NOT contain "✗" or "Not enough" or "Invalid" or "cooldown".
# Also require them to have a color code at the start of string.

pattern = re.compile(r'^\s*player\.sendMessage\("§.*"\);')
exclude_keywords = ["✗", "Not enough", "Invalid", "cooldown", "must be", "failed"]

count = 0
for dirpath, dirnames, filenames in os.walk(root_dir):
    for filename in filenames:
        if filename.endswith(".java"):
            filepath = os.path.join(dirpath, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                lines = f.readlines()
            
            new_lines = []
            modified = False
            for line in lines:
                # Check if line matches player.sendMessage
                if "player.sendMessage" in line and '"' in line:
                    # Check if it's an announcement
                    is_announcement = True
                    
                    # Must contain a section sign
                    if "§" not in line:
                        is_announcement = False
                        
                    # Must NOT match excluded keywords
                    for keyword in exclude_keywords:
                        if keyword in line:
                            is_announcement = False
                            break
                    
                    if is_announcement:
                        print(f"Removing from {filename}: {line.strip()}")
                        modified = True
                        continue  # Skip adding this line
                
                new_lines.append(line)
            
            if modified:
                with open(filepath, "w", encoding="utf-8") as f:
                    f.writelines(new_lines)
                count += 1

print(f"Modified {count} files.")
