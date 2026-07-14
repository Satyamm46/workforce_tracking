import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for user-management API calls.
 *
 * Groups every /v1/users request behind named functions. The apiClient
 * request interceptor attaches the JWT automatically, and its response
 * interceptor unwraps the ApiResponse envelope — so each function resolves to
 * { data, message, success }.
 */

/**
 * Fetches a page of users.
 *
 * @param {number} page zero-based page number
 * @param {number} size page size
 * @returns {Promise<{ data: { content: object[], page: number, size: number,
 *   totalElements: number, totalPages: number, first: boolean, last: boolean },
 *   message: string, success: boolean }>}
 */
export const getUsers = (page = 0, size = 10) => {
  return apiClient.get(API_PATHS.USERS, { params: { page, size } });
};

/** Fetches a single user by id. */
export const getUserById = (id) => {
  return apiClient.get(`${API_PATHS.USERS}/${id}`);
};

/** Creates a new user. */
export const createUser = (payload) => {
  return apiClient.post(API_PATHS.USERS, payload);
};

/** Updates a user's name and role. */
export const updateUser = (id, payload) => {
  return apiClient.put(`${API_PATHS.USERS}/${id}`, payload);
};

/** Deactivates a user (soft delete). */
export const deactivateUser = (id) => {
  return apiClient.patch(`${API_PATHS.USERS}/${id}/deactivate`);
};

/** Reactivates a user. */
export const activateUser = (id) => {
  return apiClient.patch(`${API_PATHS.USERS}/${id}/activate`);
};

export const userService = {
  getUsers,
  getUserById,
  createUser,
  updateUser,
  deactivateUser,
  activateUser,
};