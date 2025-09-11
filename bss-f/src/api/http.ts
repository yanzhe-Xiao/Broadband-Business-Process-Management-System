import axios from 'axios'
import { useAuthStore } from '../store/auth'

export const http = axios.create({
    baseURL: 'http://192.168.3.6:8080',
    timeout: 15000,
})

http.interceptors.request.use((config) => {
    const token = useAuthStore.getState().token;
    config.headers = config.headers ?? {}   //防止是undefined
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
})