<template>
  <div class="main">
    <div class="content">
      <div><h1>ItemTypes</h1></div>
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
          placeholder="Search by Itemtype name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('name')">
              Name <span v-if="currentSort === 'name'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('modifier')">
              Modfier <span v-if="currentSort === 'modifier'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('metal')">
              Metal <span v-if="currentSort === 'metal'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!editedObject">
            <td>
              <input
                v-model="newItemType.name"
                type="text"
                placeholder="Name"
                required="true"
              >
            </td>
            <td>
              <input
                v-model.number="newItemType.modifier"
                type="number"
                placeholder="Factor"
              >
            </td>
            <td>
              <select
                id="options"
                v-model="newItemType.material.id"
              >
                <option
                  v-for="metal in metals"
                  :key="metal.value"
                  :value="metal.value"
                >
                  {{ metal.text }}
                </option>
              </select>
            </td>
            <td>
              <button
                class="actionbutton"
                @click="addItemType()"
              >
                Add New
              </button>
            </td>
          </tr>
          <tr v-if="editedObject">
            <td>
              <input
                v-model="editedObject.name"
                type="text"
                placeholder="Name"
                required="true"
              >
            </td>
            <td>
              <input
                v-model.number="editedObject.modifier"
                type="number"
                placeholder="Factor"
              >
            </td>
            <td>
              <select
                id="options"
                v-model="editedObject.material.id"
              >
                <option
                  v-for="metal in metals"
                  :key="metal.value"
                  :value="metal.value"
                >
                  {{ metal.text }}
                </option>
              </select>
            </td>
            <td>
              <button
                class="actionbutton"
                @click="updateObject()"
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
            <td>{{ object.modifier }}</td>
            <td>
              {{ object.material.name }}
            </td>
            <td>
              <button
                v-if="editedObject == null"
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
                v-if="editedObject == null"
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
/*eslint no-mixed-spaces-and-tabs: ["error", "smart-tabs"]*/
import axios from '../axios';

export default {
	name: 'ItemTypes',
	data() {
		return {
			editedObject: null,
			itemTypes: [],
			metals: [],
			newItemType: {
				name: '',
				modifier: 1,
				material: {
					id: ''
				},
			},
			preSelectedMetal: '',
			errorMessage: '',
			searchQuery: '',
			currentSort: '',
		    currentSortDir:'',
			currentPageNumber: 1,
			pageSize: 10,
			highlightedRow:null,
			highlightedType:''
		};

	}, computed: {

		sortedObjects() {
			let itemTypesCopy = [...this.itemTypes];
			return itemTypesCopy.sort((a, b) => {
				let modifier = 1;
				if (this.currentSortDir === 'desc') modifier = -1;
				let valA = "";
				let valB = "";
				if (this.currentSort === 'metal') {
					valA = a.material.name;
					valB = b.material.name;
				}				
				else {
					valA = a[this.currentSort];
					valB = b[this.currentSort];
				}
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
				return this.sortedObjects.filter(itemType =>
					itemType.name.toLowerCase().includes(this.searchQuery.toLowerCase())
				);
			}
			return this.sortedObjects;
		}
	},

	mounted() {
		this.currentSort=localStorage.getItem("ItemTypesColumnsSort")?localStorage.getItem("ItemTypesColumnsSort"):"name";
		this.currentSortDir = localStorage.getItem("ItemTypesColumnsSortDir")?localStorage.getItem("ItemTypesColumnsSortDir"):"asc";
		this.fetchData();
	},
	methods: {

		getMaterialName(materialId){
			let metal = this.metals.find(metal =>metal.value === materialId);
			if(metal){
				return metal.text;
			}
			return "";
		},
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
					localStorage.setItem("ItemTypesColumnsSortDir",this.currentSortDir )				
			 }
		    this.currentSort=column;
			localStorage.setItem("ItemTypesColumnsSort",column);
			
		},
		getHighlightClass(itemTypeId) {
		     if (this.highlightedRow === itemTypeId) {
		       return "highlight_"+this.highlightedType;
		     }
		     return '';
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
                addItemType() {
                        this.errorMessage="";
			this.newItemType.material["name"]=this.getMaterialName(this.newItemType.material.id);
			axios.post('/itemTypes', this.newItemType).then(postResponse => {
                    let objectId = postResponse.data.id;
					this.itemTypes.push(postResponse.data);
					this.resetNewItemType();
					this.higlightRow(objectId,"saved");
				})
				.catch(error => {
					console.error("Error adding new unit:", error);
					this.setErrorMessage(error, "Error adding itemType. Please try again later.");
				});
		},
		resetNewItemType() {
			this.newItemType = {
				name: '',
				modifier: 1,
				material: {
					id: this.preSelectedMetal
				}
			};
		},
		async fetchData() {
			this.errorMessage="";
			try {
				const response = await axios.get(`/materials`);
				this.metals = response.data.map(option => ({
					value: option.id,
					text: option.name
				}));
				if (this.metals.length > 0) {
					this.preSelectedMetal = this.metals[0].value;
				}
				this.newItemType.material.id = this.preSelectedMetal;

				const itemtyperesponse = await axios.get(`/itemTypes`);
				this.itemTypes = itemtyperesponse.data;
			} catch (error) {
				console.error('Error fetching data:', error);
				this.setErrorMessage(error, "Error fetching data. Please try again later.");
			}
		},
		async updateObject() {
			this.errorMessage="";
			let id =  this.editedObject.id;
			
			this.editedObject.material["name"]=this.getMaterialName(this.editedObject.material.id);
			axios.put(`/itemTypes/` + id, this.editedObject)
				.then(response => {
					// Handle success
					this.higlightRow(id,"saved");
					console.log("Update erfolgreich:", response);
					this.editedObject = null;
					this.fetchData();
					
				})
				.catch(error => {
					// Handle error
					console.error("Update fehlgeschlagen:", error);
					this.setErrorMessage(error, "Could not update ItemType");
					this.higlightRow(id,"error");
				});
		},
		async deleteObject(objectId) {
			
			if (window.confirm('Are you sure you want to delete this Item Type?')) {
				this.errorMessage="";
				this.higlightRow(objectId,"deleted");
				axios.delete(`/itemTypes/${objectId}`)
					.then(response => {
						// Handle success
						console.log("Delete erfolgreich:", response);
						this.fetchData();
					})
					.catch(error => {
						// Handle error
						console.error("Delete fehlgeschlagen:", error);
						this.higlightRow(objectId,"error");
						this.setErrorMessage(error, "Could not delete ItemType");
					});
				}
		},

		setErrorMessage(error, defaultMessage) {
			if (error.response != null && error.response.data != null && error.response.data.message != null) {
				this.errorMessage = error.response.data.message;
			}
			else {
				this.errorMessage = defaultMessage;
			}
		}

	}
};
</script>
