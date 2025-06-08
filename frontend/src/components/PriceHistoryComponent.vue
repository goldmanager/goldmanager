<template>
  <div class="main">
    <div class="content">
      <div><h1>Price History</h1></div>
      <div
        v-if="errorMessage"
        class="error"
      >
        {{ errorMessage }}
      </div>
   
	
      <table v-if="metals.length>0"> 
        <thead>
          <tr>
            <th>Select Metal</th>
            <th>Date Range</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <select
                id="metals"
                v-model="currentMetal"
                @change="setCurrentFilter($event)"
              >
                <option
                  v-for="metal in metals"
                  :key="metal.id"
                  :value="metal.id"
                >
                  {{ metal.name }}
                </option>
              </select>
            </td>
            <td>
              <el-date-picker
                v-model="dateRange"
                type="datetimerange"
                range-separator="to"
                start-placeholder="Begin Date"
                end-placeholder="Ende Date"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss.SSS"
                :clearable="false"
                :unlink-panels="true"
                :shortcuts="dateTimeShortcuts"
                :arrow-control="true"
                @change="dateChanged()"
              />
            </td>
            <td>
              <button
                v-if="priceHistories.length>0"
                class="actionbutton"
                @click="deleteHistoryInRange()"
              >
                Delete Selected Price History
              </button>
              <button
                class="actionbutton"
                @click="deleteMetalhistory()"
              >
                Delete complete Metal Price History
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <price-chart
        v-if="priceHistories.length>0"
        v-model="priceHistories"
      />
      <div v-else>
        No Price History available in selected date range.
      </div>
    </div>
  </div>
</template>

