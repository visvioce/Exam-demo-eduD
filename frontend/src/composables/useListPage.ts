import { ref, reactive, computed, onMounted } from 'vue'
import type { PageRequest, PageResult } from '@/types'

export interface UseListPageOptions<T> {
  fetchFn: (params: PageRequest) => Promise<{ data: PageResult<T> }>
  defaultPageSize?: number
  immediate?: boolean
}

export function useListPage<T>(options: UseListPageOptions<T>) {
  const { fetchFn, defaultPageSize = 10, immediate = true } = options

  const data = ref<T[]>([])
  const loading = ref(false)
  const total = ref(0)

  const pagination = reactive({
    current: 1,
    size: defaultPageSize
  })

  const searchParams = reactive<Record<string, unknown>>({})

  const hasData = computed(() => data.value.length > 0)
  const isEmpty = computed(() => !loading.value && data.value.length === 0)

  async function loadData() {
    loading.value = true
    try {
      const params: PageRequest = {
        current: pagination.current,
        size: pagination.size,
        ...searchParams
      }
      const response = await fetchFn(params)
      data.value = response.data.records
      total.value = response.data.total
    } catch (error) {
      console.error('Failed to load data:', error)
      data.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  async function refresh() {
    await loadData()
  }

  async function reset() {
    pagination.current = 1
    await loadData()
  }

  async function handlePageChange(page: number) {
    pagination.current = page
    await loadData()
  }

  async function handleSizeChange(size: number) {
    pagination.size = size
    pagination.current = 1
    await loadData()
  }

  async function setSearchParams(params: Record<string, unknown>) {
    Object.assign(searchParams, params)
    await reset()
  }

  async function clearSearchParams() {
    Object.keys(searchParams).forEach(key => {
      delete searchParams[key]
    })
    await reset()
  }

  if (immediate) {
    onMounted(loadData)
  }

  return {
    data,
    loading,
    total,
    pagination,
    searchParams,
    hasData,
    isEmpty,
    loadData,
    refresh,
    reset,
    handlePageChange,
    handleSizeChange,
    setSearchParams,
    clearSearchParams
  }
}
