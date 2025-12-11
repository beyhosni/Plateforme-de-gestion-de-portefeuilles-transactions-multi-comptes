import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Wallets from './pages/Wallets'
import Transactions from './pages/Transactions'

function App() {
    const [token, setToken] = useState(localStorage.getItem('token'))
    const [user, setUser] = useState(JSON.parse(localStorage.getItem('user') || 'null'))

    const handleLogin = (authData) => {
        setToken(authData.token)
        setUser(authData)
        localStorage.setItem('token', authData.token)
        localStorage.setItem('user', JSON.stringify(authData))
    }

    const handleLogout = () => {
        setToken(null)
        setUser(null)
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={
                    !token ? <Login onLogin={handleLogin} /> : <Navigate to="/dashboard" />
                } />
                <Route path="/register" element={
                    !token ? <Register onLogin={handleLogin} /> : <Navigate to="/dashboard" />
                } />
                <Route path="/dashboard" element={
                    token ? <Dashboard user={user} onLogout={handleLogout} /> : <Navigate to="/login" />
                } />
                <Route path="/wallets" element={
                    token ? <Wallets user={user} onLogout={handleLogout} /> : <Navigate to="/login" />
                } />
                <Route path="/transactions" element={
                    token ? <Transactions user={user} onLogout={handleLogout} /> : <Navigate to="/login" />
                } />
                <Route path="/" element={<Navigate to="/dashboard" />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
