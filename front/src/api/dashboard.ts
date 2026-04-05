import { getApi } from './http'
import type { ApiEnvelope, DashboardPayload, RecentGame, TopPlayer } from './types'

const DEFAULT_DASHBOARD: DashboardPayload = {
  stats: {
    activePlayers: 450,
    teams: 30,
    seasonGames: 1230,
    predictions: 1525,
  },
  trend: [
    { season: '2014', avgPoints: 100.0 },
    { season: '2015', avgPoints: 102.7 },
    { season: '2016', avgPoints: 105.6 },
    { season: '2017', avgPoints: 106.3 },
    { season: '2018', avgPoints: 111.2 },
    { season: '2019', avgPoints: 111.8 },
    { season: '2020', avgPoints: 112.1 },
    { season: '2021', avgPoints: 110.6 },
    { season: '2022', avgPoints: 114.7 },
    { season: '2023', avgPoints: 115.4 },
  ],
  positionDistribution: [
    { value: 120, name: '后卫 (G)' },
    { value: 150, name: '前锋 (F)' },
    { value: 80, name: '中锋 (C)' },
    { value: 60, name: '后卫-前锋 (G-F)' },
    { value: 40, name: '前锋-中锋 (F-C)' },
  ],
  recentGames: [
    { date: '2023-10-24', homeTeam: '湖人', awayTeam: '掘金', score: '107-119' },
    { date: '2023-10-24', homeTeam: '太阳', awayTeam: '勇士', score: '108-104' },
    { date: '2023-10-25', homeTeam: '凯尔特人', awayTeam: '尼克斯', score: '108-104' },
    { date: '2023-10-25', homeTeam: '独行侠', awayTeam: '马刺', score: '126-119' },
  ],
  topPlayers: [
    { name: '乔尔·恩比德', team: '76人', points: 33.1 },
    { name: '卢卡·东契奇', team: '独行侠', points: 32.4 },
    { name: '谢伊·吉尔杰斯-亚历山大', team: '雷霆', points: 31.4 },
    { name: '扬尼斯·阿德托昆博', team: '雄鹿', points: 31.1 },
  ],
}

export async function getDashboardOverview(): Promise<DashboardPayload> {
  try {
    const data = await getApi<ApiEnvelope<DashboardPayload> & Record<string, unknown>>('/api/dashboard/overview')
    const maybe = data as ApiEnvelope<DashboardPayload> & Record<string, unknown>
    const raw = (maybe.data || maybe) as Record<string, unknown>
    return normalizeDashboard(raw)
  } catch {
    // Fallback keeps dashboard usable before backend dashboard API is ready.
    return DEFAULT_DASHBOARD
  }
}

export async function getGameRecords(page = 1, limit = 20): Promise<{ list: RecentGame[]; total: number }> {
  try {
    const data = await getApi<ApiEnvelope<{ list: RecentGame[]; total: number }>>(`/api/dashboard/games?page=${page}&limit=${limit}`)
    const maybe = data as ApiEnvelope<{ list: RecentGame[]; total: number }>
    const result = maybe.data || (data as unknown as { list: RecentGame[]; total: number })
    return {
      list: result.list || [],
      total: result.total || 0,
    }
  } catch {
    return { list: DEFAULT_DASHBOARD.recentGames, total: DEFAULT_DASHBOARD.recentGames.length }
  }
}

export async function getPowerRankings(page = 1, limit = 20): Promise<{ list: TopPlayer[]; total: number }> {
  try {
    const data = await getApi<ApiEnvelope<{ list: TopPlayer[]; total: number }>>(`/api/dashboard/rankings?page=${page}&limit=${limit}`)
    const maybe = data as ApiEnvelope<{ list: TopPlayer[]; total: number }>
    const result = maybe.data || (data as unknown as { list: TopPlayer[]; total: number })
    return {
      list: result.list || [],
      total: result.total || 0,
    }
  } catch {
    return { list: DEFAULT_DASHBOARD.topPlayers, total: DEFAULT_DASHBOARD.topPlayers.length }
  }
}

