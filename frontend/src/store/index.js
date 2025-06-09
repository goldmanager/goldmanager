import { createStore } from 'vuex'; // Import createStore from 'vuex'

export default createStore({
  state: {
    isAuthenticated: !!sessionStorage.getItem('username') // Initialize the authentication status
  },
  mutations: {
    setAuthStatus(state, status) {
      state.isAuthenticated = status; // Set the authentication status
    }
  },
  actions: {
    login({ commit }) {
      commit('setAuthStatus', true); // Set authentication status to true
    },
    logout({ commit }) {
      commit('setAuthStatus', false); // Set authentication status to false
    }
  },
  getters: {
    isAuthenticated: state => state.isAuthenticated // Getter for the authentication status
  }
});
