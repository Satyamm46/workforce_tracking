/**
 * Service worker: receives Web Push messages and shows OS notifications —
 * this is what makes notifications appear on the phone even when the app
 * (or the whole browser) is closed.
 */
self.addEventListener('push', (event) => {
  let payload = { title: 'Workforce Tracking', body: '' };
  try {
    payload = { ...payload, ...event.data.json() };
  } catch {
    payload.body = event.data ? event.data.text() : '';
  }

  event.waitUntil(
    self.registration.showNotification(payload.title, {
      body: payload.body,
      icon: '/favicon.svg',
      badge: '/favicon.svg',
    })
  );
});

/** Clicking the notification focuses the app (or opens it). */
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      const existing = windowClients.find((client) => 'focus' in client);
      if (existing) {
        return existing.focus();
      }
      return clients.openWindow('/');
    })
  );
});
