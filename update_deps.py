import re

toml_file = 'gradle/libs.versions.toml'
with open(toml_file, 'r') as f:
    content = f.read()

versions = """room = "2.6.1"
navigationCompose = "2.7.7"
work = "2.9.0"
ksp = "2.0.21-1.0.27"
"""

libraries = """androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
"""

plugins = """ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
"""

# Insert versions
content = re.sub(r'(\[versions\]\n)', r'\1' + versions, content)
# Insert libraries
content = re.sub(r'(\[libraries\]\n)', r'\1' + libraries, content)
# Insert plugins
content = re.sub(r'(\[plugins\]\n)', r'\1' + plugins, content)

with open(toml_file, 'w') as f:
    f.write(content)