function normalizeDashboard(raw: Record<string, unknown>): DashboardPayload {
  if (!raw || typeof raw !== 'object') return DEFAULT_DASHBOARD

  const statsRaw = (raw.stats || {}) as Record<string, unknown>
  const trendRaw = Array.isArray(raw.trend)
    ? (raw.trend as Record<string, unknown>[])
    : Array.isArray(raw.trend_data)
      ? (raw.trend_data as Record<string, unknown>[])
      : []

  const positionRaw = Array.isArray(raw.positionDistribution)
    ? (raw.positionDistribution as Record<string, unknown>[])
    : Array.isArray(raw.position_distribution)
      ? (raw.position_distribution as Record<string, unknown>[])
      : []

  const gamesRaw = Array.isArray(raw.recentGames)
    ? (raw.recentGames as Record<string, unknown>[])
    : Array.isArray(raw.recent_games)
      ? (raw.recent_games as Record<string, unknown>[])
      : []

  const topPlayersRaw = Array.isArray(raw.topPlayers)
    ? (raw.topPlayers as Record<string, unknown>[])
    : Array.isArray(raw.top_players)
      ? (raw.top_players as Record<string, unknown>[])
      : []

  return {
    stats: {
      activePlayers: toNumber(
        statsRaw.activePlayers,
        statsRaw.active_players,
        (raw.activePlayers as number | undefined) || (raw.active_players as number | undefined),
        DEFAULT_DASHBOARD.stats.activePlayers,
      ),
      teams: toNumber(
        statsRaw.teams,
        statsRaw.team_count,
        (raw.teams as number | undefined) || (raw.team_count as number | undefined),
        DEFAULT_DASHBOARD.stats.teams,
      ),
      seasonGames: toNumber(
        statsRaw.seasonGames,
        statsRaw.season_games,
        (raw.seasonGames as number | undefined) || (raw.season_games as number | undefined),
        DEFAULT_DASHBOARD.stats.seasonGames,
      ),
      predictions: toNumber(
        statsRaw.predictions,
        statsRaw.prediction_count,
        (raw.predictions as number | undefined) || (raw.prediction_count as number | undefined),
        DEFAULT_DASHBOARD.stats.predictions,
      ),
    },
    trend: trendRaw.length
      ? trendRaw.map((item) => ({
          season: String(item.season ?? item.year ?? ''),
          avgPoints: toNumber(item.avgPoints, item.avg_points, item.points, 0),
        }))
      : DEFAULT_DASHBOARD.trend,
    positionDistribution: positionRaw.length
      ? positionRaw.map((item) => ({
          name: String(item.name ?? item.position ?? ''),
          value: toNumber(item.value, item.count, 0),
        }))
      : DEFAULT_DASHBOARD.positionDistribution,
    recentGames: gamesRaw.length
      ? gamesRaw.map((item) => ({
          date: String(item.date ?? item.game_date ?? '-'),
          homeTeam: String(item.homeTeam ?? item.home_team ?? '-'),
          awayTeam: String(item.awayTeam ?? item.away_team ?? '-'),
          score: String(item.score ?? item.final_score ?? '-'),
        }))
      : DEFAULT_DASHBOARD.recentGames,
    topPlayers: topPlayersRaw.length
      ? topPlayersRaw.map((item) => ({
          name: String(item.name ?? item.player_name ?? '-'),
          team: String(item.team ?? item.team_name ?? '-'),
          points: toNumber(item.points, item.avg_points, 0),
        }))
      : DEFAULT_DASHBOARD.topPlayers,
  }
}

function toNumber(...args: unknown[]): number {
  for (const value of args) {
    const num = Number(value)
    if (Number.isFinite(num)) return num
  }
  return 0
}
