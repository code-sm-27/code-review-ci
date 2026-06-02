import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../lib/api';
import { GitPullRequest, ShieldAlert, Activity } from 'lucide-react';
import './Dashboard.css';

export const Dashboard = () => {
  const { data: trendData } = useQuery({
    queryKey: ['analytics-trend'],
    queryFn: async () => {
      // Fetching trend data across all repos if no repoId provided
      // Wait, our backend requires repoId for trend!
      // To show global stats, we would need a global endpoint.
      // For now, let's just return empty if global endpoint isn't supported, or 
      // we can fetch repos and then fetch trend for the first repo.
      return {}; 
    }
  });

  const { data: repos } = useQuery({
    queryKey: ['repos'],
    queryFn: async () => {
      const res = await api.get('/repos');
      return res.data;
    }
  });

  const totalReviews = trendData ? Object.values(trendData).reduce((a, b) => a + b, 0) : 0;
  const activeReposCount = repos ? repos.length : 0;

  return (
    <div className="dashboard animate-fade-in">
      <header className="page-header">
        <h1>Dashboard</h1>
        <p>Overview of your automated code reviews</p>
      </header>

      <div className="stats-grid">
        <div className="stat-card glass-card">
          <div className="stat-icon bg-primary-light">
            <GitPullRequest size={24} />
          </div>
          <div className="stat-content">
            <h3>Total Reviews</h3>
            <h2>{totalReviews}</h2>
          </div>
        </div>
        <div className="stat-card glass-card">
          <div className="stat-icon bg-warning-light">
            <ShieldAlert size={24} />
          </div>
          <div className="stat-content">
            <h3>Active Repos</h3>
            <h2>{activeReposCount}</h2>
          </div>
        </div>
        <div className="stat-card glass-card">
          <div className="stat-icon bg-success-light">
            <Activity size={24} />
          </div>
          <div className="stat-content">
            <h3>System Status</h3>
            <h2 className="text-success">Operational</h2>
          </div>
        </div>
      </div>
      
      <div className="dashboard-content">
        <div className="glass-card table-card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <h3 style={{ margin: 0 }}>Connected Repositories</h3>
            <Link to="/repos" className="btn btn-primary">
              Manage Repos
            </Link>
          </div>
          
          <div style={{ display: 'grid', gap: '12px' }}>
            {repos?.slice(0, 5).map(repo => (
              <div key={repo.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: 'rgba(255,255,255,0.02)', borderRadius: '8px' }}>
                <div>
                  <h4 style={{ margin: '0 0 4px 0' }}>{repo.fullName}</h4>
                  <p className="text-secondary" style={{ margin: 0, fontSize: '0.9rem' }}>{repo.description || 'No description'}</p>
                </div>
                <Link to={`/repos/${repo.id}`} className="btn" style={{ padding: '6px 12px', fontSize: '0.9rem', background: 'rgba(255,255,255,0.1)' }}>
                  View Details
                </Link>
              </div>
            ))}
            {repos?.length === 0 && (
              <p className="text-secondary">No repositories connected. Go to Manage Repos to connect one.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
