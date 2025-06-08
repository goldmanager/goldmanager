<template>
  <nav v-if="isAuthenticated">
    <router-link to="/">
      Prices
    </router-link>
    <router-link to="/priceHistory">
      Price History
    </router-link>
    <router-link to="/items">
      Items
    </router-link>
    <router-link to="/itemTypes">
      ItemTypes
    </router-link>
    <router-link to="/itemStorages">
      Item Storages
    </router-link>
    <router-link to="/metals">
      Metals
    </router-link>
    <router-link to="/units">
      Units
    </router-link>
    <router-link to="/users">
      Users
    </router-link>
    <router-link to="/dataexport">
      Data Export
    </router-link>
    <router-link to="/dataimport">
      Data Import
    </router-link>
    <button @click="logout">
      Logout
    </button>
  </nav>
  <nav v-else>
    <router-link to="/login">
      Login
    </router-link>
  </nav>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';
import axios from '../axios';

export default {
  name: 'NavBar',
  computed: {
    ...mapGetters(['isAuthenticated']) // Bind the getter for the authentication status
  },
  methods: {
     ...mapActions(['logout']),
    async logout() {
      try{
       await axios.get('/auth/logoutuser');
      }
      catch(error){
        console.error("logout request failed",error)
      }
      this.$store.dispatch('logout');
      sessionStorage.removeItem('username');
      sessionStorage.removeItem('jwtRefresh');
      this.$router.push('/login');
    }
  }
};
</script>

<style>
nav {
  background-color: #DBAC34;
  padding: 1rem;
}

nav a {
  margin: 0 1rem;
  color: white;
}

button {
  margin-left: 1rem;
}
</style>

