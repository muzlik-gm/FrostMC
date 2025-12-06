
import os
import re

root_dir = r"src\main\java\com\muzlik\fragment\ability\executors"

for dirpath, dirnames, filenames in os.walk(root_dir):
    for filename in filenames:
        if filename.endswith(".java"):
            filepath = os.path.join(dirpath, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            
            # Simple check for if (...) followed directly by }
            # We need to be careful about whitespace and comments.
            
            lines = content.splitlines()
            for i in range(len(lines)):
                line = lines[i].strip()
                if line.startswith("if (") and not line.endswith("{"):
                    # Check next non-empty line
                    j = i + 1
                    while j < len(lines) and not lines[j].strip():
                        j += 1
                    
                    if j < len(lines):
                        next_line = lines[j].strip()
                        if next_line.startswith("}"):
                            print(f"Suspicious hanging if in {filename} at line {i+1}")
                        elif next_line.startswith("else"):
                            # if (...) else ... -> valid but weird if body is empty?
                            # Java requires a statement.
                            # If I deleted the statement, it's effectively empty.
                            # But if lines[j] is "else", then previous if has no body?
                            # "if (...) else" is syntax error.
                             print(f"Suspicious hanging if before else in {filename} at line {i+1}")
