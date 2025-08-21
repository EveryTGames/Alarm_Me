# Alarm Me

**Alarm Me** is an intelligent, rule-based alarm app for Android that wakes you up or alerts you only when it truly matters.  
Unlike traditional alarms that rely on fixed times, Alarm Me listens to your notifications and activates based on the content, sender, and app — all customizable by you.

---

## How It Works

You create rules that define when an alarm should trigger, based on:

- **Apps**: Choose from any installed app like WhatsApp, Messenger, Discord, etc.
- **Senders**: Specify exact names, or allow all
- **Keywords**: Trigger on certain words or phrases
- **Mode**: Choose between AND (both must match) or OR (either matches)

When a notification matches your rule, the alarm activates immediately.

---

## Example Use Cases

- **WhatsApp**: Alarm rings only if "John" sends a message containing "quiz"
- **Messenger**: Any sender using `#urgent` in their message triggers an alarm
- **Discord**: All messages, regardless of sender, activate the alarm

---

## Features

- 🔹 Smart Notification Triggering  
- 🔹 Customizable Rules per App  
- 🔹 Multiple Rules Supported  
- 🔹 Deep Sleep Mode Toggle  
- 🔹 Real-Time Alarm Activation  
- 🔹 Modern, Responsive UI  
- 🔹 Live Rule Editing and Management  
- 🔹 Minimal Battery Usage  

---

## Architecture & Tech Stack

- **Architecture**: MVVM (Model-View-ViewModel)
- **Language**: Java
- **Persistence**: Room Database + SharedPreferences
- **Core APIs**:
  - `NotificationListenerService`
  - `AlarmManager`
  - `WorkManager` (for reliability)
- **UI**:
  - Material Components
  - LiveData & ViewModel
  - ListAdapter + Custom Dialogs

---

## Permissions Required

- **Notification Access**: To read and evaluate incoming notifications
- **Overlay Permission (optional)**: For advanced features like floating controls

> All data stays on your device. Alarm Me does not connect to the internet, nor does it track or upload any user data.

---

## What's New

- 🌟 Rewritten using MVVM architecture for better maintainability and scalability  
- 🌟 Migrated from SharedPreferences to Room Database for robust data handling  
- 🌟 Improved UI with modern design and better accessibility  
- 🌟 Optimized performance and responsiveness  
- 🌟 Clean, thread-safe background operations using proper threading practices  

---

## Developer Note

Alarm Me started as a simple idea but evolved into a full-featured, event-driven alarm app.  
It's built with care, a lot of late nights, and a genuine desire to solve a real problem.

If you’ve ever slept through an important message, you’ll understand the motivation behind this.

Built solo, refined over time — and always improving.