<script>
/*eslint no-mixed-spaces-and-tabs: ["error", "smart-tabs"]*/
import axios from '../axios';
import PriceChart from './PriceChart.vue';
export default {
  name: 'PricesComponent',
  components: {
        PriceChart,
      },
  data() {
    return {
		errorMessage:null,
		metals: [],
		currentMetal:'',
		priceHistories:[],
		dateRange: this.getDefaultDateRange(),

    };

  },

computed:{ 
	
	dateTimeShortcuts(){
		let currentDate = new Date();
		let date24HoursAgo = new Date();
		let today= new Date();
		
		today.setHours(0);
		today.setMinutes(0);
		today.setSeconds(0);
		today.setMilliseconds(0);
		let daysSinceStartOfWeek = (today.getDay()+6)%7;
		let startOfweek = new Date();
		startOfweek.setTime(today.getTime());
		startOfweek.setDate(today.getDate()-daysSinceStartOfWeek);
		date24HoursAgo.setHours(date24HoursAgo.getHours()-24);
		let currentYear = currentDate.getFullYear();
		
		let oneWeekAgo = new Date();
		oneWeekAgo.setTime(oneWeekAgo.getTime());
		oneWeekAgo.setDate(oneWeekAgo.getDate()-7);
		
		let oneMonthAgo = new Date();
		oneMonthAgo.setMonth(oneMonthAgo.getMonth()-1);
		
		let oneYearAgo = new Date();
		oneYearAgo.setFullYear(oneYearAgo.getFullYear()-1);
		
		return [
			{
				text: 'This Year',
				value: [new  Date(currentYear, 0, 1), currentDate],
			},
			{
				text: 'This Week',
				value: [startOfweek, currentDate],
			},
			{
				text: 'This Month',
				value: [new Date(currentYear,currentDate.getMonth(),1), currentDate],
			},
			{
				text: 'Today',
				value: [today, currentDate],
			},
			{
				text: 'One Day',
				value: [date24HoursAgo, currentDate],
			},
			{
				text: 'One Week',
				value: [oneWeekAgo, currentDate],
			},
			{
				text: 'One Month',
				value: [oneMonthAgo, currentDate],
			},
			{
				text: 'One Year',
				value: [oneYearAgo, currentDate],
			},
			
		];
	}
},
 

  mounted() {
	this.currentMetal = this.getCurrentMetal();
	this.fetchData();
  },  
methods: {
	
	formatDate(date) {
	    return new Date(date).toLocaleString();
		
	 },
	getDefaultDateRange(){
		return 	[this.formatDateCustom(new Date(new Date().getFullYear(),0,1)), this.formatDateCustom(new Date())];

	},
	formatDateCustom(date){
		var month=date.getMonth()+1;
		var day=date.getDate();
		var hour =date.getHours();
		var minutes =date.getMinutes();
		var seconds =date.getSeconds();
		if(month <10){
			month ="0"+month;
		}
		if(day <10){
			day ="0"+day
		}
		if(hour <10){
			hour="0"+hour;
	     }
	     if(minutes <10){
			minutes="0"+minutes;
		}
		if(seconds <10){
			seconds="0"+seconds;
		}
	
	     return `${date.getFullYear()}-${month}-${day}T${hour}:${minutes}:${minutes}.000`;
	},
	
	addOffsetToDateString(dateString){
		let offset = new Date().getTimezoneOffset();
		let hours = String(Math.floor(Math.abs(offset) / 60)).padStart(2, '0');
		let minutes = String(Math.abs(offset) % 60).padStart(2, '0');
		let sign = offset > 0 ? "-" : "%2b";
		let timeZoneFormat = `${sign}${hours}:${minutes}`;
		return `${dateString}${timeZoneFormat}`;
	 },
	 getCurrentMetal(){
	      return localStorage.getItem("PriceHistoryCurrentMetal")?localStorage.getItem("PriceHistoryCurrentMetal"):"";
	  },
	setCurrentFilter(event){
		localStorage.setItem("PriceHistoryCurrentMetal",event.target.value);
		this.currentMetal=event.target.value;
		this.fetchData();
	},
	dateChanged(){
			this.fetchPriceHistories();		
	},
	async fetchData() {
		this.errorMessage='';
		var errorMessage="Error fetching data. Please try again later.";
		try{
			errorMessage="Error fetching metals. Please try again later.";
			const metalsResponse = await axios.get(`/materials`);
			this.metals = metalsResponse.data.map(metal => ({
				id: metal.id,
				name:  metal.name,
			}));
			if((!this.currentMetal || this.currentMetal == '') && this.metals.length>0){
				this.currentMetal=this.metals[0].id;
			}
			if(this.metals.length === 0){ 
				throw new Error("No metal available");
			}
			this.fetchPriceHistories();
		} catch (error) {
			console.error('Error fetching data:', error);
			this.setErrorMessage(error,errorMessage);
		}
	},
	async fetchPriceHistories(){
		this.errorMessage='';
		var errorMessage="Error fetching price history. Please try again later.";
		this.priceHistories =[];
		try{
			let startDate =this.addOffsetToDateString(this.dateRange[0]);
			let endDate =this.addOffsetToDateString(this.dateRange[1]);
			const priceHistoryResponse = await axios.get(`/priceHistory/${this.currentMetal}?startDate=${startDate}&endDate=${endDate}`);
			this.priceHistories= priceHistoryResponse.data.priceHistories.reverse().map(priceHistory=>(
			{date: this.formatDate(priceHistory.date), totalPrice:priceHistory.priceList.totalPrice, metalPrice: priceHistory.materialPrice,materialHistoryId:priceHistory.materialHistoryId}));
			
		}
		catch(error){
			console.error('Error fetching data:', error);
			this.setErrorMessage(error,errorMessage);
		}
	},
	async deleteHistoryInRange(){
			if (window.confirm('Are you sure you want to delete the selected price history?')) {
				this.errorMessage='';
				var errorMessage="Error deleting price history. Please try again later.";
				let startDate =this.addOffsetToDateString(this.dateRange[0]);
				let endDate =this.addOffsetToDateString(this.dateRange[1]);
				try{
					
					 await axios.delete(`/materialHistory/byMaterial/${this.currentMetal}?startDate=${startDate}&endDate=${endDate}`);
				}
				catch(error){
					console.error('Error deleting price history range:', error);
					this.setErrorMessage(error,errorMessage);
				}
				this.priceHistories=[];
				
			}
	},
			
	async deleteMetalhistory(){
		if (window.confirm('Are you sure you want to delete the complete price history of the selected metal?')) {
			this.errorMessage='';
			var errorMessage="Error deleting metal price history. Please try again later.";
			try{
				 await axios.delete(`/materialHistory/byMaterial/${this.currentMetal}`);
			}
			catch(error){
				console.error('Error deleting metal price history range:', error);
				this.setErrorMessage(error,errorMessage);
			}
			this.priceHistories=[];
			
		}
	},
	setErrorMessage(error, defaultMessage){
		if( error.response != null &&  error.response.data != null  && error.response.data.message != null){
			this.errorMessage =error.response.data.message;
		}
		else{
			this.errorMessage=defaultMessage;
		}
	},
}
};
</script>
