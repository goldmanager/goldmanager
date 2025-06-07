<template>
  <div class="main">
    <div class="content">
      <div><h1>Data Export</h1></div>
      <div v-if="statusMessage">{{ statusMessage }}</div>
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div v-if="showExportStatus">{{ exportStatusMessage }}</div>
      <div>
        <input v-model="exportPassword" type="password" placeholder="Export Password">
        <input v-model="exportPasswordConfirm" type="password" placeholder="Export Password (Confirm)">
        <button :class="getExportButtonClass" @click="exportData" :disabled="disableExport">Export</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../axios';

export default {
  name: 'DataExportComponent',
  data() {
    return {
      exportPassword: '',
      exportPasswordConfirm: '',
      errorMessage: '',
      statusMessage: '',
      exportStatusMessage: '',
      showExportStatus: false,
      statusIntervalId: null,
      exportStatus: '',
      exportStarted: false
    };
  },
  computed: {
    disableExport() {
      return this.exportStatus === 'RUNNING' || this.exportStarted;
    }
  },
  methods: {
    async exportData() {
      this.statusMessage = '';
      this.errorMessage = '';
      if (this.exportPassword !== this.exportPasswordConfirm) {
        this.errorMessage = 'Passwords do not match!';
        return;
      }
      try {
        await axios.post('/dataexport/export', {
          password: this.exportPassword
        });
        this.exportStarted = true;
        this.exportStatusMessage = 'Export started...';
        this.showExportStatus = true;
        this.startStatusInterval();
      } catch (error) {
        this.exportStarted=false;
        this.setErrorMessage(error, 'Error exporting data. Please try again later.');
      }
    },
    async checkExportStatus() {
      try {
        const response = await axios.get('/dataexport/status');
        this.exportStatus = response.data.status;
        const backendMsg = response.data.message ? response.data.message : '';
        if (this.exportStatus === 'RUNNING') {
          this.exportStatusMessage = backendMsg || 'Export is running...';
          this.showExportStatus = true;
          this.startStatusInterval();
        } else {
          this.clearStatusInterval();
          this.showExportStatus = false;
          if (this.exportStatus === 'PASSWORD_ERROR') {
            this.errorMessage = 'The data export failed because of an invalid export password.';
            this.exportStarted = false;
          } else if (this.exportStatus === 'FAILED') {
            this.errorMessage = `${backendMsg} Please start the export again.`.trim();
            this.exportStarted = false;
          } else if (this.exportStatus === 'SUCCESS' && this.exportStarted) {
            await this.downloadExport();
            this.exportStarted = false;
          }
        }
      } catch (error) {
        this.clearStatusInterval();
        this.showExportStatus = false;
      }
    },
    async downloadExport() {
      try {
        const response = await axios.get('/dataexport/download', { responseType: 'blob' });
        if (response.data) {
          const blob = new Blob([response.data], { type: 'application/zip' });
          await this.saveFile(blob, 'goldmanager-export.zip');
          this.statusMessage = 'Data exported successfully.';
        }
      } catch (error) {
        this.setErrorMessage(error, 'Error downloading export data.');
      }
    },

    async saveFile(blob, filename) {
      if (window.showSaveFilePicker) {
        try {
          const handle = await window.showSaveFilePicker({
            suggestedName: filename,
            types: [{ description: 'ZIP file', accept: { 'application/zip': ['.zip'] } }]
          });
          const writable = await handle.createWritable();
          await writable.write(blob);
          await writable.close();
        } catch (err) {
          if (err.name !== 'AbortError') {
            throw err;
          }
        }
      } else {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    },
    startStatusInterval() {
      if (this.statusIntervalId == null) {
        this.statusIntervalId = setInterval(this.checkExportStatus, 5000);
      }
    },
    clearStatusInterval() {
      if (this.statusIntervalId != null) {
        clearInterval(this.statusIntervalId);
        this.statusIntervalId = null;
      }
    },
    getExportButtonClass() {
      return this.disableExport ? 'actionbutton_disabled' : 'actionbutton';
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
    await this.checkExportStatus();
  },
  beforeUnmount() {
    this.clearStatusInterval();
  }
};
</script>

