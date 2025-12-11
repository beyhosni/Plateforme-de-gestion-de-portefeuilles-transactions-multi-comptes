import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { walletAPI, transactionAPI } from '../services/api'

export default function Dashboard({ user, onLogout }) {
    const [wallets, setWallets] = useState([])
    const [recentTransactions, setRecentTransactions] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        loadData()
    }, [])

    const loadData = async () => {
        try {
            const walletsRes = await walletAPI.getByUser(user.userId)
            setWallets(walletsRes.data)

            if (walletsRes.data.length > 0) {
                const txnRes = await transactionAPI.getByWallet(walletsRes.data[0].id)
                setRecentTransactions(txnRes.data.slice(0, 5))
            }
        } catch (error) {
            console.error('Error loading data:', error)
        } finally {
            setLoading(false)
        }
    }

    const totalBalance = wallets.reduce((sum, wallet) => sum + parseFloat(wallet.balance), 0)

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
                <h1 style={{ color: 'white', marginBottom: '30px' }}>
                    Welcome, {user.firstName} {user.lastName}!
                </h1>

                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Balance</div>
                        <div className="stat-value">${totalBalance.toFixed(2)}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active Wallets</div>
                        <div className="stat-value">{wallets.length}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Recent Transactions</div>
                        <div className="stat-value">{recentTransactions.length}</div>
                    </div>
                </div>

                <div className="card">
                    <h2>Recent Transactions</h2>
                    {loading ? (
                        <p>Loading...</p>
                    ) : recentTransactions.length === 0 ? (
                        <p>No recent transactions</p>
                    ) : (
                        <ul className="transaction-list">
                            {recentTransactions.map(txn => (
                                <li key={txn.id} className="transaction-item">
                                    <div>
                                        <div style={{ fontWeight: 600 }}>{txn.description || 'Transaction'}</div>
                                        <div style={{ fontSize: '14px', color: '#666' }}>
                                            {new Date(txn.transactionDate).toLocaleDateString()}
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                        <span style={{ fontSize: '18px', fontWeight: 600 }}>
                                            ${txn.amount}
                                        </span>
                                        <span className={`status-badge status-${txn.status.toLowerCase()}`}>
                                            {txn.status}
                                        </span>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    )
}
