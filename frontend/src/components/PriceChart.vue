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
    visible: {
      type: Object,
      default: () => ({ totalPrice: true, metalPrice: true })
    },
  },
  emits: ['update:modelValue', 'legend-visibility-change'],
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
            hidden: this.visible ? !this.visible.totalPrice : false,
          },
          {
			label: 'Metal Price',
			backgroundColor: '#9A9A9A',
			borderColor: '#9A9A9A',
			data: this.modelValue.map(item => item.metalPrice),
            hidden: this.visible ? !this.visible.metalPrice : false,
          },
        ],
      };
	return result;
    },
    chartOptions() {
      // Mirror Chart.js default legend toggle behavior, then emit visibility state
      const self = this;
      return {
        responsive: true,
        plugins: {
          legend: {
            onClick(e, legendItem, legend) {
              const index = legendItem.datasetIndex;
              const ci = legend.chart;
              const meta = ci.getDatasetMeta(index);
              // Toggle hidden like default
              meta.hidden = meta.hidden === null ? !ci.data.datasets[index].hidden : null;
              ci.update();

              // Emit current visibility so parent can persist
              const visibility = {
                totalPrice: !ci.getDatasetMeta(0).hidden,
                metalPrice: !ci.getDatasetMeta(1).hidden,
              };
              self.$emit('legend-visibility-change', visibility);
            },
          },
        },
      };
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
