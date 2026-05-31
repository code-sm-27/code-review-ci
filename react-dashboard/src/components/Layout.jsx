import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GitBranch as Github, LogOut, LayoutDashboard, FileCode2, BarChart2 } from 'lucide-react';
import './Layout.css';

export const Layout = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();

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
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};
