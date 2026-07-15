import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/** Service module for notification REST calls (history and read state). */

export const getMyNotifications = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.NOTIFICATIONS_ME, { params: { page, size } });
};

export const getMyUnreadCount = () => {
  return apiClient.get(API_PATHS.NOTIFICATIONS_UNREAD_COUNT);
};

export const markAllRead = () => {
  return apiClient.patch(API_PATHS.NOTIFICATIONS_READ_ALL);
};

export const notificationService = {
  getMyNotifications,
  getMyUnreadCount,
  markAllRead,
};
