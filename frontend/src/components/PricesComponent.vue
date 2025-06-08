<template>
  <div class="main">
    <div class="content">
      <div><h1>Prices</h1></div>
      <div
        v-if="errorMessage"
        class="error"
      >
        {{ errorMessage }}
      </div>
      <div v-if="currentViewType==='PriceList'">
        <div>
          <input
            v-model="searchQueryPriceList"
            type="text"
            placeholder="Search by item name"
          >
        </div>
        <table>
          <thead>
            <tr>
              <th>Total Price</th>
              <th>Filter by</th>
              <th>Switch View Type</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ formatPrice(priceList.totalPrice) }}</td>
              <td>
                <select
                  id="filters"
                  v-model="currentFilter"
                  @change="setCurrentFilter($event)"
                >
                  <option value="">
                    None
                  </option>
                  <option
                    v-for="filter in filters"
                    :key="filter.id"
                    :value="filter.type+':'+filter.id"
                  >
                    {{ filter.typeName+ ": " + filter.name }}
                  </option>
                </select>
              </td>
              <td>
                <select
                  id="viewtype"
                  v-model="currentViewType"
                  @change="setCurrentViewType($event)"
                >
                  <option
                    v-for="viewType in viewTypes"
                    :key="viewType.id"
                    :value="viewType.id"
                  >
                    {{ viewType.label }}
                  </option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="priceList.prices.length >0">
          <table>
            <thead>
              <tr>
                <th @click="sortPriceListBy('name')">
                  Name <span v-if="currentPriceListSort === 'name'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('amount')">
                  Weight <span v-if="currentPriceListSort === 'amount'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('unit')">
                  Unit <span v-if="currentPriceListSort === 'unit'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('itemCount')">
                  Number of Items <span v-if="currentPriceListSort === 'itemCount'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('price')">
                  Unit Price <span v-if="currentPriceListSort === 'price'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('priceTotal')">
                  Total Price <span v-if="currentPriceListSort === 'priceTotal'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('metal')">
                  Metal <span v-if="currentPriceListSort === 'metal'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortPriceListBy('itemStorage')">
                  Item Storage <span v-if="currentPriceListSort === 'itemStorage'">{{ currentPriceListSortDir === 'asc' ? '▲' : '▼' }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="price in paginatedPriceList.prices"
                :key="price.item.id"
              >
                <td>{{ price.item.name }}</td>
                <td>{{ price.item.amount }}</td>
                <td>{{ price.item.unit.name }}</td>
                <td>{{ price.item.itemCount }}</td>
                <td>{{ formatPrice(price.price) }}</td>
                <td>{{ formatPrice(price.priceTotal) }}</td>
                <td>{{ price.item.itemType.material.name }}</td>
                <td>{{ price.item.itemStorage?price.item.itemStorage.name:'' }}</td>
              </tr>
            </tbody>
          </table>
          <div
            v-if="totalPriceListPages > 0"
            class="pagination"
          >
            <button
              :class="currentPriceListPage === 1 ?'pagingButton_disabled':'pagingButton'"
              :disabled="currentPriceListPage === 1"
              @click="prevPriceListPage"
            >
              Previous
            </button>
            <span>Page {{ currentPriceListPage }} of {{ totalPriceListPages }}</span>
            <button
              :class="currentPriceListPage === totalPriceListPages?'pagingButton_disabled':'pagingButton'"
              :disabled="currentPriceListPage === totalPriceListPages"
              @click="nextPriceListPage"
            >
              Next
            </button>
            <span>(Items per page: {{ priceListPageSize }})</span>		 
          </div>
        </div>
      </div>
      <div v-else>
        <div>
          <input
            v-model="groupsSearchQuery"
            type="text"
            placeholder="Search by group name"
          >
        </div>
        <table>
          <thead>
            <tr>
              <th>Switch View Type</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <select
                  id="viewtype"
                  v-model="currentViewType"
                  @change="setCurrentViewType($event)"
                >
                  <option
                    v-for="viewType in viewTypes"
                    :key="viewType.id"
                    :value="viewType.id"
                  >
                    {{ viewType.label }}
                  </option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
	  
        <div
          v-for="priceGroup in paginatedGroups"
          :key="priceGroup.groupName"
        >
          <H2>{{ priceGroup.groupName }}</H2>
          <table>
            <thead>
              <tr>
                <th>Total Price</th>
                <th>Total Weight</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{{ formatPrice(priceGroup.totalPrice) }}</td>
                <td>{{ formatPrice(priceGroup.amount) }} Oz</td>
              </tr>
            </tbody>
          </table>
          <div v-if="priceGroup.prices.length >0">
            <input
              v-model="groupsListQuerys[priceGroup.groupName]"
              type="text"
              placeholder="Search by item name"
            >
          </div>
          <table v-if="priceGroup.prices.length >0">
            <thead>
              <tr>
                <th @click="sortGroupPricesBy('name',priceGroup.groupName)">
                  Name <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'name'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('amount',priceGroup.groupName)">
                  Weight <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'amount'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('unit',priceGroup.groupName)">
                  Unit <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'unit'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('itemCount',priceGroup.groupName)">
                  Number of Items <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'itemCount'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('price',priceGroup.groupName)">
                  Unit Price <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'price'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('priceTotal',priceGroup.groupName)">
                  Total Price <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'priceTotal'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('metal',priceGroup.groupName)">
                  Metal  <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'metal'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
                <th @click="sortGroupPricesBy('itemStorage',priceGroup.groupName)">
                  Item Storage  <span v-if="getGroupPricesSortForGroup(priceGroup.groupName) === 'itemStorage'">{{ getGroupPricesSortDirForGroup(priceGroup.groupName) === 'asc' ? '▲' : '▼' }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="price in filteredGroupPrices(priceGroup.groupName)"
                :key="price.item.id"
              >
                <td>{{ price.item.name }}</td>
                <td>{{ price.item.amount }}</td>
                <td>{{ price.item.unit.name }}</td>
                <td>{{ price.item.itemCount }}</td>
                <td>{{ formatPrice(price.price) }}</td>
                <td>{{ formatPrice(price.priceTotal) }}</td>
                <td>{{ price.item.itemType.material.name }}</td>
                <td>{{ price.item.itemStorage ? price.item.itemStorage.name : '' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div
          v-if="totalGroupPages > 0"
          class="pagination"
        >
          <button
            :class="currentGroupPage === 1 ?'pagingButton_disabled':'pagingButton'"
            :disabled="currentGroupPage === 1"
            @click="prevGroupPage"
          >
            Previous
          </button>
          <span>Page {{ currentGroupPage }} of {{ totalGroupPages }}</span>
          <button
            :class="currentGroupPage === totalGroupPages?'pagingButton_disabled':'pagingButton'"
            :disabled="currentGroupPage === totalGroupPages"
            @click="nextGroupPage"
          >
            Next
          </button>
          <span>(Price groups per page: {{ groupsPageSize }})</span>		 
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/*eslint no-mixed-spaces-and-tabs: ["error", "smart-tabs"]*/
import axios from '../axios';
export default {
  name: 'PricesComponent',
  data() {
    return {
      viewTypes:[
      {
       id: 'PriceList',
       label: 'Price list'

      },
      {
       id: 'GroupByMetal',
       label: 'Group by Metal'
      }

      ],
      priceList: {totalPrice:0, prices:[]},
      priceGroups:[],
      filters: [],
      currentViewType: 'PriceList',
      errorMessage: '',
      currentFilter: '',
	  searchQueryPriceList:'',
	  priceListPageSize:10,
	  currentPricePageNumber: 1,
	  currentPriceListSortDir:'',
	  currentPriceListSort:'name',
	  groupsPageSize:2,
	  groupsListPageSize:5,
	  groupsCurrentPageNumer:1,
	  groupsSearchQuery:'',
	  groupsListSort:{},
	  groupsListSortDir:{},
	  groupsListQuerys:{},

	  
    };

  },
  computed: {
	
	totalGroupPages(){
		return Math.ceil(Object.entries(this.filteredGroups).length / this.groupsPageSize);
	},
	currentGroupPage() {
			if(this.groupsCurrentPageNumer > this.totalGroupPages){
				return 1;
			}
			return this.groupsCurrentPageNumer;
	},
	filteredGroups() {
		if (this.groupsSearchQuery != null && this.groupsSearchQuery != '') {		
			return this.priceGroups.filter(group =>
			group.groupName.toLowerCase().includes(this.groupsSearchQuery.toLowerCase())
			);
		}
		return this.priceGroups;
	},
	paginatedGroups() {
			let start = (this.currentGroupPage - 1) * this.groupsPageSize;
			let end = start + this.groupsPageSize;
			
			
			return this.filteredGroups.slice(start, end);
		},
	sortedPriceList() {
		
		let sortedPrices =[...this.priceList.prices];
		return sortedPrices.sort((a, b) => {
			let modifier = 1;
			if (this.currentPriceListSortDir === 'desc') modifier = -1;
			let valA = "";
			let valB = "";
			if (this.currentPriceListSort === 'name') {
				valA = a.item.name;
				valB = b.item.name;
			}
			else if (this.currentPriceListSort === 'amount') {
				valA = a.item.amount;
				valB = b.item.amount;
			}
			else if (this.currentPriceListSort === 'unit') {
				valA = a.item.unit.name;
				valB = b.item.unit.name;
			}
			else if (this.currentPriceListSort === 'itemCount') {
				valA = a.item.itemCount;
				valB = b.item.itemCount;
			}
			else if (this.currentPriceListSort === 'metal') {
				valA = a.item.itemType.material.name;
				valB = b.item.itemType.material.name;
			}
			else if (this.currentPriceListSort === 'itemStorage') {
				valA = a.item.itemStorage ? a.item.itemStorage.name: ''; 
				valB = b.item.itemStorage ? b.item.itemStorage.name: '';
			}							
			else {
				valA = a[this.currentPriceListSort];
				valB = b[this.currentPriceListSort];
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
	filteredPriceList() {
		if (this.searchQueryPriceList != null && this.searchQueryPriceList != '') {		
			return this.sortedPriceList.filter(price =>
			price.item.name.toLowerCase().includes(this.searchQueryPriceList.toLowerCase())
			);
		}
		return this.sortedPriceList;
	},
	paginatedPriceList() {
		let result = {};
		result["totalPrice"] = this.priceList.totalPrice;
		let start = (this.currentPriceListPage - 1) * this.priceListPageSize;
		let end = start + this.priceListPageSize;
		result["prices"]= this.filteredPriceList.slice(start, end);
		return result;
	},
	totalPriceListPages() {
		return Math.ceil(this.filteredPriceList.length / this.priceListPageSize);
	},
	currentPriceListPage() {
		if(this.currentPricePageNumber > this.totalPriceListPages){
			return 1;
		}
		return this.currentPricePageNumber;
	},
},

  mounted() {
    this.currentFilter=this.getCurrentFilter();
    this.currentViewType=this.getCurrentViewType();
	this.currentPriceListSort=localStorage.getItem("PriceListColumnsSort")?localStorage.getItem("PriceListColumnsSort"):"name";
	this.currentPriceListSortDir = localStorage.getItem("PriceListColumnsSortDir")?localStorage.getItem("PriceListColumnsSortDir"):"asc";
    this.fetchData();

  },
  methods: {
	findPricesGroupforPriceGroupName(priceGroupName){
		if(priceGroupName != null && priceGroupName !=''){
			return this.priceGroups.find(pg=>pg.groupName === priceGroupName);
		}
		return null;
	},
	filteredGroupPrices(groupName){
		let pg = this.findPricesGroupforPriceGroupName(groupName);
		if(!pg){
			throw new Error("invalid group name");
		}
		if(this.groupsListQuerys[pg.groupName] != null && this.groupsListQuerys[pg.groupName] != ''){
			return this.sortedGroupPriceList(pg.groupName,pg.prices).filter(price =>price.item.name.toLowerCase().includes(this.groupsListQuerys[pg.groupName].toLowerCase()));	
		}
		return this.sortedGroupPriceList(pg.groupName,pg.prices);
	},
	
	getGroupPricesSortForGroup(groupName){ 
		if(this.groupsListSort[groupName] == null || this.groupsListSort[groupName] == ''){
			return "name";
		}
		return this.groupsListSort[groupName];
	},
	getGroupPricesSortDirForGroup(groupName){ 
		if(this.groupsListSortDir[groupName] == null || this.groupsListSortDir[groupName] == ''){
			return "desc";
		}
		return this.groupsListSortDir[groupName];
	},	
	sortGroupPricesBy(column, groupName){
		let pg = this.findPricesGroupforPriceGroupName(groupName);
		if(!pg){
			throw new Error("invalid group name");
		}			
		if (this.getGroupPricesSortForGroup(groupName) === column) {
			this.groupsListSortDir[groupName] = this.getGroupPricesSortDirForGroup(groupName)=== 'asc' ? 'desc' : 'asc';
			}
		this.groupsListSort[groupName]=column;
				
	},
	sortedGroupPriceList(groupName, prices) {
		const sortDir =this.getGroupPricesSortDirForGroup(groupName);
		const groupSort = this.getGroupPricesSortForGroup(groupName);
		let sortedPrices =[...prices];
		return sortedPrices.sort((a, b) => {
			let modifier = 1;
			if (sortDir === 'desc') modifier = -1;
			let valA = "";
			let valB = "";
			if (groupSort === 'name') {
				valA = a.item.name;
				valB = b.item.name;
			}
			else if (groupSort === 'amount') {
				valA = a.item.amount;
				valB = b.item.amount;
			}
			else if (groupSort === 'unit') {
				valA = a.item.unit.name;
				valB = b.item.unit.name;
			}
			else if (groupSort === 'itemCount') {
				valA = a.item.itemCount;
				valB = b.item.itemCount;
			}
			else if (groupSort === 'metal') {
				valA = a.item.itemType.material.name;
				valB = b.item.itemType.material.name;
			}
			else if (groupSort === 'itemStorage') {
				valA = a.item.itemStorage ? a.item.itemStorage.name: ''; 
				valB = b.item.itemStorage ? b.item.itemStorage.name: '';
			}								
			else {
				valA = a[groupSort];
				valB = b[groupSort];
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
	nextGroupPage() {
		if (this.currentGroupPage < this.totalGroupPages) {
			this.groupsCurrentPageNumer = this.currentGroupPage+1;
		}
	},
	prevGroupPage() {
		
		if (this.currentGroupPage > 1) {
			this.groupsCurrentPageNumer = this.currentGroupPage-1;
		}
		
	},		
    getCurrentFilter(){
      return localStorage.getItem("PriceListFilter")?localStorage.getItem("PriceListFilter"):"";
    },

    setCurrentFilter(event){
      localStorage.setItem("PriceListFilter",event.target.value);
      this.currentFilter=event.target.value;
      this.fetchData();
    },
    getCurrentViewType(){
      return localStorage.getItem("PriceViewType")?localStorage.getItem("PriceViewType"):"PriceList";
    },
    setCurrentViewType(event){

      localStorage.setItem("PriceViewType",event.target.value);
      this.currentViewType=event.target.value;
      this.fetchData();
    },
    async fetchData() {
      this.errorMessage='';
      this.priceList={totalPrice:0, prices:[]};
      this.priceGroups=[];
	  this.priceGroupNames=[];
      var errorMessage="Error fetching data. Please try again later.";

      if(this.currentViewType === "PriceList"){
         console.log("PriceList");
         this.fetchPriceList();
		 this.currentPricePageNumber=1;
      }
      else{
        this.fetchPriceGroupsByMetal();
      }

      try{
        errorMessage="Error fetching metals. Please try again later.";
		
        const response = await axios.get(`/materials`);
        let metals = response.data.map(metal => ({
          id: metal.id,
          name:  metal.name,
		  type: 'Metal',
		  typeName: 'Metal',
        }));
		const itemStoragesresponse = await axios.get(`/itemStorages`);
		let itemStorages = itemStoragesresponse.data.map(storage => ({
		          id: storage.id,
		          name:  storage.name,
				  type: 'ItemStorage',
				  typeName: 'Item Storage'
			  }));
		this.filters =[...metals,...itemStorages];
		
      } catch (error) {
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },
   async fetchPriceList(){
      var errorMessage="Error fetching data. Please try again later.";
      try{
          if(this.currentFilter != "" && this.currentFilter != null){

            errorMessage="No price for selected filter available.";
            console.log('currentFilter:',this.currentFilter);
			let filterArray = this.currentFilter.split(":");
			if(filterArray.length == 2){
			let path =`/prices/itemStorage/`;
			if(filterArray[0] === 'Metal'){
				path =`/prices/material/`;
			}
            const response = await axios.get(path+filterArray[1]);

            this.priceList = response.data;
            console.log('priceList:',this.priceList);
			}
			else{
				throw new Error("Invalid pricelist filter.");
			}
          }
          else{

            const response =  await axios.get(`/prices`);

            this.priceList = response.data;
            console.log('priceList:',this.priceList);
          }
      }
      catch(error){
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },

   async fetchPriceGroupsByMetal(){
      var errorMessage="No price groups available.";
      try{
         console.log('currentFilter:',this.currentFilter);
         const response = await axios.get(`/prices/groupBy/material`);
         this.priceGroups = response.data.priceGroups;
		 this.priceGroups.forEach(pg=>this.priceGroupNames.push(pg.getGroupName));
         console.log('priceGroups:',this.priceGroups);

      }
      catch(error){
        console.error('Error fetching data:', error);
        this.setErrorMessage(error,errorMessage);
      }
    },
    formatPrice(value) {
      return Number(value).toFixed(2)
    },

    setErrorMessage(error, defaultMessage){
          if( error.response != null &&  error.response.data != null  && error.response.data.message != null){
            this.errorMessage =error.response.data.message;
          }
          else{
            this.errorMessage=defaultMessage;
          }
    },
	sortPriceListBy(column){			
		if (this.currentPriceListSort === column) {
			this.currentPriceListSortDir = this.currentPriceListSortDir === 'asc' ? 'desc' : 'asc';
			localStorage.setItem("PriceListColumnsSortDir",this.currentSortDir )				
		}
		this.currentPriceListSort=column;
		localStorage.setItem("PriceListColumnsSort",column);
		
	},
	nextPriceListPage() {
		if (this.currentPriceListPage < this.totalPriceListPages) {
			this.currentPricePageNumber = this.currentPriceListPage+1;
		}
	},
	prevPriceListPage() {
		
		if (this.currentPriceListPage > 1) {
			this.currentPricePageNumber = this.currentPriceListPage-1;
		}
		
	}		
  }
};
</script>
