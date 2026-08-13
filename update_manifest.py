import re

manifest_file = 'app/src/main/AndroidManifest.xml'
with open(manifest_file, 'r') as f:
    content = f.read()

permissions = """
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
"""

service = """
        <service
            android:name=".BatteryMonitorService"
            android:exported="false"
            android:foregroundServiceType="dataSync" />
"""

content = re.sub(r'(<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />)', r'\1\n' + permissions, content)
content = re.sub(r'(</application>)', service + r'\1', content)

with open(manifest_file, 'w') as f:
    f.write(content)
