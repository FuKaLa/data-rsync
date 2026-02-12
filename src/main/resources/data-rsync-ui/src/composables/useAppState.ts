import { reactive } from 'vue'

interface ErrorAction {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  callback?: () => void
}

interface AppState {
  loading: {
    isLoading: boolean
    text: string
  }
  error: {
    show: boolean
    type: 'error' | 'warning' | 'info' | 'success'
    title: string
    message: string
    actions: ErrorAction[]
    closable: boolean
  }
}

const useAppState = () => {
  const appState = reactive<AppState>({
    loading: {
      isLoading: false,
      text: '加载中...'
    },
    error: {
      show: false,
      type: 'error',
      title: '',
      message: '',
      actions: [],
      closable: true
    }
  })

  const showLoading = (text: string = '加载中...') => {
    appState.loading.isLoading = true
    appState.loading.text = text
  }

  const hideLoading = () => {
    appState.loading.isLoading = false
  }

  const showError = (options: {
    type?: 'error' | 'warning' | 'info' | 'success'
    title?: string
    message: string
    actions?: ErrorAction[]
    closable?: boolean
  }) => {
    appState.error.show = true
    appState.error.type = options.type || 'error'
    appState.error.title = options.title || ''
    appState.error.message = options.message
    appState.error.actions = options.actions || []
    appState.error.closable = options.closable !== undefined ? options.closable : true
  }

  const hideError = () => {
    appState.error.show = false
  }

  return {
    appState,
    showLoading,
    hideLoading,
    showError,
    hideError
  }
}

export default useAppState