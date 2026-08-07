import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for repeating-class (lecture series) API calls.
 */

/** Teacher: creates a repeating series; occurrences are scheduled at once. */
export const createSeries = (payload) => {
  return apiClient.post(API_PATHS.LECTURE_SERIES, payload);
};

/** Teacher: fetches own active series. */
export const getMySeries = () => {
  return apiClient.get(API_PATHS.LECTURE_SERIES_ME);
};

/** Teacher: stops a series and cancels its future classes. */
export const stopSeries = (id) => {
  return apiClient.patch(`${API_PATHS.LECTURE_SERIES}/${id}/stop`);
};

export const lectureSeriesService = {
  createSeries,
  getMySeries,
  stopSeries,
};
