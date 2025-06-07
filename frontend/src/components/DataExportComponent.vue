<template>
  <div class="main">
    <div class="content">
      <div><h1>Data Export</h1></div>
      <div v-if="statusMessage">{{ statusMessage }}</div>
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div>
        <input v-model="exportPassword" type="password" placeholder="Export Password">
        <input v-model="exportPasswordConfirm" type="password" placeholder="Export Password (Confirm)">
        <button class="actionbutton" @click="exportData">Export</button>
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
      statusMessage: ''
    };
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
        const response = await axios.post('/dataexport/export', {
          password: this.exportPassword
        }, { responseType: 'blob' });
        const blob = new Blob([response.data], { type: 'application/zip' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'goldmanager-export.zip');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        this.statusMessage = 'Data exported successfully.';
      } catch (error) {
        this.setErrorMessage(error, 'Error exporting data. Please try again later.');
      }
    },
    setErrorMessage(error, defaultMessage) {
      if (error.response != null && error.response.data != null && error.response.data.message != null) {
        this.errorMessage = error.response.data.message;
      } else {
        this.errorMessage = defaultMessage;
      }
    }
  }
};
</script>

