import { useState, Suspense } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { ArrowLeft, AlertTriangle, AlertCircle, Info, ShieldAlert } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import './Dashboard.css';

const severityIcons = {
  HIGH: <AlertTriangle className="text-danger" size={16} />,
  MEDIUM: <AlertCircle className="text-warning" size={16} />,
  LOW: <Info className="text-primary" size={16} />
};

const ReviewsList = ({ repoId, page }) => {
  const { data } = useQuery({
    queryKey: ['reviews', repoId, page],
    queryFn: async () => {
      const res = await api.get(`/reviews?repoId=${repoId}&page=${page}&size=10`);
      return res.data;
    },
    suspense: true
  });

  return (
    <div className="glass-card" style={{ padding: '24px' }}>
      <h3 style={{ marginTop: 0, marginBottom: '24px' }}>Code Reviews</h3>
      
      {data?.content?.length === 0 ? (
        <p className="text-secondary text-center">No reviews found for this repository.</p>
      ) : (
        <div style={{ display: 'grid', gap: '16px' }}>
          {data?.content?.map(review => (
            <div key={review.id} style={{ padding: '16px', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '8px' }}>
                {severityIcons[review.severity] || <ShieldAlert size={16} />}
                <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{review.severity}</span>
                <span className="text-secondary" style={{ fontSize: '0.9rem' }}>• {review.category}</span>
                <span className="text-secondary" style={{ fontSize: '0.9rem', marginLeft: 'auto' }}>
                  {review.file}:{review.lineNumber}
                </span>
              </div>
              <p style={{ margin: 0, fontSize: '0.95rem' }}>{review.comment}</p>
            </div>
          ))}
        </div>
      )}
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '24px' }}>
        <p className="text-secondary" style={{ margin: 0 }}>
          Page {data?.number + 1} of {data?.totalPages || 1}
        </p>
      </div>
    </div>
  );
};

const TrendChart = ({ repoId }) => {
  const { data: trendData } = useQuery({
    queryKey: ['analytics-trend', repoId],
    queryFn: async () => {
      const res = await api.get(`/analytics/trend?repoId=${repoId}`);
      if (!res.data || typeof res.data !== 'object') return [];
      return Object.entries(res.data).map(([date, count]) => ({ date, count }));
    },
    suspense: true
  });

  return (
    <div className="glass-card" style={{ padding: '24px' }}>
      <h3 style={{ marginTop: 0 }}>Reviews Over Time</h3>
      <div style={{ height: '250px', width: '100%' }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={trendData?.length ? trendData : [{ date: 'Today', count: 0 }]}>
            <XAxis dataKey="date" stroke="#94a3b8" />
            <YAxis stroke="#94a3b8" />
            <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: '8px', color: '#fff' }} />
            <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export const RepoDetails = () => {
  const { id } = useParams();
  const [page, setPage] = useState(0);

  return (
    <div className="animate-fade-in">
      <header className="page-header" style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <Link to="/repos" className="btn" style={{ padding: '8px', background: 'var(--surface-hover)' }}>
          <ArrowLeft size={20} />
        </Link>
        <div>
          <h1 style={{ margin: 0 }}>Repository Details</h1>
          <p style={{ margin: 0 }}>Analytics and automated reviews</p>
        </div>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px', marginBottom: '24px' }}>
        <Suspense fallback={<div className="glass-card animate-pulse" style={{ height: '300px' }}>Loading Chart...</div>}>
          <TrendChart repoId={id} />
        </Suspense>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
        <button 
          className="btn" 
          onClick={() => setPage(p => Math.max(0, p - 1))}
          disabled={page === 0}
          style={{ background: 'var(--surface-hover)' }}
        >
          Previous
        </button>
        <button 
          className="btn" 
          onClick={() => setPage(p => p + 1)}
          style={{ background: 'var(--surface-hover)' }}
        >
          Next
        </button>
      </div>

      <Suspense fallback={<div className="glass-card animate-pulse" style={{ height: '500px' }}>Loading Reviews...</div>}>
        <ReviewsList repoId={id} page={page} />
      </Suspense>
    </div>
  );
};
