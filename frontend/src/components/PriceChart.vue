<template>
  <div class="chart-container">
    <line-chart
      v-if="chartData"
      :data="chartData"
      :options="chartOptions"
    />
  </div>
</template>

<script>
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement);

export default {
  name: 'PriceChart',
  components: {
    LineChart: Line,
  },
  props: {
    modelValue: {
      type: Array,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  computed: {
    chartData() {
     let result = {
        labels: this.modelValue.map(item => item.date),
        datasets: [
          {
            label: 'Total Price',
            backgroundColor: '#D4AF37',
            borderColor: '#D4AF37',
            data: this.modelValue.map(item => item.totalPrice),
          },
          {
			label: 'Metal Price',
			backgroundColor: '#9A9A9A',
			borderColor: '#9A9A9A',
			data: this.modelValue.map(item => item.metalPrice),
          },
        ],
      };
	return result;
    },
  },
  methods: {
    updatePrices(newPrices) {
      this.$emit('update:modelValue', newPrices);
    },
  },
};
</script>

<style scoped>
.chart-container {
  position: relative;
  height: 100%;
  min-width: 100vh;
  width: 100%;
}
</style>
