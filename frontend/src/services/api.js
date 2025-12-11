import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Add token to requests
apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

export const authAPI = {
    register: (data) => apiClient.post('/api/accounts/register', data),
    login: (data) => apiClient.post('/api/accounts/login', data)
}

export const walletAPI = {
    getByUser: (userId) => apiClient.get(`/api/wallets/user/${userId}`),
    create: (data) => apiClient.post('/api/wallets', data),
    getById: (id) => apiClient.get(`/api/wallets/${id}`)
}

export const transactionAPI = {
    getByWallet: (walletId) => apiClient.get(`/api/transactions/wallet/${walletId}`),
    create: (data) => apiClient.post('/api/transactions', data),
    getById: (id) => apiClient.get(`/api/transactions/${id}`)
}

export default apiClient
