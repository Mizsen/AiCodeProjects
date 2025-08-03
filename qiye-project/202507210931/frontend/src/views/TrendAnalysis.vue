<template>
  <div class="trend-analysis">
    <el-card shadow="hover" class="trend-card">
      <div class="trend-header-col">
        <div class="trend-title">职位趋势分析</div>
        <div class="trend-filters-row">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="max-width: 320px;"
            @change="onDateChange"
          />
          <el-select v-model="selectedCity" placeholder="选择城市" class="trend-select" clearable @change="onCityChange">
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
          <el-select v-model="selectedType" placeholder="选择职位类型" class="trend-select" clearable @change="onFilterChange">
            <el-option v-for="type in types" :key="type" :label="type" :value="type" />
          </el-select>
        </div>
      </div>
      <div ref="chartRef" class="trend-chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts/index';
import { jobApi } from '@/api/index';

import dayjs from 'dayjs';

const chartRef = ref(null);
const chartInstance = ref(null);
const trendData = ref([]);
const cities = ref([]);
const types = ref([]);
const selectedCity = ref('');
const selectedType = ref('');

const dateRange = ref([]);

const fetchData = async () => {
  let startDate = dateRange.value?.[0] || '';
  let endDate = dateRange.value?.[1] || '';
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  const res = await jobApi.getJobTrendData(params);
  trendData.value = res.data || [];
  updateFilters();
  renderChart();
};

const onDateChange = () => {
  fetchData();
};

const updateFilters = () => {
  // 获取所有城市，去除空格
  const citySet = new Set();
  trendData.value.forEach(item => {
    citySet.add((item.city || '').trim());
  });
  cities.value = Array.from(citySet);
  // 获取当前城市下的职位类型
  let typeSet = new Set();
  if (selectedCity.value) {
    trendData.value.forEach(item => {
      if ((item.city || '').trim() === selectedCity.value.trim()) {
        typeSet.add((item.query_type || '').trim());
      }
    });
  } else {
    trendData.value.forEach(item => {
      typeSet.add((item.query_type || '').trim());
    });
  }
  types.value = Array.from(typeSet);
  // 选中项也做 trim，防止下拉和数据分组不一致
  if (selectedCity.value) selectedCity.value = selectedCity.value.trim();
  if (selectedType.value) selectedType.value = selectedType.value.trim();
  // 如果当前选中的类型不在新列表里，自动清空
  if (selectedType.value && !types.value.includes(selectedType.value)) {
    selectedType.value = '';
  }
};


const onCityChange = () => {
  updateFilters();
  // 切换城市时重置类型选择
  selectedType.value = '';
  renderChart();
};

const onFilterChange = () => {
  renderChart();
};

