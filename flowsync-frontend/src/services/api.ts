import axios from 'axios';

const api = axios.create({
  baseURL: 'https://flowsync-miy6.onrender.com',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT token to every request if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('flowsync_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('flowsync_token');
      localStorage.removeItem('flowsync_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
