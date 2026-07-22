import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for post-lecture summary API calls.
 */

/** Teacher: submits a summary for a specific completed lecture. */
export const submitSummary = (lectureId, payload) => {
  return apiClient.post(`${API_PATHS.LECTURE_SUMMARIES}/${lectureId}`, payload);
};

/** Teacher: fetches the summary for a specific lecture. */
export const getMySummaryForLecture = (lectureId) => {
  return apiClient.get(`${API_PATHS.LECTURE_SUMMARIES}/${lectureId}/me`);
};

/** Teacher: page of own summaries, newest first. */
export const getMySummaries = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.LECTURE_SUMMARIES_ME, { params: { page, size } });
};

/** Admin: all summaries for lectures on a given day (null = today). */
export const getSummariesByDate = (date = null, page = 0, size = 20) => {
  const params = { page, size };
  if (date) params.date = date;
  return apiClient.get(API_PATHS.LECTURE_SUMMARIES, { params });
};

/** Admin: all summaries whose lecture date falls within [from, to] — backs monthly export. */
export const getSummariesByRange = (from, to, page = 0, size = 100) => {
  return apiClient.get(API_PATHS.LECTURE_SUMMARIES, { params: { from, to, page, size } });
};

export const lectureSummaryService = {
  submitSummary,
  getMySummaryForLecture,
  getMySummaries,
  getSummariesByDate,
  getSummariesByRange,
};
