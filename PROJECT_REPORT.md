# Institute Workforce Tracking System — Project Report

*A plain-language guide to what this system is, who uses it, and everything it can do.*

---

## 1. What is this project?

This is a **staff attendance and workforce management system** built for an institute (school/college/coaching center). Think of it as a digital replacement for the attendance register, the leave application file, the lecture timetable board, and the notice board — all rolled into one website that works on both computers and phones.

It answers everyday questions like:

- *Who is working right now? Who is on a break? Who has left for the day?*
- *How many hours did each person actually work this month?*
- *Who has applied for leave, and has it been approved?*
- *Which teacher has a lecture going on right now?*

---

## 2. Who uses it? (Roles)

Every person who logs in has exactly **one role**, and the role decides what they can see and do:

| Role | Who they are | What they can do |
|------|--------------|------------------|
| **Super Admin** | The owner/head of the system (only one is created when the system is first installed) | Everything below, **plus**: approve or reject new people joining the system, create/deactivate user accounts, and change anyone's role |
| **Admin** | HR / office manager | See the live dashboard, view everyone's attendance, approve/reject leave requests, manage lecture schedules, download monthly reports |
| **Teacher** | Teaching staff | Everything an Employee can do, **plus**: see their own lecture schedule and manage their lectures |
| **Employee** | Regular staff | Track their own working day (check in, breaks, clock out), apply for leave, see their own history and notifications |

**Key principle:** nobody can be added to the system by just anyone. Only the Super Admin exists at the start — everyone else must **register and be approved** (see next section).

---

## 3. How does someone join the system? (Registration & Approval)

1. A new person opens the website and clicks **"Request one"** on the login page.
2. They fill in: their name, email, a password of their choice, an optional phone number, and the role they are asking for (Employee, Teacher, or Admin — nobody can request Super Admin).
3. Their request goes into a **waiting list**. They cannot log in yet.
4. The **Super Admin instantly gets notified** — a notification bell alert in the app, a push notification on their phone, and an email.
5. The Super Admin opens the **Registrations** page and either:
   - **Approves** the request (and can change the role — e.g., someone asked for Admin but is given Employee), or
   - **Rejects** it (optionally with a reason).
6. Only after approval does a real account come into existence. The person can now log in with the email and password they chose at signup.

This means the Super Admin has full control over who enters the system, without ever having to create accounts by hand.

---

## 4. The working day (Attendance)

This is the heart of the system. The rules are designed so that working time is recorded **honestly and automatically**, with minimal button-pressing.

### Starting the day
- Logging in does **not** start the working day. The person presses the **Check In** button on their attendance page when they actually begin work. Status: **WORKING**.

### Breaks
- A person can press **Start Break** anytime (lunch, tea, etc.). Status: **ON BREAK**.
- Pressing **Resume Work** ends the break. Break time is subtracted from working hours.

### Ending the day
- Pressing **Logout** ends the day properly — the system records the time and calculates total working hours. Status: **CHECKED OUT**.
- Changed your mind or came back? A **Check In Again** button reopens the day. The time you were away is honestly counted as a break, not as work.

### What if someone just closes the browser without logging out?
This is where the system is smart:

1. Closing the browser does **not** count as leaving. Instead, after about **1½ minutes** of being disconnected, the system automatically puts the person **on a break** (and notifies them).
2. If they come back, they simply press **Resume Work** — no harm done.
3. But if that automatic break drags on beyond **30 minutes**, the system assumes they left for the day and **checks them out automatically** (and notifies them).

So: **only pressing Logout ends your day by choice** — but you can't leave your status as "working" all night by just closing the laptop lid. Manual breaks (pressed by the person) have no time limit — only the automatic ones do.

### What gets recorded each day
For every person, every day: login time, logout time, every break (with duration), total break minutes, total working minutes, and a status. Days on approved leave are marked **ON LEAVE** automatically.

---

## 5. Leave management

- Any employee applies for leave from their **My Leaves** page: a date range and a reason.
- Everyone has a yearly allowance (default: **24 days**) — the system shows how many days remain and refuses applications that exceed the balance.
- Admins see all pending requests on the **Leaves** admin page and approve or reject them (with an optional comment).
- The employee is **notified of the decision** (in-app + phone push).
- Approved leave days are automatically marked in the attendance records, so reports stay accurate.

---

## 6. Lectures (for Teachers)

