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

async function refreshToken(oldToken) {
	try {
                const response = await axios.get((import.meta.env.VITE_API_BASE_URL ?? "") + "/api/auth/refresh", {
			headers: {
				'Authorization': `Bearer ${oldToken}`
			},
			withCredentials: true
		});
		const newToken = response.data.token;
	
		if (newToken) {
			sessionStorage.setItem('jwt-token', newToken);
			sessionStorage.setItem('jwtRefresh', response.data.refreshAfter);
			return newToken;
		}
	} catch (error) {
		console.error('Token refresh failed:', error);
		return null;
	}
}

const instance = axios.create({
        baseURL: (import.meta.env.VITE_API_BASE_URL ?? "") + "/api/",
	withCredentials: true
});


instance.interceptors.request.use(async config => {
	let token = sessionStorage.getItem('jwt-token');

	if (token && isTokenExpiringSoon()) {
		token = await refreshToken(token);
	}

	if (token) {
		config.headers['Authorization'] = `Bearer ${token}`;
	}

	return config;
}, error => {
	return Promise.reject(error);
});


instance.interceptors.response.use(response => {

	return response;
}, error => {

	if (error.response && error.response.status === 403) {
		
		sessionStorage.removeItem('jwt-token');
		sessionStorage.removeItem('username');
		sessionStorage.removeItem('jwtRefresh');
		store.dispatch('logout');
		if (router.currentRoute.path !== '/login') {
			router.push({ path: '/login' });
		}
	
	}
	return Promise.reject(error);
});

export default instance;