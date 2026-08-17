<script setup lang="ts">
import { computed } from 'vue'

import type { CollectionGraphConfig, ResearchJobDetail } from '../model/research'

const props = defineProps<{
  job: ResearchJobDetail
}>()

const config = computed(() => props.job.collectionConfig)
const seedAsins = computed(() => props.job.seedAsins ?? [])

function value(path: (string | number)[], fallback = '--') {
  let current: unknown = config.value
  for (const segment of path) {
    if (typeof current !== 'object' || current === null) return fallback
    current = (current as Record<string | number, unknown>)[segment]
  }
  if (current === null || current === undefined || current === '') return fallback
  if (Array.isArray(current)) return current.length > 0 ? current.join('、') : '不限'
  return String(current)
}

function collectionConfigAvailable(
  collectionConfig: CollectionGraphConfig | undefined,
): collectionConfig is CollectionGraphConfig {
  return Boolean(collectionConfig)
}
</script>

<template>
  <div
    class="task-input-summary"
    data-testid="research-task-input-summary"
  >
    <div class="task-input-summary__heading">
      <div>
        <h2>本次任务参数</h2>
        <span>创建时已固化，只读</span>
      </div>
      <ElTag
        size="small"
        type="info"
        effect="plain"
      >
        执行快照
      </ElTag>
    </div>

    <ElDescriptions
      class="task-input-summary__base"
      :column="1"
      size="small"
      border
    >
      <ElDescriptionsItem label="报告名称">
        {{ job.reportName }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="市场 / 月份">
        {{ job.marketplace }} / {{ job.month }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="类目路径">
        <code>{{ job.nodeIdPath }}</code>
      </ElDescriptionsItem>
      <ElDescriptionsItem label="核心关键词">
        {{ job.keyword || '未填写' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="分析目标">
        {{ job.analysisGoal || '未填写' }}
      </ElDescriptionsItem>
      <ElDescriptionsItem label="种子 ASIN">
        <div
          v-if="seedAsins.length > 0"
          class="task-input-summary__asins"
        >
          <ElTag
            v-for="asin in seedAsins"
            :key="asin"
            size="small"
            effect="plain"
          >
            {{ asin }}
          </ElTag>
        </div>
        <span v-else>未填写</span>
      </ElDescriptionsItem>
    </ElDescriptions>

    <section class="task-input-summary__collection">
      <header>
        <h3>采集节点参数</h3>
        <span>本任务实际使用值</span>
      </header>
      <ElEmpty
        v-if="!collectionConfigAvailable(config)"
        :image-size="42"
        description="该历史任务未返回采集参数快照"
      />
      <dl v-else>
        <div>
          <dt>商品池</dt>
          <dd>目标 {{ value(['collectProducts', 'pagination', 'targetCount']) }}，每页 {{ value(['collectProducts', 'pagination', 'pageSize']) }}，补充 ASIN {{ value(['collectProducts', 'enrichmentAsinLimit']) }}</dd>
        </div>
        <div>
          <dt>销售趋势</dt>
          <dd>{{ value(['collectMarketSalesTrend', 'monthCount']) }} 个月</dd>
        </div>
        <div>
          <dt>需求趋势</dt>
          <dd>样本 {{ value(['collectKeywordDemandTrend', 'topN']) }}</dd>
        </div>
        <div>
          <dt>细分市场</dt>
          <dd>目标 {{ value(['collectSegmentOpportunity', 'pagination', 'targetCount']) }}，每页 {{ value(['collectSegmentOpportunity', 'pagination', 'pageSize']) }}，分布 Top {{ value(['collectSegmentOpportunity', 'distribution', 'topN']) }}</dd>
        </div>
        <div>
          <dt>评论</dt>
          <dd>每个 ASIN {{ value(['collectReviews', 'pagination', 'targetCountPerAsin']) }} 条，每页 {{ value(['collectReviews', 'pagination', 'pageSize']) }}；星级 {{ value(['collectReviews', 'starList'], '不限') }}</dd>
        </div>
        <div>
          <dt>关键词情报</dt>
          <dd>流量词 ASIN {{ value(['collectKeywordIntelligence', 'trafficAsinLimit']) }}，选品每页 {{ value(['collectKeywordIntelligence', 'keywordResearch', 'size']) }}，挖掘每页 {{ value(['collectKeywordIntelligence', 'keywordMiner', 'size']) }}</dd>
        </div>
      </dl>
    </section>
  </div>
</template>

<style scoped>
.task-input-summary {
  display: grid;
  min-width: 0;
  gap: 18px;
}

.task-input-summary__heading,
.task-input-summary__heading > div,
.task-input-summary__collection,
.task-input-summary__collection header {
  min-width: 0;
}

.task-input-summary__heading,
.task-input-summary__collection header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.task-input-summary__heading h2,
.task-input-summary__collection h3 {
  margin: 0;
  color: var(--color-text);
  letter-spacing: 0;
}

.task-input-summary__heading h2 {
  font-size: 16px;
}

.task-input-summary__collection h3 {
  font-size: 14px;
}

.task-input-summary__heading span,
.task-input-summary__collection header span {
  display: block;
  margin-top: 4px;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.task-input-summary__base :deep(.el-descriptions__label) {
  width: 96px;
}

.task-input-summary code {
  color: var(--color-text-secondary);
  font: 11px/1.5 var(--font-mono);
  overflow-wrap: anywhere;
}

.task-input-summary__asins {
  display: flex;
  min-width: 0;
  flex-wrap: wrap;
  gap: 6px;
}

.task-input-summary__collection {
  display: grid;
  gap: 10px;
}

.task-input-summary__collection dl {
  display: grid;
  margin: 0;
  border-top: 1px solid var(--color-border);
}

.task-input-summary__collection dl > div {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}

.task-input-summary__collection dt {
  color: var(--color-text);
  font-size: 12px;
  font-weight: 650;
}

.task-input-summary__collection dd {
  min-width: 0;
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 11px;
  line-height: 1.6;
  overflow-wrap: anywhere;
}
</style>