- Admins schedule lectures: subject, class, teacher, date, start and end time.
- The system moves each lecture through its life automatically: **Scheduled → Live → Completed** — no one has to press anything when a lecture starts or ends.
- A teacher can **extend** a running lecture if it's overshooting.
- Teachers get a **reminder notification shortly before their lecture ends**.
- Teachers see their own schedule on **My Lectures**; admins see and manage everything on the admin **Lectures** page.

---

## 7. Notifications — in the app and on your phone

Whenever something relevant happens, the person concerned is told about it through **up to three channels at once**:

1. **The bell icon** in the app — a badge shows unread count; clicking it lists recent notifications.
2. **Phone/desktop push notifications** — after a one-time "Enable notifications on this device" tap in the bell menu, notifications appear on the phone's lock screen **even when the browser is completely closed** (the website can also be installed on the phone like an app).
3. **Email** — currently used to alert the Super Admin about new registration requests.

Things that trigger notifications: a new registration request (→ Super Admin), leave approved/rejected (→ the employee), lecture ending soon (→ the teacher), put on automatic break (→ the employee), automatically checked out (→ the employee).

---

## 8. The live dashboard (Admins)

A control-room view that updates **by itself every few seconds** — no refreshing needed:

- Total active staff
- How many are **online right now** (working or on break)
- How many are working / on break / checked out
- Who is on approved leave today
- Who is absent (no sign-in today)
- How many lectures are **live at this moment**

---

## 9. Reports (Admins)

For any chosen month, the system produces:

- **Attendance report** — per person: days present, days on leave, total hours worked, total break time.
- **Teaching report** — per teacher: lectures taken and total teaching minutes.

Both can be **downloaded as spreadsheet (CSV) files** for record-keeping or salary calculations.

---

## 10. User management (Super Admin)

- A **Users** page lists everyone with their role and status.
- The Super Admin can **deactivate** an account (the person can no longer log in — but all their history is preserved) and reactivate it later.
- Roles and names can be edited at any time.

---

## 11. How is it kept secure?

- **Passwords are never stored as readable text.** They are scrambled with a one-way process (bcrypt) — even the database administrator cannot read anyone's password.
- After login, the browser holds a **signed digital pass (JWT token)** that expires after an hour — every request is checked against it.
- Every action is **checked on the server** against the person's role. Hiding a button in the browser is cosmetic; the real locks are on the server.
- Nobody can self-assign a powerful role — role grants go through the Super Admin.

---

## 12. What is it built with? (in plain words)

| Part | Technology | What it means |
|------|-----------|----------------|
| The website you see | **React** (JavaScript) with Material UI | A modern, fast, phone-friendly interface |
| The brain on the server | **Java + Spring Boot** | An industry-standard, reliable server framework |
| The memory | **MySQL database** | All records stored in structured tables |
| Live updates | **WebSockets** | The dashboard and bell update instantly without refreshing |
| Phone notifications | **Web Push + service worker** | Notifications reach the phone even with the browser closed |
| Emails | **Gmail SMTP** | The system sends emails through a configured Gmail account |

The system also runs small **background jobs** every minute — these are what start lectures on time, put disconnected people on break, and enforce the 30-minute rule, all without any human involvement.

---

## 13. A day in the life (example)

> **9:02** — Priya opens the site, logs in, and presses *Check In* to start her day.
> **11:30** — She presses *Start Break* for tea; back at **11:45**, presses *Resume Work*.
> **13:00** — Her laptop battery dies mid-work. A minute and a half later the system quietly puts her *On Break* and sends a notification to her phone.
> **13:20** — She's back on a charger, opens the site, presses *Resume Work*.
> **15:00** — She applies for leave next Friday. Her admin gets it in their queue.
> **17:15** — The admin approves. Priya's phone buzzes: *"Your leave request was approved."* Friday is automatically marked as leave.
> **18:05** — She presses *Logout*. Day recorded: 8 h 18 m worked, 47 m of breaks.

Meanwhile the Super Admin approved one new Teacher registration, and the dashboard showed the whole picture live, all day.

---

## 14. What needs to be set up to run it (one-time)

1. **MySQL** running with the configured password.
2. **Super Admin details** (email/password) in the settings file — the account is created automatically on first start.
3. **Gmail App Password** in settings — for registration alert emails.
4. **Push keys** (already generated) in settings — for phone notifications.
5. In production: the site must be served over **HTTPS** for phone push notifications to work, and secrets should move into environment variables.

---

*Report generated on 16 July 2026.*
