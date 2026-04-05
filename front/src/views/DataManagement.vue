<template>
  <div class="data-management">
    <h1>后台数据管理</h1>

    <el-alert
      v-if="message"
      :title="message"
      :type="messageType"
      show-icon
      style="margin-bottom: 12px"
    />

    <el-upload
      class="upload-demo"
      drag
      action="/api/admin/upload"
      multiple
      :before-upload="beforeUpload"
      :on-success="onUploadSuccess"
      :on-error="onUploadError"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        将文件拖到此处，或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          请上传不超过 500kb 的 CSV 文件
        </div>
      </template>
    </el-upload>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="header-row">
          <span>最近上传记录</span>
          <el-button size="small" @click="loadHistory">刷新</el-button>
        </div>
      </template>

      <el-table :data="history" v-loading="loading" border>
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column prop="rows" label="行数" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUploadHistory } from '@/api/admin'
import type { UploadHistoryItem } from '@/api/types'

const loading = ref(false)
const history = ref<UploadHistoryItem[]>([])
const message = ref('')
const messageType = ref<'success' | 'error'>('success')

const statusTag = (status: UploadHistoryItem['status']) => {
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  return 'warning'
}

const beforeUpload = (file: File) => {
  const isCsv = file.name.toLowerCase().endsWith('.csv')
  const lessThan500kb = file.size / 1024 < 500
  if (!isCsv) {
    messageType.value = 'error'
    message.value = '仅支持 CSV 文件'
    return false
  }
  if (!lessThan500kb) {
    messageType.value = 'error'
    message.value = '文件大小不能超过 500kb'
    return false
  }
  return true
}

const onUploadSuccess = () => {
  messageType.value = 'success'
  message.value = '上传成功，后台正在处理数据。'
  loadHistory(true)
}

const onUploadError = () => {
  messageType.value = 'error'
  message.value = '上传失败，请检查后端服务与文件内容。'
}

const loadHistory = async (silent = false) => {
  loading.value = true
  try {
    history.value = await getUploadHistory()
    if (!silent) {
      ElMessage.success('上传历史已更新')
    }
  } catch (err) {
    if (!silent) {
      ElMessage.error('获取历史失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHistory(true)
})
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
