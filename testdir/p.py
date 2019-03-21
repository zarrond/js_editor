import os
cmds = [r"set PATH=%PATH%;C:\Program Files\Java\jdk1.8.0_162\bin",
        "javac testJS.java",
        "java testJS",
        "del testJS.class"]
with open("run.bat", "w") as f:
    for cmd in cmds:
        f.write(cmd+"\n")
os.system("run.bat")
os.system("del run.bat")
