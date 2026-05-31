import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { GitPullRequest, ShieldAlert, Activity } from 'lucide-react';
import './Dashboard.css';

export const Dashboard = () => {
  const { data: trendData } = useQuery({
    queryKey: ['analytics-trend'],
    queryFn: async () => {
      const res = await api.get('/analytics/trend');
      return res.data;
    }
  });

  const totalReviews = trendData ? Object.values(trendData).reduce((a, b) => a + b, 0) : 0;

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
            <h2>-</h2>
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
          <h3>Recent Activity</h3>
          <p className="text-secondary">Dashboard activity feed coming soon.</p>
        </div>
      </div>
    </div>
  );
};
