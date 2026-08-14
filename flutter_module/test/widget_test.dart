import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:flutter_module/main.dart';

void main() {
  testWidgets('Patch panel rendering test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const BatteryRockFlutterModule());

    // Verify that the title is present.
    expect(find.text('Xposed Hook Panel'), findsOneWidget);

    // Verify that the Telemetry Killer list tile is present
    expect(find.text('Telemetry Killer'), findsOneWidget);

    // Verify the status says Active initially
    expect(find.text('Hook Active'), findsWidgets);

    // Tap the 'Telemetry Killer' tile and trigger a frame.
    await tester.tap(find.text('Telemetry Killer'));
    await tester.pump();

    // After tapping, there should be one more 'Hook Disabled'
    expect(find.text('Hook Disabled'), findsWidgets);
  });
}
