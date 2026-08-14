import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

# We have multiple copies of CustomizableBatteryWidgetReceiver.
# Let's find one and remove all others.

pattern = re.compile(r'\s*<receiver\s+android:name="\.widget\.CustomizableBatteryWidgetReceiver".*?</receiver>', re.DOTALL)
matches = list(pattern.finditer(content))

if len(matches) > 1:
    # Keep the first one, remove the rest
    first_match = matches[0]

    new_content = content[:first_match.end()]
    rest = content[first_match.end():]

    new_rest = pattern.sub('', rest)

    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(new_content + new_rest)
    print("Fixed duplicate receivers.")
else:
    print("No duplicate receivers found.")
