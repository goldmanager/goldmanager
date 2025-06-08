<template>
  <div class="main">
    <div>
      <div class="content">
        <template v-if="!showImportStatus">
          <h1>Login</h1>
          <form @submit.prevent="handleLogin">
            <input v-model="username" type="text" placeholder="Username" required />
            <input v-model="password" type="password" placeholder="Password" required />
            <button class="loginbutton" type="submit">Login</button>
          </form>
          <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
        </template>
        <template v-else>
          <h1>Import Status</h1>
          <p>{{ importStatusMessage }}</p>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../axios';
import { mapActions } from 'vuex';

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
      importStatus: '',
      importStatusMessage: '',
      showImportStatus: false,
      statusIntervalId: null
    };
  },
  methods: {
    ...mapActions(['login']), // Mappe die Vuex-Action zum `login`
    async handleLogin() {
      try {
        const response = await axios.post('/auth/login', {
          username: this.username,
          password: this.password
        });
		sessionStorage.setItem('jwtRefresh',response.data.refreshAfter);
        sessionStorage.setItem('jwt-token', response.data.token); 
        sessionStorage.setItem('username', this.username); 
        await this.$store.dispatch('login'); 
        this.$router.push('/'); 
      } catch (error) {
        this.errorMessage = 'Login failed. Please check your credentials.';
		console.error("error on login",error);
      }
    }
    ,
    startStatusInterval() {
      if (this.statusIntervalId == null) {
        this.statusIntervalId = setInterval(this.checkImportStatus, 5000);
      }
    },
    clearStatusInterval() {
      if (this.statusIntervalId != null) {
        clearInterval(this.statusIntervalId);
        this.statusIntervalId = null;
      }
    },
    async checkImportStatus() {
      try {
        const response = await axios.get('/dataimport/status');
        this.importStatus = response.data.status;

        if (this.importStatus === 'RUNNING') {
          this.importStatusMessage =
            'A data import is currently running. Login is not possible at the moment.';
          this.showImportStatus = true;
          this.startStatusInterval();
          this.errorMessage = '';
        } else {
          this.clearStatusInterval();
          this.showImportStatus = false;
          if (this.importStatus === 'PASSWORD_ERROR') {
            this.errorMessage =
              'The current data import failed because of an invalid import password. Please log in and restart the import.';
          } else if (this.importStatus === 'FAILED') {
            const backendMsg = response.data.message ? response.data.message + '. ' : '';
            this.errorMessage =
              backendMsg +
              'Try logging in and starting the data import again. If login fails, please restart the application.';
          } else {
            this.errorMessage = '';
          }
          this.importStatusMessage = response.data.message || '';
        }
      } catch (error) {
        this.clearStatusInterval();
        this.showImportStatus = false;
      }
    }

  },
  async mounted() {
    await this.checkImportStatus();
  }
  ,
  beforeUnmount() {
    this.clearStatusInterval();
  }
};
</script>
