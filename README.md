# Alarm Me

**Alarm Me** is a smart notification-based alarm app for Android.  
Instead of setting alarm times, you define rules for notifications from apps like WhatsApp, Messenger, or Discord. When a notification matches your criteria, the alarm is triggered.

---

## How It Works

For each app, you can define:
- **Senders**: Specific names or allow all
- **Keywords**: Words or phrases that trigger the alarm

If a notification matches both or one of them ( you control that ), the alarm activates instantly.

---

## Example Scenarios

- WhatsApp: Alarm triggers if "n1" or "n2" sends "wake up" or "quiz"
- Messenger: Any sender using `#important#` triggers the alarm
- Discord: Any message from anyone triggers the alarm

---

## Features

- Multi-rule support per app (multiple senders and keywords)
- Fast and reliable notification listener
- Simple rule manager (add, edit, delete rules)
- App search for easier rule assignment

---

## Tech Stack

- **Language**: Java  
- **Core APIs**: Notification Listener, AlarmManager  
- **Storage**: SharedPreferences  
- **UI**: ListAdapter, Dialogs

---

## Permissions

- Requires **Notification Access**
- All data is stored locally — no internet usage or tracking

---

## Developer Note

This was a challenging and rewarding project to build — designed to notify you when it truly matters.
