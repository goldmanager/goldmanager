<template>
  <div class="main">
    <div class="content">
      <div><h1>Users</h1></div>
      <div v-if="statusMessage">
        {{ statusMessage }}
      </div>
      <div
        v-if="errorMessage"
        class="error"
      >
        {{ errorMessage }}
      </div>
      <div>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search by user name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('username')">
              Name {{ currentSortDir === 'asc' ? '▲' : '▼' }}
            </th>
            <th>Password</th>
            <th>Password (Confirm)</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <input
                v-model="newUser.username"
                type="text"
                placeholder="Name"
              >
            </td>
            <td>
              <input
                v-model="newUserPasword"
                type="password"
                placeholder="Password"
              >
            </td>
            <td>
              <input
                v-model="newUserPaswordConfirm"
                type="password"
                placeholder="Password (Confirm)"
              >
            </td>

            <td>
              <button
                class="actionbutton"
                @click="addUser()"
              >
                Add New
              </button>
            </td>
          </tr>
          <tr
            v-for="userInfo in paginatedObjects"
            :key="userInfo.username"
            :class="getHighlightClass(userInfo.username)"
          >
            <td>
              <input
                v-model="userInfo.username"
                type="text"
                disabled="true/"
              >
            </td>
            <td>
              <input
                v-model="userInfo.password"
                type="password"
                placeholder="Password"
              >
            </td>
            <td>
              <input
                v-model="userInfo.passwordConfirm"
                type="password"
                placeholder="Password (Confirm)"
              >
            </td>
            <td>
              <button
                class="actionbutton"
                @click="updatePassword(userInfo)"
              >
                Update Password
              </button>
              <button
                :class="getButtonClass(isUserCurrentUser(userInfo.username))"
                :disabled="isUserCurrentUser(userInfo.username)"
                @click="deleteUser(userInfo.username)"
              >
                Delete
              </button>
              <button
                v-if="userInfo.active"
                :class="getButtonClass(isUserCurrentUser(userInfo.username))"
                :disabled="isUserCurrentUser(userInfo.username)"
                @click="setUserStatus(false, userInfo)"
              >
                Disable
              </button>
              <button
                v-else
                :class="getButtonClass(isUserCurrentUser(userInfo.username))"
                :disabled="isUserCurrentUser(userInfo.username)"
                @click="setUserStatus(true, userInfo)"
              >
                Enable
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div
        v-if="totalPages > 0"
        class="pagination"
      >	
        <button
          :class="currentPage === 1 ?'pagingButton_disabled':'pagingButton'"
          :disabled="currentPage === 1"
          @click="prevPage"
        >
          Previous
        </button>
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
        <button
          :class="currentPage === totalPages?'pagingButton_disabled':'pagingButton'"
          :disabled="currentPage === totalPages"
          @click="nextPage"
        >
          Next
        </button>
        <span>(Items per page: {{ pageSize }})</span>
      </div>
    </div>
  </div>
</template>

<script>
/*eslint no-mixed-spaces-and-tabs: ["error", "smart-tabs"]*/
import axios from '../axios';
import { getCurrentUsername } from '@/utils/session';

