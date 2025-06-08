import axios from 'axios';
import router from '@/router';
import store from '@/store';

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
                        sessionStorage.setItem('jwtRefresh', response.data.refreshAfter);
                }
        } catch (error) {
                console.error('Token refresh failed:', error);
        }
}

const instance = axios.create({
        baseURL: (import.meta.env.VITE_API_BASE_URL ?? "") + "/api/",
        withCredentials: true
});


instance.interceptors.request.use(async config => {
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
		
                sessionStorage.removeItem('username');
                sessionStorage.removeItem('jwtRefresh');
                store.dispatch('logout');
                if (router.currentRoute.value.path !== '/login') {
                        router.push({ path: '/login' });
                }
	
	}
	return Promise.reject(error);
});

export default instance;
