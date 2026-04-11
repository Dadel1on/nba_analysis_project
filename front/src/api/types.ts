export interface ApiEnvelope<T> {
  code?: number
  message?: string
  data?: T
}

export interface PlayerStats {
  points: number
  rebounds: number
  assists: number
}

export interface PlayerSummary {
  id: number
  name: string
  team: string | null
  position: string | null
  stats: PlayerStats
}

export interface TeamSummary {
  id?: number
  name: string
  city: string
  conference: string
}

export interface TeamComparisonMetrics {
  team: string
  points: number
  rebounds: number
  assists: number
  wins: number
  winRate: number
}

export interface TeamComparisonResult {
  teams: TeamComparisonMetrics[]
}

export interface PlayerPredictionResult {
  player_id: number
  predicted_stats: PlayerStats
  confidence: number
}

export interface SearchPlayersParams {
  name?: string
  page?: number
  limit?: number
}

export interface DashboardStats {
  activePlayers: number
  teams: number
  seasonGames: number
  predictions: number
}

export interface TrendPoint {
  season: string
  avgPoints: number
}

export interface PositionDistributionItem {
  name: string
  value: number
}

export interface RecentGame {
  date: string
  homeTeam: string
  awayTeam: string
  score: string
}

export interface TopPlayer {
  id?: number
  name: string
  team: string
  points: number
}

export interface DashboardPayload {
  stats: DashboardStats
  trend: TrendPoint[]
  positionDistribution: PositionDistributionItem[]
  recentGames: RecentGame[]
  topPlayers: TopPlayer[]
}

export interface MatchPredictionRequest {
  home_team: string
  away_team: string
}

export interface MatchPredictionResult {
  home_team: string
  away_team: string
  home_win_probability: number
  away_win_probability: number
  confidence: number
  key_factors?: string[]
}

export interface SeasonTrendPoint {
  season: string
  points: number
  rebounds: number
  assists: number
}

export interface SeasonTrendPredictionResult {
  player_id: number
  history: SeasonTrendPoint[]
  forecast: SeasonTrendPoint[]
}

export interface ExplanationFactor {
  factor: string
  contribution: number
  direction: 'positive' | 'negative'
}

export interface PredictionExplainability {
  player_id: number
  model_name: string
  model_version: string
  key_factors: ExplanationFactor[]
}

export interface TeamCompareOptions {
  season?: number
  metric?: 'all' | 'offense' | 'defense'
}

export interface UploadHistoryItem {
  fileName: string
  rows: number
  status: 'success' | 'failed' | 'processing'
  createdAt: string
}

export interface SparkJobRunItem {
  id: number
  jobName: string
  status: 'success' | 'failed' | 'running' | 'skipped'
  createdAt: string
  updatedAt: string
  detail: string
}

export interface PredictionResultItem {
  id: number
  playerId: number
  playerName: string
  predictedPoints: number
  predictedRebounds: number
  predictedAssists: number
  confidence: number
  modelVersion: string
  createdAt: string
}
