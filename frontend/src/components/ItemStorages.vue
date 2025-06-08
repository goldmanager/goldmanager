<template>
  <div class="main">
    <div class="content">
      <div><h1>Item Storages</h1></div>
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
          placeholder="Search by Item Storage name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('name')">
              Name <span v-if="currentSort === 'name'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('description')">
              Description <span v-if="currentSort === 'description'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
					
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!editedObject">
            <td>
              <input
                v-model="newItemStorage.name"
                type="text"
                placeholder="Name"
              >
            </td>
            <td>
              <textarea
                v-model="newItemStorage.description"
                rows="4"
                cols="70"
              />
            </td>
            <td>
              <button
                class="actionbutton"
                @click="addItemStorage"
              >
                Add New
              </button>
            </td>
          </tr>
          <tr v-else>
            <td>
              <input
                v-model="editedObject.name"
                type="text"
                placeholder="Name"
              >
            </td>
            <td>
              <textarea
                id="editdescription"
                v-model="editedObject.description"
                rows="4"
                cols="70"
              />
            </td>
            <td>
              <button
                class="actionbutton"
                @click="updateObject"
              >
                Save
              </button>
              <button
                class="actionbutton"
                @click="cancelEdit"
              >
                Cancel
              </button>
            </td>
          </tr>
          <tr
            v-for="object in paginatedObjects"
            :key="object.id"
            :class="getHighlightClass(object.id)"
          >
            <td>{{ object.name }}</td>
            <td>{{ object.description && object.description.length> 50? object.description.substring(0,50)+'...': object.description?object.description:'' }}</td>
            <td>
              <button
                v-if="!editedObject"
                class="actionbutton"
                @click="editObject(object)"
              >
                Edit
              </button>
              <button
                v-if="editedObject != null && editedObject.id === object.id"
                class="actionbutton"
                @click="cancelEdit"
              >
                Cancel
              </button>
              <button
                v-if="!editedObject"
                class="actionbutton"
                @click="deleteObject(object.id)"
              >
                Delete
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
import axios from '../axios';

export default {
  name: 'ItemStorages',
	data() {
		return {
			itemStorages: [],
                        newItemStorage: { // Data model for a new material
				name: '',
				description: ''
			},
			errorMessage: '',
			searchQuery: '',
			currentSort: '',
			currentSortDir: '',
			currentPageNumber: 1,
			pageSize: 10,
			highlightedRow: null,
			highlightedType: '',
			editedObject: null
		};

	},
  computed: {
	sortedObjects() {
	let result = [...this.itemStorages];
	return result.sort((a, b) => {
		let modifier = 1;
		if (this.currentSortDir === 'desc') modifier = -1;
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
				return this.sortedObjects.filter(itemType =>itemType.name.toLowerCase().includes(this.searchQuery.toLowerCase()));
			}
			return this.sortedObjects;
		}
	},

  mounted() {
	this.currentSort=localStorage.getItem("ItemSoragesColumnsSort")?localStorage.getItem("ItemSoragesColumnsSort"):"name";
	this.currentSortDir = localStorage.getItem("ItemSoragesColumnsSortDir")?localStorage.getItem("ItemSoragesColumnsSortDir"):"asc";
    this.fetchData();
  },
	methods: {
		editObject(object) {
			this.errorMessage="";
			this.highlightedRow=object.id;
			this.highlightedType="editmode";
			this.editedObject =  { ...object };
		},
		cancelEdit() {
			this.errorMessage="";
			this.highlightedRow='';
			this.highlightedType='';
			this.editedObject =null;
		},
		sortBy(column){
			if (this.currentSort === column) {
				this.currentSortDir = this.currentSortDir === 'asc' ? 'desc' : 'asc';
				localStorage.setItem("ItemSoragesColumnsSortDir",this.currentSortDir )				
			}
			this.currentSort=column;
			localStorage.setItem("ItemSoragesColumnsSort",column);					
		},
		getHighlightClass(id) {
			return this.highlightedRow === id? "highlight_"+this.highlightedType :""; 
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
		addItemStorage() {
			this.clearErrorMessage();
			this.newItemStorage.entryDate = new Date(this.newItemStorage.entryDate);
			axios.post('/itemStorages', this.newItemStorage).then(response => {
				this.resetNewItemStorage();
				this.itemStorages.push(response.data); 
				this.higlightRow(response.data.id,"saved");
			}).catch(error => {
				console.error("Error adding new item storage:", error);
				this.setErrorMessage(error, "Error adding metal. Please try again later.");
			});
		},
		resetNewItemStorage() {
			this.newItemStorage = {
				name: '',
				description: ''
			};
    },
    async fetchData() {
      try {
        const response = await axios.get(`/itemStorages`);
        this.itemStorages = response.data;
        this.clearErrorMessage();
      } catch (error) {
        console.error('Error fetching metals:', error);
         this.setErrorMessage(error,"Error fetching metals. Please try again later.");
      }
    },
   async updateObject() {
	let id = this.editedObject.id;
	this.clearErrorMessage();
	this.editedObject.entryDate=new Date();
	axios.put(`/itemStorages/`+id, this.editedObject)
	.then(response => {
		// Handle success
		console.log("Update successfull:", response);
		this.editedObject=null;
		this.fetchData();
		this.higlightRow(id,"saved");
	})
	.catch(error => {
		// Handle error
		console.error("Update failed:", error);
		this.higlightRow(id,"error");
		this.setErrorMessage(error,"Could not update ItemS torage");});
    },
    async deleteObject(objectid) {
		this.clearErrorMessage();
		if (window.confirm('Are you sure you want to delete this Item Storage?')) {
			this.higlightRow(objectid,"deleted");
			axios.delete(`/itemStorages/`+objectid).then(response => {
			// Handle success
			console.log("Delete successfull:", response);
			this.fetchData();}).catch(error => {
				// Handle error
				console.error("Delete failed:", error);
				this.higlightRow(objectid,"error");
				this.setErrorMessage(error,"Could not delete Item Storage");
			});
		}
    },

    setErrorMessage(error, defaultMessage){
          if(error.response.data != null && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    },
    clearErrorMessage(){
      this.errorMessage='';
     }
    

  }
};
</script>
