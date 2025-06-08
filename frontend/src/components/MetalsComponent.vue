<template>
  <div class="main">
    <div class="content">
      <div><h1>Metals</h1></div>
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
          placeholder="Search by metal name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('name')">
              Name <span v-if="currentSort === 'name'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('price')">
              Price (per Oz) <span v-if="currentSort === 'price'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('entryDate')">
              Entry Date <span v-if="currentSort === 'entryDate'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!editedObject">
            <td>
              <input
                v-model="newMaterial.name"
                type="text"
                placeholder="Name"
              >
            </td>
            <td>
              <input
                v-model.number="newMaterial.price"
                type="number"
                placeholder="Price"
              >
            </td>
					
            <td>
              <el-date-picker
                v-model="newMaterial.entryDate"
                type="datetime"
                range-separator="to"
                start-placeholder="Begin Date"
                end-placeholder="Ende Date"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss.SSS"
                :clearable="false"
                :arrow-control="true"
              />
            </td>
					
            <td>
              <button
                class="actionbutton"
                @click="addMaterial"
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
              <input
                v-model.number="editedObject.price"
                type="number"
                placeholder="Price"
              >
            </td>
            <td>---</td>
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
            <td>{{ object.price }}</td>
            <td>{{ formatDate(object.entryDate) }}</td>
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
  name: 'MetalsComponent',
	data() {
		return {
			metals: [],
                        newMaterial: { // Data model for a new material
				name: '',
				price: 0,
				entryDate: this.formatDateCustom(new Date())
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
	let metalsCopy = [...this.metals];
	return metalsCopy.sort((a, b) => {
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
	this.currentSort=localStorage.getItem("MetalsColumnsSort")?localStorage.getItem("MetalsColumnsSort"):"name";
	this.currentSortDir = localStorage.getItem("MetalsColumnsSortDir")?localStorage.getItem("MetalsColumnsSortDir"):"asc";
    this.fetchMetals();
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
				localStorage.setItem("MetalsColumnsSortDir",this.currentSortDir )				
			}
			this.currentSort=column;
			localStorage.setItem("MetalsColumnsSort",column);					
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
		addMaterial() {
			this.clearErrorMessage();
			let materialToAdd={
				entryDate:new Date(this.newMaterial.entryDate).toJSON(),
				price: this.newMaterial.price,
				name: this.newMaterial.name,
			};
			
			axios.post('/materials', materialToAdd).then(response => {
				this.resetNewMaterial();
				this.metals.push(response.data); 
				this.higlightRow(response.data.id,"saved");
			}).catch(error => {
				console.error("Error adding new material:", error);
				this.setErrorMessage(error, "Error adding metal. Please try again later.");
			});
		},
		resetNewMaterial() {
			this.newMaterial = {
				name: '',
				price: 0,
				entryDate: this.formatDateCustom(new Date())
			};
    },
    async fetchMetals() {
      try {
        const response = await axios.get(`/materials`);
        this.metals = response.data;
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
	axios.put(`/materials/`+id, this.editedObject)
	.then(response => {
		// Handle success
		console.log("Update erfolgreich:", response);
		this.editedObject=null;
		this.fetchMetals();
		this.higlightRow(id,"saved");
	})
	.catch(error => {
		// Handle error
		console.error("Update fehlgeschlagen:", error);
		this.higlightRow(id,"error");
		this.setErrorMessage(error,"Could not update Metal");});
    },
    async deleteObject(objectid) {
		this.clearErrorMessage();
		if (window.confirm('Are you sure you want to delete this metal?')) {
			this.higlightRow(objectid,"deleted");
			axios.delete(`/materials/`+objectid).then(response => {
			// Handle success
			console.log("Delete erfolgreich:", response);
			this.fetchMetals();}).catch(error => {
				// Handle error
				console.error("Delete fehlgeschlagen:", error);
				this.higlightRow(objectid,"error");
				this.setErrorMessage(error,"Could not delete Metal");
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
     },
    formatDate(date) {
      return new Date(date).toLocaleString();
    },
    formatDateCustom(date){

      var month=date.getMonth()+1;
      var day=date.getDate();
      var hour =date.getHours();
      var minutes =date.getMinutes();

      if(month <10){
         month =`0${month}`;
      }
      if(day <10){
         day =`0${day}`;
      }
      if(hour < 10){
        hour =`0${hour}`;
      }
      if(minutes < 10){
         minutes =`0${minutes}`;
      }
		
      return `${date.getFullYear()}-${month}-${day}T${hour}:${minutes}:00.000`;
    }

  }
};
</script>
