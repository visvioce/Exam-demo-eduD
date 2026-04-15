type ErrorWithResponse = {
  response?: {
    data?: {
      message?: string
      error?: string
      msg?: string
    }
  }
  message?: string
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error !== null) {
    const maybeError = error as ErrorWithResponse
    if (maybeError.response?.data?.message) {
      return maybeError.response.data.message
    }
    if (maybeError.response?.data?.error) {
      return maybeError.response.data.error
    }
    if (maybeError.response?.data?.msg) {
      return maybeError.response.data.msg
    }
    if (maybeError.message) {
      return maybeError.message
    }
  }
  return fallback
}

