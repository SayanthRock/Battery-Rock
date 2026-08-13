import re

build_file = 'app/build.gradle.kts'
with open(build_file, 'r') as f:
    content = f.read()

plugins_addition = """    alias(libs.plugins.ksp)
"""

deps_addition = """    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
"""

content = re.sub(r'(plugins \{)', r'\1\n' + plugins_addition, content)
content = re.sub(r'(dependencies \{(\n\s+.*)*\n\})', r'\1\n\ndependencies {\n' + deps_addition + '}\n', content, count=1)

with open(build_file, 'w') as f:
    f.write(content)