const renderChart = () => {
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value);
  }
  // 每次渲染前清空，防止曲线叠加
  chartInstance.value.clear();
  if (!trendData.value.length) {
    return;
  }
  // 过滤和清洗数据，去除空格
  let filtered = trendData.value.map(i => ({
    ...i,
    city: (i.city || '').trim(),
    query_type: (i.query_type || '').trim(),
    date: (i.date || '').trim()
  }));
  if (selectedCity.value) {
    filtered = filtered.filter(i => i.city === selectedCity.value.trim());
  }
  if (selectedType.value) {
    filtered = filtered.filter(i => i.query_type === selectedType.value.trim());
  }
  // ...existing code...
  // 计算横轴
  const dateSet = Array.from(new Set(filtered.map(i => i.date))).sort();
  // 生成 series
  let series = [];
  let legendData = [];
  if (selectedCity.value && selectedType.value) {
    // 先筛选city和type，只生成一条线
    const city = selectedCity.value;
    const type = selectedType.value;
    series = [{
      name: city + '-' + type,
      type: 'line',
      smooth: true,
      connectNulls: true,
      label: {
        show: false,
        position: 'top',
        formatter: function(params) {
          if (params.value == null) return '';
          return params.seriesName + ': ' + params.value;
        }
      },
      emphasis: {
        label: {
          show: true,
          fontWeight: 'bold',
          color: '#333',
          position: 'top',
          formatter: function(params) {
            if (params.value == null) return '';
            return params.seriesName + ': ' + params.value;
          }
        }
      },
      data: dateSet.map(date => {
        const found = filtered.find(i => i.date === date && i.city === city && i.query_type === type);
        return found ? found.job_count : null;
      })
    }];
    legendData = [city + '-' + type];
  } else if (selectedCity.value) {
    // 只选城市，type为一组
    const groupKeys = Array.from(new Set(filtered.map(i => i.query_type)));
    series = groupKeys.map(type => {
      legendData.push(type);
      return {
        name: type,
        type: 'line',
        smooth: true,
        connectNulls: true,
        label: {
          show: false,
          position: 'top',
          formatter: function(params) {
            if (params.value == null) return '';
            return params.seriesName + ': ' + params.value;
          }
        },
        emphasis: {
          label: {
            show: true,
            fontWeight: 'bold',
            color: '#333',
            position: 'top',
            formatter: function(params) {
              if (params.value == null) return '';
              return params.seriesName + ': ' + params.value;
            }
          }
        },
        data: dateSet.map(date => {
          const found = filtered.find(i => i.date === date && i.query_type === type);
          return found ? found.job_count : null;
        })
      };
    });
  } else if (selectedType.value) {
    // 只选类型，city为一组
    const groupKeys = Array.from(new Set(filtered.map(i => i.city)));
    series = groupKeys.map(city => {
      legendData.push(city);
      return {
        name: city,
        type: 'line',
        smooth: true,
        connectNulls: true,
        label: {
          show: false,
          position: 'top',
          formatter: function(params) {
            if (params.value == null) return '';
            return params.seriesName + ': ' + params.value;
          }
        },
        emphasis: {
          label: {
            show: true,
            fontWeight: 'bold',
            color: '#333',
            position: 'top',
            formatter: function(params) {
              if (params.value == null) return '';
              return params.seriesName + ': ' + params.value;
            }
          }
        },
        data: dateSet.map(date => {
          const found = filtered.find(i => i.date === date && i.city === city);
          return found ? found.job_count : null;
        })
      };
    });
  } else {
    // 全部，city-type为一组
    const groupKeys = Array.from(new Set(filtered.map(i => i.city + '-' + i.query_type)));
    series = groupKeys.map(key => {
      const [city, type] = key.split('-');
      legendData.push(city + '-' + type);
      return {
        name: city + '-' + type,
        type: 'line',
        smooth: true,
        connectNulls: true,
        label: {
          show: false,
          position: 'top',
          formatter: function(params) {
            if (params.value == null) return '';
            return params.seriesName + ': ' + params.value;
          }
        },
        emphasis: {
          label: {
            show: true,
            fontWeight: 'bold',
            color: '#333',
            position: 'top',
            formatter: function(params) {
              if (params.value == null) return '';
              return params.seriesName + ': ' + params.value;
            }
          }
        },
        data: dateSet.map(date => {
          const found = filtered.find(i => i.date === date && i.city === city && i.query_type === type);
          return found ? found.job_count : null;
        })
      };
    });
  }
  chartInstance.value.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: legendData, type: 'scroll', top: 0 },
    grid: { left: 40, right: 60, bottom: 60, top: 60 },
    xAxis: {
      type: 'category',
      data: dateSet,
      boundaryGap: false,
      axisLabel: {
        rotate: 45,
        interval: Math.ceil(dateSet.length / 10) - 1 > 0 ? Math.ceil(dateSet.length / 10) - 1 : 0 // 最多显示10个标签
      }
    },
    yAxis: { type: 'value', name: '职位数量' },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: 0,
        height: 18,
        bottom: 10,
        start: 0,
        end: dateSet.length > 10 ? 30 : 100 // 默认显示部分，用户可拖动
      },
      {
        type: 'inside',
        xAxisIndex: 0
      }
    ],
    series
  });
};


onMounted(async () => {
  // 默认近6个月
  const end = dayjs().format('YYYY-MM-DD');
  const start = dayjs().subtract(6, 'month').format('YYYY-MM-DD');
  dateRange.value = [start, end];
  await fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize();
  }
};
</script>

<style scoped>
  .trend-analysis {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    height: 100%;
    min-height: 0;
    min-width: 0;
    padding: 0;
    box-sizing: border-box;
  }
  .trend-card {
    width: 100%;
    height: 100%;
    min-height: 400px;
    border-radius: 16px;
    box-shadow: 0 4px 24px rgba(0,0,0,0.08);
    background: #fff;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }
  .trend-header-col {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 16px;
    gap: 12px;
    width: 100%;
  }
  .trend-title {
    font-size: 22px;
    font-weight: 600;
    color: #333;
    line-height: 1.2;
    margin-bottom: 0;
    text-align: center;
  }
  .trend-filters-row {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 16px;
    width: 100%;
    justify-content: flex-start;
  }
  .trend-select {
    min-width: 120px;
    max-width: 180px;
  }
  .trend-chart {
    flex: 1 1 auto;
    width: 100%;
    max-width: 1200px;
    min-width: 800px;
    min-height: 320px;
    height: 100%;
    max-height: none;
    box-sizing: border-box;
    margin: 0 auto;
  }

  @media (max-width: 900px) {
    .trend-card {
      min-height: 320px;
    }
    .trend-chart {
      min-height: 200px;
      height: 60vw;
      max-height: 400px;
    }
  }
  @media (max-width: 600px) {
    .trend-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    .trend-card {
      min-height: 200px;
      border-radius: 8px;
    }
    .trend-chart {
      height: 200px;
      min-height: 120px;
      max-height: 240px;
    }
  }
</style>
