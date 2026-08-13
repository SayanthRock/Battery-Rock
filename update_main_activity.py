import re

main_file = 'app/src/main/java/dev/sayanthrock/batteryrock/MainActivity.kt'
with open(main_file, 'r') as f:
    content = f.read()

content = content.replace('BatteryScreen()', 'BatteryRockApp()')

with open(main_file, 'w') as f:
    f.write(content)
