import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { walletAPI, transactionAPI } from '../services/api'

export default function Transactions({ user, onLogout }) {
    const [wallets, setWallets] = useState([])
    const [transactions, setTransactions] = useState([])
    const [selectedWallet, setSelectedWallet] = useState('')
    const [showCreateForm, setShowCreateForm] = useState(false)
    const [formData, setFormData] = useState({
        sourceWalletId: '',
        destinationWalletId: '',
        amount: '',
        currency: 'USD',
        transactionType: 'TRANSFER',
        description: ''
    })

    useEffect(() => {
        loadWallets()
    }, [])

    useEffect(() => {
        if (selectedWallet) {
            loadTransactions(selectedWallet)
        }
    }, [selectedWallet])

    const loadWallets = async () => {
        try {
            const response = await walletAPI.getByUser(user.userId)
            setWallets(response.data)
            if (response.data.length > 0) {
                setSelectedWallet(response.data[0].id)
                setFormData({ ...formData, sourceWalletId: response.data[0].id, currency: response.data[0].currency })
            }
        } catch (error) {
            console.error('Error loading wallets:', error)
        }
    }

    const loadTransactions = async (walletId) => {
        try {
            const response = await transactionAPI.getByWallet(walletId)
            setTransactions(response.data)
        } catch (error) {
            console.error('Error loading transactions:', error)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        try {
            await transactionAPI.create(formData)
            setShowCreateForm(false)
            setFormData({ ...formData, amount: '', description: '', destinationWalletId: '' })
            loadTransactions(selectedWallet)
        } catch (error) {
            console.error('Error creating transaction:', error)
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
                    <h1 style={{ color: 'white' }}>Transactions</h1>
                    <button className="btn btn-primary" onClick={() => setShowCreateForm(!showCreateForm)}>
                        {showCreateForm ? 'Cancel' : '+ New Transaction'}
                    </button>
                </div>

                <div className="card">
                    <div className="form-group">
                        <label>Select Wallet</label>
                        <select value={selectedWallet} onChange={(e) => setSelectedWallet(e.target.value)}>
                            {wallets.map(wallet => (
                                <option key={wallet.id} value={wallet.id}>
                                    {wallet.name} ({wallet.currency} {parseFloat(wallet.balance).toFixed(2)})
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                {showCreateForm && (
                    <div className="card">
                        <h3>New Transaction</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Transaction Type</label>
                                <select
                                    value={formData.transactionType}
                                    onChange={(e) => setFormData({ ...formData, transactionType: e.target.value })}
                                >
                                    <option value="TRANSFER">Transfer</option>
                                    <option value="DEPOSIT">Deposit</option>
                                    <option value="WITHDRAWAL">Withdrawal</option>
                                    <option value="PAYMENT">Payment</option>
                                </select>
                            </div>

                            {formData.transactionType === 'TRANSFER' && (
                                <div className="form-group">
                                    <label>Destination Wallet</label>
                                    <select
                                        value={formData.destinationWalletId}
                                        onChange={(e) => setFormData({ ...formData, destinationWalletId: e.target.value })}
                                    >
                                        <option value="">Select destination</option>
                                        {wallets.filter(w => w.id !== formData.sourceWalletId).map(wallet => (
                                            <option key={wallet.id} value={wallet.id}>{wallet.name}</option>
                                        ))}
                                    </select>
                                </div>
                            )}

                            <div className="form-group">
                                <label>Amount</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={formData.amount}
                                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Description</label>
                                <input
                                    type="text"
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                />
                            </div>

                            <button type="submit" className="btn btn-primary">Create Transaction</button>
                        </form>
                    </div>
                )}

                <div className="card">
                    <h3>Transaction History</h3>
                    {transactions.length === 0 ? (
                        <p>No transactions yet</p>
                    ) : (
                        <ul className="transaction-list">
                            {transactions.map(txn => (
                                <li key={txn.id} className="transaction-item">
                                    <div>
                                        <div style={{ fontWeight: 600 }}>
                                            {txn.description || txn.transactionType}
                                            {txn.category && (
                                                <span style={{ marginLeft: '10px', fontSize: '12px', background: '#f0f0f0', padding: '2px 8px', borderRadius: '4px' }}>
                                                    {txn.category}
                                                </span>
                                            )}
                                        </div>
                                        <div style={{ fontSize: '14px', color: '#666' }}>
                                            {new Date(txn.transactionDate).toLocaleString()}
                                        </div>
                                        <div style={{ fontSize: '12px', color: '#999' }}>
                                            Ref: {txn.reference}
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                        <span style={{ fontSize: '18px', fontWeight: 600 }}>
                                            {txn.currency} {parseFloat(txn.amount).toFixed(2)}
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
