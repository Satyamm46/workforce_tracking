import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { API_BASE_URL } from '../constants/appConfig';
import { notificationService } from '../services/notificationService';
import { useAuth } from './AuthContext';

/** How many notifications to keep in the in-memory panel list. */
const PANEL_SIZE = 15;

/** The WebSocket endpoint, derived from the API base (http→ws). */
const WS_URL = API_BASE_URL.replace(/^http/, 'ws') + '/ws';

const NotificationContext = createContext(undefined);

/**
 * App-wide notification state: recent notifications, unread count, and the
 * live STOMP connection. Connects when a user is authenticated, disconnects
 * on logout, and merges pushed messages into state as they arrive.
 */
export const NotificationProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Initial load + live connection, tied to the auth lifecycle.
  useEffect(() => {
    if (!isAuthenticated) {
      setNotifications([]);
      setUnreadCount(0);
      return undefined;
    }

    // 1. Load current truth from REST.
    Promise.all([
      notificationService.getMyNotifications(0, PANEL_SIZE),
      notificationService.getMyUnreadCount(),
    ])
      .then(([listRes, countRes]) => {
        setNotifications(listRes.data.content);
        setUnreadCount(countRes.data);
      })
      .catch(() => {
        // Non-fatal: the bell simply starts empty.
      });

    // 2. Open the live channel.
    const client = new Client({
      brokerURL: WS_URL,
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/user/queue/notifications', (message) => {
          const notification = JSON.parse(message.body);
          setNotifications((prev) => [notification, ...prev].slice(0, PANEL_SIZE));
          setUnreadCount((prev) => prev + 1);
        });
      },
    });
    client.activate();

    // 3. Cleanup: close the socket when auth state changes or the app unmounts.
    return () => {
      client.deactivate();
    };
  }, [isAuthenticated]);

  const markAllRead = useCallback(async () => {
    await notificationService.markAllRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    setUnreadCount(0);
  }, []);

  const value = useMemo(
    () => ({ notifications, unreadCount, markAllRead }),
    [notifications, unreadCount, markAllRead]
  );

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};

/** The only supported way to read notification state. */
export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error('useNotifications must be used within a <NotificationProvider>.');
  }
  return context;
};
