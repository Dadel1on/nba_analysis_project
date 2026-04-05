import { getApi } from './http'
import type { ApiEnvelope, PlayerSummary, SearchPlayersParams } from './types'

type PlayersApiPayload = {
  players?: PlayerSummary[]
}

const EMPTY_STATS = {
  points: 0,
  rebounds: 0,
  assists: 0,
}

function normalizePlayers(payload: unknown): PlayerSummary[] {
  if (!payload || typeof payload !== 'object') return []

  const maybe = payload as PlayersApiPayload & ApiEnvelope<PlayersApiPayload>
  const rawPlayers = maybe.players || maybe.data?.players || []

  if (!Array.isArray(rawPlayers)) return []

  return rawPlayers.map((p) => ({
    id: Number((p as PlayerSummary).id),
    name: (p as PlayerSummary).name || 'Unknown',
    team: (p as PlayerSummary).team ?? null,
    position: (p as PlayerSummary).position ?? null,
    stats: (p as PlayerSummary).stats || EMPTY_STATS,
  }))
}

export async function searchPlayers(params: SearchPlayersParams): Promise<PlayerSummary[]> {
  const data = await getApi<unknown>('/api/players', { params })
  return normalizePlayers(data)
}

export async function getPlayerById(playerId: number): Promise<PlayerSummary | null> {
  const data = await getApi<unknown>(`/api/players/${playerId}`)
  const list = normalizePlayers(data)
  if (list.length) return list[0]

  if (data && typeof data === 'object') {
    const direct = data as PlayerSummary & ApiEnvelope<PlayerSummary>
    const item = direct.data || direct
    if (item && typeof item.id !== 'undefined') {
      return {
        id: Number(item.id),
        name: item.name || 'Unknown',
        team: item.team ?? null,
        position: item.position ?? null,
        stats: item.stats || EMPTY_STATS,
      }
    }
  }

  return null
}
