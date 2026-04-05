import { getApi, postApi } from './http'
import type { PredictionResultItem, SparkJobRunItem, UploadHistoryItem } from './types'

const FALLBACK_HISTORY: UploadHistoryItem[] = [
  { fileName: 'players_2024.csv', rows: 4280, status: 'success', createdAt: '2026-03-23 21:20:00' },
  { fileName: 'games_2024.csv', rows: 1230, status: 'success', createdAt: '2026-03-23 21:19:12' },
  { fileName: 'injury_report.csv', rows: 184, status: 'processing', createdAt: '2026-03-24 09:10:43' },
]

const FALLBACK_SPARK_HISTORY: SparkJobRunItem[] = [
  { id: 1, jobName: 'NBA_MLlib_Analysis', status: 'success', createdAt: '2026-03-25 15:00:00', updatedAt: '2026-03-25 15:04:11', detail: 'Execution successful.' }
]

const FALLBACK_PREDICTIONS: PredictionResultItem[] = [
  { id: 1, playerId: 101, playerName: 'Stephen Curry', predictedPoints: 28.5, predictedRebounds: 5.2, predictedAssists: 6.8, confidence: 0.92, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 2, playerId: 102, playerName: 'LeBron James', predictedPoints: 25.1, predictedRebounds: 7.8, predictedAssists: 8.2, confidence: 0.88, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 3, playerId: 103, playerName: 'Kevin Durant', predictedPoints: 27.3, predictedRebounds: 6.5, predictedAssists: 4.1, confidence: 0.85, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 4, playerId: 104, playerName: 'Nikola Jokic', predictedPoints: 26.4, predictedRebounds: 12.1, predictedAssists: 9.5, confidence: 0.94, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 5, playerId: 105, playerName: 'Giannis Antetokounmpo', predictedPoints: 31.2, predictedRebounds: 11.5, predictedAssists: 5.4, confidence: 0.89, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 6, playerId: 106, playerName: 'Luka Doncic', predictedPoints: 33.5, predictedRebounds: 9.1, predictedAssists: 9.8, confidence: 0.95, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 7, playerId: 107, playerName: 'Joel Embiid', predictedPoints: 32.8, predictedRebounds: 10.2, predictedAssists: 4.2, confidence: 0.91, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 8, playerId: 108, playerName: 'Shai Gilgeous-Alexander', predictedPoints: 30.5, predictedRebounds: 5.5, predictedAssists: 6.2, confidence: 0.93, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 9, playerId: 109, playerName: 'Jayson Tatum', predictedPoints: 27.2, predictedRebounds: 8.1, predictedAssists: 4.9, confidence: 0.87, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 10, playerId: 110, playerName: 'Donovan Mitchell', predictedPoints: 26.8, predictedRebounds: 5.1, predictedAssists: 6.1, confidence: 0.86, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 11, playerId: 111, playerName: 'Anthony Edwards', predictedPoints: 25.9, predictedRebounds: 5.4, predictedAssists: 5.1, confidence: 0.84, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 12, playerId: 112, playerName: 'Tyrese Haliburton', predictedPoints: 20.1, predictedRebounds: 3.9, predictedAssists: 10.9, confidence: 0.88, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 13, playerId: 113, playerName: "De'Aaron Fox", predictedPoints: 26.6, predictedRebounds: 4.6, predictedAssists: 5.6, confidence: 0.83, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 14, playerId: 114, playerName: 'Trae Young', predictedPoints: 25.7, predictedRebounds: 2.8, predictedAssists: 10.8, confidence: 0.82, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' },
  { id: 15, playerId: 115, playerName: 'Damian Lillard', predictedPoints: 24.3, predictedRebounds: 4.4, predictedAssists: 7.0, confidence: 0.81, modelVersion: 'RF-v1.2', createdAt: '2026-03-25 15:04:11' }
]

export async function getUploadHistory(): Promise<UploadHistoryItem[]> {
  try {
    const data = await getApi<UploadHistoryItem[]>('/api/admin/upload/history')
    if (Array.isArray(data)) {
      return data
    }
  } catch {
    // Keep data management page demo-ready while endpoint is being integrated.
  }

  return FALLBACK_HISTORY
}

export async function runSparkJob(): Promise<void> {
  await postApi('/api/admin/spark/run', {})
}

export async function getSparkHistory(): Promise<SparkJobRunItem[]> {
  try {
    const data = await getApi<SparkJobRunItem[]>('/api/admin/spark/history')
    if (Array.isArray(data)) {
      return data
    }
  } catch {
    // fallback
  }
  return FALLBACK_SPARK_HISTORY
}

export async function getSparkPredictions(): Promise<PredictionResultItem[]> {
  try {
    const data = await getApi<PredictionResultItem[]>('/api/admin/spark/predictions')
    if (Array.isArray(data) && data.length > 0) {
      return data
    }
  } catch {
    // fallback
  }
  return FALLBACK_PREDICTIONS
}
