<template>
  <div class="main">
    <div class="content">
      <div><h1>Units</h1></div>
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
          placeholder="Search by unit name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('name')">
              Name <span v-if="currentSort === 'name'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('factor')">
              Conversion factor to 1 Oz <span v-if="currentSort === 'factor'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!editedObject">
            <td>
              <input
                v-model="newUnit.name"
                type="text"
                placeholder="Name"
              >
            </td>
            <td>
              <input
                v-model.number="newUnit.factor"
                type="number"
                placeholder="Factor"
              >
            </td>

            <td>
              <button
                class="actionbutton"
                @click="addUnit()"
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
                disabled="true"
              >
            </td>
            <td>
              <input
                v-model.number="editedObject.factor"
                type="number"
                placeholder="Factor"
              >
            </td>
            <td>
              <button
                class="actionbutton"
                @click="updateUnit"
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
            v-for="unit in paginatedObjects"
            :key="unit.name"
            :class="getHighlightClass(unit.name)"
          >
            <td>{{ unit.name }}</td>
            <td>{{ unit.factor }}</td>
            <td>
              <button
                v-if="editedObject && editedObject.name === unit.name"
                class="actionbutton"
                @click="cancelEdit"
              >
                Cancel
              </button>
              <button
                v-if="!editedObject"
                class="actionbutton"
                @click="editObject(unit)"
              >
                Edit
              </button>
              <button
                v-if="!editedObject"
                class="actionbutton"
                @click="deleteUnit(unit.name)"
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
  name: 'UnitsComponent',
  data() {
    return {
      units: [],
      newUnit: {
        name: '',
        factor: 1
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
  let unitsCopy = [...this.units];
  return unitsCopy.sort((a, b) => {
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
			return this.sortedObjects.filter(itemType =>itemType.name.toLowerCase().includes(this.searchQuery.toLowerCase()));
		}
		return this.sortedObjects;
	}
},

  mounted() {
	this.currentSort=localStorage.getItem("UnitsColumnsSort")?localStorage.getItem("UnitsColumnsSort"):"name";
	this.currentSortDir = localStorage.getItem("UnitsColumnsSortDir")?localStorage.getItem("UnitsColumnsSortDir"):"asc";
    this.fetchUnits();
  },
  methods: {
	editObject(object) {
		this.errorMessage="";
		this.highlightedRow=object.name;
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
			localStorage.setItem("UnitsColumnsSortDir",this.currentSortDir )				
		}
		this.currentSort=column;
		localStorage.setItem("UnitsColumnsSort",column);					
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
   addUnit(){
	this.errorMessage="";
      axios.post('/units', this.newUnit)
        .then(response => {
			this.units.push(response.data);
			this.higlightRow(this.newUnit.name,"saved");
			this.resetNewUnit();
        })
        .catch(error => {
          console.error("Error adding new unit:", error);
          this.setErrorMessage(error,"Error adding unit. Please try again later.");
        });
    },
    resetNewUnit() {
      this.newUnit = {
        name: '',
        factor: 1

      };
    },
    async fetchUnits() {
		this.errorMessage="";
		try {
			const response = await axios.get(`/units`);
			this.units = response.data;
		} catch (error) {
			console.error('Error fetching units:', error);
			this.setErrorMessage(error,"Error fetching units. Please try again later.");
		}
	},
	async updateUnit() {
		this.errorMessage="";
		let unitName = this.editedObject.name;
		axios.put(`/units/`+unitName, this.editedObject)
		.then(response => {
			console.log("Update erfolgreich:", response);
			this.higlightRow(unitName,"saved");
			this.editedObject=null;
			this.fetchUnits();
		}).catch(error => {
			console.error("Update fehlgeschlagen:", error);
			this.higlightRow(unitName,"error")
			this.setErrorMessage(error,"Could not update Unit");
		});
	},
    async deleteUnit(unitname) {
		this.errorMessage="";
		if (window.confirm('Are you sure you want to delete this unit?')) {
			this.higlightRow(unitname,"deleted")
			axios.delete(`/units/${unitname}`).then(response => {
				// Handle success
				console.log("Delete erfolgreich:", response);
				this.fetchUnits();
			})
			.catch(error => {
				// Handle error
				this.higlightRow(unitname,"error")
				console.error("Delete fehlgeschlagen:", error);
				this.setErrorMessage(error,"Could not delete Unit");
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
    }

  }
};
</script>
