import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GitBranch as Github, LogOut, LayoutDashboard, FileCode2, BarChart2, Bell } from 'lucide-react';
import { useState, useEffect } from 'react';
import { useRealTimeReviews } from '../hooks/useRealTimeReviews';
import './Layout.css';

export const Layout = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [toast, setToast] = useState(null);

  // Initialize the real-time SSE connection globally while authenticated
  useRealTimeReviews();

  useEffect(() => {
    const handleNewReview = (event) => {
      const data = event.detail;
      setToast(`Review completed for PR #${data.prNumber} in ${data.repoFullName}`);
      
      // Auto-hide after 5 seconds
      setTimeout(() => setToast(null), 5000);
    };

    window.addEventListener('new-review-toast', handleNewReview);
    return () => window.removeEventListener('new-review-toast', handleNewReview);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <aside className="sidebar glass-card">
        <div className="brand">
          <Github size={28} className="brand-icon" />
          <h2>CodeReview CI</h2>
        </div>
        <nav className="nav-menu">
          <Link to="/" className="nav-item">
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </Link>
          <Link to="/repos" className="nav-item">
            <FileCode2 size={20} />
            <span>Repositories</span>
          </Link>
          <Link to="/analytics" className="nav-item">
            <BarChart2 size={20} />
            <span>Analytics</span>
          </Link>
        </nav>
        <div className="sidebar-footer">
          <button onClick={handleLogout} className="btn logout-btn">
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>
      <main className="main-content" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <div style={{ flex: 1 }}>
          <Outlet />
        </div>
        <footer className="footer" style={{ textAlign: 'center', padding: '1rem', color: '#666', fontSize: '0.875rem' }}>
          &copy; {new Date().getFullYear()} CodeReview CI. All rights reserved.
        </footer>
      </main>
      
      {/* Toast Notification Container */}
      {toast && (
        <div className="toast-notification glass-card">
          <Bell size={20} className="toast-icon" />
          <span>{toast}</span>
        </div>
      )}
    </div>
  );
};
