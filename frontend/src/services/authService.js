import apiClient from './apiClient';
import { API_PATHS } from '../constants/apiPaths';

/**
 * Service module for authentication API calls.
 *
 * Groups every auth-related request behind named functions. Components and
 * context never call apiClient directly for auth — they call these. The
 * apiClient response interceptor has already unwrapped the ApiResponse
 * envelope, so each function resolves to { data, message, success }.
 */

/**
 * Logs in with email + password.
 *
 * @param {{ email: string, password: string }} credentials
 * @returns {Promise<{ data: { accessToken: string, tokenType: string, user: object },
 *                     message: string, success: boolean }>}
 */
export const login = (credentials) => {
  return apiClient.post(API_PATHS.AUTH_LOGIN, credentials);
};

/**
 * Fetches the currently authenticated user. The JWT is attached automatically
 * by the apiClient request interceptor, so no token is passed here.
 *
 * @returns {Promise<{ data: object, message: string, success: boolean }>}
 */
export const getCurrentUser = () => {
  return apiClient.get(API_PATHS.AUTH_ME);
};

/**
 * Grouped export for namespace-style imports:
 *   import { authService } from '../services/authService';
 *   authService.login(...)
 */
export const authService = {
  login,
  getCurrentUser,
};