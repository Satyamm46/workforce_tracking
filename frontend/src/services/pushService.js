import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Web Push subscription flow. The service worker (public/sw.js) displays the
 * notifications; this module handles permission, browser subscription, and
 * registering the subscription with the backend.
 */

/** Whether this browser supports service workers + push at all. */
export const isPushSupported = () =>
  'serviceWorker' in navigator && 'PushManager' in window && 'Notification' in window;

/** Registers the service worker (idempotent). Called once at startup. */
export const registerServiceWorker = async () => {
  if (!('serviceWorker' in navigator)) {
    return null;
  }
  return navigator.serviceWorker.register('/sw.js');
};

/** Converts a URL-safe base64 VAPID key to the Uint8Array subscribe() needs. */
const urlBase64ToUint8Array = (base64String) => {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = window.atob(base64);
  return Uint8Array.from([...raw].map((ch) => ch.charCodeAt(0)));
};

/**
 * Full enable flow: ask permission, subscribe this browser with the server's
 * VAPID key, and register the subscription with the backend.
 * Throws with a readable message when any step is refused/unavailable.
 */
export const enablePush = async () => {
  if (!isPushSupported()) {
    throw new Error('This browser does not support push notifications.');
  }

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    throw new Error('Notification permission was not granted.');
  }

  const { data: publicKey } = await apiClient.get(API_PATHS.PUSH_PUBLIC_KEY);
  if (!publicKey) {
    throw new Error('Push is not configured on the server yet.');
  }

  const registration = await navigator.serviceWorker.ready;
  const subscription = await registration.pushManager.subscribe({
    userVisibleOnly: true,
    applicationServerKey: urlBase64ToUint8Array(publicKey),
  });

  const json = subscription.toJSON();
  await apiClient.post(API_PATHS.PUSH_SUBSCRIBE, {
    endpoint: json.endpoint,
    p256dh: json.keys.p256dh,
    auth: json.keys.auth,
  });

  return subscription;
};

/** Whether this browser already has an active push subscription. */
export const isSubscribed = async () => {
  if (!isPushSupported()) {
    return false;
  }
  const registration = await navigator.serviceWorker.ready;
  const subscription = await registration.pushManager.getSubscription();
  return subscription !== null;
};

export const pushService = {
  isPushSupported,
  registerServiceWorker,
  enablePush,
  isSubscribed,
};
