
try:
    with open("build_output.txt", "r", encoding="utf-16le") as f:
        content = f.read()
except UnicodeError:
    try:
        with open("build_output.txt", "r", encoding="utf-8") as f:
            content = f.read()
    except:
        with open("build_output.txt", "r", errors="ignore") as f:
            content = f.read()

for line in content.splitlines():
    if "ERROR" in line:
        print(line)