export default {
  name: 'UsersComponent',
  data() {
    return {
      userInfos: [],
      newUser: {
        username: '',
        password: ''
        },
      newUserPasword:'',
      newUserPaswordConfirm:'',
      errorMessage: '',
      statusMessage: '',
      searchQuery: '',
	  currentSort: 'username',
	  currentSortDir: '',
	  currentPageNumber: 1,
	  pageSize: 10,
	  highlightedRow: null,
	  highlightedType: ''
    };

  },
  computed: {
	sortedObjects() {
		let usersCopy = [...this.userInfos];
		return usersCopy.sort((a, b) => {
			let modifier = 1;
			if (this.currentSortDir === 'desc'){
				modifier = -1;
			}
			let valA = "";
			let valB = "";
			valA = a[this.currentSort];
			valB = b[this.currentSort];
			if (typeof valA === 'string' && typeof valB === 'string') {
				valA = valA.toLowerCase();
				valB = valB.toLowerCase();
			}
			if (valA < valB) return -1 * modifier;
			if (valA > valB) return 1 * modifier;
			return 0;
		});
	},
	paginatedObjects() {
		let start = (this.currentPage - 1) * this.pageSize;
		let end = start + this.pageSize;
		return this.filteredObjects.slice(start, end);
	},
	totalPages() {
		return Math.ceil(this.filteredObjects.length / this.pageSize);
	},
	currentPage() {
		if(this.currentPageNumber > this.totalPages){
			return 1;
		}
		return this.currentPageNumber;
	},
	filteredObjects() {
		if (this.searchQuery != null && this.searchQuery != '') {
			return this.sortedObjects.filter(userInfo =>userInfo.username.toLowerCase().includes(this.searchQuery.toLowerCase()));
		}
		return this.sortedObjects;
	}
},

  mounted() {
	this.currentSortDir = localStorage.getItem("UsersColumnsSortDir")?localStorage.getItem("UsersColumnsSortDir"):"asc";
    this.fetchData();
  },
  methods: {
	sortBy(column){
		if (this.currentSort === column) {
			this.currentSortDir = this.currentSortDir === 'asc' ? 'desc' : 'asc';
			localStorage.setItem("UsersColumnsSortDir",this.currentSortDir )				
		}
		this.currentSort=column;
	},
	getHighlightClass(itemTypeId) {
		return this.highlightedRow === itemTypeId? "highlight_"+this.highlightedType :""; 
	},
	nextPage() {
		if (this.currentPage < this.totalPages) {
			this.currentPageNumber = this.currentPage+1;
		}
	},
	prevPage() {
		if (this.currentPage > 1) {
			this.currentPageNumber = this.currentPage-1;
		}					
	},
	higlightRow(objectId,highlightedType){
		this.highlightedRow=objectId;
		this.highlightedType=highlightedType;
		setTimeout(() => {
			if(!this.editedObject){
				this.highlightedRow = null;
				this.highlightedType='';
			}
			else{
				this.highlightedType='editmode';
			}
		}, 3000);
	},
  isUserCurrentUser(username){
   return getCurrentUsername() === username;
  },
  getButtonClass(isCurrentUser){
    if(isCurrentUser){
      return "actionbutton_disabled";
    }
    else{
    return "actionbutton";
    }
  },

   addUser(){
    this.statusMessage='';
    this.errorMessage='';

    if(this.newUserPasword === this.newUserPaswordConfirm){
		this.newUser.password=this.newUserPasword;
		let username = this.newUser.username;
        console.log("add new user:", this.newUser);
        axios.post('/userService', this.newUser) .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
        this.fetchData();
		this.higlightRow(username,"saved");
        this.resetNewUser();
            })
        .catch(error => {
          // Handle error
          console.error("Add User fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not add new User");
        });

    }
    else{
        this.errorMessage="Passwords do not match!";
    }
  },
    resetNewUser() {
      this.newUser= {
        username: '',
        password: ''
        };
       this.newUserPasword='';
       this.newUserPaswordConfirm='';


    },
    async fetchData() {
      try {
      this.statusMessage='';
      this.errorMessage='';
        const response = await axios.get('/userService');
        console.log("Response", response.data)
         this.userInfos = response.data.userInfos.map(user => ({
          username: user.userName,
          active: user.active,
          password: '',
          passwordConfirm: ''
        }));
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,"Error fetching data. Please try again later.");
      }
    },

    async updatePassword(userInfo) {
      this.statusMessage='';
	  this.errorMessage='';
      if(userInfo.password === userInfo.passwordConfirm){

        axios.put('/userService/updatePassword/'+userInfo.username, {newPassword: userInfo.password})
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          userInfo.password='';
          userInfo.passwordConfirm='';
		  this.higlightRow(userInfo.username,"saved");
          this.statusMessage='Password successfully updated!';
        })
        .catch(error => {
          // Handle error
		  this.higlightRow(userInfo.username,"error");
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update UserStatus");
        });
      }
      else{
		this.higlightRow(userInfo.username,"error");
        this.errorMessage="Passwords do not match!";
      }

    },
   async setUserStatus(newStatus, userInfo) {
      this.statusMessage='';
	  this.errorMessage='';
      axios.put('/userService/setStatus/'+userInfo.username, {active: newStatus})
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
          //userInfo.active=newStatus;
		  this.higlightRow(userInfo.username,"saved");
		  this.fetchData();

        })
        .catch(error => {
          // Handle error
		  this.higlightRow(userInfo.username,"error");
          console.error("Update fehlgeschlagen:", error);
          this.setErrorMessage(error,"Could not update UserStatus");
        });
    },
    async deleteUser(userID) {
      this.statusMessage='';
	  this.errorMessage='';
	  if (window.confirm('Are you sure you want to delete this user?')) {
		  this.higlightRow(userID,"deleted");
	      axios.delete("/userService/deleteuser/"+userID)
	        .then(response => {
	          // Handle success
	          console.log("Delete erfolgreich:", response);
	          this.fetchData();
	        })
	        .catch(error => {
	          // Handle error
			  this.higlightRow(userID,"error");
	          console.error("Delete fehlgeschlagen:", error);
	          this.setErrorMessage(error,"Could not delete ItemType");
	        });
		}
    },

    setErrorMessage(error, defaultMessage){
          this.statusMessage='';
          if( error.response != null &&  error.response.data != null  && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    }

  }
};
</script>
