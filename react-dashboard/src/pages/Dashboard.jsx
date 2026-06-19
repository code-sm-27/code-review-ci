import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../lib/api';
import { GitPullRequest, ShieldAlert, Activity, ChevronRight, GitBranch as Github } from 'lucide-react';

export const Dashboard = () => {
  const { data: trendData } = useQuery({
    queryKey: ['analytics-trend'],
    queryFn: async () => {
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

  const statCards = [
    { title: 'Total Reviews', value: totalReviews, icon: GitPullRequest, color: 'text-blue-400', bg: 'bg-blue-500/10' },
    { title: 'Active Repos', value: activeReposCount, icon: ShieldAlert, color: 'text-amber-400', bg: 'bg-amber-500/10' },
    { title: 'System Status', value: 'Operational', icon: Activity, color: 'text-emerald-400', bg: 'bg-emerald-500/10' },
  ];

  return (
    <div className="flex flex-col gap-8 animate-fade-in w-full">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl md:text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">
          CodeReview CI Dashboard
        </h1>
        <p className="text-slate-400 text-lg">Overview of your automated code reviews</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {statCards.map((stat, idx) => {
          const Icon = stat.icon;
          return (
            <div key={idx} className="glass-card p-6 flex items-center gap-5 group">
              <div className={`w-14 h-14 rounded-2xl flex items-center justify-center ${stat.bg} ${stat.color} transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3 shadow-inner`}>
                <Icon size={28} />
              </div>
              <div>
                <h3 className="text-slate-400 font-medium text-sm uppercase tracking-wider mb-1">{stat.title}</h3>
                <h2 className={`text-2xl font-bold ${stat.value === 'Operational' ? 'text-emerald-400' : 'text-slate-100'}`}>
                  {stat.value}
                </h2>
              </div>
            </div>
          );
        })}
      </div>
      
      <div className="glass-card p-6 md:p-8 flex flex-col gap-6">
        <div className="flex justify-between items-center pb-4 border-b border-slate-700/50">
          <h3 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Github size={24} className="text-slate-400" />
            Connected Repositories
          </h3>
          <Link to="/repos" className="btn btn-primary text-sm shadow-blue-500/20">
            Manage Repos
          </Link>
        </div>
        
        <div className="grid gap-4">
          {repos?.slice(0, 5).map(repo => (
            <Link 
              key={repo.id} 
              to={`/repos/${repo.id}`} 
              className="flex justify-between items-center p-5 bg-slate-800/30 hover:bg-slate-700/40 rounded-xl border border-slate-700/50 hover:border-slate-600/50 transition-all duration-300 group"
            >
              <div className="flex flex-col gap-1">
                <h4 className="text-lg font-semibold text-slate-200 group-hover:text-blue-400 transition-colors">
                  {repo.fullName}
                </h4>
                <p className="text-slate-400 text-sm">{repo.description || 'No description available for this repository.'}</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center group-hover:bg-blue-500/20 group-hover:text-blue-400 transition-colors">
                <ChevronRight size={20} />
              </div>
            </Link>
          ))}
          
          {repos?.length === 0 && (
            <div className="text-center py-10 px-4 bg-slate-800/20 rounded-xl border border-dashed border-slate-700/50">
              <p className="text-slate-400 text-lg">No repositories connected yet.</p>
              <Link to="/repos" className="text-blue-400 hover:text-blue-300 underline mt-2 inline-block">
                Go to Manage Repos to connect one
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
