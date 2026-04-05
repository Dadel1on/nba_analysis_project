import { getApi, postApi } from './http'
import type {
  ApiEnvelope,
  PredictionExplainability,
  MatchPredictionRequest,
  MatchPredictionResult,
  PlayerPredictionResult,
  SeasonTrendPredictionResult,
} from './types'

function normalizePrediction(payload: unknown): PlayerPredictionResult {
  if (!payload || typeof payload !== 'object') {
    throw new Error('Invalid prediction response')
  }

  const maybe = payload as PlayerPredictionResult & ApiEnvelope<PlayerPredictionResult>
  return maybe.data || maybe
}

export async function predictPlayerPerformance(playerId: number): Promise<PlayerPredictionResult> {
  const data = await postApi<PlayerPredictionResult | ApiEnvelope<PlayerPredictionResult>>('/api/prediction/player', {
    player_id: playerId,
  })
  return normalizePrediction(data)
}

export async function predictMatchOutcome(payload: MatchPredictionRequest): Promise<MatchPredictionResult> {
  const data = await postApi<MatchPredictionResult | ApiEnvelope<MatchPredictionResult>>('/api/prediction/match', payload)
  const maybe = data as MatchPredictionResult & ApiEnvelope<MatchPredictionResult>
  const result = maybe.data || maybe
  if (!result || typeof result !== 'object') {
    throw new Error('Invalid match prediction response')
  }
  return result
}

const TREND_FALLBACK: SeasonTrendPredictionResult = {
  player_id: 0,
  history: [
    { season: '2021', points: 22.5, rebounds: 6.8, assists: 5.4 },
    { season: '2022', points: 26.4, rebounds: 7.5, assists: 6.4 },
    { season: '2023', points: 24.2, rebounds: 6.9, assists: 5.8 },
  ],
  forecast: [
    { season: '2024', points: 28.2, rebounds: 8.0, assists: 7.0 },
    { season: '2025', points: 29.5, rebounds: 8.4, assists: 7.5 },
  ],
}

export async function predictSeasonTrend(playerId: number): Promise<SeasonTrendPredictionResult> {
  try {
    const data = await postApi<SeasonTrendPredictionResult | ApiEnvelope<SeasonTrendPredictionResult>>('/api/prediction/season', {
      player_id: playerId,
    })
    const maybe = data as SeasonTrendPredictionResult & ApiEnvelope<SeasonTrendPredictionResult>
    const result = maybe.data || maybe
    if (result?.history?.length || result?.forecast?.length) {
      return {
        ...result,
        player_id: result.player_id || playerId,
      }
    }
  } catch {
    // Keep UI usable before season endpoint is fully implemented.
  }

  return {
    ...TREND_FALLBACK,
    player_id: playerId,
  }
}

export async function getPredictionExplainability(playerId: number): Promise<PredictionExplainability> {
  try {
    const data = await getApi<PredictionExplainability | ApiEnvelope<PredictionExplainability>>('/api/prediction/explain/player', {
      params: { player_id: playerId },
    })
    const maybe = data as PredictionExplainability & ApiEnvelope<PredictionExplainability>
    const result = maybe.data || maybe
    if (result?.key_factors?.length) {
      return result
    }
  } catch {
    // Keep UI usable before explainability endpoint is fully implemented.
  }

  return {
    player_id: playerId,
    model_name: 'Baseline-Regressor',
    model_version: 'v1.0-fallback',
    key_factors: [
      { factor: '近5场得分均值', contribution: 0.32, direction: 'positive' },
      { factor: '最近背靠背疲劳系数', contribution: -0.18, direction: 'negative' },
      { factor: '对手防守效率', contribution: -0.14, direction: 'negative' },
      { factor: '主客场修正项', contribution: 0.11, direction: 'positive' },
    ],
  }
}
