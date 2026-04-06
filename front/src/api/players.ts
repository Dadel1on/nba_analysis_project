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

const POSITION_MAP: Record<string, string> = {
  'G': '后卫 (G)',
  'F': '前锋 (F)',
  'C': '中锋 (C)',
  'GUARD': '后卫 (G)',
  'FORWARD': '前锋 (F)',
  'CENTER': '中锋 (C)',
  'PG': '控球后卫 (PG)',
  'POINT GUARD': '控球后卫 (PG)',
  'SG': '得分后卫 (SG)',
  'SHOOTING GUARD': '得分后卫 (SG)',
  'SF': '小前锋 (SF)',
  'SMALL FORWARD': '小前锋 (SF)',
  'PF': '大前锋 (PF)',
  'POWER FORWARD': '大前锋 (PF)',
  'G-F': '后卫-前锋 (G-F)',
  'F-G': '前锋-后卫 (F-G)',
  'GUARD-FORWARD': '后卫-前锋 (G-F)',
  'FORWARD-GUARD': '前锋-后卫 (F-G)',
  'F-C': '前锋-中锋 (F-C)',
  'C-F': '中锋-前锋 (C-F)',
  'FORWARD-CENTER': '前锋-中锋 (F-C)',
  'CENTER-FORWARD': '中锋-前锋 (C-F)',
  'UNKNOWN': '未知 (UNK)',
  'UNK': '未知 (UNK)',
}

function translatePosition(pos: string | null): string | null {
  if (!pos) return null
  const upper = pos.trim().toUpperCase()
  return POSITION_MAP[upper] || pos
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
    position: translatePosition((p as PlayerSummary).position ?? null),
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
        position: translatePosition(item.position ?? null),
        stats: item.stats || EMPTY_STATS,
      }
    }
  }

  return null
}
