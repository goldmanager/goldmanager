<template>
  <div class="main">
    <div class="content">
      <div><h1>Items</h1></div>
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
          placeholder="Search by Item name"
        >
      </div>
      <table>
        <thead>
          <tr>
            <th @click="sortBy('name')">
              Name <span v-if="currentSort === 'name'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('itemType')">
              Type <span v-if="currentSort === 'itemType'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('amount')">
              Weight <span v-if="currentSort === 'amount'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('unit')">
              Unit <span v-if="currentSort === 'unit'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('itemCount')">
              Number of Items <span v-if="currentSort === 'itemCount'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th @click="sortBy('itemStorage')">
              Item Storage <span v-if="currentSort === 'itemStorage'">{{ currentSortDir === 'asc' ? '▲' : '▼' }}</span>
            </th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!editedObject">
            <td>
              <input
                v-model="newItem.name"
                type="text"
                placeholder="Name"
                required="true"
              >
            </td>

            <td>
              <select
                id="optionsItemType"
                v-model="newItem.itemType.id"
              >
                <option
                  v-for="itemType in itemTypes"
                  :key="itemType.value"
                  :value="itemType.value"
                >
                  {{ itemType.text }}
                </option>
              </select>
            </td>
            <td>
              <input
                v-model.number="newItem.amount"
                type="number"
                placeholder="Weight"
              >
            </td>
            <td>
              <select
                id="optionsUnit"
                v-model="newItem.unit.name"
              >
                <option
                  v-for="unit in units"
                  :key="unit.value"
                  :value="unit.value"
                >
                  {{ unit.text }}
                </option>
              </select>
            </td>
            <td>
              <input
                v-model.number="newItem.itemCount"
                type="integer"
                placeholder="Number of items"
              >
            </td>
            <td>
              <select
                id="optionsStorage"
                v-model="newItem.itemStorage.id"
              >
                <option
                  key=""
                  value=""
                >
                  None
                </option> 
                <option
                  v-for="storage in itemStorages"
                  :key="storage.value"
                  :value="storage.value"
                >
                  {{ storage.text }}
                </option>
              </select>
            </td>
            <td>
              <button
                class="actionbutton"
                @click="addItem()"
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
              <select
                id="optionsItemType"
                v-model="editedObject.itemType.id"
              >
                <option
                  v-for="itemType in itemTypes"
                  :key="itemType.value"
                  :value="itemType.value"
                >
                  {{ itemType.text }}
                </option>
              </select>
            </td>
            <td>
              <input
                v-model.number="editedObject.amount"
                type="number"
                placeholder="Weight"
              >
            </td>
            <td>
              <select
                id="optionsUnit"
                v-model="editedObject.unit.name"
              >
                <option
                  v-for="unit in units"
                  :key="unit.value"
                  :value="unit.value"
                >
                  {{ unit.text }}
                </option>
              </select>
            </td>
            <td>
              <input
                v-model.number="editedObject.itemCount"
                type="integer"
                placeholder="Number of items"
              >
            </td>
            <td>
              <select
                id="optionsStorage"
                v-model="editedObject.itemStorage.id"
              >
                <option
                  key=""
                  value=""
                >
                  None
                </option> 
                <option
                  v-for="storage in itemStorages"
                  :key="storage.value"
                  :value="storage.value"
                >
                  {{ storage.text }}
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
            v-for="item in paginatedObjects"
            :key="item.id"
            :class="getHighlightClass(item.id)"
          >
            <td>{{ item.name }}</td>
            <td>
              {{ item.itemType.name }}
            </td>
            <td>{{ item.amount }}</td>
            <td>
              {{ item.unit.name }}          
            </td>
            <td>{{ item.itemCount }}</td>
            <td>{{ item.itemStorage && item.itemStorage.name ? item.itemStorage.name: '' }}</td>
            <td>
              <button
                v-if="editedObject == null"
                class="actionbutton"
                @click="editObject(item)"
              >
                Edit
              </button>
              <button
                v-if="editedObject != null && editedObject.id === item.id"
                class="actionbutton"
                @click="cancelEdit"
              >
                Cancel
              </button>
              <button
                v-if="editedObject == null"
                class="actionbutton"
                @click="deleteObject(item.id)"
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
  name: 'ItemsComponent',
  data() {
    return {
      items: [],
      units: [],
      itemTypes: [],
	  itemStorages: [],
      newItem: {
        name: '',
        amount: 0,
		itemStorage:{
			id: '',
			name: ''
		},
        itemType: {
          id: ''
          },
        unit: {
          name: ''
          },
        },
	  editedObject: null,  
      preSelectedUnit:'',
      preSelectedItemtype:'',
	  errorMessage: '',
	  currentSort: '',
	  currentSortDir:'',
	  currentPageNumber: 1,
	  pageSize: 10,
	  searchQuery: '',
	  highlightedRow:null,
	  highlightedType:''
	  
    };

  },
  computed: {
	
     sortedObjects() {
	   let itemsCopy = [...this.items];
       return itemsCopy.sort((a, b) => {
         let modifier = 1;
         if (this.currentSortDir === 'desc') modifier = -1;
		 let valA= "";
		 let valB= "";
		 if(this.currentSort === 'itemType'){
			valA = a.itemType.name;
			valB = b.itemType.name;
		 }
		 if(this.currentSort === 'itemStorage'){
			valA = a.itemStorage ? a.itemStorage.name: '';
			valB = b.itemStorage ? b.itemStorage.name: '';
		  }
		 else if(this.currentSort === 'unit'){
                valA = a.unit.name;
                valB = b.unit.name;
		 }
		 else{
			valA= a[this.currentSort];
			valB= b[this.currentSort];
		 }
		 if (typeof valA === 'string' && typeof valB === 'string') {
			valA = valA.toLowerCase();
			valB = valB.toLowerCase();
		 }
		 if (valA< valB) return -1 * modifier;
		 if (valA >valB) return 1 * modifier;
         return 0;
       });
     },
	 paginatedObjects() {
	     const start = (this.currentPage - 1) * this.pageSize;
	     const end = start + this.pageSize;
	     return this.filteredObjects.slice(start, end);
	},
	currentPage() {
		if(this.currentPageNumber > this.totalPages){
			return 1;
		}
		return this.currentPageNumber;
	},
	totalPages() {
	   return Math.ceil(this.filteredObjects.length / this.pageSize);
	},
	filteredObjects(){
		
		if(this.searchQuery != null && this.searchQuery !=''){
			return this.sortedObjects.filter(item =>
			        item.name.toLowerCase().includes(this.searchQuery.toLowerCase())
			 );
		}
		return this.sortedObjects;
	}
  },

  mounted() {
	this.currentSort=localStorage.getItem("ItemsColumnsSort")?localStorage.getItem("ItemsColumnsSort"):"name";
	this.currentSortDir = localStorage.getItem("ItemsColumnsSortDir")?localStorage.getItem("ItemsColumnsSortDir"):"asc";
	
    this.fetchData();
	
  },
  methods: {
	getItemStorageName(itemStorageId){
		let itemtStorage = this.itemStorages.find(itemStorage =>itemStorage.value === itemStorageId);
		if(itemtStorage){
			return itemtStorage.text;
		}
		return "";
	},
	getItemTypeName(itemTypeId){
				let itemtype = this.itemTypes.find(itemType =>itemType.value === itemTypeId);
				if(itemtype){
					return itemtype.text;
				}
				return "";
	},
	editObject(object) {
		this.errorMessage="";
		this.highlightedRow=object.id;
		this.highlightedType="editmode";
		this.editedObject =  { ...object };
		if(!this.editedObject.itemStorage){
			this.editedObject.itemStorage={
				id:'',
				name: ''
			};
		}
		
	},
	cancelEdit() {
		this.errorMessage="";
		this.editedObject = null;
		this.highlightedRow='';
		this.highlightedType='';
	},
	sortBy(column){
		
		if (this.currentSort === column) {
		        this.currentSortDir = this.currentSortDir === 'asc' ? 'desc' : 'asc';
				localStorage.setItem("ItemsColumnsSortDir",this.currentSortDir )				
		    }
	    this.currentSort=column;
		localStorage.setItem("ItemsColumnsSort",column);
		
	},
	getHighlightClass(itemId) {
	    if (this.highlightedRow === itemId) {
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
   addItem(){
	  this.errorMessage="";
	  this.newItem.itemType["name"]=this.getItemTypeName(this.newItem.itemType.id);
	  if (this.newItem.itemStorage && this.newItem.itemStorage.id != ''){
		this.newItem.itemStorage["name"]=this.getItemStorageName(this.newItem.itemStorage.id);
	  }
	  let toStore ={...this.newItem};
	  if(toStore.itemStorage.id == ''){
		toStore.itemStorage=null;
	  }
      axios.post('/items', toStore)
        .then(response => {
			this.items.push(response.data);
			this.higlightRow(response.data.id,"saved");
			this.resetNewItem();
        })
        .catch(error => {
          console.error("Error adding new unit:", error);
          this.setErrorMessage(error,"Error adding unit. Please try again later.");
        });
    },
    resetNewItem() {
      this.newItem= {

        name: '',
        amount: 0,
        itemType: {
          id: this.preSelectedItemtype
          },
        unit: {
          name: this.preSelectedUnit
          },
		  itemStorage:{
			id: '',
			name:''
		  }
        };
    },
    async fetchData() {
      try {
        const response = await axios.get(`/units`);
         this.units = response.data.map(option => ({
          value: option.name,
          text: option.name
        }));
        if (this.units.length > 0) {
          this.preSelectedUnit = this.units[0].value;
        }
        this.newItem.unit.name=this.preSelectedUnit;
		const itemstorageresponse = await axios.get(`/itemStorages`);
		      this.itemStorages = itemstorageresponse.data.map(option => ({
		        value: option.id,
		        text: option.name
		      }));
		      
       const itemtyperesponse = await axios.get(`/itemTypes`);
        this.itemTypes = itemtyperesponse.data.map(option => ({
          value: option.id,
          text: option.name
        }));
         if (this.itemTypes.length > 0) {
          this.preSelectedItemtype = this.itemTypes[0].value;
        }
        this.newItem.itemType.id=this.preSelectedItemtype;

        const itemresponse = await axios.get(`/items`);

        this.items = itemresponse.data;
		this.itemsToSort=[...this.items];
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,"Error fetching data. Please try again later.");
      }
    },
   async updateObject() {
	  let id =  this.editedObject.id;
	  
	  this.higlightRow(id,"saved");
	  let objectTostore ={...this.editedObject};
	  if (objectTostore.itemStorage.id === ''){
		objectTostore.itemStorage=null;
	  }
      axios.put(`/items/`+id, objectTostore)
        .then(response => {
          // Handle success
          console.log("Update erfolgreich:", response);
		  this.editedObject = null;
          this.fetchData();
		  
        })
        .catch(error => {
          // Handle error
          console.error("Update fehlgeschlagen:", error);
		  this.higlightRow(id,"error");
          this.setErrorMessage(error,"Could not update ItemType");
        });
    },
    async deleteObject(objectId) {
		if (window.confirm('Are you sure you want to delete this item?')) {
			 this.higlightRow(objectId,"deleted");
						 
		     await axios.delete(`/items/${objectId}`)
			 
		        .then(response =>  {
		          // Handle success
		          console.log("Delete erfolgreich:", response);
				  
		          this.fetchData();
		        })
		        .catch(error => {
		          // Handle error
		          console.error("Delete fehlgeschlagen:", error);
				  this.higlightRow(objectId,"error");
		          this.setErrorMessage(error,"Could not delete ItemType");
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
