
import os
import re

root_dir = r"src\main\java\com\muzlik\fragment\ability\executors"

suspicious_patterns = [
    re.compile(r'^\s*\+'),  # Starts with +
    re.compile(r'^\s*String\.format'), # Starts with String.format (unlikely as a statement)
    re.compile(r'^\s*".*"\);'), # Starts with quote, ends with );
    re.compile(r'^\s*"\);'), # Just ");
    re.compile(r'^\s*\);') # Just );
]

for dirpath, dirnames, filenames in os.walk(root_dir):
    for filename in filenames:
        if filename.endswith(".java"):
            filepath = os.path.join(dirpath, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                lines = f.readlines()
            
            for i, line in enumerate(lines):
                stripped = line.strip()
                for pattern in suspicious_patterns:
                    if pattern.match(stripped):
                         # Exclude legitimate uses?
                         # Starting with + is rare in java unless multi-line.
                         # If previous line is empty (deleted), it's suspicious.
                         if i > 0 and not lines[i-1].strip():
                             print(f"Suspicious line in {filename}:{i+1}: {stripped}")
                         elif i == 0:
                             print(f"Suspicious line in {filename}:{i+1}: {stripped}")
