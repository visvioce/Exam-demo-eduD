import { reactive, ref } from 'vue'

export interface PagedListResponse<T> {
  records: T[]
  total: number
}

interface UsePagedListOptions<T, TSearch extends Record<string, unknown>> {
  createSearchForm: () => TSearch
  fetchPage: (params: { current: number; size: number } & Partial<TSearch>) => Promise<PagedListResponse<T>>
  pageSize?: number
  transformSearch?: (search: TSearch) => Partial<TSearch>
  onError?: (error: unknown) => void
}

export function usePagedList<T, TSearch extends Record<string, unknown>>(options: UsePagedListOptions<T, TSearch>) {
  const records = ref<T[]>([])
  const loading = ref(false)
  const pagination = reactive({
    current: 1,
    size: options.pageSize ?? 10,
    total: 0
  })
  const searchForm = reactive(options.createSearchForm()) as TSearch

  let latestRequestId = 0

  function getSearchSnapshot(): TSearch {
    return { ...(searchForm as TSearch) }
  }

  async function load() {
    const requestId = ++latestRequestId
    loading.value = true
    try {
      const searchSnapshot = getSearchSnapshot()
      const searchParams = options.transformSearch
        ? options.transformSearch(searchSnapshot)
        : (searchSnapshot as Partial<TSearch>)
      const result = await options.fetchPage({
        current: pagination.current,
        size: pagination.size,
        ...searchParams
      })

      // 仅应用最后一次请求结果，避免慢请求覆盖新筛选结果
      if (requestId !== latestRequestId) {
        return
      }

      records.value = result.records
      pagination.total = result.total
    } catch (error) {
      if (requestId === latestRequestId) {
        options.onError?.(error)
      }
    } finally {
      if (requestId === latestRequestId) {
        loading.value = false
      }
    }
  }

  async function loadFromFirstPage() {
    pagination.current = 1
    await load()
  }

  async function resetSearch() {
    Object.assign(searchForm, options.createSearchForm())
    await loadFromFirstPage()
  }

  async function toggleSearch<K extends keyof TSearch>(key: K, value: TSearch[K], defaultValue: TSearch[K]) {
    searchForm[key] = searchForm[key] === value ? defaultValue : value
    await loadFromFirstPage()
  }

  return {
    records,
    loading,
    pagination,
    searchForm,
    load,
    loadFromFirstPage,
    resetSearch,
    toggleSearch
  }
}
