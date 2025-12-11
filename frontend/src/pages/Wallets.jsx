import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { walletAPI } from '../services/api'

export default function Wallets({ user, onLogout }) {
    const [wallets, setWallets] = useState([])
    const [showCreateForm, setShowCreateForm] = useState(false)
    const [formData, setFormData] = useState({
        userId: user.userId,
        name: '',
        currency: 'USD',
        initialBalance: '',
        walletType: 'CHECKING'
    })

    useEffect(() => {
        loadWallets()
    }, [])

    const loadWallets = async () => {
        try {
            const response = await walletAPI.getByUser(user.userId)
            setWallets(response.data)
        } catch (error) {
            console.error('Error loading wallets:', error)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        try {
            await walletAPI.create(formData)
            setShowCreateForm(false)
            setFormData({ ...formData, name: '', initialBalance: '' })
            loadWallets()
        } catch (error) {
            console.error('Error creating wallet:', error)
        }
    }

    return (
        <div>
            <nav className="navbar">
                <div className="navbar-brand">Wallet & Transaction Platform</div>
                <div className="navbar-menu">
                    <Link to="/dashboard" className="navbar-link">Dashboard</Link>
                    <Link to="/wallets" className="navbar-link">Wallets</Link>
                    <Link to="/transactions" className="navbar-link">Transactions</Link>
                    <button className="btn btn-secondary" onClick={onLogout}>Logout</button>
                </div>
            </nav>

            <div className="container">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
                    <h1 style={{ color: 'white' }}>My Wallets</h1>
                    <button className="btn btn-primary" onClick={() => setShowCreateForm(!showCreateForm)}>
                        {showCreateForm ? 'Cancel' : '+ Create Wallet'}
                    </button>
                </div>

                {showCreateForm && (
                    <div className="card">
                        <h3>Create New Wallet</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Wallet Name</label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Currency</label>
                                <select
                                    value={formData.currency}
                                    onChange={(e) => setFormData({ ...formData, currency: e.target.value })}
                                >
                                    <option value="USD">USD</option>
                                    <option value="EUR">EUR</option>
                                    <option value="GBP">GBP</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Initial Balance</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={formData.initialBalance}
                                    onChange={(e) => setFormData({ ...formData, initialBalance: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Wallet Type</label>
                                <select
                                    value={formData.walletType}
                                    onChange={(e) => setFormData({ ...formData, walletType: e.target.value })}
                                >
                                    <option value="CHECKING">Checking</option>
                                    <option value="SAVINGS">Savings</option>
                                    <option value="INVESTMENT">Investment</option>
                                    <option value="BUSINESS">Business</option>
                                </select>
                            </div>
                            <button type="submit" className="btn btn-primary">Create Wallet</button>
                        </form>
                    </div>
                )}

                <div className="wallet-grid">
                    {wallets.map(wallet => (
                        <div key={wallet.id} className="wallet-card">
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <div>{wallet.walletType}</div>
                                <div>{wallet.currency}</div>
                            </div>
                            <h3>{wallet.name}</h3>
                            <div className="wallet-balance">
                                {wallet.currency} {parseFloat(wallet.balance).toFixed(2)}
                            </div>
                            <div style={{ fontSize: '14px', opacity: 0.8 }}>
                                Created {new Date(wallet.createdAt).toLocaleDateString()}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
