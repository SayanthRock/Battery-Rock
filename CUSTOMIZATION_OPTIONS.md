# 🎛️ Battery-Rock Customization Options

## Battery Backup · Mobile Performance · Screen Refresh Rate

This document defines the customization options planned for the next Battery-Rock module update.

Battery-Rock should give users simple controls instead of confusing hidden tweaks. Every option should be reversible, easy to understand, and safe to test through LSPosed.

---

## 🔋 Battery Backup Mode

| Mode | Best For | Behavior |
|---|---|---|
| Safe | Daily users | Conservative background optimization with lower risk |
| Balanced | Most users | Good battery backup improvement without being too aggressive |
| Advanced | Power users | Stronger background control with warning before enabling |

Recommended default: **Balanced**

---

## ⚡ Mobile Performance Mode

| Mode | Best For | Behavior |
|---|---|---|
| Standard | Normal daily use | Keeps default smoothness and safe behavior |
| Smooth | Better UI feel | Prioritizes smoother foreground experience |
| Performance | Gaming and heavy use | Stronger performance preference with more battery usage possible |

Recommended default: **Standard**

---

## 📱 Screen Refresh Rate

| Mode | Best For | Behavior |
|---|---|---|
| Auto-select | Recommended default | Phone chooses the best refresh rate automatically |
| High | Smoothness | Prefers high refresh rate where supported |
| Standard | Battery backup | Prefers standard refresh behavior for better battery life |

Recommended default: **Auto-select**

Important:

- Device must support refresh-rate switching.
- ROM must expose compatible display settings.
- Battery impact depends on screen usage and panel type.
- The module should check compatibility before applying changes.

---

## 🎨 UI Design Direction

Battery-Rock UI should use a premium SayanthRock 2026 style:

- Dark glassmorphism background
- Rounded option cards
- Active status badge
- Large mode selector buttons
- Clear selected state
- Simple icons for battery, performance, and refresh rate
- Warning label for Advanced options
- Clean spacing and mobile-first layout

---

## 🧭 Suggested App Screens

```text
Home
├── Battery-Rock status
├── Battery backup mode
├── Mobile performance mode
├── Screen refresh rate mode
└── Quick test checklist

Hooks
├── Framework hooks
├── Telemetry hooks
├── Wakelock hooks
└── Scope status

Settings
├── Safe reset
├── Export settings
├── Import settings
└── About Battery-Rock
```

---

## ✅ Implementation Checklist

- [ ] Add mode data model
- [ ] Add Compose selector cards
- [ ] Save selected options locally
- [ ] Add LSPosed active status check
- [ ] Add compatibility check for refresh-rate support
- [ ] Add warning dialog for Advanced mode
- [ ] Add reset to default button
- [ ] Test on OPPO firmware
- [ ] Test on Realme firmware
- [ ] Test on OnePlus firmware

---

## Default Recommended Setup

```text
Battery Backup: Balanced
Mobile Performance: Standard
Screen Refresh Rate: Auto-select
Background Control: Optimized
UI Style: Dark Glass
```

---

## GitHub Release Message

```markdown
Battery-Rock now has a clear customization direction for battery backup, mobile performance, and screen refresh-rate control.

Planned options:

- Battery Backup: Safe, Balanced, Advanced
- Mobile Performance: Standard, Smooth, Performance
- Screen Refresh Rate: Auto-select, High, Standard

The goal is better battery backup and cleaner performance control on supported OPPO, Realme, and OnePlus devices using LSPosed.
```
