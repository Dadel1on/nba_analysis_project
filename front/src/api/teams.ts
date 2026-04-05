import { getApi, postApi } from './http'
import type { ApiEnvelope, TeamCompareOptions, TeamComparisonResult, TeamSummary } from './types'

type TeamsApiPayload = {
  teams?: TeamSummary[]
}

function normalizeTeams(payload: unknown): TeamSummary[] {
  if (!payload || typeof payload !== 'object') return []

  const maybe = payload as TeamsApiPayload & ApiEnvelope<TeamsApiPayload>
  const rawTeams = maybe.teams || maybe.data?.teams || []
  if (!Array.isArray(rawTeams)) return []

  return rawTeams.map((team) => ({
    id: (team as TeamSummary).id,
    name: (team as TeamSummary).name || 'Unknown',
    city: (team as TeamSummary).city || 'Unknown',
    conference: (team as TeamSummary).conference || 'Unknown',
  }))
}

export async function getTeams(): Promise<TeamSummary[]> {
  const data = await getApi<unknown>('/api/teams')
  return normalizeTeams(data)
}

function teamSeed(name: string): number {
  return Array.from(name).reduce((sum, ch) => sum + ch.charCodeAt(0), 0)
}

function buildFallbackComparison(teamA: string, teamB: string): TeamComparisonResult {
  const seeds = [teamSeed(teamA), teamSeed(teamB)]
  const teams = [teamA, teamB].map((name, index) => {
    const seed = seeds[index]
    const points = 100 + (seed % 21)
    const rebounds = 38 + (seed % 12)
    const assists = 20 + (seed % 11)
    const wins = 35 + (seed % 23)
    const winRate = Number((wins / 82).toFixed(3))

    return {
      team: name,
      points,
      rebounds,
      assists,
      wins,
      winRate,
    }
  })

  return { teams }
}

export async function compareTeams(teamA: string, teamB: string, options: TeamCompareOptions = {}): Promise<TeamComparisonResult> {
  try {
    const data = await postApi<TeamComparisonResult | ApiEnvelope<TeamComparisonResult>>('/api/teams/compare', {
      teams: [teamA, teamB],
      season: options.season,
      metric: options.metric,
    })
    const maybe = data as TeamComparisonResult & ApiEnvelope<TeamComparisonResult>
    const result = maybe.data || maybe
    if (result?.teams?.length === 2) {
      return result
    }
  } catch {
    // Fallback keeps comparison interaction available before backend endpoint lands.
  }

  return buildFallbackComparison(teamA, teamB)
}
