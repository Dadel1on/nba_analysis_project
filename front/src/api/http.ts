import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import type { ApiEnvelope } from './types'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请稍后重试'))
    }
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || 'Request failed'
    return Promise.reject(new Error(status ? `[${status}] ${message}` : message))
  },
)

export function unwrapApiEnvelope<T>(payload: T | ApiEnvelope<T>): T {
  if (payload && typeof payload === 'object' && 'data' in (payload as Record<string, unknown>)) {
    const envelope = payload as ApiEnvelope<T>
    if (typeof envelope.data !== 'undefined') {
      return envelope.data
    }
  }
  return payload as T
}

export async function getApi<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await http.get<T | ApiEnvelope<T>>(url, config)
  return unwrapApiEnvelope<T>(data)
}

export async function postApi<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const { data } = await http.post<T | ApiEnvelope<T>>(url, body, config)
  return unwrapApiEnvelope<T>(data)
}
