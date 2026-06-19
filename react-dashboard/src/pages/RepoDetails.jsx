import { useState, Suspense } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { ArrowLeft, AlertTriangle, AlertCircle, Info, ShieldAlert, ChevronLeft, ChevronRight } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const severityIcons = {
  HIGH: <AlertTriangle className="text-red-400" size={18} />,
  MEDIUM: <AlertCircle className="text-amber-400" size={18} />,
  LOW: <Info className="text-blue-400" size={18} />
};

const severityBg = {
  HIGH: 'bg-red-500/10 border-red-500/20',
  MEDIUM: 'bg-amber-500/10 border-amber-500/20',
  LOW: 'bg-blue-500/10 border-blue-500/20'
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
    <div className="glass-card p-6 md:p-8">
      <h3 className="text-xl font-bold text-slate-100 mb-6">Code Reviews</h3>
      
      {data?.content?.length === 0 ? (
        <div className="text-center py-10 px-4 bg-slate-800/20 rounded-xl border border-dashed border-slate-700/50">
          <p className="text-slate-400 text-lg">No reviews found for this repository yet.</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {data?.content?.map(review => (
            <div key={review.id} className={`p-5 rounded-xl border transition-all duration-300 hover:shadow-lg ${severityBg[review.severity] || 'bg-slate-800/30 border-slate-700/50'}`}>
              <div className="flex flex-wrap gap-3 items-center mb-3">
                <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-900/50">
                  {severityIcons[review.severity] || <ShieldAlert className="text-slate-400" size={16} />}
                  <span className="font-semibold text-sm tracking-wide">{review.severity}</span>
                </div>
                <span className="text-slate-400 text-sm font-medium px-2 py-1 bg-slate-900/30 rounded-md">
                  {review.category}
                </span>
                <span className="text-slate-400 text-sm font-mono ml-auto bg-slate-900/50 px-2 py-1 rounded-md">
                  {review.file}:{review.lineNumber}
                </span>
              </div>
              <p className="text-slate-200 leading-relaxed text-sm md:text-base">{review.comment}</p>
            </div>
          ))}
        </div>
      )}
      
      <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-700/50">
        <p className="text-slate-400 font-medium">
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
    <div className="glass-card p-6 md:p-8">
      <h3 className="text-xl font-bold text-slate-100 mb-6">Reviews Over Time</h3>
      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={trendData?.length ? trendData : [{ date: 'Today', count: 0 }]}>
            <XAxis dataKey="date" stroke="#64748b" tick={{ fill: '#64748b' }} axisLine={{ stroke: '#334155' }} tickLine={false} dy={10} />
            <YAxis stroke="#64748b" tick={{ fill: '#64748b' }} axisLine={{ stroke: '#334155' }} tickLine={false} dx={-10} allowDecimals={false} />
            <Tooltip 
              contentStyle={{ background: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '0.5rem', color: '#f8fafc', backdropFilter: 'blur(8px)' }}
              itemStyle={{ color: '#60a5fa' }}
            />
            <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4, fill: '#1e293b', strokeWidth: 2 }} activeDot={{ r: 6, fill: '#3b82f6', stroke: '#fff' }} animationDuration={1500} />
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
    <div className="flex flex-col gap-8 animate-fade-in w-full max-w-5xl mx-auto">
      <header className="flex items-center gap-4">
        <Link to="/repos" className="w-10 h-10 rounded-xl bg-slate-800/50 border border-slate-700/50 flex items-center justify-center text-slate-400 hover:text-white hover:bg-slate-700 transition-colors">
          <ArrowLeft size={20} />
        </Link>
        <div>
          <h1 className="text-2xl md:text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">
            Repository Details
          </h1>
          <p className="text-slate-400">Analytics and automated reviews</p>
        </div>
      </header>

      <div className="grid grid-cols-1 gap-6">
        <Suspense fallback={
          <div className="glass-card p-8 h-80 flex flex-col items-center justify-center gap-4">
            <div className="w-8 h-8 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin"></div>
            <p className="text-slate-400 font-medium">Loading analytics...</p>
          </div>
        }>
          <TrendChart repoId={id} />
        </Suspense>
      </div>

      <div className="flex justify-between items-center bg-slate-800/30 p-2 rounded-xl border border-slate-700/50">
        <button 
          className="flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-slate-300 hover:text-white hover:bg-slate-700/50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          onClick={() => setPage(p => Math.max(0, p - 1))}
          disabled={page === 0}
        >
          <ChevronLeft size={18} /> Previous
        </button>
        <button 
          className="flex items-center gap-2 px-4 py-2 rounded-lg font-medium text-slate-300 hover:text-white hover:bg-slate-700/50 transition-colors"
          onClick={() => setPage(p => p + 1)}
        >
          Next <ChevronRight size={18} />
        </button>
      </div>

      <Suspense fallback={
        <div className="glass-card p-8 h-96 flex flex-col items-center justify-center gap-4">
          <div className="w-8 h-8 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin"></div>
          <p className="text-slate-400 font-medium">Loading reviews...</p>
        </div>
      }>
        <ReviewsList repoId={id} page={page} />
      </Suspense>
    </div>
  );
};
