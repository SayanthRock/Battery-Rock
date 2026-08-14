import 'package:flutter/material.dart';

void main() => runApp(const BatteryRockFlutterModule());

class BatteryRockFlutterModule extends StatelessWidget {
  const BatteryRockFlutterModule({super.key});

  @override
  Widget build(BuildContext context) {
    // Diagnostic Console Palette
    const backgroundColor = Color(0xFF0E1013);
    const cardColor = Color(0xFF16191D);
    const primaryTextColor = Color(0xFFE8E6E1);
    const secondaryTextColor = Color(0xFFA9A9A9);
    const accentCyan = Color(0xFF4DE0FF);
    const accentAmber = Color(0xFFFFB627);
    const alertRed = Color(0xFFD32F2F);
    const dividerColor = Color(0xFF333840);

    return MaterialApp(
      title: 'Battery-Rock Module',
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: backgroundColor,
        cardColor: cardColor,
        dividerColor: dividerColor,
        textTheme: const TextTheme(
          bodyMedium: TextStyle(color: primaryTextColor),
          titleLarge: TextStyle(color: primaryTextColor, fontWeight: FontWeight.bold),
          titleMedium: TextStyle(color: primaryTextColor, fontWeight: FontWeight.w600),
          titleSmall: TextStyle(color: secondaryTextColor),
        ),
        colorScheme: const ColorScheme.dark(
          primary: accentAmber,
          secondary: accentCyan,
          surface: cardColor,
          error: alertRed,
        ),
      ),
      home: const PatchPanelScreen(),
    );
  }
}

class PatchPanelScreen extends StatefulWidget {
  const PatchPanelScreen({super.key});

  @override
  State<PatchPanelScreen> createState() => _PatchPanelScreenState();
}

class _PatchPanelScreenState extends State<PatchPanelScreen> {
  final Map<String, bool> _hookStates = {
    'Telemetry Killer': true,
    'Charge Limiter': true,
    'Wakelock Blocker': false,
    'ROM Adaptive Engine': true,
    'Thermal Throttler': false,
  };

  @override
  Widget build(BuildContext context) {
    const accentAmber = Color(0xFFFFB627);
    const dividerColor = Color(0xFF333840);
    const cardColor = Color(0xFF16191D);
    const primaryTextColor = Color(0xFFE8E6E1);
    const secondaryTextColor = Color(0xFFA9A9A9);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Xposed Hook Panel'),
        backgroundColor: Colors.transparent,
        elevation: 0,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1.0),
          child: Container(
            color: dividerColor,
            height: 1.0,
          ),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
        children: [
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 16.0),
            child: Text(
              'MODULE STATUS: ACTIVE',
              style: TextStyle(
                fontFamily: 'monospace',
                color: accentAmber,
                fontWeight: FontWeight.bold,
                fontSize: 12,
              ),
            ),
          ),
          ..._hookStates.entries.map((entry) {
            return Card(
              color: cardColor,
              shape: const RoundedRectangleBorder(
                side: BorderSide(color: dividerColor, width: 1.0),
                borderRadius: BorderRadius.all(Radius.circular(4.0)),
              ),
              margin: const EdgeInsets.only(bottom: 12.0),
              child: ListTile(
                contentPadding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                leading: Container(
                  width: 12,
                  height: 12,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: entry.value ? accentAmber : Colors.transparent,
                    border: Border.all(
                      color: entry.value ? accentAmber : secondaryTextColor,
                      width: 2,
                    ),
                    boxShadow: entry.value
                        ? [
                            BoxShadow(
                              color: accentAmber.withOpacity(0.4),
                              blurRadius: 4,
                              spreadRadius: 1,
                            )
                          ]
                        : null,
                  ),
                ),
                title: Text(
                  entry.key,
                  style: const TextStyle(fontWeight: FontWeight.bold, color: primaryTextColor),
                ),
                subtitle: Text(
                  entry.value ? 'Hook Active' : 'Hook Disabled',
                  style: const TextStyle(fontFamily: 'monospace', color: secondaryTextColor, fontSize: 12),
                ),
                onTap: () {
                  setState(() {
                    _hookStates[entry.key] = !entry.value;
                  });
                },
              ),
            );
          }),
          const SizedBox(height: 24),
          const Center(
            child: Text(
              'Battery-Rock System Settings',
              style: TextStyle(
                color: secondaryTextColor,
                fontSize: 12,
              ),
            ),
          )
        ],
      ),
    );
  }
}
