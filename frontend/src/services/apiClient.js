import axios from 'axios';
import { API_BASE_URL, API_TIMEOUT_MS } from '../constants/appConfig';

/**
 * The single, shared Axios instance used for ALL backend communication.
 *
 * Creating one configured client (rather than calling axios directly in each
 * component) gives us one place to define the base URL, timeout, default
 * headers, and — most importantly — request/response interceptors that apply
 * to every call in the application.
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT_MS,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

/**
 * REQUEST INTERCEPTOR
 *
 * Runs before every outgoing request. This is where the JWT will be attached
 * once authentication is implemented: read the token from storage and set the
 * Authorization header. Structured now so the auth module only fills in the
 * token source — the wiring already exists.
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * RESPONSE INTERCEPTOR
 *
 * Runs after every response. Two responsibilities:
 *
 * 1. SUCCESS PATH — the backend wraps every payload in the ApiResponse
 *    envelope: { success, message, data, timestamp }. We unwrap it here and
 *    hand callers the `data` directly, plus the `message`, so components work
 *    with clean payloads instead of repeatedly digging into `.data.data`.
 *
 * 2. ERROR PATH — the backend returns a consistent ErrorResponse envelope.
 *    We normalize any failure (server error, network failure, timeout) into a
 *    single predictable shape so callers always catch the same structure.
 */
apiClient.interceptors.response.use(
  (response) => {
    const envelope = response.data;

    // Return the meaningful parts of the envelope to the caller.
    return {
      data: envelope?.data ?? null,
      message: envelope?.message ?? '',
      success: envelope?.success ?? true,
    };
  },
  (error) => {
    // The request reached the server, which replied with an error envelope.
    if (error.response) {
      const envelope = error.response.data;
      return Promise.reject({
        success: false,
        status: error.response.status,
        message: envelope?.message ?? 'An unexpected error occurred.',
        error: envelope?.error ?? 'UNKNOWN_ERROR',
        details: envelope?.details ?? null,
      });
    }

    // The request was made but no response arrived (server down, CORS, timeout).
    if (error.request) {
      return Promise.reject({
        success: false,
        status: 0,
        message:
          'Unable to reach the server. Please check your connection and try again.',
        error: 'NETWORK_ERROR',
        details: null,
      });
    }

    // Something failed while setting up the request.
    return Promise.reject({
      success: false,
      status: 0,
      message: error.message ?? 'Request could not be sent.',
      error: 'REQUEST_SETUP_ERROR',
      details: null,
    });
  }
);

export default apiClient;