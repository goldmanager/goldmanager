<template>
  <div id="app">
    <NavBar /> <!-- Add the navigation menu -->
    <router-view></router-view> <!-- Displays the content of the current route -->
  </div>
</template>

<script>
import NavBar from './components/NavBar.vue'; // Import the NavBar component

export default {
  name: 'App',
  components: {
    NavBar // Register the NavBar component
  }
};
</script>

<style>
.main {
  background-image: url('@/assets/logo.png'); /* Use the logo as background */
  background-size: cover;
  background-position: center; /* Center the image */
  width: 100%; /* Width of the form */
  min-height: 100vh;
  height: 100%; /* Height according to the screen size */
  display: flex;
  justify-content: center;
  align-items: center;
}

.content {
  background: rgba(255, 255, 255, 0.8); /* Slightly transparent white background for the form */
  padding: 10px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);

}
select{
  margin: 10px 0;
  display: block;
  width: 90%;
  padding: 10px;
}
input[type="text"], input[type="password"], input[type="number"] , input[type="datetime-local"]{
  margin: 10px 0;
  display: block;
  width: 90%;
  padding: 10px;
}

.loginbutton {
  width: 100%;
  margin-left: 0rem;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  cursor: pointer;
  display: block;
}
.actionbutton{
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  border: 2px;
  box-shadow: 0 5px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.pagingButton{
  padding: 2px;
  background-color: rgba(255, 255, 255, 0.8);
  border: 2px;
  box-shadow: 0 5px 8px rgba(0, 0, 0, 0.1);
  margin-right: 6px;
  cursor: pointer;
}
.pagingButton_disabled{
  padding: 2px;
  background-color: rgba(255, 255, 255, 0.8);
  border: 2px;
  box-shadow: 0 5px 8px rgba(0, 0, 0, 0.1);
  margin-right: 6px;
  cursor: not-allowed;
}
.actionbutton_disabled{
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  border: 2px;
  box-shadow: 0 5px 8px rgba(0, 0, 0, 0.1);
 
  cursor: not-allowed;

}
.highlight_saved{
	background-color: green;
	transition: background-color 0.5s ease;
}
.highlight_deleted{
	background-color: blue;
	transition: background-color 0.5s ease;
}
.highlight_error{
	background-color: darkred;
	transition: background-color 0.5s ease;
}
.highlight_editmode{
	background-color: gold;
	transition: background-color 0.5s ease;
}
table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  border: 1px solid #ddd;
  padding: 8px;
}

th {
  background-color: #f2f2f2;
  text-align: left;
}

.error {
  color: red;
  margin-bottom: 1rem;
}

button {
  margin-left: 1rem;
}
</style>
