type ErrorWithResponse = {
  response?: {
    data?: {
      message?: string
    }
  }
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error !== null) {
    const maybeError = error as ErrorWithResponse
    if (maybeError.response?.data?.message) {
      return maybeError.response.data.message
    }
  }
  return fallback
}

