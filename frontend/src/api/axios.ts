import axios from 'axios';
import Cookies from 'js-cookie';


export const api = axios.create({
    baseURL: '/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true'
    },
});

// Interceptor for adding JWT token to every request
api.interceptors.request.use(
    (config) => {
        // get cookie before every request
        const token = Cookies.get('jwt_token');

        // add to authorize header
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Interceptor if 401 Unauthorized
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isAuthRequest = error.config?.url?.includes('/auth/');

        if (error.response && error.response.status === 401 && !isAuthRequest) {
            Cookies.remove('jwt_token');
           // window.location.href = '/';
        }
        return Promise.reject(error);
    }
);