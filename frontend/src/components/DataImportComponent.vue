<template>
  <div class="main">
    <div class="content">
      <div><h1>Data Import</h1></div>
      <div v-if="statusMessage">{{ statusMessage }}</div>
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div v-if="showImportStatus">{{ importStatusMessage }}</div>
      <div>
        <input type="file" @change="handleFileUpload" />
        <input v-model="importPassword" type="password" placeholder="Import Password">
        <input v-model="importPasswordConfirm" type="password" placeholder="Import Password (Confirm)">
        <button class="actionbutton" @click="importData" :disabled="disableImport">Import</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../axios';
import { mapActions } from 'vuex';

export default {
  name: 'DataImportComponent',
  data() {
    return {
      importPassword: '',
      importPasswordConfirm: '',
      fileData: null,
      errorMessage: '',
      statusMessage: '',
      importStatusMessage: '',
      showImportStatus: false,
      statusIntervalId: null,
      importStatus: '',
      importStarted: false
    };
  },
  computed: {
    disableImport() {
      return this.importStatus === 'RUNNING' || !this.fileData;
    }
  },
  methods: {
    ...mapActions(['logout']),
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (!file) {
        this.fileData = null;
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        const arrayBuffer = reader.result;
        const bytes = new Uint8Array(arrayBuffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        this.fileData = btoa(binary);
      };
      reader.readAsArrayBuffer(file);
    },
    async importData() {
      this.statusMessage = '';
      this.errorMessage = '';
      if (this.importPassword !== this.importPasswordConfirm) {
        this.errorMessage = 'Passwords do not match!';
        return;
      }
      if (!this.fileData) {
        this.errorMessage = 'Please select a file to import.';
        return;
      }
      const confirmed = window.confirm(
        'Importing will delete all existing data and replace it with the imported data. After a successful import you will be logged out. Continue?'
      );
      if (!confirmed) {
        return;
      }
      try {
        await axios.post('/dataimport/import', {
          password: this.importPassword,
          data: this.fileData
        });
        this.importStarted = true;
        this.importStatusMessage = 'Import started...';
        this.showImportStatus = true;
        this.startStatusInterval();
      } catch (error) {
        this.setErrorMessage(error, 'Error starting import. Please try again later.');
      }
    },
    async checkImportStatus() {
      try {
        const response = await axios.get('/dataimport/status');
        this.importStatus = response.data.status;
        const backendMsg = response.data.message ? response.data.message : '';
        if (this.importStatus === 'RUNNING') {
          this.importStatusMessage = backendMsg || 'Import is running...';
          this.showImportStatus = true;
          this.startStatusInterval();
        } else {
          this.clearStatusInterval();
          this.showImportStatus = false;
          if (this.importStatus === 'PASSWORD_ERROR') {
            this.errorMessage = 'The data import failed because of an invalid import password.';
            this.importStarted = false;
          } else if (this.importStatus === 'FAILED') {
            this.errorMessage = `${backendMsg} Please start the import again.`.trim();
            this.importStarted = false;
          } else if (this.importStatus === 'SUCCESS' && this.importStarted) {
            this.statusMessage = 'Data imported successfully. Logging out in 30 seconds.';
            setTimeout(this.performLogout, 30000);
          }
        }
      } catch (error) {
        this.clearStatusInterval();
        this.showImportStatus = false;
      }
    },
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
    async performLogout() {
      try {
        await axios.get('/auth/logoutuser');
      } catch (error) {
        console.error('logout request failed', error);
      }
      this.$store.dispatch('logout');
      sessionStorage.removeItem('jwt-token');
      sessionStorage.removeItem('username');
      sessionStorage.removeItem('jwtRefresh');
      this.$router.push('/login');
    },
    setErrorMessage(error, defaultMessage) {
      if (error.response != null && error.response.data != null && error.response.data.message != null) {
        this.errorMessage = error.response.data.message;
      } else {
        this.errorMessage = defaultMessage;
      }
    }
  },
  async mounted() {
    await this.checkImportStatus();
  },
  beforeUnmount() {
    this.clearStatusInterval();
  }
};
</script>

