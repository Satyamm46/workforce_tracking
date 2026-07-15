import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for lecture-scheduling API calls.
 */

/** Teacher: schedules a new lecture. */
export const scheduleLecture = (payload) => {
  return apiClient.post(API_PATHS.LECTURES, payload);
};

/** Teacher: fetches a page of own lectures from today onward. */
export const getMyUpcomingLectures = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.LECTURES_ME, { params: { page, size } });
};

/** Teacher: cancels one of their own scheduled lectures. */
export const cancelLecture = (id) => {
  return apiClient.patch(`${API_PATHS.LECTURES}/${id}/cancel`);
};

/** Admin: fetches a page of all lectures for a date (null = today). */
export const getLecturesByDate = (date = null, page = 0, size = 20) => {
  const params = { page, size };
  if (date) {
    params.date = date;
  }
  return apiClient.get(API_PATHS.LECTURES, { params });
};

export const lectureService = {
  scheduleLecture,
  getMyUpcomingLectures,
  cancelLecture,
  getLecturesByDate,
};

/** Teacher: ends one of their own live lectures. */
export const endLecture = (id) => {
  return apiClient.patch(`${API_PATHS.LECTURES}/${id}/end`);
};

/** Teacher: extends one of their own live lectures by N minutes. */
export const extendLecture = (id, minutes) => {
  return apiClient.patch(`${API_PATHS.LECTURES}/${id}/extend`, { minutes });
};

