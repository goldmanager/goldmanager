import axios from 'axios';
import router from '@/router';
import store from '@/store';
import { saveSession, clearSession, getCurrentUsername } from '@/utils/session';

function getCookie(name) {
        const match = document.cookie.match(new RegExp('(^|;\\s*)' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[2]) : null;
}

function isTokenExpiringSoon() {
        const refresh = sessionStorage.getItem('jwtRefresh');
        if (!refresh) return false;

        const refreshDate = new Date(refresh);
        const now = new Date();
        return now >= refreshDate;
}

async function refreshToken() {
        try {
                const response = await axios.get((import.meta.env.VITE_API_BASE_URL ?? "") + "/api/auth/refresh", {
                        withCredentials: true
                });

                if (response.data.refreshAfter) {
                        saveSession(getCurrentUsername(), response.data.refreshAfter);
                }
        } catch (error) {
                console.error('Token refresh failed:', error);
        }
}

const instance = axios.create({
        baseURL: (import.meta.env.VITE_API_BASE_URL ?? "") + "/api/",
        withCredentials: true,
        xsrfCookieName: 'XSRF-TOKEN',
        xsrfHeaderName: 'X-XSRF-TOKEN'
});


instance.interceptors.request.use(async config => {
        if (['post', 'put', 'delete', 'patch'].includes(config.method)) {
                if (!getCookie('XSRF-TOKEN')) {
                        await axios.get((import.meta.env.VITE_API_BASE_URL ?? "") + "/api/auth/csrf", { withCredentials: true });
                }
                const token = getCookie('XSRF-TOKEN');
                if (token) {
                        config.headers['X-XSRF-TOKEN'] = token;
                }
        }
        if (isTokenExpiringSoon()) {
                await refreshToken();
        }

        return config;
}, error => {
        return Promise.reject(error);
});


instance.interceptors.response.use(response => {

	return response;
}, error => {

	if (error.response && error.response.status === 403) {
		
                clearSession();
                store.dispatch('logout');
                if (router.currentRoute.value.path !== '/login') {
                        router.push({ path: '/login' });
                }
	
	}
	return Promise.reject(error);
});

export default instance;
